package com.neonex.lbs.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.neonex.lbs.LeScanPopupActivity;
import com.neonex.lbs.R;
import com.neonex.lbs.broadcast.RestartService;
import com.neonex.lbs.network.api.ApiService;
import com.neonex.lbs.network.api.DeviceObjectInfo;
import com.neonex.lbs.network.api.DisconnectEquipUser;
import com.neonex.lbs.utils.CommonUtils;
import com.neonex.lbs.utils.LbsPreferences;
import com.neonex.lbs.vo.LbsBeacon;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by yun on 2017-12-11.
 */

public class PersistentService extends Service {
    // CONST
    private static final int ADD_SCAN_COUNT = 11;
    private static final int START_SCANNING = 12;

    // BT
    private BluetoothAdapter mBtAdapter = null;
    private BluetoothManager mBtManager;
    private BluetoothLeScanner mBtScanner;

    private ArrayList<Integer> mScanResultList = new ArrayList<>();

    private Boolean mIsContain = false;

    private static PowerManager.WakeLock mCpuWakeLock = null;

    // 서비스 종료시 재부팅 딜레이 시간.
    private static final int REBOOT_DELAY_TIMER = 10 * 1000;

    // scan timer
    private int mScanTimer = 0;

    // 20초 스캔 후 현재 동승자 또는 운전자로 지목된 비콘이 검색되지 않는다면 1씩 증가.
    private int mMissingCount = 0;

    private IBinder mBinder = new PersistentServiceBinder();

    private ApiService mApiService = null;

    private final MyHandler mTimeHandler = new MyHandler(this);

    public class PersistentServiceBinder extends Binder {
        public PersistentService getService() {
            return PersistentService.this;
        }
    }

    public interface ICallback {
        void setDetectLbsBeaconInfo(LbsBeacon beaconInfo);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
//        Log.e("PersistentService", "onBind");
        return mBinder;
    }

