package com.evistek.vr.activity.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.evistek.vr.R;

/**
 * Created by evis on 2016/8/16.
 */
public class ErrorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private LayoutInflater mLayoutInflater;

    public ErrorAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ErrorViewHolder(mLayoutInflater.inflate(R.layout.network_unavailable, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    public class ErrorViewHolder extends RecyclerView.ViewHolder {

        public ErrorViewHolder(View view) {
            super(view);
        }
    }
}
