package com.neonex.lbs.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by yun on 2017-10-31.
 */

public class LbsPreferences {
    private static final String FCM_TOKEN = "FCM_TOKEN";
    private static final String APP_FIRST_LAUNCH = "APP_FIRST_LAUNCH";
    private static final String USER_ID = "USER_ID";
    private static final String IGNORE_PIN_NO = "IGNORE_PIN_NO";
    private static final String MAPPING_PIN_NO = "MAPPING_PIN_NO";
    private static final String LAST_SERVER_ADDRESS = "LAST_SERVER_ADDRESS";

    public static final String PREF_NAME = "nsok.pref";

    private static void setString(Context context, String key, String value) {
        assert context != null;
        assert key != null;

        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private static String getString(Context context, String key, String defValue) {
        assert context != null;
        assert key != null;

        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, 0);
        return pref.getString(key, defValue);
    }

    private static void setBoolean(Context context, String key, boolean value) {
        assert context != null;
        assert key != null;

        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    private static boolean getBoolean(Context context, String key, boolean defValue) {
        assert context != null;
        assert key != null;

        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, 0);
        return pref.getBoolean(key, defValue);
    }

    public static void setFcmToken(Context context, String fcmToken) {
        assert context != null;
        setString(context, FCM_TOKEN, fcmToken);
    }

    public static String getFcmToken(Context context) {
        assert context != null;
        return getString(context, FCM_TOKEN, "");
    }

    public static void setAppFirstLaunch(Context context, boolean isFirstLaunch) {
        assert context != null;
        setBoolean(context, APP_FIRST_LAUNCH, isFirstLaunch);
    }

    public static boolean getAppFirstLaunch(Context context) {
        assert context != null;
        return getBoolean(context, APP_FIRST_LAUNCH, true);
    }

    public static void setUserId(Context context, String userId) {
        assert context != null;
        setString(context, USER_ID, userId);
    }

    public static String getUserId(Context context) {
        assert context != null;
        return getString(context, USER_ID, "");
    }

    public static void setIgnoreBeaconPinNo(Context context, String pinNo) {
        assert context != null;
        setString(context, IGNORE_PIN_NO, pinNo);
    }

    public static String getIgnoreBeaconPinNo(Context context) {
        assert context != null;
        return getString(context, IGNORE_PIN_NO, "");
    }

    public static void setMappingBeaconPinNo(Context context, String pinNo) {
        assert context != null;
        setString(context, MAPPING_PIN_NO, pinNo);
    }

    public static String getMappingBeaconPinNo(Context context) {
        assert context != null;
        return getString(context, MAPPING_PIN_NO, "");
    }

    public static void setLastServerAddress(Context context, String serverAddr) {
        assert context != null;
        setString(context, LAST_SERVER_ADDRESS, serverAddr);
    }

    public static String getLastServerAddress(Context context) {
        assert context != null;
        return getString(context, LAST_SERVER_ADDRESS, "");
    }
}
