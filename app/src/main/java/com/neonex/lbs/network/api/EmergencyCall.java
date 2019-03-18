package com.neonex.lbs.network.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by macpro on 2018. 2. 5..
 */

public class EmergencyCall {
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

}