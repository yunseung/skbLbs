package com.neonex.lbs.utils;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * Created by macpro on 2018. 2. 6..
 */

public class Lbs {
    private static volatile Lbs instance = null;

    public static Lbs getInstance() {
        if (instance == null) {
            synchronized (Lbs.class) {
                if (instance == null) {
                    instance = new Lbs();
                }
            }
        }

        return instance;
    }

    /**
     * 안병천 과장
     */
    public static final int SERVER_152 = 152;
    /**
     * 이길재 차장
     */
    public static final int SERVER_158 = 158;
    /**
     * 양준성 과장
     */
    public static final int SERVER_156 = 156;
    /**
     * 조한영 과장
     */
    public static final int SERVER_157 = 157;
    /**
     * 운영
     */
    public static final int SERVER_STAGE = 100;
    /**
     * 개발
     */
    public static final int SERVER_DEV = 101;
    /**
     * 테스트용. 그때그때 바뀜.
     */
    public static final int SERVER_TEST = 111;

    public void setServerUri(Context context, int serverType) {
        switch (serverType) {
            case SERVER_156:
                LbsPreferences.setLastServerAddress(context, "http://192.168.1.156:8080");
                break;
            case SERVER_152:
                LbsPreferences.setLastServerAddress(context, "http://192.168.1.152:8080");
                break;
            case SERVER_157:
                LbsPreferences.setLastServerAddress(context, "http://192.168.1.157:8080");
                break;
            case SERVER_158:
                LbsPreferences.setLastServerAddress(context, "http://192.168.1.158:8080");
                break;
            case SERVER_STAGE:
                LbsPreferences.setLastServerAddress(context, "https://lbs.skbroadband.com");
                break;
            case SERVER_DEV:
                LbsPreferences.setLastServerAddress(context, "http://192.168.1.???/8080");
                break;
            case SERVER_TEST:
                LbsPreferences.setLastServerAddress(context, "https://lbs.skbroadband.com/resources/html/test001.html");
                break;
            default:
                LbsPreferences.setLastServerAddress(context, "http://lbs.skbroadband.com");
                break;

        }
    }
}
