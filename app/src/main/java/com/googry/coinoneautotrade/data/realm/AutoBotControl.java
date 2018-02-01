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
    public static final String IOTA = "iota";
    public static final String BTG = "btg";
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
                pricePercent = 1.05f;
                askPriceRange = 1.1f;
                bidPriceRange = 0.98f;
                buyAmount = Double.valueOf("0.0001");
                sellAmount = Double.valueOf("0.0001");
                divideUnit = 16000;
            }
            break;
            case BCH:{
                pricePercent = 1.05f;
                askPriceRange = 1.1f;
                bidPriceRange = 0.98f;
                buyAmount = Double.valueOf("0.001");
                sellAmount = Double.valueOf("0.001");
                divideUnit = 8000;
            }
            break;
            case ETH:{
                pricePercent = 1.05f;
                askPriceRange = 1.1f;
                bidPriceRange = 0.98f;
                buyAmount = Double.valueOf("0.01");
                sellAmount = Double.valueOf("0.01");
                divideUnit = 12800;
            }
            break;
            case ETC:{
                pricePercent = 1.05f;
                askPriceRange = 1.1f;
                bidPriceRange = 0.98f;
                buyAmount = Double.valueOf("0.1");
                sellAmount = Double.valueOf("0.1");
                divideUnit = 80;
            }
            break;
            case XRP:{
                pricePercent = 1.05f;
                askPriceRange = 1.1f;
                bidPriceRange = 0.98f;
                buyAmount = Double.valueOf("2");
                sellAmount = Double.valueOf("2");
                divideUnit = 8;
            }
            break;
            case QTUM:{
                pricePercent = 1.05f;
                askPriceRange = 1.1f;
                bidPriceRange = 0.98f;
                buyAmount = Double.valueOf("0.1");
                sellAmount = Double.valueOf("0.1");
                divideUnit = 320;
            }
            break;
            case LTC:{
                pricePercent = 1.05f;
                askPriceRange = 1.1f;
                bidPriceRange = 0.98f;
                buyAmount = Double.valueOf("0.1");
                sellAmount = Double.valueOf("0.1");
                divideUnit = 6400;
            }
            break;
            case IOTA:{
                pricePercent = 1.05f;
                askPriceRange = 1.1f;
                bidPriceRange = 0.98f;
                buyAmount = Double.valueOf("0.7");
                sellAmount = Double.valueOf("0.7");
                divideUnit = 10;
            }
            break;
            case BTG:{
                pricePercent = 1.05f;
                askPriceRange = 1.1f;
                bidPriceRange = 0.98f;
                buyAmount = Double.valueOf("0.01");
                sellAmount = Double.valueOf("0.01");
                divideUnit = 1600;
            }
            break;
        }
    }

    public AutoBotControl() {
    }

}
