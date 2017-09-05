package com.googry.coinoneautotrade;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.googry.coinoneautotrade.data.CoinoneBalance;
import com.googry.coinoneautotrade.data.remote.CoinoneApiManager;
import com.googry.coinoneautotrade.databinding.MainActivityBinding;
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

        secretKey = getString(R.string.secret_key);
        accessToken = getString(R.string.access_token);

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

    //databinding
    public void onBuyCoinClick(View v) {
        String buysell = ((Button) v).getText().toString();
        long nonce = System.currentTimeMillis();
        String orderBuyPayload = EncryptionUtil.getJsonOrderBuy(accessToken,
                Long.parseLong(mBinding.etCoinPrice.getText().toString()),
                Double.parseDouble(mBinding.etCoinAmount.getText().toString()),
                mBinding.etCoinType.getText().toString(),
                nonce);
        String encyptOrderBuyPayload = EncryptionUtil.getEncyptPayload(orderBuyPayload);
        String signature = EncryptionUtil.getSignature(secretKey, encyptOrderBuyPayload);

        CoinoneApiManager.CoinonePrivateApi api =
                CoinoneApiManager.getApiManager().create(CoinoneApiManager.CoinonePrivateApi.class);
        Call<Void> call = api.buysell(buysell, encyptOrderBuyPayload, signature, encyptOrderBuyPayload);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                LogUtil.i(response.toString());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });
    }
}
