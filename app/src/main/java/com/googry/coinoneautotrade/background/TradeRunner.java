package com.googry.coinoneautotrade.background;

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
 * Created by seokjunjeong on 2017. 10. 22..
 */

public class TradeRunner {

    private static final int LIMIT_PRIVATE_API_CALL_COUNT = 5;

    private String mCoinType;
    private int mCallCnt;
    private long mBuyPriceMin;
    private long mSellPriceMax;
    private double mBuyAmount;
    private double mSellAmount;
    private double mPricePercent;
    private double divideUnit;
    private float mBidPriceRange;
    private float mAskPriceRange;
    private long mLastPrice;

    private CoinoneBalance.Balance mBalance;
    private CoinoneBalance.Balance mBalanceKrw;

    private CoinoneApiManager.CoinonePrivateApi mPrivateApi;
    private CoinoneApiManager.CoinonePublicApi mPublicApi;

    private String mAccessToken;
    private String mSecretKey;

    /**
     * prices
     */
    private ArrayList<Long> mAsks = new ArrayList<>();
    private ArrayList<Long> mBids = new ArrayList<>();
    private ArrayList<Order> mBidOrders = new ArrayList<>();

    private Realm mRealm;

    public TradeRunner(String coinType, String accessToken, String secretKey) {
        mCoinType = coinType;
        mPrivateApi = CoinoneApiManager.getApiManager().create(CoinoneApiManager.CoinonePrivateApi.class);
        mPublicApi = CoinoneApiManager.getApiManager().create(CoinoneApiManager.CoinonePublicApi.class);
        mRealm = Realm.getDefaultInstance();

        this.mAccessToken = accessToken;
        this.mSecretKey = secretKey;
    }

    public boolean run() {
        AutoBotControl control = mRealm.where(AutoBotControl.class).equalTo("coinType", mCoinType).findFirst();

        LogUtil.i("----------------------------------------");
        LogUtil.i("[ " + mCoinType + " ]");
        if (control == null) {
            LogUtil.i("is null");
            return false;
        }

        mCallCnt = 0;
        mCoinType = control.coinType;
        mBuyAmount = control.buyAmount;
        mSellAmount = control.sellAmount;
        mPricePercent = control.pricePercent;
        mBidPriceRange = control.bidPriceRange;
        mAskPriceRange = control.askPriceRange;
        divideUnit = control.divideUnit;

        /**
         * Ticker
         */
        if (!control.runFlag)
            return false;
        callTicker();
        return true;
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
                mSellPriceMax = (long) (Math.round(((float) ticker.last) * mAskPriceRange / divideUnit) * divideUnit);

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
                mAccessToken, mCoinType, System.currentTimeMillis());
        String encryptlimitOrdersPayload = EncryptionUtil.getEncyptPayload(limitOrdersPayload);
        String limitOrdersSignature = EncryptionUtil.getSignature(mSecretKey, encryptlimitOrdersPayload);
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
                if (mBalance == null) {
                    LogUtil.e("mBalance is null");
                    return;
                }
                if (mBalanceKrw == null) {
                    LogUtil.e("mBalanceKrw is null");
                    return;
                }

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
                mAccessToken, mCoinType, System.currentTimeMillis());
        String encryptlimitOrdersPayload = EncryptionUtil.getEncyptPayload(limitOrdersPayload);
        String limitOrdersSignature = EncryptionUtil.getSignature(mSecretKey, encryptlimitOrdersPayload);
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
                if (limitOrder.limitOrders == null) {
                    LogUtil.e("limitorder.limitOrders is null");
                    return;
                }

                mAsks.clear();
                mBids.clear();
                mBidOrders.clear();

                for (Order order : limitOrder.limitOrders) {
                    if (order.type.equals("ask")) {
                        mAsks.add(order.price);
                    } else {
                        if (mBids.contains(order.price)) {
                            callCancelLimit(order);
                            continue;
                        }
                        mBids.add(order.price);
                        mBidOrders.add(order);
                    }
                }

                Collections.sort(mAsks);
                Collections.sort(mBids, Collections.<Long>reverseOrder());

                double coinAvail = mBalance.avail;
                double krwAvail = mBalanceKrw.avail;


                if (mBalance.avail < mSellAmount) {
                    for (long i = mBuyPriceMin + (long) divideUnit; i <= (long) (mLastPrice - divideUnit); i = (long) (i + divideUnit)) {
                        /**
                         * bid(매수)에 가격이 없으므로 매수에 걸어야함
                         */
                        if (!mBids.contains(i)) {
                            long price = (long) (Math.ceil(((float) i) * mPricePercent / divideUnit) * divideUnit);
                            if (!mAsks.contains(price) && krwAvail >= price * mBuyAmount) {
                                /**
                                 * 매수를 price로 요청
                                 */
                                krwAvail -= price * mBuyAmount;
                                callBuyLimit(i, mBuyAmount);
                            }
                        }
                    }
                } else {
                    /**
                     * Ticker.last +
                     * 이 bid에 걸려있는지 확인
                     * 없으면 ask에 매도 주문
                     */
                    for (long i = mSellPriceMax; i >= (long) (mLastPrice + divideUnit); i = (long) (i - divideUnit)) {
                        if (!mAsks.contains(i)) {
                            long price = (long) (Math.ceil(((float) i) / mPricePercent / divideUnit) * divideUnit);
                            if (!mBids.contains(price) && coinAvail >= mSellAmount) {
                                /**
                                 * 매도에 걸어둘꺼니까 매수하지 말라고 추가함
                                 */
                                mAsks.add(i);
                                coinAvail -= mSellAmount;
                                callSellLimit(i, mSellAmount);
                            }
                        }
                    }
                }