    @Override
    public void onCreate() {
        // 등록된 알람 제거.
        unRegisterRestartAlarm();

        super.onCreate();

        mBtManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter = mBtManager.getAdapter();
        mBtScanner = mBtAdapter.getBluetoothLeScanner();

        mApiService = new Retrofit.Builder()
                .baseUrl(LbsPreferences.getLastServerAddress(this))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);
    }

    @Override
    public void onDestroy() {
        // 서비스가 죽을 때 알람 등록.
        registerRestartAlarm();

        stopScanning();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 스캐닝 시작.
        startScanning();
        // 타이머 시작.
        mTimeHandler.sendEmptyMessage(ADD_SCAN_COUNT);

        return super.onStartCommand(intent, flags, startId);
    }

    private void handleMessage(Message msg) {
        switch (msg.what) {
            case START_SCANNING:
                startScanning();
                break;
            case ADD_SCAN_COUNT:
                mScanTimer++;

                if (mScanTimer > 20) {
                    stopScanning();
                    mScanTimer = 0;
                    return;
                } else {
                    mTimeHandler.removeMessages(ADD_SCAN_COUNT);
                    mTimeHandler.sendEmptyMessageDelayed(ADD_SCAN_COUNT, 1000); /** 1초마다 카운트 1 증가 */
                }
                break;
            default:
                break;
        }

    }

    /**
     * BLE scan result callback.
     */
    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            final String pinNo = Integer.toString(CommonUtils.getSerialNo(Integer.parseInt(CommonUtils.getMajor(result.getScanRecord())),
                    Integer.parseInt(CommonUtils.getMinor(result.getScanRecord()))));

            Log.d("PersistentService", "Serial_No: " + pinNo);
            Log.d("PersistentService", "Device Name: " + result.getDevice().getName() + " rssi: " + result.getRssi());


            if (TextUtils.isEmpty(result.getDevice().getName())) {

            } else {
                mScanResultList.add(Integer.parseInt(pinNo));

                if (mCpuWakeLock != null) {
                    return;
                }

                // LbsBeacon 단말과의 거리가 -80보다 가까울 때에만 팝업을 띄움.
                if (result.getRssi() >= -70 &&
                        ((result.getDevice().getName().toUpperCase().contains("GPER")) || (result.getDevice().getName().toUpperCase().contains("V31-LORA")))) {
                    Log.e("PersistentService", "Serial_No: " + pinNo);
                    Log.e("PersistentService", "Device Name: " + result.getDevice().getName() + " rssi: " + result.getRssi());
                    // 운전자 또는 동승자를 선택했을 때에는 일정 interval 동안 띄우지 않음
                    Log.e("PersistentService", "ignoreBeaconPinNo is Empty : " + TextUtils.isEmpty(LbsPreferences.getIgnoreBeaconPinNo(PersistentService.this)));

                    if (!TextUtils.isEmpty(LbsPreferences.getIgnoreBeaconPinNo(PersistentService.this)) ||
                            !TextUtils.isEmpty(LbsPreferences.getMappingBeaconPinNo(PersistentService.this))) {
                        // if (동승자 또는 운전자가 선택되어 있는 비콘이 아닌 새로운 비콘.)
                        if (!LbsPreferences.getIgnoreBeaconPinNo(PersistentService.this).equals(pinNo) ||
                                !LbsPreferences.getMappingBeaconPinNo(PersistentService.this).equals(pinNo)) {
                            showBeaconPopup(Integer.parseInt(pinNo));
                        } else {
                            // 팝업을 띄우지 않음. 무시 list 에 있는 beacon 이 감지가 됨.
                        }
                    } else {
                        // mIgnoreBeacon 이 null 이라는 것은 매칭된 비콘이 없다는 말이기 때문에 팝업을 다 띄운다.
                        showBeaconPopup(Integer.parseInt(pinNo));
                    }
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
            super.onScanFailed(errorCode);
        }
    };

    /**
     * Ble scan start
     */
    private void startScanning() {
        System.out.println("start scanning");
        if (mBtScanner == null) {
            mBtScanner = mBtAdapter.getBluetoothLeScanner();
        }
        mBtScanner.startScan(mLeScanCallback);
    }

    /**
     * Ble scan stop
     */
    private void stopScanning() {
        System.out.println("stopping scanning");
        if (!TextUtils.isEmpty(LbsPreferences.getIgnoreBeaconPinNo(this))
                || !TextUtils.isEmpty(LbsPreferences.getMappingBeaconPinNo(this))) {
            if (mScanResultList.size() <= 0) { // 스캔된게 없다.
                mIsContain = false;
            } else {
                for (Integer pinNo : mScanResultList) {
                    if (Integer.toString(pinNo).equals(LbsPreferences.getIgnoreBeaconPinNo(PersistentService.this))
                            || Integer.toString(pinNo).equals(LbsPreferences.getMappingBeaconPinNo(PersistentService.this))) {
                        mIsContain = true;
                    } else {
                        mIsContain = false;
                    }
                }
                mScanResultList.clear();
            }

            if (!mIsContain) {
                mMissingCount++;
            } else {
                mMissingCount = 0;
            }

            if (mMissingCount > 15) {   // 15회 (5분) 이상 검색이 안됐다면 무시 list 에서 삭제 후 다시 팝업을 띄운다.
                LbsPreferences.setIgnoreBeaconPinNo(PersistentService.this, null);
                if (mMissingCount > 360) { // 360회 (2시간) 이상 검색이 안됐다면 서버에 매칭 해제 api 를 날린다.
                    mMissingCount = 0;
                    Call<DisconnectEquipUser> comment = mApiService.disconnectEquipUser(LbsPreferences.getMappingBeaconPinNo(PersistentService.this),
                            LbsPreferences.getUserId(PersistentService.this));
                    comment.enqueue(new Callback<DisconnectEquipUser>() {
                        @Override
                        public void onResponse(Call<DisconnectEquipUser> call, Response<DisconnectEquipUser> response) {
                            try {
                                if (response.body().getResultCode().equals("SUCC")) {
                                    // 매핑 해지
                                    LbsPreferences.setMappingBeaconPinNo(PersistentService.this, null);
                                    mMissingCount = 0;
                                } else {

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<DisconnectEquipUser> call, Throwable t) {

                        }
                    });
                }
            }
        } else {
            mMissingCount = 0;
        }

        mBtScanner.stopScan(mLeScanCallback);

        mTimeHandler.sendEmptyMessageDelayed(START_SCANNING, 1000);
        mTimeHandler.sendEmptyMessageDelayed(ADD_SCAN_COUNT, 1000);
    }

    /**
     * beacon scan popup
     *
     * @param pinNo
     */
    private void showBeaconPopup(final int pinNo) {
        Call<DeviceObjectInfo> comment = mApiService.deviceObjectInfo(Integer.toString(pinNo), LbsPreferences.getUserId(PersistentService.this));

        comment.enqueue(new Callback<DeviceObjectInfo>() {
            @Override
            public void onResponse(Call<DeviceObjectInfo> call, Response<DeviceObjectInfo> response) {
                try {
                    DeviceObjectInfo deviceObjectInfo = response.body();
                    //SUCC 가 응답 왔따는 것은 비콘과 아이디가 같은 소속이라는 것이므로 팝업 액티비티를 띄운다.
                    if (deviceObjectInfo.getResultCode().equals("SUCC")) {
                        if (!LbsPreferences.getAppFirstLaunch(PersistentService.this)) {
                            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                            mCpuWakeLock = pm.newWakeLock(
                                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                                            PowerManager.ACQUIRE_CAUSES_WAKEUP |
                                            PowerManager.ON_AFTER_RELEASE, "hi");

                            mCpuWakeLock.acquire();

                            Intent i = new Intent(PersistentService.this, LeScanPopupActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.putExtra("BEACON_DATA", deviceObjectInfo.getData());
                            i.putExtra("PIN_NO", pinNo);
                            startActivity(i);
                        } else {
                            // 앱 최초 실행시에는 로그인 전까지 비콘에 대한 팝업을 띄우지 않는다. 10초마다 토스트
                            if (mScanTimer > 15) {
                                Toast.makeText(PersistentService.this, getString(R.string.do_login_for_beacon_scan), Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        if (deviceObjectInfo.getErrCode().equals("0004")) {
                            Log.w("PersistentService", "소속 그룹이 다른 것들이다.");
                        } else {
                            Log.w("PersistentService", "deviceObjectInfo.getErrorMsg : " + deviceObjectInfo.getErrMsg());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (mCpuWakeLock != null) {
                    mCpuWakeLock.release();
                    mCpuWakeLock = null;
                }
            }

            @Override
            public void onFailure(Call<DeviceObjectInfo> call, Throwable t) {

            }
        });
    }


    /**
     * 죽지않는 서비스 구현을 위해 서비스 재시작 알람 등록.
     */
    private void registerRestartAlarm() {
        Intent intent = new Intent(PersistentService.this, RestartService.class);
        intent.setAction(RestartService.ACTION_RESTART_PERSISTENTSERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(PersistentService.this, 0, intent, 0);

        long firstTime = SystemClock.elapsedRealtime();
        firstTime += REBOOT_DELAY_TIMER; // 10초 후 알람 이벤트 발생.

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, REBOOT_DELAY_TIMER, sender);
    }

    /**
     * 알람 해지.
     */
    private void unRegisterRestartAlarm() {
        Intent intent = new Intent(PersistentService.this, RestartService.class);
        intent.setAction(RestartService.ACTION_RESTART_PERSISTENTSERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(PersistentService.this, 0, intent, 0);

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.cancel(sender);
    }

    private static class MyHandler extends Handler {
        private final WeakReference<PersistentService> weakReference;

        public MyHandler(PersistentService service) {
            this.weakReference = new WeakReference<PersistentService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            PersistentService service = weakReference.get();

            if (service != null) {
                service.handleMessage(msg);
            }
            super.handleMessage(msg);
        }
    }
}
