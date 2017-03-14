package com.evistek.vr.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.evistek.vr.R;
import com.evistek.vr.activity.fragment.HomeFragment;
import com.evistek.vr.model.PlayRecord;
import com.evistek.vr.model.Video;
import com.evistek.vr.net.BitmapLoadManager;
import com.evistek.vr.net.NetWorkService;
import com.evistek.vr.net.callback.PlayRecordCallback;
import com.evistek.vr.net.callback.VideoCallback;
import com.evistek.vr.net.json.JsonRespPlayRecord;
import com.evistek.vr.net.json.JsonRespVideo;
import com.evistek.vr.user.User;

import org.rajawali3d.vr.activity.GvrVideoActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends Activity {
    private static final String TAG = "HistoryActivity";
    private static final int MSG_GET_PLAY_RECORD = 0;
    private static final int MSG_GET_VIDEO = 1;
    private static Boolean mDeleteMode = false;

    private View mNormalView;
    private View mEmptyView;
    private User mUser;
    private ImageView mBack;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private TextView mDeleteBt;
    private TextView mSelectBt;
    private TextView mEditBt;

    private ImageView mEmptyViewBack;
    private TextView mEmptyVIewTitle;
    private ImageView mEmptyViewIcon;
    private TextView mEmptyViewMsg;

    private ListAdapter mAdapter;
    private List<Integer> mPositionList = new ArrayList<>();
    private List<PlayRecord> mList = new ArrayList<>();
    private List<PlayRecord> mSelectList = new ArrayList<>();
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_GET_PLAY_RECORD:
                    updateView(mList.size() == 0);
                    break;
                case MSG_GET_VIDEO:
                    Video video = (Video)msg.obj;
                    startDetailActivity(video);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mLayoutInflater = this.getLayoutInflater();
        mUser = E3DApplication.getInstance().getUser();

        initializeView();
        getData();
    }

    @Override
    public void onBackPressed() {
        if (mDeleteMode) {
            mSelectList.clear();
            mPositionList.clear();
            mDeleteBt.setVisibility(View.GONE);
            mSelectBt.setVisibility(View.GONE);
            mEditBt.setVisibility(View.VISIBLE);
            mAdapter.notifyDataSetChanged();
        } else {
            super.onBackPressed();
        }
        mDeleteMode = false;
    }

    private void inflateNormalView(View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.v2_user_history_recyclerView);
        mLinearLayoutManager = new LinearLayoutManager(mContext);
        mAdapter = new ListAdapter();
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mBack = (ImageView) view.findViewById(R.id.v2_user_history_backbt);
        mDeleteBt = (TextView) view.findViewById(R.id.v2_user_history_listitem_delete);
        mSelectBt = (TextView) view.findViewById(R.id.v2_user_history_listitem_selectall);
        mEditBt = (TextView) view.findViewById(R.id.v2_user_history_listitem_edit);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDeleteMode) {
                    mSelectList.clear();
                    mPositionList.clear();
                    mDeleteBt.setVisibility(View.GONE);
                    mSelectBt.setVisibility(View.GONE);
                    mEditBt.setVisibility(View.VISIBLE);
                    mAdapter.notifyDataSetChanged();
                } else {
                    finish();
                }
                mDeleteMode = false;
            }
        });
        mEditBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDeleteMode = true;
                mEditBt.setVisibility(View.GONE);
                mDeleteBt.setVisibility(View.VISIBLE);
                mSelectBt.setVisibility(View.VISIBLE);
                mSelectList.clear();
                mPositionList.clear();
            }
        });
        mDeleteBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectList.size() == 0) {
                    new  AlertDialog.Builder(HistoryActivity.this)
                            .setTitle(R.string.downloadDeletetip)
                            .setMessage(R.string.v2_choose_no)
                            .setPositiveButton(R.string.ok,null)
                            .show();
                } else {
                    NetWorkService.deletePlayRecords(mSelectList, new PlayRecordCallback() {
                        @Override
                        public void onResult(int code, JsonRespPlayRecord jsonResp) {
                            if (code == 200) {
                                for (int i = 0; i < mSelectList.size(); i++) {
                                    mList.remove(mSelectList.get(i));
                                }
                                mSelectList.clear();
                                mPositionList.clear();
                                mAdapter.notifyDataSetChanged();
                                mEditBt.setVisibility(View.VISIBLE);
                                mDeleteBt.setVisibility(View.GONE);
                                mSelectBt.setVisibility(View.GONE);
                                mDeleteMode = false;
                                sendMessage(MSG_GET_PLAY_RECORD, null);
                            }
                        }
                    });
                }
            }
        });
        mSelectBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mList.size() == mSelectList.size()) {
                    mSelectBt.setText(R.string.v2_user_title_select);
                    mSelectList.clear();
                    mPositionList.clear();
                    mAdapter.notifyDataSetChanged();
                } else if (mSelectBt.getText().equals(mContext.getString(R.string.v2_user_title_select))) {
                    for (int i = 0; i < mList.size(); i++) {
                        mSelectList.add(mList.get(i));
                        mPositionList.add(i);
                    }
                    mSelectBt.setText(R.string.v2_user_title_cancel);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void inflateEmptyView(View view) {
        mEmptyViewBack = (ImageView) view.findViewById(R.id.v2_empty_backbt);
        mEmptyVIewTitle = (TextView) view.findViewById(R.id.v2_empty_title);
        mEmptyViewIcon = (ImageView) view.findViewById(R.id.v2_empty_icon);
        mEmptyViewMsg = (TextView) view.findViewById(R.id.v2_empty_tv);

        mEmptyViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mEmptyVIewTitle.setText(getString(R.string.v2_history));
        mEmptyViewIcon.setBackgroundResource(R.drawable.v2_play_record);
        mEmptyViewMsg.setText(getString(R.string.empty_playrecord_no));
    }

    public void initializeView() {
        mNormalView = mLayoutInflater.inflate(R.layout.activity_history, null);
        inflateNormalView(mNormalView);

        mEmptyView = mLayoutInflater.inflate(R.layout.empty, null);
        inflateEmptyView(mEmptyView);
    }

    private void updateView(boolean empty){
        if (empty) {
            setContentView(mEmptyView);
        } else {
            setContentView(mNormalView);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void getData() {
        NetWorkService.getPlayRecordsByUserId(mUser.id, new PlayRecordCallback() {
            @Override
            public void onResult(int code, JsonRespPlayRecord jsonResp) {
                if (code == 200) {
                    mList = jsonResp.getResults();
                }

                sendMessage(MSG_GET_PLAY_RECORD, null);
            }
        });
    }

    @Override
    protected void onDestroy() {
        mSelectList.clear();
        mPositionList.clear();
        mDeleteMode = false;
        super.onDestroy();
    }

    private int getPattern(Video video) {
        List<Video> stereoList = HomeFragment.getStereoVideoList();
        List<Video> panoList = HomeFragment.getPanoVideoList();

        for (Video v: panoList) {
            if (v.getContentId().equals(video.getContentId())) {
                return GvrVideoActivity.PATTERN_TYPE_2;
            }
        }

        for (Video v: stereoList) {
            if (v.getContentId().equals(video.getContentId())) {
                return GvrVideoActivity.PATTERN_TYPE_3;
            }
        }

        return GvrVideoActivity.PATTERN_TYPE_1;
    }

    private void startDetailActivity(Video video) {
        Intent intent = new Intent(mContext, StereoVideoDetailActivity.class);
        intent.putExtra(StereoVideoDetailActivity.INTENT_VIDEO, video);
        intent.putExtra(StereoVideoDetailActivity.INTENT_PATTERN, getPattern(video));
        startActivity(intent);
    }

    private void sendMessage(int what, Object obj) {
        Message message = mHandler.obtainMessage();
        message.what = what;
        message.obj = obj;
        mHandler.sendMessage(message);
    }

    private class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public ListAdapter() {
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = mLayoutInflater.from(parent.getContext()).inflate(R.layout.user_history_listitem, parent, false);
            return new ListAdapter.CardViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            bindViewHolder((CardViewHolder) holder, position);
        }

        private void bindViewHolder(CardViewHolder holder, final int position) {
            if (!mPositionList.contains(position)) {
                holder.itemView.setAlpha(1.0f);
            } else {
                holder.itemView.setAlpha(0.5f);
            }

            if (mList.get(position).getCoverUrl() != null) {
                BitmapLoadManager.display(holder.mImage, mList.get(position).getCoverUrl());
            } else {
                holder.mImage.setImageResource(R.drawable.home_place_holder);
            }
            if (mList.get(position).getContentName() != null) {
                holder.mName.setText(mList.get(position).getContentName());
            }
            holder.mDuration.setText(R.string.v2_user_listitem_time);
            SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (mList.get(position).getEndTime() != null) {
                holder.mDuration.setText(time.format(mList.get(position).getEndTime()));
            }

            holder.mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDeleteMode) {
                        if (mSelectList.contains(mList.get(position))) {
                            mSelectList.remove(mList.get(position));
                            mPositionList.remove((Object) position);
                            mAdapter.notifyDataSetChanged();
                        } else {
                            mSelectList.add(mList.get(position));
                            mPositionList.add(position);
                            mAdapter.notifyDataSetChanged();
                        }
                    } else {
                        NetWorkService.getVideoById(mList.get(position).getContentId().intValue(),
                                new VideoCallback() {
                                    @Override
                                    public void onResult(int code, JsonRespVideo JsonResp) {
                                        if (code == 200) {
                                            ArrayList<Video> videos = JsonResp.getResults();
                                            if (videos != null && videos.size() > 0) {
                                                sendMessage(MSG_GET_VIDEO, videos.get(0));
                                            }
                                        }
                                    }
                                });
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        public class CardViewHolder extends RecyclerView.ViewHolder {
            public CardView mCardView;
            public ImageView mImage;
            public TextView mName;
            public TextView mDuration;
            public TextView mSize;

            public CardViewHolder(View view) {
                super(view);
                mCardView = (CardView) view.findViewById(R.id.v2_user_history_cardview);
                mImage = (ImageView) view.findViewById(R.id.v2_user_history_listitem_icon);
                mName = (TextView) view.findViewById(R.id.v2_user_history_listitem_name);
                mDuration = (TextView) view.findViewById(R.id.v2_user_history_listitem_time);
                mSize = (TextView) view.findViewById(R.id.v2_user_history_listitem_size);
            }
        }
    }
}
