package com.googry.coinoneautotrade.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.googry.coinoneautotrade.util.LogUtil;


/**
 * Created by seokjunjeong on 2017. 6. 1..
 */

public class RestartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.i("RestartReceiver called : " + intent.getAction());

        /**
         * 서비스 죽일때 알람으로 다시 서비스 등록
         */
        if (intent.getAction().equals("ACTION.RESTART.PersistentService")) {
            LogUtil.i("ACTION.RESTART.PersistentService ");

            Intent i = new Intent(context, PersistentService.class);
            context.startService(i);
        }

        /**
         * 폰 재시작 할때 서비스 등록
         */
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            LogUtil.i("ACTION_BOOT_COMPLETED");
            Intent i = new Intent(context, PersistentService.class);
            context.startService(i);

        }


    }
}
