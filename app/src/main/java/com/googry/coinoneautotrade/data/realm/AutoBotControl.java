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
    public static final String LTC = "ltc";
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
        switch (coinType) {
            case BTC:{
                pricePercent = 1.1f;
                askPriceRange = 1.15f;
                bidPriceRange = 0.975f;
                buyAmount = Double.valueOf("0.0015");
                sellAmount = Double.valueOf("0.0014");
                divideUnit = 100000;
            }
            break;
            case BCH:{
                pricePercent = 1.02f;
                askPriceRange = 1.1f;
                bidPriceRange = 0.975f;
                buyAmount = Double.valueOf("0.011");
                sellAmount = Double.valueOf("0.0109");
                divideUnit = 500;
            }
            break;
            case ETH:{
                pricePercent = 1.02f;
                askPriceRange = 1.1f;
                bidPriceRange = 0.975f;
                buyAmount = Double.valueOf("0.011");
                sellAmount = Double.valueOf("0.0109");
                divideUnit = 500;
            }
            break;
            case ETC:{
                pricePercent = 1.01f;
                askPriceRange = 1.1f;
                bidPriceRange = 0.975f;
                buyAmount = Double.valueOf("0.2");
                sellAmount = Double.valueOf("0.1998");
                divideUnit = 10;
            }
            break;
            case XRP:{
                pricePercent = 1.01f;
                askPriceRange = 1.1f;
                bidPriceRange = 0.975f;
                buyAmount = Double.valueOf("10");
                sellAmount = Double.valueOf("9.992");
                divideUnit = 1;
            }
            break;
            case QTUM:{
                pricePercent = 1.01f;
                askPriceRange = 1.1f;
                bidPriceRange = 0.975f;
                buyAmount = Double.valueOf("0.2");
                sellAmount = Double.valueOf("0.1998");
                divideUnit = 10;
            }
            break;
            case LTC:{
                pricePercent = 1.01f;
                askPriceRange = 1.1f;
                bidPriceRange = 0.975f;
                buyAmount = Double.valueOf("0.11");
                sellAmount = Double.valueOf("0.1099");
                divideUnit = 500;
            }
            break;
        }
    }

    public AutoBotControl() {
    }

}
