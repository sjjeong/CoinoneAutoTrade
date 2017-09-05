package com.googry.coinoneautotrade.ui.control_center;

import com.googry.coinoneautotrade.R;
import com.googry.coinoneautotrade.base.ui.BaseFragment;
import com.googry.coinoneautotrade.data.realm.AutoBotControl;
import com.googry.coinoneautotrade.databinding.ControlCenterFragmentBinding;

/**
 * Created by seokjunjeong on 2017. 9. 5..
 */

public class ControlCenterFragment
        extends BaseFragment<ControlCenterFragmentBinding>
        implements ControlCenterContract.View {
    private ControlCenterContract.Presenter mPresenter;

    public static ControlCenterFragment newInstance(){
        ControlCenterFragment fragment = new ControlCenterFragment();
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.control_center_fragment;
    }

    @Override
    protected void initView() {
        mBinding.setNowPrice(10l);
        mBinding.setHoldAmount(10.0f);
        mBinding.setFee(0.001f);
        mBinding.setAvailableKrw(1000000.0f);
        AutoBotControl autoBotControl = new AutoBotControl();
        autoBotControl.bidPriceRange = 0.9f;
        autoBotControl.buyAmount = 100f;
        autoBotControl.sellAmout = 99f;
        autoBotControl.coinType = AutoBotControl.XRP;
        autoBotControl.runFlag = true;
        autoBotControl.pricePercent = 1.015f;
        mBinding.setControl(autoBotControl);


    }

    @Override
    protected void newPresenter() {
        new ControlCenterPresenter(this);
    }

    @Override
    protected void startPresenter() {
        mPresenter.start();
    }

    @Override
    public void setPresenter(ControlCenterContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
