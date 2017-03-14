package com.evistek.vr.activity.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.evistek.vr.R;
import com.evistek.vr.activity.ApplicationDetailedActivity;
import com.evistek.vr.activity.E3DApplication;
import com.evistek.vr.model.Application;
import com.evistek.vr.net.BitmapLoadManager;
import com.evistek.vr.net.NetWorkService;
import com.evistek.vr.service.DownloadService;
import com.evistek.vr.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ymzhao on 2016/8/16.
 */
public class ApplicationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private ArrayList<Application> mApplicationList = new ArrayList<Application>();
    private static final int STYLE_ONE = 0;
    private static final int STYLE_ANOTHER = 1;
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private boolean mIsDownloadDone;
    private boolean mIsInstalled;
    private boolean mIsDownloading;
    private boolean mIsWaitingDownload;
    private DownloadService mDLService;
    private HashMap<Integer, Application> mApp = new HashMap<Integer, Application>();
    private static final  String TAG = "ApplicationAdapter";
    private DownloadService.OnSuccessListener mOnSuccessListener;
    private DownloadService.OnProgressListener mOnProgressListener;
    private int mProgress;

    public ApplicationAdapter (Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mDLService = E3DApplication.getInstance().getDownloadService();
        initStatus();
    }

    private void initStatus () {
        mIsDownloadDone = false;
        mIsDownloading = false;
        mIsInstalled = false;
        mIsWaitingDownload = false;
    }

    public void setApplicationList (ArrayList<Application> list) {
        mApplicationList = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.application_card_view, parent, false);
        return new ApplicationViewHolder(view, new IMyViewHolderClicks() {
            @Override
            public void onItemClick(int position) {
                Log.i(TAG, "item:" +position);
                if (mApplicationList != null && mApplicationList.size() > 0) {
                    Intent intent = new Intent(E3DApplication.getInstance(), ApplicationDetailedActivity.class);
                    intent.putExtra(ApplicationDetailedActivity.APPLICATION, mApplicationList.get(position));
                    if (Utils.judgeIsDownLoad(mApp.get(position).getUrl())) {
                        mIsDownloadDone = true;
                        mIsDownloading = false;
                        mIsWaitingDownload = false;
                    } else {
                        mIsDownloadDone = false;
                    }
                    intent.putExtra(ApplicationDetailedActivity.IS_DOWNLOAD, mIsDownloadDone);
                    fetchIsDownloading(position);
                    if (mIsDownloading) {
                        intent.putExtra(ApplicationDetailedActivity.PROGRESS, mProgress);
                    } else {
                        intent.putExtra(ApplicationDetailedActivity.PROGRESS, -1);
                    }
                    mContext.startActivity(intent);
                }
            }

            @Override
            public void onDownloadClick(int position) {
                initStatus();
                checkDownButtonStatus(position);
                notifyItemChanged(position);
                if (mIsInstalled) {
                    Utils.launchApp(mContext, mApplicationList.get(position).getUrl());
                } else if(mIsDownloadDone){
                    String dirPath = E3DApplication.getInstance().getExternalCacheDir().getAbsolutePath() + "/app/";
                    String url =  mApp.get(position).getUrl();
                    String localUrl = dirPath + url.substring(url.lastIndexOf("/") + 1);
                    Utils.installApk(mContext, mApplicationList.get(position).getUrl(), localUrl);
                } else {
                    downloadApp(mApplicationList.get(position).getUrl(), position);
                }
                notifyItemChanged(position);
            }
        });
    }

    private void checkDownButtonStatus (int position) {
        String url = mApp.get(position).getUrl();
        mIsInstalled = Utils.checkApkInstalled(mContext, mApp.get(position).getUrl());
        if (mIsInstalled) {
            Utils.deleteApkFile(url, mDLService);
        }
        if (Utils.judgeIsDownLoad(mApp.get(position).getUrl())) {
            mIsDownloadDone = true;
            mIsDownloading = false;
            mIsWaitingDownload = false;
        } else {
            mIsDownloadDone = false;
        }
        fetchIsDownloading(position);
        fetchWaitingDownload(url);
    }

    private void fetchIsDownloading(int position) {
        String url = mApp.get(position).getUrl();
        if (Utils.judgeIsDownloading(url, mDLService)) {
            mIsDownloading = true;
            executeProgressListener(position);
            mIsWaitingDownload = false;
            mIsInstalled = false;
            mIsDownloadDone = false;
        } else {
            mIsDownloading = false;
        }
    }

    public void fetchWaitingDownload(String url) {
        if (Utils.judgeIsWaitingDownload(url, mDLService)) {
            mIsWaitingDownload = true;
            mIsDownloading = false;
            mIsInstalled = false;
            mIsDownloadDone = false;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        mApp.put(position, mApplicationList.get(position));
        ApplicationViewHolder viewHolder = (ApplicationViewHolder)holder;
        BitmapLoadManager.display(viewHolder.mIcon, mApplicationList.get(position).getIconUrl());
        viewHolder.mName.setText(mApplicationList.get(position).getName());
        viewHolder.mDownloadCount.setText(mApplicationList.get(position).getDownloadCount() + mContext.getString(R.string.detail_download_count_suffix));
        viewHolder.mSize.setText( Formatter.formatFileSize(E3DApplication.getInstance(), mApplicationList.get(position).getSize()));
        viewHolder.mDownLoad.setText(R.string.downloadNetVideo);
        initStatus();
        checkDownButtonStatus(position);
        Button downloadButton = viewHolder.mDownLoad;
        if (mIsInstalled) {
            setButtonStyle(downloadButton, STYLE_ANOTHER);
            downloadButton.setText(R.string.application_launch);
        }
        else if (mIsDownloadDone) {
            setButtonStyle(downloadButton, STYLE_ANOTHER);
            downloadButton.setText(R.string.application_install);
        } else if (mIsDownloading) {
            setButtonStyle(downloadButton, STYLE_ONE);
            if (mProgress < 100) {
                downloadButton.setText(mProgress + "%");
            }
        } else if (mIsWaitingDownload){
            setButtonStyle(downloadButton, STYLE_ONE);
            downloadButton.setText(R.string.application_wait_download);
        } else {
            setButtonStyle(downloadButton, STYLE_ONE);
            downloadButton.setText(R.string.application_download);
        }
    }

    private void setButtonStyle (Button button, int style) {
        switch (style) {
            case STYLE_ONE:
                button.setBackgroundResource(R.drawable.bt_game_download);
                button.setTextColor(E3DApplication.getInstance().getResources().getColor(R.color.colorPrimary));
                break;
            case STYLE_ANOTHER:
                button.setBackgroundResource(R.drawable.bt_game_isdownload);
                button.setTextColor(E3DApplication.getInstance().getResources().getColor(R.color.v2_application));
                break;
        }
    }

    private void downloadApp(String url,final int position) {
        mDLService.addTask(mApp.get(position).getUrl());
        notifyItemChanged(position);
        executeProgressListener(position);
        mOnSuccessListener = new DownloadService.OnSuccessListener() {
            @Override
            public void onSuccess(String url, String filePath) {
                if (url.equals(mApp.get(position).getUrl())) {
                    initStatus();
                    mIsDownloadDone = true;
                    Utils.installApk(mContext, url, filePath);
                    NetWorkService.updateDownloadCount(url, mApp.get(position).getId());
                    notifyItemChanged(position);
                }
            }
        };
        mDLService.setOnSuccessListener(mOnSuccessListener);
    }

    private void executeProgressListener (final int position) {
        mOnProgressListener = new DownloadService.OnProgressListener() {
            @Override
            public void onProgress(int index, String url, int progress) {
                if (url.equals(mApp.get(position).getUrl())) {
                    mProgress = progress;
                    notifyItemChanged(position);
                }
            }
        };

        mDLService.setOnProgressListener(mOnProgressListener);
    }

    @Override
    public int getItemCount() {
        return mApplicationList.size();
    }

    public class ApplicationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        CardView cardViewContainer;
        private Button mDownLoad;
        IMyViewHolderClicks mListener;
        private ImageView mIcon;
        private TextView mName;
        private TextView mDownloadCount;
        private TextView mSize;

        public ApplicationViewHolder(View view,IMyViewHolderClicks listener) {
            super(view);
            mIcon = (ImageView) view.findViewById(R.id.application_icon);
            mName = (TextView) view.findViewById(R.id.application_name);
            mDownloadCount = (TextView) view.findViewById(R.id.application_download_count);
            mSize = (TextView) view.findViewById(R.id.application_size);
            mDownLoad = (Button) view.findViewById(R.id.application_download);
            cardViewContainer = (CardView)view.findViewById(R.id.application_card_view);
            mListener = listener;
            mDownLoad.setOnClickListener(this);
            cardViewContainer.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.application_download:
                    mListener.onDownloadClick(getLayoutPosition());
                    break;
                case R.id.application_card_view:
                    mListener.onItemClick(getLayoutPosition());
                    break;
            }
        }
    }

    private interface IMyViewHolderClicks{
        public void onItemClick(int position);
        public void onDownloadClick(int position);
    }
}
