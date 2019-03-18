package com.neonex.lbs.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.neonex.lbs.service.PersistentService;

/**
 * Created by yun on 2017-12-11.
 */

public class RestartService extends BroadcastReceiver {

    public static final String ACTION_RESTART_PERSISTENTSERVICE = "ACTION.Restart.PersistentService";

    @Override
    public void onReceive(Context context, Intent intent) {
        // 서비스 죽을 때 알람으로 다시 서비스 등록
        if (intent.getAction().equals(ACTION_RESTART_PERSISTENTSERVICE)) {
            Intent i = new Intent(context, PersistentService.class);
            context.startService(i);
        }

        // 폰 부팅 할 때 서비스 등록
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent i = new Intent(context, PersistentService.class);
            context.startService(i);
        }
    }
}
