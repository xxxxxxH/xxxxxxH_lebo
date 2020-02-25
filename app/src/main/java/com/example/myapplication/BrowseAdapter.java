package com.example.myapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hpplay.sdk.source.browse.api.LelinkServiceInfo;

import java.util.ArrayList;
import java.util.List;

public class BrowseAdapter extends RecyclerView.Adapter<BrowseAdapter.RecyclerHolder> {

    private Context mContext;
    private List<LelinkServiceInfo> mDatas;
    private LayoutInflater mInflater;
    private OnItemClickListener mItemClickListener;
    private LelinkServiceInfo mSelectInfo;

    public BrowseAdapter(Context context, List<LelinkServiceInfo> mDatas) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        this.mDatas = mDatas;
    }

    @NonNull
    @Override
    public RecyclerHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = mInflater.inflate(R.layout.item, viewGroup, false);
        return new RecyclerHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerHolder recyclerHolder, final int i) {
        LelinkServiceInfo info = mDatas.get(i);
        if (null == info) {
            return;
        }
        String item = info.getName() + " uid:" + info.getUid() + " types:" + info.getTypes();
        recyclerHolder.textView.setText(item);
        recyclerHolder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mItemClickListener.onClick(i, mDatas.get(i));
            }
        });
    }

    public LelinkServiceInfo getSelectInfo() {
        return mSelectInfo;
    }

    public void setSelectInfo(LelinkServiceInfo selectInfo) {
        mSelectInfo = selectInfo;
    }

    @Override
    public int getItemCount() {
        return null == mDatas ? 0 : mDatas.size();
    }

    public void setmItemClickListener(OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    class RecyclerHolder extends RecyclerView.ViewHolder {

        TextView textView;

        private RecyclerHolder(android.view.View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.info);
        }

    }
}
