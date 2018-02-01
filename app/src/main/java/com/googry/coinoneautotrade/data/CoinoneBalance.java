package com.googry.coinoneautotrade.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by seokjunjeong on 2017. 8. 28..
 */

public class CoinoneBalance implements IBalance{
    @SerializedName("btc")
    public CommonBalance balanceBtc;
    @SerializedName("bch")
    public CommonBalance balanceBch;
    @SerializedName("eth")
    public CommonBalance balanceEth;
    @SerializedName("etc")
    public CommonBalance balanceEtc;
    @SerializedName("xrp")
    public CommonBalance balanceXrp;
    @SerializedName("qtum")
    public CommonBalance balanceQtum;
    @SerializedName("ltc")
    public CommonBalance balanceLtc;
    @SerializedName("iota")
    public CommonBalance balanceIota;
    @SerializedName("btg")
    public CommonBalance balanceBtg;
    @SerializedName("krw")
    public CommonBalance balanceKrw;

    @Override
    public List<CommonBalance> getCommonBalances() {
        ArrayList<CommonBalance> balances
                = new ArrayList<>();
        if(balanceBtc != null) {
            balanceBtc.coinName = "btc";
            balances.add(balanceBtc);
        }
        if(balanceBch != null) {
            balanceBch.coinName = "bch";
            balances.add(balanceBch);
        }
        if(balanceEth != null) {
            balanceEth.coinName = "eth";
            balances.add(balanceEth);
        }
        if(balanceEtc != null) {
            balanceEtc.coinName = "etc";
            balances.add(balanceEtc);
        }
        if(balanceXrp != null) {
            balanceXrp.coinName = "xrp";
            balances.add(balanceXrp);
        }
        if(balanceQtum != null) {
            balanceQtum.coinName = "qtum";
            balances.add(balanceQtum);
        }
        if(balanceLtc != null) {
            balanceLtc.coinName = "ltc";
            balances.add(balanceLtc);
        }
        if(balanceIota != null) {
            balanceIota.coinName = "iota";
            balances.add(balanceIota);
        }
        if(balanceBtg != null) {
            balanceBtg.coinName = "btg";
            balances.add(balanceBtg);
        }
        if(balanceKrw != null) {
            balanceKrw.coinName = "krw";
            balances.add(balanceKrw);
        }
        return balances;
    }

}
