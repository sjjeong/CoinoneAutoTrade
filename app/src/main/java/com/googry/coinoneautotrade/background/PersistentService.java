package com.googry.coinoneautotrade.background;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;

import com.googry.coinoneautotrade.Config;
import com.googry.coinoneautotrade.data.realm.AutoBotControl;
import com.googry.coinoneautotrade.util.LogUtil;

import java.util.ArrayList;


/**
 * Created by seokjunjeong on 2017. 6. 1..
 */

public class PersistentService extends Service {

    private static final int COUNT_DOWN_INTERVAL = 1000 * 3;
    private static final int MILLISINFUTURE = 86400 * 1000;

    private static int mCoinCycle;

    private CountDownTimer countDownTimer;

    private ArrayList<TradeRunner> mTradeRunners;

    @Override
    public void onCreate() {
        unregisterRestartAlarm();
        super.onCreate();


        initData();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, new Notification());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        countDownTimer.cancel();
        /**
         * 서비스 종료 시 알람 등록을 통해 서비스 재 실행
         */
        registerRestartAlarm();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 데이터 초기화
     */
    private void initData() {
        mCoinCycle = 0;
        mTradeRunners = new ArrayList<>();
        mTradeRunners.add(new TradeRunner(AutoBotControl.BTC, Config.ACCESS_TOKEN_HOME_BTC, Config.SECRET_KEY_HOME_BTC));
        mTradeRunners.add(new TradeRunner(AutoBotControl.BCH, Config.ACCESS_TOKEN_HOME_BCH, Config.SECRET_KEY_HOME_BCH));
        mTradeRunners.add(new TradeRunner(AutoBotControl.ETH, Config.ACCESS_TOKEN_HOME_ETH, Config.SECRET_KEY_HOME_ETH));
        mTradeRunners.add(new TradeRunner(AutoBotControl.ETC, Config.ACCESS_TOKEN_HOME_ETC, Config.SECRET_KEY_HOME_ETC));
        mTradeRunners.add(new TradeRunner(AutoBotControl.XRP, Config.ACCESS_TOKEN_HOME_XRP, Config.SECRET_KEY_HOME_XRP));
        mTradeRunners.add(new TradeRunner(AutoBotControl.QTUM, Config.ACCESS_TOKEN_HOME_QTUM, Config.SECRET_KEY_HOME_QTUM));

        countDownTimer();
        countDownTimer.start();
    }

    public void countDownTimer() {

        countDownTimer = new CountDownTimer(MILLISINFUTURE, COUNT_DOWN_INTERVAL) {
            public void onTick(long millisUntilFinished) {
                for (int i = 0; i < mTradeRunners.size(); i++) {
                    mCoinCycle = (mCoinCycle + 1) % mTradeRunners.size();
                    if(mTradeRunners.get(mCoinCycle).run())
                        break;
                }
            }

            public void onFinish() {
                countDownTimer();
                countDownTimer.start();
            }
        };
    }


    /**
     * 알람 매니져에 서비스 등록
     */
    private void registerRestartAlarm() {
        LogUtil.i("registerRestartAlarm");
        Intent intent = new Intent(PersistentService.this, RestartReceiver.class);
        intent.setAction("ACTION.RESTART.PersistentService");
        PendingIntent sender = PendingIntent.getBroadcast(PersistentService.this, 0, intent, 0);

        long firstTime = SystemClock.elapsedRealtime();
        firstTime += 1 * 1000;

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        /**
         * 알람 등록
         */
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 1 * 1000, sender);

    }

    /**
     * 알람 매니져에 서비스 해제
     */
    private void unregisterRestartAlarm() {
        LogUtil.i("unregisterRestartAlarm");
        Intent intent = new Intent(PersistentService.this, RestartReceiver.class);
        intent.setAction("ACTION.RESTART.PersistentService");
        PendingIntent sender = PendingIntent.getBroadcast(PersistentService.this, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        /**
         * 알람 취소
         */
        alarmManager.cancel(sender);


    }
}
