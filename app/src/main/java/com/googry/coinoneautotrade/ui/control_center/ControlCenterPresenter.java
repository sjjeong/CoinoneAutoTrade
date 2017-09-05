package com.googry.coinoneautotrade.ui.control_center;

/**
 * Created by seokjunjeong on 2017. 9. 5..
 */

public class ControlCenterPresenter implements ControlCenterContract.Presenter {
    private ControlCenterContract.View mView;

    public ControlCenterPresenter(ControlCenterContract.View view) {
        mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }
}
