package com.googry.coinoneautotrade.ui.control_center;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.googry.coinoneautotrade.R;
import com.googry.coinoneautotrade.base.ui.BaseFragment;
import com.googry.coinoneautotrade.data.realm.AutoBotControl;
import com.googry.coinoneautotrade.databinding.ControlCenterFragmentBinding;
import com.googry.coinoneautotrade.util.LogUtil;

import io.realm.Realm;

/**
 * Created by seokjunjeong on 2017. 9. 5..
 */

public class ControlCenterFragment
        extends BaseFragment<ControlCenterFragmentBinding>
        implements ControlCenterContract.View {
    private static final String EXTRA_COIN_TYPE = "EXTRA_COIN_TYPE";

    private ControlCenterContract.Presenter mPresenter;
    private Realm mRealm;

    public static ControlCenterFragment newInstance(String coinType) {
        ControlCenterFragment fragment = new ControlCenterFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_COIN_TYPE, coinType);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.control_center_fragment;
    }

    @Override
    protected void initView() {
        mBinding.setPresenter(mPresenter);
        mBinding.setFragment(this);
    }

    @Override
    protected void newPresenter() {
        mRealm = Realm.getDefaultInstance();

        new ControlCenterPresenter(this,
                mRealm,
                getArguments().getString(EXTRA_COIN_TYPE));
    }

    @Override
    protected void startPresenter() {
        mPresenter.start();
    }

    @Override
    public void onDestroy() {
        if (mRealm != null) {
            mRealm.close();
            mRealm = null;
        }
        super.onDestroy();
    }

    @Override
    public void setPresenter(ControlCenterContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void initData(long last, double holdAmount, double krw, AutoBotControl control) {
        mBinding.setNowPrice(last);
        mBinding.setHoldAmount(holdAmount);
        mBinding.setAvailableKrw(krw);
        mBinding.setRunning(control.runFlag);
        mBinding.setControl(control);

    }

    @Override
    public void showRunning(boolean isRun) {
        LogUtil.i("isrun : " + isRun);
        mBinding.setRunning(isRun);
    }

    private ProgressDialog mProgressDialog;
    @Override
    public void showDialog(String msg) {
        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setMessage(msg);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    public void hideDialog() {
        if(mProgressDialog != null && mProgressDialog.isShowing()){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressDialog.hide();
                    mProgressDialog = null;
                }
            });
        }

    }

    public void prepareRun() {
        mPresenter.requestRun(
                Float.valueOf(mBinding.etPricePercent.getText().toString()),
                Float.valueOf(mBinding.etBidPriceRange.getText().toString()),
                Double.valueOf(mBinding.etBuyAmount.getText().toString()),
                Double.valueOf(mBinding.etSellAmout.getText().toString())
        );
    }
}
