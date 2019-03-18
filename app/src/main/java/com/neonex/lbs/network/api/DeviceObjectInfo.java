package com.neonex.lbs.network.api;

/**
 * Created by macpro on 2018. 2. 1..
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DeviceObjectInfo {

    @SerializedName("resultCode")
    @Expose
    private String resultCode;
    @SerializedName("errCode")
    @Expose
    private String errCode;
    @SerializedName("errMsg")
    @Expose
    private String errMsg;

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }


    @SerializedName("data")
    @Expose
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public class Data implements Serializable {

        @SerializedName("objectName")
        @Expose
        private String objectName;
        @SerializedName("objectType")
        @Expose
        private String objectType;
        @SerializedName("objectTypeName")
        @Expose
        private String objectTypeName;
        @SerializedName("equipUseYn")
        @Expose
        private String equipUseYn;

        public String getObjectName() {
            return objectName;
        }

        public void setObjectName(String objectName) {
            this.objectName = objectName;
        }

        public String getObjectType() {
            return objectType;
        }

        public void setObjectType(String objectType) {
            this.objectType = objectType;
        }

        public String getObjectTypeName() {
            return objectTypeName;
        }

        public void setObjectTypeName(String objectTypeName) {
            this.objectTypeName = objectTypeName;
        }

        public String getEquipUseYn() {
            return equipUseYn;
        }

        public void setEquipUseYn(String equipUseYn) {
            this.equipUseYn = equipUseYn;
        }

    }

}
