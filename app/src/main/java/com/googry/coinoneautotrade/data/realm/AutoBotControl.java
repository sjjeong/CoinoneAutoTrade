package com.googry.coinoneautotrade.data.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by seokjunjeong on 2017. 9. 5..
 * <p>
 * 단타봇을 제어하는 모델이다.
 * coinType은 상수로 된 값으로만 설정해야함
 */
public class AutoBotControl extends RealmObject {
    /**
     * coinType들
     */
    public static final String BTC = "btc";
    public static final String BCH = "bch";
    public static final String ETH = "eth";
    public static final String ETC = "etc";
    public static final String XRP = "xrp";
    public static final String QTUM = "qtum";
    @PrimaryKey
    public String coinType;

    /**
     * true     : 실행
     * false    : 멈춤
     */
    public boolean runFlag;

    /**
     * 1.xx
     */
    public float pricePercent;

    /**
     * 어디까지 살 것인지
     */
    public float bidPriceRange;
    /**
     * 어디까지 팔 것이지
     */
    public float askPriceRange;

    /**
     * 사는 수량
     * 파는 수량
     */
    public double buyAmount;
    public double sellAmount;

    /**
     * 나누는 단위
     */
    public int divideUnit;



    public AutoBotControl(String coinType) {
        this.coinType = coinType;
        this.runFlag = false;
        pricePercent = 1.01f;
        askPriceRange = 1.1f;
        bidPriceRange = 0.95f;
        switch (coinType) {
            case BTC:{
                buyAmount = Double.valueOf("1");
                sellAmount = Double.valueOf("0.999");
                divideUnit = 500;
            }
            break;
            case BCH:{
                buyAmount = Double.valueOf("1");
                sellAmount = Double.valueOf("0.999");
                divideUnit = 100;
            }
            break;
            case ETH:{
                buyAmount = Double.valueOf("1");
                sellAmount = Double.valueOf("0.999");
                divideUnit = 50;
            }
            break;
            case XRP:{
                buyAmount = Double.valueOf("10");
                sellAmount = Double.valueOf("9.99");
                divideUnit = 1;
            }
            break;
            case ETC:{
                buyAmount = Double.valueOf("1");
                sellAmount = Double.valueOf("0.999");
                divideUnit = 10;
            }
            break;
            case QTUM:{
                buyAmount = Double.valueOf("1");
                sellAmount = Double.valueOf("0.999");
                divideUnit = 10;
            }
            break;
        }
    }

    public AutoBotControl() {
    }

}
