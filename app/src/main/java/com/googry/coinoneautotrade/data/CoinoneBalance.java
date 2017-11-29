package com.googry.coinoneautotrade.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by seokjunjeong on 2017. 8. 28..
 */

public class CoinoneBalance {
    @SerializedName("btc")
    public Balance balanceBtc;
    @SerializedName("eth")
    public Balance balanceEth;
    @SerializedName("etc")
    public Balance balanceEtc;
    @SerializedName("xrp")
    public Balance balanceXrp;
    @SerializedName("qtum")
    public Balance balanceQtum;
    @SerializedName("bch")
    public Balance balanceBch;
    @SerializedName("ltc")
    public Balance balanceLtc;
    @SerializedName("iota")
    public Balance balanceIota;
    @SerializedName("krw")
    public Balance balanceKrw;

    public class Balance {
        @SerializedName("avail")
        public double avail;
        @SerializedName("balance")
        public double balance;
    }
}
