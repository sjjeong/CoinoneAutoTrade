package com.googry.coinoneautotrade.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by seokjunjeong on 2017. 9. 5..
 */

public class CompleteOrder {
    @SerializedName("index")
    public int index;
    @SerializedName("timestamp")
    public long timestamp;
    @SerializedName("price")
    public long price;
    @SerializedName("qty")
    public double qty;
    @SerializedName("orderId")
    public String orderId;
    @SerializedName("type")
    public String type;
    @SerializedName("feeRate")
    public float feeRate;
}
