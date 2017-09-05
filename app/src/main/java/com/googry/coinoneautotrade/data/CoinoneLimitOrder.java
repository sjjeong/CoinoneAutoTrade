package com.googry.coinoneautotrade.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by seokjunjeong on 2017. 9. 4..
 */

public class CoinoneLimitOrder {

    @SerializedName("limitOrders")
    public List<Order> limitOrders;


}
