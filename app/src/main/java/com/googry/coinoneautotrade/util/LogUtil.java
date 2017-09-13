package com.googry.coinoneautotrade.util;

import android.util.Log;

import com.googry.coinoneautotrade.BuildConfig;

/**
 * Created by seokjunjeong on 2017. 6. 17..
 */

public class LogUtil {
    private static final boolean IS_DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "googry";

    public static void i(String message) {
        if (IS_DEBUG) Log.i(TAG, message);
    }
    public static void e(String message) {
        if (IS_DEBUG) Log.e(TAG, message);
    }
}
