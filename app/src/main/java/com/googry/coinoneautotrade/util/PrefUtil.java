package com.googry.coinoneautotrade.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by seokjunjeong on 2017. 6. 16..
 */

public class PrefUtil {
    private static final String PREF_TIME_STAMP = "pref_time_stamp";

    private static SharedPreferences getSharedPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        return getSharedPrefs(context).edit();
    }

    // if you do not care about the result and calling from the main thread
    private static void apply(SharedPreferences.Editor editor) {
        editor.putLong(PREF_TIME_STAMP, getCurrentTime());
        editor.apply();
    }

    private static void commit(SharedPreferences.Editor editor) {
        editor.putLong(PREF_TIME_STAMP, getCurrentTime());
        editor.commit();
    }

    private static long getCurrentTime() {
        return System.currentTimeMillis();
    }
}