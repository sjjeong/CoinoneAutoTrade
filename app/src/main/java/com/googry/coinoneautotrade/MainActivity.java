package com.googry.coinoneautotrade;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.googry.coinoneautotrade.data.CoinoneBalance;
import com.googry.coinoneautotrade.data.realm.AutoBotControl;
import com.googry.coinoneautotrade.data.remote.CoinoneApiManager;
import com.googry.coinoneautotrade.databinding.MainActivityBinding;
import com.googry.coinoneautotrade.ui.control_center.ControlCenterActivity;
import com.googry.coinoneautotrade.util.EncryptionUtil;
import com.googry.coinoneautotrade.util.LogUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private MainActivityBinding mBinding;

    private String secretKey;
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.main_activity);
        mBinding.setActivity(this);

        secretKey = Config.SECRET_KEY;
        accessToken = Config.ACCESS_TOKEN;

        long nonce = System.currentTimeMillis();
        String balancePayload = EncryptionUtil.getJsonBalance(accessToken, nonce);
        String encyptbalancePayload = EncryptionUtil.getEncyptPayload(balancePayload);
        String signature = EncryptionUtil.getSignature(secretKey, encyptbalancePayload);

        CoinoneApiManager.CoinonePrivateApi api =
                CoinoneApiManager.getApiManager().create(CoinoneApiManager.CoinonePrivateApi.class);
        Call<CoinoneBalance> call = api.balance(encyptbalancePayload, signature, encyptbalancePayload);
        call.enqueue(new Callback<CoinoneBalance>() {
            @Override
            public void onResponse(Call<CoinoneBalance> call, Response<CoinoneBalance> response) {
                CoinoneBalance coinoneBalance = response.body();
                mBinding.setCoinoneBalance(coinoneBalance);
            }

            @Override
            public void onFailure(Call<CoinoneBalance> call, Throwable t) {

            }
        });

    }

    public void onCoinClick(View v){
        Intent intent = new Intent(getApplicationContext(), ControlCenterActivity.class);
        switch (v.getId()){
            case R.id.ll_btc:{
                intent.putExtra(ControlCenterActivity.EXTRA_COIN_TYPE, AutoBotControl.BTC);
            }
            break;
            case R.id.ll_bch:{
                intent.putExtra(ControlCenterActivity.EXTRA_COIN_TYPE, AutoBotControl.BCH);
            }
            break;
            case R.id.ll_eth:{
                intent.putExtra(ControlCenterActivity.EXTRA_COIN_TYPE, AutoBotControl.ETH);
            }
            break;
            case R.id.ll_etc:{
                intent.putExtra(ControlCenterActivity.EXTRA_COIN_TYPE, AutoBotControl.ETC);
            }
            break;
            case R.id.ll_xrp:{
                intent.putExtra(ControlCenterActivity.EXTRA_COIN_TYPE, AutoBotControl.XRP);
            }
            break;
            case R.id.ll_qtum:{
                intent.putExtra(ControlCenterActivity.EXTRA_COIN_TYPE, AutoBotControl.QTUM);
            }
            break;
            case R.id.ll_ltc:{
                intent.putExtra(ControlCenterActivity.EXTRA_COIN_TYPE, AutoBotControl.LTC);
            }
            break;
        }
        startActivity(intent);

    }

}
