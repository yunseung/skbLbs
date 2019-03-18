package com.neonex.lbs.javascript;

import android.content.Context;
import android.webkit.JavascriptInterface;

import java.io.Serializable;

/**
 * Created by macpro on 2018. 1. 17..
 */

public class LbsAndroidBridge {

    private Context mContext = null;

    private OnLbsAndroidBridgeListener mOnLbsAndroidBridgeListener = null;

    public interface OnLbsAndroidBridgeListener {
        void sendFcmToken();
        void loginComplete(String userId);
        void sendLocation();
        void sendStopUseEquip();
    }

    public void setOnAndroidBridgeListener(OnLbsAndroidBridgeListener listener) {
        mOnLbsAndroidBridgeListener = listener;
    }

    public LbsAndroidBridge(Context ctx) {
        mContext = ctx;
    }

    @JavascriptInterface
    public void sendToken() {
        mOnLbsAndroidBridgeListener.sendFcmToken();
    }

    @JavascriptInterface
    public void loginComplete(String userId) {
        mOnLbsAndroidBridgeListener.loginComplete(userId);
    }

    @JavascriptInterface
    public void sendGeoPosition() {
        mOnLbsAndroidBridgeListener.sendLocation();
    }

    @JavascriptInterface
    public void sendStopUseEquip() {
        mOnLbsAndroidBridgeListener.sendStopUseEquip();
    }

}
