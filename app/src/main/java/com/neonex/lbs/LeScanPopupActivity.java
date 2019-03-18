package com.neonex.lbs;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.neonex.lbs.network.api.ApiService;
import com.neonex.lbs.network.api.DeviceObjectInfo;
import com.neonex.lbs.network.api.UpdateWorkGbnObject;
import com.neonex.lbs.service.PersistentService;
import com.neonex.lbs.utils.BusProvider;
import com.neonex.lbs.utils.Lbs;
import com.neonex.lbs.utils.LbsPreferences;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by yun on 2017-12-13.
 */

public class LeScanPopupActivity extends Activity {
    private RadioButton mRbCommute, mRbWork = null;
    private TextView mTvCarNo, mTvCarName = null;
    private Button mBtnCancel, mBtnDriver, mBtnPassenger = null;

    private boolean mIsBind = false;

    private ApiService mApiService = null;

    private DeviceObjectInfo.Data mDeviceObjectInfoData = null;
    private int mPinNo = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_le_scan_popup);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        mRbCommute = findViewById(R.id.rb_commute);
        mRbWork = findViewById(R.id.rb_work);
        mTvCarNo = findViewById(R.id.tv_car_no);
        mTvCarName = findViewById(R.id.tv_car_name);
        mBtnCancel = findViewById(R.id.btn_cancel);
        mBtnDriver = findViewById(R.id.btn_driver);
        mBtnPassenger = findViewById(R.id.btn_passenger);

        mBtnCancel.setOnClickListener(mOnClickListener);
        mBtnDriver.setOnClickListener(mOnClickListener);
        mBtnPassenger.setOnClickListener(mOnClickListener);

        mDeviceObjectInfoData = (DeviceObjectInfo.Data) getIntent().getSerializableExtra("BEACON_DATA");
        mPinNo = getIntent().getIntExtra("PIN_NO", 0);

        mTvCarName.setText(String.format(getString(R.string.car_name), mDeviceObjectInfoData.getObjectTypeName()));
        mTvCarNo.setText(String.format(getString(R.string.car_no), mDeviceObjectInfoData.getObjectName()));

        mApiService = new Retrofit.Builder()
                .baseUrl(LbsPreferences.getLastServerAddress(this))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);

//        Call<DeviceObjectInfo> comment = mApiService.deviceObjectInfo(Integer.toString(mLbsBeacon.getPinNo()), LbsPreferences.getUserId(LeScanPopupActivity.this));
//        Log.e("LeScanActivity", "id : " + LbsPreferences.getUserId(LeScanPopupActivity.this));
//        comment.enqueue(new Callback<DeviceObjectInfo>() {
//            @Override
//            public void onResponse(Call<DeviceObjectInfo> call, Response<DeviceObjectInfo> response) {
//                try {
//                    mDeviceObjectInfoData = response.body();
//                    if (mDeviceObjectInfoData.getResultCode().equals("SUCC")) {
//                        mTvCarName.setText(String.format(getString(R.string.car_name), mDeviceObjectInfoData.getData().getObjectTypeName()));
//                        mTvCarNo.setText(String.format(getString(R.string.car_no), mDeviceObjectInfoData.getData().getObjectName()));
//                    } else {
//                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LeScanPopupActivity.this);
//                        alertDialogBuilder.setTitle(getString(R.string.alert))
//                                .setMessage(mDeviceObjectInfoData.getErrMsg())
//                                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        finish();
//                                    }
//                                });
//                        AlertDialog dialog = alertDialogBuilder.create();
//                        dialog.show();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<DeviceObjectInfo> call, Throwable t) {
//
//            }
//        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        Intent serviceIntent = new Intent(getApplicationContext(), PersistentService.class);
        bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mIsBind) {
            unbindService(mServiceConnection);
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            mIsBind = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mIsBind = false;
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_cancel:
                    finish();
                    break;
                case R.id.btn_driver:
                    if (!mRbCommute.isChecked() && !mRbWork.isChecked()) {
                        Toast.makeText(LeScanPopupActivity.this, getString(R.string.usage_warning_toast), Toast.LENGTH_LONG).show();
                    } else {
                        // if (비콘이 본인 아닌 누군가와 매핑이 돼있다면) -> 다른 사용자와 매칭 돼있으니 새로운 매핑이 불가능하다는 팝업.
                        if (mDeviceObjectInfoData.getEquipUseYn().equals("Y")) {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LeScanPopupActivity.this);
                            alertDialogBuilder.setTitle(getString(R.string.alert))
                                    .setMessage(getString(R.string.already_mapping))
                                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    });
                            AlertDialog dialog = alertDialogBuilder.create();
                            dialog.show();
                        } else {
                            Call<UpdateWorkGbnObject> updateWorkGbnObject = mApiService.updateWorkGbnObject(Integer.toString(mPinNo),
                                    mRbCommute.isChecked()? "0002" : "0001" ,LbsPreferences.getUserId(LeScanPopupActivity.this));
                            Log.e("LeScanPopupActivity", "id : " + LbsPreferences.getUserId(LeScanPopupActivity.this));
                            Log.e("LeScanPopupActivity", mRbCommute.isChecked()? "0001" : "0002");
                            updateWorkGbnObject.enqueue(new Callback<UpdateWorkGbnObject>() {
                                @Override
                                public void onResponse(Call<UpdateWorkGbnObject> call, Response<UpdateWorkGbnObject> response) {
                                    UpdateWorkGbnObject updateWorkGbnObject = response.body();
                                    if (updateWorkGbnObject.getResultCode().equals("SUCC")) {
                                        Toast.makeText(LeScanPopupActivity.this, getString(R.string.matching_success), Toast.LENGTH_LONG).show();
                                        LbsPreferences.setIgnoreBeaconPinNo(LeScanPopupActivity.this, Integer.toString(mPinNo));
                                        LbsPreferences.setMappingBeaconPinNo(LeScanPopupActivity.this, Integer.toString(mPinNo));

                                        // 단말(Beacon)과 매핑 성공 시 Main 에 있는 웹뷰에 자바스크립트를 날려줘야 하기 때문에 여기서 Main 으로 신호를 보냄.
                                        BusProvider.getInstance().post(updateWorkGbnObject);
                                    } else {
                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LeScanPopupActivity.this);
                                        alertDialogBuilder.setTitle(getString(R.string.alert))
                                                .setMessage(updateWorkGbnObject.getErrMsg())
                                                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        finish();
                                                    }
                                                });
                                        AlertDialog dialog = alertDialogBuilder.create();
                                        dialog.show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<UpdateWorkGbnObject> call, Throwable t) {

                                }
                            });
                            finish();
                        }
                    }
                    break;
                case R.id.btn_passenger:
                    LbsPreferences.setIgnoreBeaconPinNo(LeScanPopupActivity.this, Integer.toString(mPinNo));
                    finish();
                    break;
                default:
                    break;
            }
        }
    };
}
