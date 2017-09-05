package com.googry.coinoneautotrade.ui.control_center;

import com.googry.coinoneautotrade.Config;
import com.googry.coinoneautotrade.data.CoinoneBalance;
import com.googry.coinoneautotrade.data.CoinoneLimitOrder;
import com.googry.coinoneautotrade.data.CoinoneTicker;
import com.googry.coinoneautotrade.data.Order;
import com.googry.coinoneautotrade.data.realm.AutoBotControl;
import com.googry.coinoneautotrade.data.remote.CoinoneApiManager;
import com.googry.coinoneautotrade.util.EncryptionUtil;
import com.googry.coinoneautotrade.util.LogUtil;

import java.io.IOException;
import java.util.List;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by seokjunjeong on 2017. 9. 5..
 */

public class ControlCenterPresenter implements ControlCenterContract.Presenter {
    private static final int ASK = 1;
    private static final int BID = 0;
    private ControlCenterContract.View mView;
    private AutoBotControl mAutoBotControl;
    private Realm mRealm;

    private CoinoneApiManager.CoinonePrivateApi mPrivateApi;
    private CoinoneApiManager.CoinonePublicApi mPublicApi;

    private CoinoneTicker.Ticker mTicker;
    private CoinoneBalance mCoinoneBalance;
    private String mCoinType;

    public ControlCenterPresenter(ControlCenterContract.View view,
                                  Realm realm,
                                  String coinType) {
        mView = view;
        mView.setPresenter(this);
        mRealm = realm;
        mAutoBotControl = mRealm.where(AutoBotControl.class)
                .equalTo("coinType", coinType)
                .findFirst();
        if (mAutoBotControl == null) {
            mAutoBotControl = new AutoBotControl(coinType);
            mAutoBotControl.runFlag = false;
        }
        mPrivateApi = CoinoneApiManager.getApiManager().create(CoinoneApiManager.CoinonePrivateApi.class);
        mPublicApi = CoinoneApiManager.getApiManager().create(CoinoneApiManager.CoinonePublicApi.class);
        mCoinType = coinType;

    }

    @Override
    public void start() {
        callTicker();
    }

    private void callTicker() {
        Call<CoinoneTicker.Ticker> callTicker = mPublicApi.ticker(mCoinType);
        callTicker.enqueue(new Callback<CoinoneTicker.Ticker>() {
            @Override
            public void onResponse(Call<CoinoneTicker.Ticker> call, Response<CoinoneTicker.Ticker> response) {
                if (response.body() == null)
                    return;
                mTicker = response.body();
                callBalance();
            }

            @Override
            public void onFailure(Call<CoinoneTicker.Ticker> call, Throwable t) {

            }
        });
    }


    private void callBalance() {
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
                mCoinoneBalance = response.body();
                long last = mTicker.last;
                double balance = 0;
                switch (mCoinType) {
                    case AutoBotControl.BTC: {
                        balance = mCoinoneBalance.balanceBtc.balance;
                    }
                    break;
                    case AutoBotControl.BCH: {
                        balance = mCoinoneBalance.balanceBch.balance;
                    }
                    break;
                    case AutoBotControl.ETH: {
                        balance = mCoinoneBalance.balanceEth.balance;
                    }
                    break;
                    case AutoBotControl.ETC: {
                        balance = mCoinoneBalance.balanceEtc.balance;
                    }
                    break;
                    case AutoBotControl.XRP: {
                        balance = mCoinoneBalance.balanceXrp.balance;
                    }
                    break;
                    case AutoBotControl.QTUM: {
                        balance = mCoinoneBalance.balanceQtum.balance;
                    }
                    break;
                }
                mView.initData(
                        last,
                        balance,
                        mCoinoneBalance.balanceKrw.avail,
                        mAutoBotControl);

            }

