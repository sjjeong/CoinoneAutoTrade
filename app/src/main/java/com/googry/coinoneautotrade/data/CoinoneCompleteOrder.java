package com.googry.coinoneautotrade.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by seokjunjeong on 2017. 9. 4..
 */

public class CoinoneCompleteOrder {

    @SerializedName("completeOrders")
    public List<CompleteOrder> completeOrders;


}