//                int lowPrice = (int) (Math.round(((float) mLastPrice) * mBidPriceRange * 0.99f / divideUnit) * divideUnit);
//                for (Order cancelOrder : mBidOrders) {
//                    if (cancelOrder.price < lowPrice) {
//                        callCancelLimit(cancelOrder);
//                        break;
//                    }
//                }
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

        String orderBuyPayload = EncryptionUtil.getJsonOrderBuy(mAccessToken,
                price,
                sellAmount,
                mCoinType,
                System.currentTimeMillis());
        String encyptOrderBuyPayload = EncryptionUtil.getEncyptPayload(orderBuyPayload);
        String signature = EncryptionUtil.getSignature(mSecretKey, encyptOrderBuyPayload);

        Call<Void> call = mPrivateApi.buysell("sell", encyptOrderBuyPayload, signature, encyptOrderBuyPayload);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                LogUtil.i(String.format("sell\t%,d\t\t%.4f amount ", price, sellAmount));
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

        String orderBuyPayload = EncryptionUtil.getJsonOrderBuy(mAccessToken,
                price,
                buyAmount,
                mCoinType,
                System.currentTimeMillis());
        String encyptOrderBuyPayload = EncryptionUtil.getEncyptPayload(orderBuyPayload);
        String signature = EncryptionUtil.getSignature(mSecretKey, encyptOrderBuyPayload);

        Call<Void> call = mPrivateApi.buysell("buy", encyptOrderBuyPayload, signature, encyptOrderBuyPayload);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                LogUtil.i(String.format("buy\t%,d\t\t%.4f amount ", price, buyAmount));
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
                mAccessToken,
                cancelOrder.orderId,
                cancelOrder.price,
                cancelOrder.qty,
                0,
                mCoinType,
                System.currentTimeMillis());
        String encryptlimitOrdersPayload = EncryptionUtil.getEncyptPayload(limitOrdersPayload);
        String limitOrdersSignature = EncryptionUtil.getSignature(mSecretKey, encryptlimitOrdersPayload);
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
}
