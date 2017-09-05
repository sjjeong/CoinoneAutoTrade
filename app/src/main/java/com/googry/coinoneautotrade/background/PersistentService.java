package com.googry.coinoneautotrade.background;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;

import com.googry.coinoneautotrade.Config;
import com.googry.coinoneautotrade.data.CoinoneCompleteOrder;
import com.googry.coinoneautotrade.data.CoinoneLimitOrder;
import com.googry.coinoneautotrade.data.CoinoneTicker;
import com.googry.coinoneautotrade.data.Order;
import com.googry.coinoneautotrade.data.realm.AutoBotControl;
import com.googry.coinoneautotrade.data.remote.CoinoneApiManager;
import com.googry.coinoneautotrade.util.EncryptionUtil;
import com.googry.coinoneautotrade.util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by seokjunjeong on 2017. 6. 1..
 */

public class PersistentService extends Service {

    private static final int COUNT_DOWN_INTERVAL = 1000 * 3;
    private static final int MILLISINFUTURE = 86400 * 1000;

    private static final int LIMIT_PRIVATE_API_CALL_COUNT = 5;

    private CountDownTimer countDownTimer;

    private Context mContext;

    private String mCoinType;
    private int mCallCnt;
    private int mBuyPriceMin;
    private double mBuyAmount;
    private double mSellAmount;
    private double mPricePercent;

    private CoinoneApiManager.CoinonePrivateApi mPrivateApi;
    private CoinoneApiManager.CoinonePublicApi mPublicApi;

    /**
     * Ticker
     */
    private CoinoneTicker.Ticker mTicker;
    
    /**
     * prices
     */
    private ArrayList<Long> mAsks = new ArrayList<>();
    private ArrayList<Long> mBids = new ArrayList<>();

    private Realm mRealm;

    @Override
    public void onCreate() {
        unregisterRestartAlarm();
        super.onCreate();

        initData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        countDownTimer.cancel();
        if (mRealm != null) {
            mRealm.close();
            mRealm = null;
        }

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
        mContext = this;
        mRealm = Realm.getDefaultInstance();

        mPrivateApi = CoinoneApiManager.getApiManager().create(CoinoneApiManager.CoinonePrivateApi.class);
        mPublicApi = CoinoneApiManager.getApiManager().create(CoinoneApiManager.CoinonePublicApi.class);

        countDownTimer();
        countDownTimer.start();
    }

    public void countDownTimer() {

        countDownTimer = new CountDownTimer(MILLISINFUTURE, COUNT_DOWN_INTERVAL) {
            public void onTick(long millisUntilFinished) {
                mCallCnt = 0;
                mCoinType = "xrp";
                mBuyAmount = 10;
                mSellAmount = 9.99;
                mPricePercent = 1.02;
                /**
                 * Ticker
                 */
                AutoBotControl control = mRealm.where(AutoBotControl.class).equalTo("coinType", mCoinType).findFirst();
                if (control.runFlag)
                    callTicker();
            }

            public void onFinish() {
                countDownTimer();
                countDownTimer.start();
            }
        };
    }

    private void callTicker() {
        Call<CoinoneTicker.Ticker> callTicker = mPublicApi.ticker(mCoinType);
        callTicker.enqueue(new Callback<CoinoneTicker.Ticker>() {
            @Override
            public void onResponse(Call<CoinoneTicker.Ticker> call, Response<CoinoneTicker.Ticker> response) {
                CoinoneTicker.Ticker ticker = response.body();
                if (ticker == null) {
                    LogUtil.i("ticker is null");
                    return;
                }
                LogUtil.i("price: " + ticker.last);
                mTicker = ticker;
                mBuyPriceMin = (int) (ticker.last * 0.9);
                /**
                 * LimitOrders
                 */
                callLimitOrders();
            }

            @Override
            public void onFailure(Call<CoinoneTicker.Ticker> call, Throwable t) {

            }
        });
    }

