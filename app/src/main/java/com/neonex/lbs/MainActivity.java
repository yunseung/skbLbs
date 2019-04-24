package com.neonex.lbs;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.neonex.lbs.broadcast.RestartService;
import com.neonex.lbs.javascript.LbsAndroidBridge;
import com.neonex.lbs.network.api.ApiService;
import com.neonex.lbs.network.api.EmergencyCall;
import com.neonex.lbs.network.api.UpdateWorkGbnObject;
import com.neonex.lbs.service.PersistentService;
import com.neonex.lbs.utils.AppExitPreventUtil;
import com.neonex.lbs.utils.BusProvider;
import com.neonex.lbs.utils.CommonUtils;
import com.neonex.lbs.utils.Lbs;
import com.neonex.lbs.utils.LbsPreferences;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    // BT
    private static final int REQUEST_ENABLE_BT = 1;

    private BroadcastReceiver mBroadcastReceiver = null;

    // View level
    private Button mBtnEmergency = null;

    private boolean mIsBound = false;

    private static WebView mSkbLbsWebView = null;                                 // Lbs WebView

    private AppExitPreventUtil mAppExitPreventHandler = null;                   // 뒤로가기 한 번에 앱 죽이는 것 방지 핸들러

    private LbsAndroidBridge mBridge = null;

    private ValueCallback<Uri> filePathCallbackNormal;
    private ValueCallback<Uri[]> filePathCallbackLollipop;
    private Uri mCapturedImageURI;

    private ApiService mApiService = null;

    private final int SERVER_ADDRESS = Lbs.SERVER_STAGE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPermissionMarshmallow();

        Lbs.getInstance().setServerUri(this, SERVER_ADDRESS);

        mApiService = new Retrofit.Builder()
                .baseUrl(LbsPreferences.getLastServerAddress(this))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);

        mBtnEmergency = (Button) findViewById(R.id.btn_emergency);

        mBtnEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Call<EmergencyCall> comment = mApiService.emergencyCall(LbsPreferences.getUserId(MainActivity.this));
                comment.enqueue(new Callback<EmergencyCall>() {
                    @Override
                    public void onResponse(Call<EmergencyCall> call, Response<EmergencyCall> response) {
                        try {
                            EmergencyCall emergencyCall = response.body();
                            if (emergencyCall.getResultCode().equals("SUCC")) {
                                Toast.makeText(MainActivity.this, getString(R.string.emergency_call), Toast.LENGTH_LONG).show();
                            } else {
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                                alertDialogBuilder.setTitle(getString(R.string.alert))
                                        .setMessage(emergencyCall.getErrMsg())
                                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                AlertDialog dialog = alertDialogBuilder.create();
                                dialog.show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<EmergencyCall> call, Throwable t) {

                    }
                });
//                getLocation();
            }
        });

        // 리시버 등록
        mBroadcastReceiver = new RestartService();

        // bus provider 등록
        BusProvider.getInstance().register(this);

        mAppExitPreventHandler = new AppExitPreventUtil(this);

        // send fcm to web
        mBridge = new LbsAndroidBridge(this);
        mBridge.setOnAndroidBridgeListener(new LbsAndroidBridge.OnLbsAndroidBridgeListener() {
            @Override
            public void sendFcmToken() {
                mSkbLbsWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("MainActivitiy", "getFcmToken : " + LbsPreferences.getFcmToken(MainActivity.this));
                        mSkbLbsWebView.loadUrl("javascript:getFcmToken('" + LbsPreferences.getFcmToken(MainActivity.this) + "');");
                    }
                });
            }

            @Override
            public void loginComplete(String userId) {
                Log.e("MainActivity", "userId : " + userId);
                if (LbsPreferences.getAppFirstLaunch(MainActivity.this)) {
                    LbsPreferences.setAppFirstLaunch(MainActivity.this, false);
                }

                // 새로 로그인을 했을 때에는 이전에 사용했던 비콘 정보 삭제, gorupSeq 새로 등록.
                LbsPreferences.setIgnoreBeaconPinNo(MainActivity.this, null);
                LbsPreferences.setMappingBeaconPinNo(MainActivity.this, null);
                LbsPreferences.setUserId(MainActivity.this, userId);

//                FirebaseMessaging.getInstance().subscribeToTopic(groupSeq);


            }

            @Override
            public void sendLocation() {

            }

            @Override
            public void sendStopUseEquip() {
                // 웹뷰에서 매핑을 해지했다.
                LbsPreferences.setMappingBeaconPinNo(MainActivity.this, null);
                LbsPreferences.setIgnoreBeaconPinNo(MainActivity.this, null);
            }
        });

        initializeWebView();
    }

    private void initializeWebView() {
        mSkbLbsWebView = (WebView) findViewById(R.id.web_view);
        mSkbLbsWebView.setWebChromeClient(new SkbLbsWebChromeClient());
        mSkbLbsWebView.setWebViewClient(new SkbLbsWebViewClient());
        mSkbLbsWebView.setScrollContainer(true);
        mSkbLbsWebView.addJavascriptInterface(mBridge, "lbsBridge");
        mSkbLbsWebView.clearView();
        mSkbLbsWebView.clearHistory();
        mSkbLbsWebView.clearCache(true);
        mSkbLbsWebView.requestFocus();
        mSkbLbsWebView.requestFocusFromTouch();
        mSkbLbsWebView.getSettings().setJavaScriptEnabled(true);
        mSkbLbsWebView.getSettings().setAllowContentAccess(true);
        mSkbLbsWebView.getSettings().setAllowFileAccessFromFileURLs(true);
        mSkbLbsWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            mSkbLbsWebView.getSettings().setTextZoom(100);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSkbLbsWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        mSkbLbsWebView.loadUrl(LbsPreferences.getLastServerAddress(this));
    }

    @Override
    protected void onStart() {
//        Log.e("MainActivity", "MainActivity onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        bluetoothCheck();

        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mIsBound) {
            unbindService(mServiceConnection);
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void bluetoothCheck() {
        // 블루투스 지원 안함.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext()).setTitle("BLE not supported")
                    .setMessage(R.string.ble_not_supported);
            builder.show();
            return;
        }

        if (CommonUtils.bluetoothOn()) {
            // 블루투스 켜져있음
            startPersistentService();
        } else {
            // 블루투스 꺼져있으니 켜라는 팝업.
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void startPersistentService() {
        try {
            IntentFilter mainFilter = new IntentFilter("com.neonex.lbs.BeaconService.ssss");

            registerReceiver(mBroadcastReceiver, mainFilter);

            // start service
            Intent serviceIntent = new Intent(this, PersistentService.class);
            startService(serviceIntent);
            mIsBound = bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    final class SkbLbsWebChromeClient extends WebChromeClient {
        @Override
        public void onPermissionRequest(final PermissionRequest request) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void run() {
                    Log.e("MainActivity", request.getOrigin().toString());
                    if (request.getOrigin().toString().equals("file:///")) {
                        Log.e("MainActivity", "GRANTED");
                        request.grant(request.getResources());
                    } else {
                        Log.e("MainActivity", "DENIED");
                        request.deny();
                    }
                }
            });
            super.onPermissionRequest(request);
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            Log.e("onCreateWindow", "onCreateWindow");
            final WebSettings settings = view.getSettings();
            settings.setDomStorageEnabled(true);
            settings.setJavaScriptEnabled(true);
            settings.setAllowFileAccess(true);
            settings.setAllowContentAccess(true);
            settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
            view.setWebChromeClient(this);
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(view);
            resultMsg.sendToTarget();
            return false;
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            return super.onJsAlert(view, url, message, result);
        }

        @Override
        public void onConsoleMessage(String message, int lineNumber, String sourceID) {
            if (message == null)
                return;
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            super.onGeolocationPermissionsShowPrompt(origin, callback);
            callback.invoke(origin, true, false);
        }

        // For Android < 3.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            openFileChooser(uploadMsg, "");
        }

        // For Android 3.0+
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyApp");
            // Create the storage directory if it does not exist
            if (!imageStorageDir.exists()) {
                imageStorageDir.mkdirs();
            }
            File file = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
            mCapturedImageURI = Uri.fromFile(file);

            final List<Intent> cameraIntents = new ArrayList<Intent>();
            final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            final PackageManager packageManager = getPackageManager();
            final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
            for (ResolveInfo res : listCam) {
                final String packageName = res.activityInfo.packageName;
                final Intent i = new Intent(captureIntent);
                i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                i.setPackage(packageName);
                i.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
                cameraIntents.add(i);

            }


            filePathCallbackNormal = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));
            MainActivity.this.startActivityForResult(chooserIntent, 100);
        }

        // For Android 4.1+
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            openFileChooser(uploadMsg, acceptType);
        }


        // For Android 5.0+
        public boolean onShowFileChooser(
                WebView webView, ValueCallback<Uri[]> filePathCallback,
                WebChromeClient.FileChooserParams fileChooserParams) {
            if (filePathCallbackLollipop != null) {
//                    filePathCallbackLollipop.onReceiveValue(null);
                filePathCallbackLollipop = null;
            }
            filePathCallbackLollipop = filePathCallback;


            // Create AndroidExampleFolder at sdcard
            File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "AndroidExampleFolder");
            if (!imageStorageDir.exists()) {
                // Create AndroidExampleFolder at sdcard
                imageStorageDir.mkdirs();
            }

            // Create camera captured image file path and name
            File file = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
            mCapturedImageURI = Uri.fromFile(file);

            Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);

            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");

            // Create file chooser intent
            Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
            // Set camera intent to file chooser
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{captureIntent});

            // On select image call onActivityResult method of activity
            startActivityForResult(chooserIntent, 200);
            return true;

        }

    }

    final class SkbLbsWebViewClient extends WebViewClient {
        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(R.string.notification_error_ssl_cert_invalid);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.proceed();
                }
            });
            builder.setNegativeButton(R.string.nok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.cancel();
                }
            });
            final AlertDialog dialog = builder.create();
            dialog.show();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            mSkbLbsWebView.invalidate();
