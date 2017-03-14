package com.evistek.vr.activity;

import android.content.Context;
import android.os.Bundle;
import android.app.Activity;

import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evistek.vr.R;
import com.evistek.vr.model.Application;
import com.evistek.vr.net.BitmapLoadManager;
import com.evistek.vr.net.NetWorkService;
import com.evistek.vr.service.DownloadService;
import com.evistek.vr.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ApplicationDetailedActivity extends Activity {
    public static final String APPLICATION = "app";
    public static final String IS_DOWNLOAD = "isDownLoadDone";
    public static final String PROGRESS = "nowProgress";
    private static final int VIEW_PAGER_NUM = 3;
    private static final int MSG_DOWNLOAD_DONE = 0;
    private static final int MSG_UPDATE_PROGRESS = 1;
    private static final int MSG_WAITING_DOWNLOAD = 3;
    private static final int MSG_CHECK_APP = 2;
    private static final int STYLE_ONE = 0;
    private static final int STYLE_ANOTHER = 1;
    private ImageView mIcon;
    private TextView mName;
    private TextView mAuthor;
    private TextView mVersion;
    private TextView mDownloadCount;
    private TextView mSize;
    private TextView mIntroduction;
    private Application mApplication;
    public ViewPager mViewPager;
    public LinearLayout mIndicatorLayout;
    private Context mContext;
    private List<String> mAppPreviewList;
    private LayoutInflater mLayoutInflater;
    private Button mDownloadButton;
    private boolean mIsDownloadDone;
    private boolean mIsInstalled;
    private boolean mIsDownloading;
    private boolean mIsWaitingDownload;
    private DownloadService mDLService;
    private int mProgress = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detailed_application);
        mContext = this;
        initView ();
        initStatus();
        receiveIntent();
        initDownloadService();
        setData();
    }

    private void initView () {
        mIcon = (ImageView) findViewById(R.id.application_icon);
        mName = (TextView) findViewById(R.id.application_name);
        mAuthor = (TextView) findViewById(R.id.application_author);
        mVersion = (TextView) findViewById(R.id.application_version);
        mDownloadCount =  (TextView) findViewById(R.id.application_download_count);
        mSize = (TextView) findViewById(R.id.application_size);
        mIntroduction = (TextView) findViewById(R.id.application_introduction);
        mViewPager = (ViewPager) findViewById(R.id.application_view_pager);
        mIndicatorLayout = (LinearLayout) findViewById(R.id.view_pager_indicator);
        mDownloadButton = (Button) findViewById(R.id.download_detailed);
    }

    private void initStatus () {
        mIsDownloadDone = false;
        mIsDownloading = false;
        mIsInstalled = false;
        mIsWaitingDownload = false;
    }

    private void initDownloadService() {
        mDLService = E3DApplication.getInstance().getDownloadService();
        mDLService.setOnStartListener(mOnStartListener);
        mDLService.setOnProgressListener(mOnProgressListener);
        mDLService.setOnCompleteListener(mOnCompleteListener);
        mDLService.setOnSuccessListener(mOnSuccessListener);
        int index = mDLService.getCurrentTask();
        if (!mDLService.getTasks().isEmpty()) {
            if (index < mDLService.getTasks().size() && mApplication.getUrl().equals(mDLService.getTasks().get(index).getUrl())) {
                mProgress = mDLService.getTasks().get(index).getProgress();
                if (mProgress > 0 && mProgress < 100) {
                    mIsDownloading = true;
                }
            }
        }
    }

    private void fetchDownLoadStatus() {
        if (Utils.judgeIsDownLoad(mApplication.getUrl())) {
            mIsDownloadDone = true;
            mIsDownloading = false;
            mIsWaitingDownload = false;
        }
    }

    private void judgeIsInstalled () {
        mIsInstalled = fetchInstalledStatus(mApplication.getUrl());
        if (mIsInstalled) {
            String url = mApplication.getUrl();
            Utils.deleteApkFile(url, mDLService);
        }
    }

    public void fetchWaitingDownload() {
        String url = mApplication.getUrl();
        if (Utils.judgeIsWaitingDownload(url, mDLService)) {
            mIsWaitingDownload = true;
            Message message = mHandler.obtainMessage();
            message.what = MSG_WAITING_DOWNLOAD;
            mHandler.sendMessage(message);
        }
    }

    private boolean fetchInstalledStatus(final String url) {
        Message message = mHandler.obtainMessage();
        message.what = MSG_CHECK_APP;
        if (Utils.checkApkInstalled(mContext, url)) {
            message.arg1 = 1;
            mHandler.sendMessage(message);
            return true;
        } else {
            message.arg1 = 0;
            mHandler.sendMessage(message);
            return false;
        }
    }

    private void receiveIntent() {
        mApplication = (Application) getIntent().getSerializableExtra(APPLICATION);
        mIsDownloadDone = getIntent().getBooleanExtra(IS_DOWNLOAD, false);
        mProgress = getIntent().getIntExtra(PROGRESS, -1);
    }

    public void backPreviousActivity(View v) {
        super.onBackPressed();
    }

    private void setData () {
        mAppPreviewList = new ArrayList<String>();
        mAppPreviewList.add(mApplication.getPreview1Url());
        mAppPreviewList.add(mApplication.getPreview2Url());
        mAppPreviewList.add(mApplication.getPreview3Url());
        BitmapLoadManager.display(mIcon,
                mApplication.getIconUrl(),
                BitmapLoadManager.URI_TYPE_REMOTE,
                R.drawable.game_placeholder);
        mName.setText(mApplication.getName());
        mAuthor.setText(getString(R.string.application_author) + mApplication.getAuthor());
        mAuthor.setVisibility(View.GONE);
        mVersion.setText(getString(R.string.application_version) + mApplication.getVersion());
        mDownloadCount.setText(mApplication.getDownloadCount() + getString(R.string.detail_download_count_suffix));
        mSize.setText(Formatter.formatFileSize(E3DApplication.getInstance(), mApplication.getSize()));
        mIntroduction.setText(mApplication.getIntro());
        setPreviewImgViewPager();
        setDownloadButton();
    }

    public void setDownloadButton () {
        fetchWaitingDownload();
        //从主页过来不好判断当前的是否下载好了，且没有安装的那种情况
        fetchDownLoadStatus();
        if (mProgress != -1) {
            mIsDownloading = true;
        }
        judgeIsInstalled();
        if (mIsInstalled) {
            setButtonStyle(mDownloadButton, STYLE_ANOTHER);
            mDownloadButton.setText(R.string.application_launch);
        }
        else if (mIsDownloadDone) {
            setButtonStyle(mDownloadButton, STYLE_ANOTHER);
            mDownloadButton.setText(R.string.application_install);
        } else if (mIsDownloading) {
            setButtonStyle(mDownloadButton, STYLE_ONE);
            if (mProgress < 100) {
                mDownloadButton.setText(mProgress + "%");
            }
        } else if (mIsWaitingDownload){
            setButtonStyle(mDownloadButton, STYLE_ONE);
            mDownloadButton.setText(R.string.application_wait_download);
        } else {
            setButtonStyle(mDownloadButton, STYLE_ONE);
            mDownloadButton.setText(R.string.application_download);
        }
        mDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsInstalled) {
                    Utils.launchApp(mContext, mApplication.getUrl());
                } else if(mIsDownloadDone){
                    String dirPath = E3DApplication.getInstance().getExternalCacheDir().getAbsolutePath() + "/app/";
                    String url =  mApplication.getUrl();
                    String localUrl = dirPath + url.substring(url.lastIndexOf("/") + 1);
                    Utils.installApk(mContext, mApplication.getUrl(), localUrl);
                } else {
                    downloadApp(mApplication.getUrl());
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        judgeIsInstalled();
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_PROGRESS:
                    if (mDownloadButton!= null) {
                        if (msg.arg1 < 100) {
                            mDownloadButton.setText(msg.arg1 + "%");
                        }
                    }
                    break;
                case MSG_DOWNLOAD_DONE:
                    NetWorkService.updateDownloadCount(mApplication.getUrl(), mApplication.getId());
                    break;
                case MSG_CHECK_APP:
                    if (msg.arg1 == 0) {
                        mIsInstalled = false;
                        if (mIsDownloadDone) {
                            setButtonStyle(mDownloadButton, STYLE_ANOTHER);
                            mDownloadButton.setText(R.string.application_install);
                        }
                    } else {
                        mIsInstalled = true;
                        setButtonStyle(mDownloadButton, STYLE_ANOTHER);
                        mDownloadButton.setText(R.string.application_launch);
                    }
                    break;
                case MSG_WAITING_DOWNLOAD:
                    if (mDownloadButton!= null) {
                        setButtonStyle(mDownloadButton, STYLE_ONE);
                        mDownloadButton.setText(R.string.application_wait_download);
                    }
                    break;
            }

            return true;
        }
    });

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

    private DownloadService.OnStartListener mOnStartListener = new DownloadService.OnStartListener() {
        @Override
        public void onStart() {

        }
    };

    private DownloadService.OnProgressListener mOnProgressListener = new DownloadService.OnProgressListener() {
        @Override
        public void onProgress(int index, String url, int progress) {
            if (url.equals(mApplication.getUrl())) {
                mIsDownloading = true;
                Message message = mHandler.obtainMessage();
                message.what = MSG_UPDATE_PROGRESS;
                message.arg1 = progress;
                mHandler.sendMessage(message);
            }
        }
    };

    private DownloadService.OnCompleteListener mOnCompleteListener = new DownloadService.OnCompleteListener() {
        @Override
        public void onComplete() {

        }
    };

    private DownloadService.OnSuccessListener mOnSuccessListener = new DownloadService.OnSuccessListener() {
        @Override
        public void onSuccess(String url, String filePath) {
            if (url.equals(mApplication.getUrl())) {
                mIsDownloading = false;
                mIsDownloadDone = true;
                Utils.installApk(mContext, url, filePath);
                Message message = mHandler.obtainMessage();
                message.what = MSG_DOWNLOAD_DONE;
                mHandler.sendMessage(message);
            }
        }
    };

    private void downloadApp(String url) {
        mDLService.addTask(mApplication.getUrl());
        fetchWaitingDownload();
    }

    public void setPreviewImgViewPager() {
        View indicatorView = null;
        for (int i = 0; i < VIEW_PAGER_NUM; i++) {
            indicatorView = new View(mContext);
            indicatorView.setBackgroundResource(R.drawable.view_pager_seclect);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(15, 15);
            if (i != 0) {
                params.leftMargin = 15;
            }
            indicatorView.setEnabled(i == 0 ? true : false);
            indicatorView.setLayoutParams(params);
            mIndicatorLayout.addView(indicatorView);
        }
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int count = mIndicatorLayout.getChildCount();
                for (int i = 0; i < count; i++) {
                    mIndicatorLayout.getChildAt(i).setEnabled(position == i);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setAdapter(new ViewPagerAdapter());
    }

    public class ViewPagerAdapter extends PagerAdapter{
        private List<View> mViewList;
        public ViewPagerAdapter() {

            mLayoutInflater = LayoutInflater.from(mContext);
            mViewList = new ArrayList<View>();
            for (int i = 0; i < VIEW_PAGER_NUM; i++) {
                View view = mLayoutInflater.inflate(R.layout.viewpager_item, null);
                ImageView imageView = (ImageView) view.findViewById(R.id.view_pager_image);
                BitmapLoadManager.display(imageView, mAppPreviewList.get(i));
                mViewList.add(imageView);
            }
        }

        @Override
        public int getCount() {
            return mViewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView view = (ImageView) mViewList.get(position);
            ViewParent vp =view.getParent();
            if (vp!=null){
                ViewGroup parent = (ViewGroup)vp;
                parent.removeView(view);
            }
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //super.destroyItem(container, position, object);
        }
    }
}