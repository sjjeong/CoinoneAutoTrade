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
                        float askPriceRange,
                        float bidPriceRange,
                        double buyAmount,
                        double sellAmount,
                        int divideUnit);
    }

    interface View extends BaseView<Presenter> {
        void initData(long last,
                      double holdAmount,
                      double krw);

        void initControl(AutoBotControl control);

        void showRunning(boolean isRun);

        void showDialog(String msg);

        void hideDialog();
    }

}