            @Override
            public void onFailure(Call<CoinoneBalance> call, Throwable t) {

            }
        });
    }

    @Override
    public void requestBidClear() {
        LogUtil.i("bidClear");
        mView.showDialog("매수 정리 중...");
        String limitOrdersPayload = EncryptionUtil.getJsonLimitOrders(
                Config.ACCESS_TOKEN, mCoinType, System.currentTimeMillis());
        String encryptlimitOrdersPayload = EncryptionUtil.getEncyptPayload(limitOrdersPayload);
        String limitOrdersSignature = EncryptionUtil.getSignature(Config.SECRET_KEY, encryptlimitOrdersPayload);
        Call<CoinoneLimitOrder> completeOrderCall = mPrivateApi.limitOrders(
                encryptlimitOrdersPayload, limitOrdersSignature, encryptlimitOrdersPayload
        );
        completeOrderCall.enqueue(new Callback<CoinoneLimitOrder>() {
            @Override
            public void onResponse(Call<CoinoneLimitOrder> call, Response<CoinoneLimitOrder> response) {
                if (response.body() == null)
                    return;
                final CoinoneLimitOrder order = response.body();
                if (order.limitOrders.size() == 0)
                    return;
                callCancelOrder(order.limitOrders, BID);
            }

            @Override
            public void onFailure(Call<CoinoneLimitOrder> call, Throwable t) {

            }
        });
    }

    @Override
    public void requestAskClear() {
        LogUtil.i("askClear");
        mView.showDialog("매도 정리 중...");

        String limitOrdersPayload = EncryptionUtil.getJsonLimitOrders(
                Config.ACCESS_TOKEN, mCoinType, System.currentTimeMillis());
        String encryptlimitOrdersPayload = EncryptionUtil.getEncyptPayload(limitOrdersPayload);
        String limitOrdersSignature = EncryptionUtil.getSignature(Config.SECRET_KEY, encryptlimitOrdersPayload);
        Call<CoinoneLimitOrder> completeOrderCall = mPrivateApi.limitOrders(
                encryptlimitOrdersPayload, limitOrdersSignature, encryptlimitOrdersPayload
        );
        completeOrderCall.enqueue(new Callback<CoinoneLimitOrder>() {
            @Override
            public void onResponse(Call<CoinoneLimitOrder> call, Response<CoinoneLimitOrder> response) {
                if (response.body() == null)
                    return;
                final CoinoneLimitOrder order = response.body();
                if (order.limitOrders.size() == 0)
                    return;
                callCancelOrder(order.limitOrders, ASK);
            }

            @Override
            public void onFailure(Call<CoinoneLimitOrder> call, Throwable t) {

            }
        });
    }

    @Override
    public void requestStop() {
        LogUtil.i("stop");
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                mAutoBotControl.runFlag = false;
                realm.copyToRealmOrUpdate(mAutoBotControl);
                mView.showRunning(mAutoBotControl.runFlag);
            }
        });

    }

    @Override
    public void requestRun(final float pricePercent,
                           final float bidPriceRange,
                           final double buyAmount,
                           final double sellAmount) {

        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                mAutoBotControl.runFlag = true;
                mAutoBotControl.pricePercent = pricePercent;
                mAutoBotControl.bidPriceRange = bidPriceRange;
                mAutoBotControl.buyAmount = buyAmount;
                mAutoBotControl.sellAmount = sellAmount;
                realm.copyToRealmOrUpdate(mAutoBotControl);
                mView.showRunning(mAutoBotControl.runFlag);
            }
        });
    }

    private void callCancelOrder(final List<Order> order, final int isAsk) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (Order cancelOrder : order) {
                    if ( ( (cancelOrder.type.equals("bid") && isAsk == ASK) ||
                            (cancelOrder.type.equals("ask") && isAsk == BID) ) )
                        continue;
                    try {
                        String limitOrdersPayload = EncryptionUtil.getJsonCancelOrder(
                                Config.ACCESS_TOKEN,
                                cancelOrder.orderId,
                                cancelOrder.price,
                                cancelOrder.qty,
                                isAsk,
                                mCoinType,
                                System.currentTimeMillis());
                        String encryptlimitOrdersPayload = EncryptionUtil.getEncyptPayload(limitOrdersPayload);
                        String limitOrdersSignature = EncryptionUtil.getSignature(Config.SECRET_KEY, encryptlimitOrdersPayload);
                        Call<Void> call = mPrivateApi.cancelOrder(
                                encryptlimitOrdersPayload, limitOrdersSignature, encryptlimitOrdersPayload
                        );
                        call.execute();
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                mView.hideDialog();
            }
        });
        thread.start();
    }
}
