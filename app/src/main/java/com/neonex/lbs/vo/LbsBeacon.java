package com.neonex.lbs.vo;

import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by yun on 2017-12-21.
 */

public class LbsBeacon implements Serializable {

    private String mBeaconName = null;
    private int mPinNo = 0;
    private int mMissingCount = 0;
    private String mObjectTypeName = null;
    private String mObjectName = null;

    public LbsBeacon(String mBeaconName, int pinNo, String objectTypeName, String objectName) {
        this.mBeaconName = mBeaconName;
        this.mPinNo = pinNo;
        this.mObjectTypeName = objectTypeName;
        this.mObjectName = objectName;
    }

    public String getBeaconName() {
        return mBeaconName;
    }

    public int getPinNo() {
        return mPinNo;
    }

    public void addMissingCount() {
        mMissingCount++;
    }

    public void initMissingCount() {
        mMissingCount = 0;
    }

    public int getMissingCount() {
        return mMissingCount;
    }

    public String getmObjectTypeName() {
        return mObjectTypeName;
    }

    public void setmObjectTypeName(String mObjectTypeName) {
        this.mObjectTypeName = mObjectTypeName;
    }

    public String getmObjectName() {
        return mObjectName;
    }

    public void setmObjectName(String mObjectName) {
        this.mObjectName = mObjectName;
    }
}
