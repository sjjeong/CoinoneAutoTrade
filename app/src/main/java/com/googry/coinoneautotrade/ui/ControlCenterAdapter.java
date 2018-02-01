package com.googry.coinoneautotrade.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.googry.coinoneautotrade.R;
import com.googry.coinoneautotrade.data.CommonBalance;
import com.googry.coinoneautotrade.databinding.CoinCardItemBinding;
import com.googry.coinoneautotrade.ui.control_center.ControlCenterActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by seokjunjeong on 2017. 12. 14..
 */

public class ControlCenterAdapter extends RecyclerView.Adapter<ControlCenterAdapter.ViewHolder> {
    private List<CommonBalance> mBalances = new ArrayList<>();


    public ControlCenterAdapter() {
    }

    public void replace(List<CommonBalance> commonBalances) {
        mBalances.clear();
        mBalances.addAll(commonBalances);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.coin_card_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mBinding.setBalance(mBalances.get(position));
    }

    @Override
    public int getItemCount() {
        return mBalances.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CoinCardItemBinding mBinding;

        public ViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
            mBinding.setViewHolder(this);
        }

        public void onItemClick() {
            if (!mBinding.getBalance().coinName.toUpperCase().equals("KRW")) {
                mBinding.getRoot().getContext()
                        .startActivity(new Intent(
                                mBinding.getRoot().getContext(),
                                ControlCenterActivity.class)
                                .putExtra(ControlCenterActivity.EXTRA_COIN_TYPE,
                                        mBinding.getBalance().coinName));
            }

        }
    }

}
