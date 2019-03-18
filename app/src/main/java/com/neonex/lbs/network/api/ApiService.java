package com.neonex.lbs.network.api;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by macpro on 2018. 2. 1..
 */

public interface ApiService {

//    String API_URL = LbsPreferences.getLastServerAddress(ApiService.this);

    @POST("/app/deviceObjectInfo.do")
    Call<DeviceObjectInfo> deviceObjectInfo(@Query("equipPinNo") String equipPinNo, @Query("userId") String userId);

    @POST("/app/connectEquipUser.do")
    Call<ConnectEquipUser> connectEquipUser(@Query("equipPinNo") String equipPinNo, @Query("userId") String userId);

    @POST("/app/disConnectEquipUser.do")
    Call<DisconnectEquipUser> disconnectEquipUser(@Query("equipPinNo") String equipPinNo, @Query("userId") String userId);

    @POST("/app/emergencyCall.do")
    Call<EmergencyCall> emergencyCall(@Query("userId") String userId);

    @POST("/app/updateWorkGbnObject.do")
    Call<UpdateWorkGbnObject> updateWorkGbnObject(@Query("equipPinNo") String equipPinNo, @Query("workGbnCode") String workGbnCode, @Query("userId") String userId);

//    Retrofit retrofit = new Retrofit.Builder()
//            .baseUrl(ApiService.API_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build();
}
