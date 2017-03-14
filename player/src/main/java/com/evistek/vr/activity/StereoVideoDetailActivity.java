package com.evistek.vr.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evistek.vr.R;
import com.evistek.vr.model.Favorite;
import com.evistek.vr.model.Video;
import com.evistek.vr.net.BitmapLoadManager;
import com.evistek.vr.net.NetWorkService;
import com.evistek.vr.net.callback.FavoriteCallback;
import com.evistek.vr.net.json.JsonRespFavorite;
import com.evistek.vr.user.User;
import com.evistek.vr.utils.Utils;

import org.rajawali3d.vr.activity.GvrVideoActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class StereoVideoDetailActivity extends Activity {
    private static final String TAG = "StereoVideoDetail";
    public static final String INTENT_VIDEO = "video";
    public static final String INTENT_PATTERN = "pattern";
    public static final String INTENT_REMOVED_ID = "removedId";
    public static final String INTENT_VIDEO_LIST = "videoList";

    private static final int MSG_ADD_FAVORITE = 0;
    private static final int MSG_DELETE_FAVORITE = 1;

    private Context mContext;
    private Video mVideo;
    private int mPattern;
    private String mUrl;
    private User mUser;
    private Favorite mFavorite;
    private int mFavoriteIDRemoved;
    private ArrayList<Video> mVideoList = new ArrayList<>();

    private ImageView mVideoImage;
    private ImageView mPlay;
    private TextView mVideoName;
    private ImageView mFavoriteView;
    private TextView mActors;
    private TextView mTime;
    private TextView mLocation;
    private TextView mPlayCount;
    private TextView mIntro;
    private ImageView mBack;
    private LinearLayout mTitleLayout;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ADD_FAVORITE:
                    mFavorite = (Favorite) msg.obj;
                    mUser.favorites.add(mFavorite);
                    mFavoriteView.setImageResource(R.drawable.favorite_enabled);
                    break;
                case MSG_DELETE_FAVORITE:
                    mFavoriteIDRemoved = mFavorite.getContentId();
                    mUser.favorites.remove(mFavorite);
                    mFavorite = null;
                    mFavoriteView.setImageResource(R.drawable.favorite_disabled);
                    break;
            }

            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stereo_video_detail);

        mContext = this;
        mFavoriteIDRemoved = -1;
        receiveIntent();
        initView();
    }

    @Override
    protected void onResume() {

        mUser = E3DApplication.getInstance().getUser();
        mFavorite = getFavorite(mVideo);
        if (mUser.isLogin && mFavorite != null) {
            mFavoriteView.setImageResource(R.drawable.favorite_enabled);
        } else {
            mFavoriteView.setImageResource(R.drawable.favorite_disabled);
        }

        super.onResume();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(INTENT_REMOVED_ID, mFavoriteIDRemoved);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    private void initView() {
        mVideoImage = (ImageView) findViewById(R.id.stereo_video_detail_image);
        mPlay = (ImageView) findViewById(R.id.stereo_video_detail_play);
        mVideoName = (TextView) findViewById(R.id.stereo_video_detail_name);
        mFavoriteView = (ImageView) findViewById(R.id.stereo_video_detail_favorite);
        mActors = (TextView) findViewById(R.id.stereo_video_detail_actors);
        mTime = (TextView) findViewById(R.id.stereo_video_detail_time);
        mLocation = (TextView) findViewById(R.id.stereo_video_detail_location);
        mPlayCount = (TextView) findViewById(R.id.stereo_video_detail_play_count);
        mIntro = (TextView) findViewById(R.id.stereo_video_detail_intro);
        mBack = (ImageView) findViewById(R.id.stereo_video_detail_back);

        mVideoName.setText(mVideo.getContentName());
        mActors.setText(mVideo.getActors());
        mTime.setText(new SimpleDateFormat("yyyy-MM-dd").format(mVideo.getDate()));
        mLocation.setText(mVideo.getLocation());
        mPlayCount.setText(String.valueOf(mVideo.getDownloadCount()) + getString(R.string.detail_play_count_suffix));
        mIntro.setText(mVideo.getIntroduction());

        mTitleLayout = (LinearLayout) findViewById(R.id.stereo_video_detail_title_layout);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            int marginTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
            layoutParams.setMargins(0, marginTop, 0, 0);
            mTitleLayout.setLayoutParams(layoutParams);
        }

        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlayerActivity(mUrl);
            }
        });

        mFavoriteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isNetworkAvailable()) {
                    if (mUser.isLogin) {
                        if (mFavorite != null) {
                            deleteFavorite(mFavorite);
                        } else {
                            addFavorite(mVideo);
                        }
                    } else {
                        showLoginAlertDialog();
                    }
                } else {
                        showAlertDialog();
                    }
                }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        BitmapLoadManager.display(mVideoImage, mVideo.getPreview1Url());
    }

    private void receiveIntent() {
        mVideo = (Video) getIntent().getSerializableExtra(INTENT_VIDEO);
        mPattern = getIntent().getIntExtra(INTENT_PATTERN, GvrVideoActivity.PATTERN_TYPE_1);
        mUrl = mVideo.getUrl();
        mVideoList = (ArrayList<Video>) getIntent().getSerializableExtra(INTENT_VIDEO_LIST);
    }

    private void startPlayerActivity(String url) {
        if (Utils.isNetworkAvailable()) {
            if (url != null && !url.isEmpty()) {
                Intent intent = new Intent(this, GvrVideoActivity.class);
                intent.putExtra(GvrVideoActivity.INTENT_NET_VIDEO, mVideo);
                intent.putExtra(GvrVideoActivity.INTENT_PLAY_PATTERN, mPattern);
                intent.putExtra(GvrVideoActivity.INTENT_VIDEO_LIST, mVideoList);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } else {
            showAlertDialog();
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(R.string.detail_alert_dialog_content)
                .setTitle(R.string.network_unavailable);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showLoginAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(R.string.need_login)
                .setTitle(R.string.not_login);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addFavorite(Video video) {
        final Favorite favorite = new Favorite();
        favorite.setUserId(mUser.id);
        favorite.setContentId(video.getContentId());
        favorite.setContentName(video.getContentName());
        favorite.setCoverUrl(video.getPreview1Url());

        NetWorkService.addFavorite(favorite, new FavoriteCallback() {
            @Override
            public void onResult(int code, JsonRespFavorite jsonResp) {
                if (code == 200) {
                    sendMessage(MSG_ADD_FAVORITE, favorite);
                } else {
                    Log.e(TAG, "add favorite fail");
                }
            }
        });
    }

    private void deleteFavorite(final Favorite favorite) {
        favorite.setUserId(mUser.id);
        favorite.setContentId(mVideo.getContentId());
        favorite.setContentName(mVideo.getContentName());
        favorite.setCoverUrl(mVideo.getPreview1Url());

        NetWorkService.deleteFavorite(favorite, new FavoriteCallback() {
            @Override
            public void onResult(int code, JsonRespFavorite jsonResp) {
                if (code == 200) {
                    sendMessage(MSG_DELETE_FAVORITE, favorite);
                } else {
                    Log.e(TAG, "fail to delete favorite, code: " + code);
                }
            }
        });
    }

    private Favorite getFavorite(Video video) {
        List<Favorite> favorites = mUser.favorites;

        for(Favorite f: favorites) {
            if (f.getContentId().equals(video.getContentId())) {
                return f;
            }
        }

        return null;
    }

    private void sendMessage(int what, Object obj) {
        Message message = mHandler.obtainMessage();
        message.what = what;
        message.obj = obj;
        mHandler.sendMessage(message);
    }
}
