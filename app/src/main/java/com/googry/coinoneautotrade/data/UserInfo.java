package com.googry.coinoneautotrade.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by seokjunjeong on 2017. 9. 5..
 */

public class UserInfo {
    @SerializedName("feeRate")
    public FeeRate feeRate;


    public class FeeRate {
        @SerializedName("btc")
        public Fee btcFee;
        @SerializedName("bch")
        public Fee bchFee;
        @SerializedName("eth")
        public Fee ethFee;
        @SerializedName("etc")
        public Fee etcFee;
        @SerializedName("xrp")
        public Fee xrpFee;
        @SerializedName("qutm")
        public Fee qtumFee;

    }

    public class Fee {
        @SerializedName("maker")
        public float maker;
    }
}
