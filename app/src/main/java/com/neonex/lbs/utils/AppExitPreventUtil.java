package com.neonex.lbs.utils;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanRecord;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.neonex.lbs.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by yun on 2017-09-25.
 */

public class AppExitPreventUtil {

    int delayTime = 2500;
    private long backKeyPressedTime = 0;
    private Toast toast;

    private Context mContext;

    public AppExitPreventUtil(Context _context) {
        mContext = _context;
    }


    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + delayTime) { //백버튼 한번 눌렀을때 가이드 보여주기
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + delayTime) { // 두번 눌렀을때 종료 처리

            CommonUtils.killAppProcess(mContext);
            ((AppCompatActivity) mContext).finish();

            toast.cancel();
        }
    }

    private void showGuide() {
        toast = Toast.makeText(mContext, mContext.getResources().getString(R.string.app_exit_message), Toast.LENGTH_LONG);
        toast.show();
    }

}
