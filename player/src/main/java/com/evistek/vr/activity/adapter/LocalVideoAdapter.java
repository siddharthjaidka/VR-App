package com.evistek.vr.activity.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.evistek.vr.R;
import com.evistek.vr.model.LocalVideo;
import com.evistek.vr.net.BitmapLoadManager;
import com.evistek.vr.utils.MediaScannerNotifier;
import com.evistek.vr.utils.Utils;
import org.rajawali3d.vr.activity.GvrVideoActivity;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by evis on 2016/8/12.
 */
public class LocalVideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int ITEM_TYPE_CARD_VIEW = 0;
    private static final int ITEM_TYPE_END_TAG = 1;

    private static final int MSG_DELETE_ITEM = 0;
    private static final int MSG_DELETE_FAIL = 1;

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<LocalVideo> mLocalVideoList;
    private MediaScannerNotifier mMediaNotifier;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DELETE_ITEM:
                    mLocalVideoList.remove(msg.arg1);
                    notifyItemRemoved(msg.arg1);
                    break;
                case MSG_DELETE_FAIL:
                    showDeleteFailureDialog();
                    break;
            }

            return true;
        }
    });

    public LocalVideoAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mLocalVideoList = new ArrayList<LocalVideo>();
        mMediaNotifier = new MediaScannerNotifier(mContext);
    }

    public void setVideoList(ArrayList<LocalVideo> list) {
        mLocalVideoList = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;

        switch (viewType) {
            case ITEM_TYPE_CARD_VIEW:
                viewHolder = new CardViewHolder(mLayoutInflater.inflate(R.layout.local_video_card_view, parent, false));
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
            bindVideoData((CardViewHolder)holder, position);
        } else if (holder instanceof EndTagHolder) {
            ((EndTagHolder)holder).mTextView.setText(R.string.end_tag_name);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position < mLocalVideoList.size()) {
            return ITEM_TYPE_CARD_VIEW;
        } else if (position == mLocalVideoList.size()) {
            return ITEM_TYPE_END_TAG;
        }

        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return mLocalVideoList.size() + 1;
    }

    private void bindVideoData(CardViewHolder holder, final int position) {
        if (position < mLocalVideoList.size()) {
            final LocalVideo video = mLocalVideoList.get(position);

            holder.mName.setText(video.displayName);
            holder.mDuration.setText(Utils.stringForTime(video.duration));
            holder.mSize.setText(Formatter.formatFileSize(mContext, video.size));
            holder.mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startPlayerActivity(video);
                }
            });

            holder.mCardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showDeleteDialog(video, position);
                    return false;
                }
            });

            if (video.thumbnail == null || video.thumbnail.equals("")) {
                holder.mImage.setImageResource(R.drawable.home_place_holder);
            } else {
                BitmapLoadManager.display(holder.mImage, video.thumbnail, BitmapLoadManager.URI_TYPE_LOCAL);
            }
        }
    }

    private void startPlayerActivity(LocalVideo localVideo) {
        if (localVideo.path != null && !localVideo.path.isEmpty()) {
            Intent intent = new Intent(mContext, GvrVideoActivity.class);
            intent.putExtra(GvrVideoActivity.INTENT_LOCAL_VIDEO, localVideo);
            intent.putExtra(GvrVideoActivity.INTENT_PLAY_PATTERN, GvrVideoActivity.PATTERN_TYPE_1);
            intent.putExtra(GvrVideoActivity.INTENT_VIDEO_LIST, mLocalVideoList);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }

    private void sendMessage(int what, int arg1) {
        Message message = mHandler.obtainMessage();
        message.what = what;
        message.arg1 = arg1;
        mHandler.sendMessage(message);
    }

    private void showDeleteDialog(final LocalVideo video, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(video.displayName)
                .setTitle(R.string.delete_dir);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        File file = new File(video.path);
                        if (file.isFile() && file.exists()) {

                            if (file.delete()){
                                mMediaNotifier.scanFile(video.path, null);
                                sendMessage(MSG_DELETE_ITEM, position);
                            } else {
                                sendMessage(MSG_DELETE_FAIL, 0);
                            }
                        }
                    }
                }).start();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showDeleteFailureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(R.string.delete_fail_msg)
                .setTitle(R.string.delete_fail);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // do nothing
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public class EndTagHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;

        public EndTagHolder(View view) {
            super(view);
            mTextView = (TextView) view.findViewById(R.id.end_tag);
        }
    }

    public class CardViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImage;
        public TextView mName;
        public TextView mDuration;
        public TextView mSize;
        public CardView mCardView;

        public CardViewHolder(View view) {
            super(view);

            mImage = (ImageView) view.findViewById(R.id.local_video_card_view_image);
            mName = (TextView) view.findViewById(R.id.local_video_card_view_name);
            mDuration = (TextView) view.findViewById(R.id.local_video_card_view_duration);
            mSize = (TextView) view.findViewById(R.id.local_video_card_view_size);
            mCardView = (CardView) view.findViewById(R.id.local_video_card_view);
        }
    }
}
