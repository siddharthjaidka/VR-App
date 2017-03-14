package com.evistek.vr.activity.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.evistek.vr.R;
import com.evistek.vr.activity.WebViewActivity;
import com.evistek.vr.model.VrOnline;
import com.evistek.vr.net.BitmapLoadManager;
import com.evistek.vr.net.NetWorkService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Author: Weixiang Zhang
 * Email: wxzhang@evistek.com
 * Date: 2016/11/1.
 */

public class VrOnlineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int ITEM_TYPE_CARD_VIEW = 0;
    public static final int ITEM_TYPE_END_TAG = 1;
    public static final int END_TAG_SPAN_SIZE = 2;
    public static final int ITEM_SPAN_SIZE = 1;

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<VrOnline> mVrOnlineList = new ArrayList<>();

    public VrOnlineAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public void setVrOnlineList(ArrayList<VrOnline> list) {
        mVrOnlineList = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;

        switch (viewType) {
            case ITEM_TYPE_CARD_VIEW:
                viewHolder = new CardViewHolder(mLayoutInflater.inflate(R.layout.vronline_card_view, parent, false));
                break;
            case ITEM_TYPE_END_TAG:
                viewHolder = new EndTagHolder(mLayoutInflater.inflate(R.layout.end_tag, parent, false));
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CardViewHolder) {
            bindVrOnlineData((CardViewHolder)holder, position);
        } else if (holder instanceof EndTagHolder) {
            ((EndTagHolder)holder).mTextView.setText(R.string.end_tag_name);
        }
    }

    @Override
    public int getItemViewType(int position) {

        if (position < mVrOnlineList.size()) {
            return ITEM_TYPE_CARD_VIEW;
        } else if (position == mVrOnlineList.size()) {
            return ITEM_TYPE_END_TAG;
        }

        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return mVrOnlineList.size() + 1;
    }

    private void bindVrOnlineData(CardViewHolder holder, int position) {

        final VrOnline item = mVrOnlineList.get(position);

        holder.mName.setText(item.getName());
        holder.mUpdateTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(item.getCreateTime()));
        holder.mPlayCount.setText(item.getPlayCount() + mContext.getString(R.string.detail_play_count_suffix));
        holder.mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetWorkService.updateVrOnlinePlaycount(item.getId());
                startPlayerActivity(item.getUrl());
            }
        });
        BitmapLoadManager.display(holder.mImage, item.getCoverUrl(), BitmapLoadManager.URI_TYPE_REMOTE);
    }

    private void startPlayerActivity(String url) {
        Intent intent = new Intent(mContext, WebViewActivity.class);
        intent.putExtra(WebViewActivity.INTENT_URL, url);
        mContext.startActivity(intent);
    }

    class EndTagHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;

        public EndTagHolder(View view) {
            super(view);
            mTextView = (TextView) view.findViewById(R.id.end_tag);
        }
    }

    class CardViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImage;
        public TextView mName;
        public TextView mPlayCount;
        public TextView mUpdateTime;

        public CardViewHolder(View view) {
            super(view);

            mImage = (ImageView) view.findViewById(R.id.vronline_card_view_image);
            mName = (TextView) view.findViewById(R.id.vronline_card_view_name);
            mPlayCount = (TextView) view.findViewById(R.id.vronline_card_view_play_count);
            mUpdateTime = (TextView) view.findViewById(R.id.vronline_card_view_update_time);
        }
    }
}
