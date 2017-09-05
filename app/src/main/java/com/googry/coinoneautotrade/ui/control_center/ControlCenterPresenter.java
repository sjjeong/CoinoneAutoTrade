package com.googry.coinoneautotrade.ui.control_center;

import com.googry.coinoneautotrade.Config;
import com.googry.coinoneautotrade.data.CoinoneBalance;
import com.googry.coinoneautotrade.data.CoinoneTicker;
import com.googry.coinoneautotrade.data.CoinoneUserInfo;
import com.googry.coinoneautotrade.data.UserInfo;
import com.googry.coinoneautotrade.data.realm.AutoBotControl;
import com.googry.coinoneautotrade.data.remote.CoinoneApiManager;
import com.googry.coinoneautotrade.util.EncryptionUtil;
import com.googry.coinoneautotrade.util.LogUtil;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by seokjunjeong on 2017. 9. 5..
 */

public class ControlCenterPresenter implements ControlCenterContract.Presenter {
    private ControlCenterContract.View mView;
    private AutoBotControl mAutoBotControl;
    private Realm mRealm;

    private CoinoneApiManager.CoinonePrivateApi mPrivateApi;
    private CoinoneApiManager.CoinonePublicApi mPublicApi;

    private CoinoneTicker.Ticker mTicker;
    private CoinoneBalance mCoinoneBalance;

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


    }

    @Override
    public void start() {
        callTicker();
    }

    private void callTicker() {
        Call<CoinoneTicker.Ticker> callTicker = mPublicApi.ticker(mAutoBotControl.coinType);
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
                Config.ACCESS_TOKEN, mAutoBotControl.coinType, System.currentTimeMillis());
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
                switch (mAutoBotControl.coinType) {
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
    }

    @Override
    public void requestAskClear() {
        LogUtil.i("askClear");

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
                           final float buyAmount,
                           final float sellAmount) {

        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                mAutoBotControl.runFlag = true;
                mAutoBotControl.pricePercent = pricePercent;
                mAutoBotControl.bidPriceRange = bidPriceRange;
                mAutoBotControl.buyAmount = buyAmount;
                mAutoBotControl.sellAmout = sellAmount;
                realm.copyToRealmOrUpdate(mAutoBotControl);
                mView.showRunning(mAutoBotControl.runFlag);
            }
        });
    }
}
