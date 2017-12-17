package com.googry.coinoneautotrade;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.googry.coinoneautotrade.data.CoinoneBalance;
import com.googry.coinoneautotrade.data.remote.CoinoneApiManager;
import com.googry.coinoneautotrade.databinding.MainActivityBinding;
import com.googry.coinoneautotrade.ui.ControlCenterAdapter;
import com.googry.coinoneautotrade.util.EncryptionUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private MainActivityBinding mBinding;

    private String secretKey;
    private String accessToken;

    private ControlCenterAdapter mControlCenterAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.main_activity);

        secretKey = Config.SECRET_KEY;
        accessToken = Config.ACCESS_TOKEN;

        mControlCenterAdapter = new ControlCenterAdapter();
        mBinding.rvContent.setAdapter(mControlCenterAdapter);

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
                mControlCenterAdapter.replace(coinoneBalance.getCommonBalances());

            }

            @Override
            public void onFailure(Call<CoinoneBalance> call, Throwable t) {

            }
        });

    }
}
