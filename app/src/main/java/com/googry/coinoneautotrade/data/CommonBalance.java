package com.googry.coinoneautotrade.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by seokjunjeong on 2017. 12. 16..
 */

public class CommonBalance {
    @SerializedName("avail")
    public double avail;
    @SerializedName("balance")
    public double balance;

    public String coinName;

}
