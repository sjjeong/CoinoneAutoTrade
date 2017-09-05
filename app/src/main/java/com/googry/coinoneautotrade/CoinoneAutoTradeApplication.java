package com.googry.coinoneautotrade;

import android.app.Application;

import io.realm.Realm;


/**
 * Created by seokjunjeong on 2017. 9. 5..
 */

public class CoinoneAutoTradeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