    private void callCompleteOrders() {
        if (mCallCnt++ >= LIMIT_PRIVATE_API_CALL_COUNT)
            return;

        String limitOrdersPayload = EncryptionUtil.getJsonLimitOrders(
                Config.ACCESS_TOKEN, mCoinType, System.currentTimeMillis());
        String encryptlimitOrdersPayload = EncryptionUtil.getEncyptPayload(limitOrdersPayload);
        String limitOrdersSignature = EncryptionUtil.getSignature(Config.SECRET_KEY, encryptlimitOrdersPayload);
        Call<CoinoneCompleteOrder> callCompleteOrders = mPrivateApi.completeOrders(
                encryptlimitOrdersPayload, limitOrdersSignature, encryptlimitOrdersPayload);

        callCompleteOrders.enqueue(new Callback<CoinoneCompleteOrder>() {
            @Override
            public void onResponse(Call<CoinoneCompleteOrder> call, Response<CoinoneCompleteOrder> response) {
                final CoinoneCompleteOrder completeOrder = response.body();
                if (completeOrder == null) {
                    LogUtil.i("completeOrder is null");
                    return;
                }

                LogUtil.i("complete size: " + completeOrder.completeOrders.size());

                for (Order order : completeOrder.completeOrders) {
                    /**
                     * bid는 매수
                     * order.type이 bid이면 매수 채결
                     * ask에 매도 주문
                     */
                    if ("bid".equals(order.type)) {
                        long price = Math.round(((float) order.price) * mPricePercent);
                        if (!mAsks.contains(price) && mTicker.last < price) {
                            LogUtil.i("sell price: " + price);
                            callSellLimit(price);
                        }
                    }
                }

                for (long i = mTicker.last - 1; i >= mBuyPriceMin; i--) {
                    /**
                     * bid(매수)에 가격이 없으므로 매수에 걸어야함
                     */
                    if (!mBids.contains(i)) {
                        if (!mAsks.contains(Math.round(((float) i) * mPricePercent))) {
                            /**
                             * 매수를 i로 요청
                             */
                            callBuyLimit(i);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<CoinoneCompleteOrder> call, Throwable t) {

            }
        });
    }


    private void callLimitOrders() {
        if (mCallCnt++ >= LIMIT_PRIVATE_API_CALL_COUNT)
            return;

        String limitOrdersPayload = EncryptionUtil.getJsonLimitOrders(
                Config.ACCESS_TOKEN, mCoinType, System.currentTimeMillis());
        String encryptlimitOrdersPayload = EncryptionUtil.getEncyptPayload(limitOrdersPayload);
        String limitOrdersSignature = EncryptionUtil.getSignature(Config.SECRET_KEY, encryptlimitOrdersPayload);
        Call<CoinoneLimitOrder> callLimitOrders = mPrivateApi.limitOrders(
                encryptlimitOrdersPayload, limitOrdersSignature, encryptlimitOrdersPayload);

        callLimitOrders.enqueue(new Callback<CoinoneLimitOrder>() {
            @Override
            public void onResponse(Call<CoinoneLimitOrder> call, Response<CoinoneLimitOrder> response) {
                final CoinoneLimitOrder limitOrder = response.body();
                if (limitOrder == null) {
                    LogUtil.i("limitorder is null");
                    return;
                }

                LogUtil.i("limit size: " + limitOrder.limitOrders.size());

                mAsks.clear();
                mBids.clear();

                for (Order order : limitOrder.limitOrders) {
                    if (order.type.equals("ask")) {
                        mAsks.add(order.price);
                    } else {
                        mBids.add(order.price);
                    }
                }

                Collections.sort(mAsks);
                Collections.sort(mBids, Collections.<Long>reverseOrder());

                callCompleteOrders();
            }

            @Override
            public void onFailure(Call<CoinoneLimitOrder> call, Throwable t) {

            }
        });
    }


    private void callBuyLimit(long price) {
        if (mCallCnt++ >= LIMIT_PRIVATE_API_CALL_COUNT)
            return;

        String orderBuyPayload = EncryptionUtil.getJsonOrderBuy(Config.ACCESS_TOKEN,
                price,
                mBuyAmount,
                mCoinType,
                System.currentTimeMillis());
        String encyptOrderBuyPayload = EncryptionUtil.getEncyptPayload(orderBuyPayload);
        String signature = EncryptionUtil.getSignature(Config.SECRET_KEY, encyptOrderBuyPayload);

        CoinoneApiManager.CoinonePrivateApi api =
                CoinoneApiManager.getApiManager().create(CoinoneApiManager.CoinonePrivateApi.class);
        Call<Void> call = api.buysell("buy", encyptOrderBuyPayload, signature, encyptOrderBuyPayload);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                LogUtil.i("buy\n" + response.toString());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });

    }

    private void callSellLimit(long price) {
        if (mCallCnt++ >= LIMIT_PRIVATE_API_CALL_COUNT)
            return;

        String orderBuyPayload = EncryptionUtil.getJsonOrderBuy(Config.ACCESS_TOKEN,
                price,
                mSellAmount,
                mCoinType,
                System.currentTimeMillis());
        String encyptOrderBuyPayload = EncryptionUtil.getEncyptPayload(orderBuyPayload);
        String signature = EncryptionUtil.getSignature(Config.SECRET_KEY, encyptOrderBuyPayload);

        CoinoneApiManager.CoinonePrivateApi api =
                CoinoneApiManager.getApiManager().create(CoinoneApiManager.CoinonePrivateApi.class);
        Call<Void> call = api.buysell("sell", encyptOrderBuyPayload, signature, encyptOrderBuyPayload);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                LogUtil.i("sell\n" + response.toString());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });

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
