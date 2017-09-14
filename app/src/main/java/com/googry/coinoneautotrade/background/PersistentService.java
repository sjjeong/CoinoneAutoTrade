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
import com.googry.coinoneautotrade.data.CoinoneBalance;
import com.googry.coinoneautotrade.data.CoinoneLimitOrder;
import com.googry.coinoneautotrade.data.CoinoneTicker;
import com.googry.coinoneautotrade.data.Order;
import com.googry.coinoneautotrade.data.realm.AutoBotControl;
import com.googry.coinoneautotrade.data.remote.CoinoneApiManager;
import com.googry.coinoneautotrade.util.EncryptionUtil;
import com.googry.coinoneautotrade.util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;

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

    private static final int COIN_TYPE_CNT = 10;

    private static final int SELL_RANGE_ABS = 20;

    private static int mCoinCycle;

    private CountDownTimer countDownTimer;

    private Context mContext;

    private String mCoinType;
    private int mCallCnt;
    private long mBuyPriceMin;
    private long mSellPriceMax;
    private double mBuyAmount;
    private double mSellAmount;
    private double mPricePercent;
    private double divideUnit;
    private float mBidPriceRange;
    private long mLastPrice;

    private CoinoneBalance.Balance mBalance;
    private CoinoneBalance.Balance mBalanceKrw;

    private CoinoneApiManager.CoinonePrivateApi mPrivateApi;
    private CoinoneApiManager.CoinonePublicApi mPublicApi;

    /**
     * prices
     */
    private ArrayList<Long> mAsks = new ArrayList<>();
    private ArrayList<Long> mBids = new ArrayList<>();
    private ArrayList<Order> mBidOrders = new ArrayList<>();

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
        mCoinCycle = 0;
        mLastPrice = 0;

        mPrivateApi = CoinoneApiManager.getApiManager().create(CoinoneApiManager.CoinonePrivateApi.class);
        mPublicApi = CoinoneApiManager.getApiManager().create(CoinoneApiManager.CoinonePublicApi.class);

        countDownTimer();
        countDownTimer.start();
    }

    public void countDownTimer() {

        countDownTimer = new CountDownTimer(MILLISINFUTURE, COUNT_DOWN_INTERVAL) {
            public void onTick(long millisUntilFinished) {
                AutoBotControl control = null;
                for (int i = 0; i < COIN_TYPE_CNT; i++) {
                    switch (mCoinCycle) {
                        case 0: {
                            mCoinType = AutoBotControl.BTC;
                            divideUnit = 500;
                        }
                        break;
                        case 1: {
                            mCoinType = AutoBotControl.BCH;
                            divideUnit = 100;
                        }
                        break;
                        case 2: {
                            mCoinType = AutoBotControl.ETH;
                            divideUnit = 50;
                        }
                        break;
                        case 3: {
                            mCoinType = AutoBotControl.ETC;
                            divideUnit = 10;
                        }
                        break;
                        case 4: {
                            mCoinType = AutoBotControl.XRP;
                            divideUnit = 1;
                        }
                        break;
                        case 5:
                        case 6:
                        case 7:
                        case 8:
                        case 9: {
                            mCoinType = AutoBotControl.QTUM;
                            divideUnit = 10;
                        }
                        break;
                    }
                    mCoinCycle = (mCoinCycle + 1) % COIN_TYPE_CNT;
                    control = mRealm.where(AutoBotControl.class).equalTo("coinType", mCoinType).findFirst();
                    if (control != null && control.runFlag)
                        break;
                }

                LogUtil.i("----------------------------------------");
                LogUtil.i("[ " + mCoinType + " ]");
                if (control == null) {
                    LogUtil.i("is null");
                    return;
                }
                mCallCnt = 0;
                mCoinType = control.coinType;
                mBuyAmount = control.buyAmount;
                mSellAmount = control.sellAmount;
                mPricePercent = control.pricePercent;
                mBidPriceRange = control.bidPriceRange;
                /**
                 * Ticker
                 */
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
                    LogUtil.e("ticker is null");
                    return;
                }
                LogUtil.i(String.format("Price\t%,d\t\t %+d %s", ticker.last, ticker.last - mLastPrice,
                        ticker.last > mLastPrice ? "UP" : ticker.last < mLastPrice ? "DOWN" : "X_X"));
                mLastPrice = ticker.last;
                mBuyPriceMin = (long) (Math.round(((float) ticker.last) * mBidPriceRange / divideUnit) * divideUnit);
                mSellPriceMax = (long) (ticker.last + (divideUnit * SELL_RANGE_ABS));

                callBalance();
            }

            @Override
            public void onFailure(Call<CoinoneTicker.Ticker> call, Throwable t) {
            }
        });
    }

    private void callBalance() {
        if (mCallCnt >= LIMIT_PRIVATE_API_CALL_COUNT)
            return;
        mCallCnt++;

        String limitOrdersPayload = EncryptionUtil.getJsonLimitOrders(
                Config.ACCESS_TOKEN, mCoinType, System.currentTimeMillis());
        String encryptlimitOrdersPayload = EncryptionUtil.getEncyptPayload(limitOrdersPayload);
        String limitOrdersSignature = EncryptionUtil.getSignature(Config.SECRET_KEY, encryptlimitOrdersPayload);
        Call<CoinoneBalance> callBalance = mPrivateApi.balance(
                encryptlimitOrdersPayload, limitOrdersSignature, encryptlimitOrdersPayload);
        callBalance.enqueue(new Callback<CoinoneBalance>() {
            @Override
            public void onResponse(Call<CoinoneBalance> call, Response<CoinoneBalance> response) {
                if (response.body() == null)
                    return;
                CoinoneBalance coinoneBalance = response.body();
                switch (mCoinType) {
                    case AutoBotControl.BTC: {
                        mBalance = coinoneBalance.balanceBtc;
                    }
                    break;
                    case AutoBotControl.BCH: {
                        mBalance = coinoneBalance.balanceBch;
                    }
                    break;
                    case AutoBotControl.ETH: {
                        mBalance = coinoneBalance.balanceEth;
                    }
                    break;
                    case AutoBotControl.ETC: {
                        mBalance = coinoneBalance.balanceEtc;
                    }
                    break;
                    case AutoBotControl.XRP: {
                        mBalance = coinoneBalance.balanceXrp;
                    }
                    break;
                    case AutoBotControl.QTUM: {
                        mBalance = coinoneBalance.balanceQtum;
                    }
                    break;
                }
                mBalanceKrw = coinoneBalance.balanceKrw;

                /**
                 * LimitOrders
                 */
                callLimitOrders();

            }


            @Override
            public void onFailure(Call<CoinoneBalance> call, Throwable t) {

            }
        });
    }

    private void callLimitOrders() {
        if (mCallCnt >= LIMIT_PRIVATE_API_CALL_COUNT)
            return;
        mCallCnt++;

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
                    LogUtil.e("limitorder is null");
                    return;
                }

                mAsks.clear();
                mBids.clear();
                mBidOrders.clear();

                for (Order order : limitOrder.limitOrders) {
                    if (order.type.equals("ask")) {
                        mAsks.add(order.price);
                    } else {
                        mBids.add(order.price);
                        mBidOrders.add(order);
                    }
                }

                Collections.sort(mAsks);
                Collections.sort(mBids, Collections.<Long>reverseOrder());

                if (mBalance.avail < mSellAmount) {
                    if (mBalanceKrw.avail < mLastPrice) {
//                        callSellLimit(1000000L, 0.5);
                    } else {
                        for (long i = mBuyPriceMin; i < (long) (mLastPrice - divideUnit * 2); i = (long) (i + divideUnit)) {
                            /**
                             * bid(매수)에 가격이 없으므로 매수에 걸어야함
                             */
                            if (!mBids.contains(i)) {
                                long price = (long) (Math.round(((float) i) * mPricePercent / divideUnit) * divideUnit);
                                if (!mAsks.contains(price)) {
                                    /**
                                     * 매수를 price로 요청
                                     */
                                    callBuyLimit(i, mBuyAmount);
                                }
                            }
                        }
                    }
                } else {
                    /**
                     * Ticker.last +
                     * 이 bid에 걸려있는지 확인
                     * 없으면 ask에 매도 주문
                     */
                    for (long i = mSellPriceMax; i > (long) (mLastPrice + divideUnit * 2); i = (long) (i - divideUnit)) {
                        if (!mAsks.contains(i)) {
                            long price = (long) (Math.round(((float) i) / mPricePercent / divideUnit) * divideUnit);
                            if (!mBids.contains(price)) {
                                /**
                                 * 매도에 걸어둘꺼니까 매수하지 말라고 추가함
                                 */
                                mAsks.add(i);
                                callSellLimit(i, mSellAmount);
                            }
                        }
                    }
                }
                int lowPrice = (int) (Math.round(((float) mLastPrice) * mBidPriceRange * 0.99f / divideUnit) * divideUnit);
                for (Order cancelOrder : mBidOrders) {
                    if (cancelOrder.price < lowPrice) {
                        callCancelLimit(cancelOrder);
                    }
                }
            }

            @Override
            public void onFailure(Call<CoinoneLimitOrder> call, Throwable t) {
            }
        });
    }


    private void callSellLimit(final long price, final double sellAmount) {
        if (mCallCnt >= LIMIT_PRIVATE_API_CALL_COUNT)
            return;
        mCallCnt++;

        String orderBuyPayload = EncryptionUtil.getJsonOrderBuy(Config.ACCESS_TOKEN,
                price,
                sellAmount,
                mCoinType,
                System.currentTimeMillis());
        String encyptOrderBuyPayload = EncryptionUtil.getEncyptPayload(orderBuyPayload);
        String signature = EncryptionUtil.getSignature(Config.SECRET_KEY, encyptOrderBuyPayload);

        Call<Void> call = mPrivateApi.buysell("sell", encyptOrderBuyPayload, signature, encyptOrderBuyPayload);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                LogUtil.i(String.format("sell\t%,d\t\tamount ", price, sellAmount));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
            }
        });

    }

    private void callBuyLimit(final long price, final double buyAmount) {
        if (mCallCnt >= LIMIT_PRIVATE_API_CALL_COUNT)
            return;
        mCallCnt++;

        String orderBuyPayload = EncryptionUtil.getJsonOrderBuy(Config.ACCESS_TOKEN,
                price,
                buyAmount,
                mCoinType,
                System.currentTimeMillis());
        String encyptOrderBuyPayload = EncryptionUtil.getEncyptPayload(orderBuyPayload);
        String signature = EncryptionUtil.getSignature(Config.SECRET_KEY, encyptOrderBuyPayload);

        Call<Void> call = mPrivateApi.buysell("buy", encyptOrderBuyPayload, signature, encyptOrderBuyPayload);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                LogUtil.i(String.format("buy\t%,d\t\tamount ", price, buyAmount));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
            }
        });

    }

    private void callCancelLimit(final Order cancelOrder) {
        if (mCallCnt >= LIMIT_PRIVATE_API_CALL_COUNT)
            return;
        mCallCnt++;

        String limitOrdersPayload = EncryptionUtil.getJsonCancelOrder(
                Config.ACCESS_TOKEN,
                cancelOrder.orderId,
                cancelOrder.price,
                cancelOrder.qty,
                0,
                mCoinType,
                System.currentTimeMillis());
        String encryptlimitOrdersPayload = EncryptionUtil.getEncyptPayload(limitOrdersPayload);
        String limitOrdersSignature = EncryptionUtil.getSignature(Config.SECRET_KEY, encryptlimitOrdersPayload);
        Call<Void> call = mPrivateApi.cancelOrder(
                encryptlimitOrdersPayload, limitOrdersSignature, encryptlimitOrdersPayload
        );
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                LogUtil.i(String.format("cancel\t%,d ", cancelOrder.price));
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
