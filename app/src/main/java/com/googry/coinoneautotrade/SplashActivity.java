package com.googry.coinoneautotrade;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.googry.coinoneautotrade.background.PersistentService;
import com.googry.coinoneautotrade.background.RestartReceiver;
import com.googry.coinoneautotrade.ui.control_center.ControlCenterActivity;

/**
 * Created by seokjunjeong on 2017. 5. 31..
 */

public class SplashActivity extends AppCompatActivity {
    private Intent intent;
    private RestartReceiver restartReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();

        startApp();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //브로드 캐스트 해제
        unregisterReceiver(restartReceiver);
    }

    private void startApp() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

    /**
     * 데이터 초기화
     */
    private void initData() {

        //리스타트 서비스 생성
        restartReceiver = new RestartReceiver();
        intent = new Intent(SplashActivity.this, PersistentService.class);


        IntentFilter intentFilter = new IntentFilter(".background.PersistentService");
        //브로드 캐스트에 등록
        registerReceiver(restartReceiver, intentFilter);
        // 서비스 시작
        startService(intent);


    }
}
