package com.googry.coinoneautotrade.ui.control_center;

import com.googry.coinoneautotrade.base.BasePresenter;
import com.googry.coinoneautotrade.base.BaseView;
import com.googry.coinoneautotrade.data.realm.AutoBotControl;

/**
 * Created by seokjunjeong on 2017. 9. 5..
 */

public interface ControlCenterContract {
    interface Presenter extends BasePresenter {
        void requestBidClear();

        void requestAskClear();

        void requestStop();

        void requestRun(float pricePercent,
                        float bidPriceRange,
                        float buyAmount,
                        float sellAmount);
    }

    interface View extends BaseView<Presenter> {
        void initData(long last,
                      double holdAmount,
                      double krw,
                      AutoBotControl control);

        void showRunning(boolean isRun);
    }

}
