package com.googry.coinoneautotrade.data.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by seokjunjeong on 2017. 9. 5..
 *
 * 단타봇을 제어하는 모델이다.
 * coinType은 상수로 된 값으로만 설정해야함
 */
public class AutoBotControl extends RealmObject{
    /**
     * coinType들
     */
    public static final String BTC = "btc";
    public static final String BCH = "bch";
    public static final String ETH = "eth";
    public static final String ETC = "etc";
    public static final String XRP = "xrp";
    public static final String QUTM = "qutm";
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
     * 사는 수량
     * 파는 수량
     */
    public float buyAmount;
    public float sellAmout;

}