//            mSkbLbsWebView.clearCache(true);
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }


        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.contains("login.do") || url.contains("userMng.do")) {
                mBtnEmergency.setVisibility(View.GONE);
                mSkbLbsWebView.clearCache(true);
            } else {
                mBtnEmergency.setVisibility(View.VISIBLE);
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
    }


    @Override
    public void onBackPressed() {

        if (mSkbLbsWebView.canGoBack()) {
            mSkbLbsWebView.goBack();
        } else {
            if (this.getSupportFragmentManager().getBackStackEntryCount() == 0) {
                mAppExitPreventHandler.onBackPressed();
            } else {
                super.onBackPressed();
            }
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
//            Log.e("MainActivity", "service disconnected");
            mIsBound = false;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    // 사용자가 블루투스 팝업을 통해 블루투스를 켬.
                    startPersistentService();
                } else {
                    // 사용자가 블루투스 팝업을 무시하거나 블루투스를 켜지 않음.
                    Toast.makeText(getApplicationContext(), "기기의 Bluetooth 를 켜지 않으면\nskbLbs 앱의 기능을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
                break;
            case 100:
                if (filePathCallbackNormal == null) return;
                Uri result = (data == null || resultCode != RESULT_OK) ? null : data.getData();
                filePathCallbackNormal.onReceiveValue(result);
                filePathCallbackNormal = null;
                break;
            case 200:
                Uri[] result2 = new Uri[0];
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    if (resultCode == RESULT_OK) {
                        result2 = (data == null) ? new Uri[]{mCapturedImageURI} : WebChromeClient.FileChooserParams.parseResult(resultCode, data);
                    }
                    filePathCallbackLollipop.onReceiveValue(result2);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBroadcastReceiver);
        BusProvider.getInstance().unregister(this);
//        unbindService(mServiceConnection);
        super.onDestroy();
    }

    @Subscribe
    public void mappingComplete(UpdateWorkGbnObject obj) {
        Log.e("MainActivity", "mappingComplete is called");
        mSkbLbsWebView.loadUrl("javascript:onRegisteredUserEquip();");
    }

    /**
     * 마시멜로우 권한 체크
     */
    private void getPermissionMarshmallow() {


        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {

            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                finish();
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setRationaleMessage(getString(R.string.and_ver_m_permission_message_1))
//                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setGotoSettingButtonText("setting").setPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();
    }
}
