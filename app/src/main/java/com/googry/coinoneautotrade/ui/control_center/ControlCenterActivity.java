package com.googry.coinoneautotrade.ui.control_center;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import com.googry.coinoneautotrade.R;
import com.googry.coinoneautotrade.base.ui.BaseActivity;
import com.googry.coinoneautotrade.data.realm.AutoBotControl;

/**
 * Created by seokjunjeong on 2017. 9. 5..
 */

public class ControlCenterActivity extends BaseActivity<ControlCenterFragment> {
    @Override
    protected int getLayoutId() {
        return R.layout.simple_activity;
    }

    @Override
    protected int getFragmentContentId() {
        return R.id.content_frame;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initToolbar(@Nullable Bundle savedInstanceState) {
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected ControlCenterFragment getFragment() {
        return ControlCenterFragment.newInstance(AutoBotControl.XRP);
    }
}
