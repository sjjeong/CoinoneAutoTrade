package com.googry.coinoneautotrade.data.remote;

import com.googry.coinoneautotrade.BuildConfig;
import com.googry.coinoneautotrade.data.CoinoneBalance;
import com.googry.coinoneautotrade.data.CoinoneCompleteOrder;
import com.googry.coinoneautotrade.data.CoinoneLimitOrder;
import com.googry.coinoneautotrade.data.CoinoneTicker;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by seokjunjeong on 2017. 5. 27..
 */

public class CoinoneApiManager {
    private static final String BASE_URL = "https://api.coinone.co.kr/";
    private static Retrofit mInstance;

    public static Retrofit getApiManager() {
        if (mInstance == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(BuildConfig.DEBUG ?
                    HttpLoggingInterceptor.Level.BODY
                    : HttpLoggingInterceptor.Level.NONE);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            OkHttpClient client = httpClient
                    .addInterceptor(loggingInterceptor)
                    .build();
            mInstance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return mInstance;
    }

    public interface CoinonePrivateApi {
        @POST("v2/account/balance/")
        Call<CoinoneBalance> balance(
                @Header("X-COINONE-PAYLOAD") String payload,
                @Header("X-COINONE-SIGNATURE") String signature,
                @Body String body);

        @POST("v2/order/limit_{buysell}/")
        Call<Void> buysell(
                @Path("buysell") String buysell,
                @Header("X-COINONE-PAYLOAD") String payload,
                @Header("X-COINONE-SIGNATURE") String signature,
                @Body String body);

        @POST("v2/order/limit_orders/")
        Call<CoinoneLimitOrder> limitOrders(
                @Header("X-COINONE-PAYLOAD") String payload,
                @Header("X-COINONE-SIGNATURE") String signature,
                @Body String body);

        @POST("v2/order/complete_orders/")
        Call<CoinoneCompleteOrder> completeOrders(
                @Header("X-COINONE-PAYLOAD") String payload,
                @Header("X-COINONE-SIGNATURE") String signature,
                @Body String body);
    }

    public interface CoinonePublicApi {
        @GET("ticker/")
        Call<CoinoneTicker.Ticker> ticker(
                @Query("currency") String currency
        );
    }

}
