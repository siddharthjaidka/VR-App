package com.evistek.vr.activity.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.Pair;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evistek.vr.R;
import com.evistek.vr.activity.ApplicationDetailedActivity;
import com.evistek.vr.activity.E3DApplication;
import com.evistek.vr.activity.StereoVideoDetailActivity;
import com.evistek.vr.activity.WebViewActivity;
import com.evistek.vr.model.Application;
import com.evistek.vr.model.Video;
import com.evistek.vr.model.VrOnline;
import com.evistek.vr.net.BitmapLoadManager;
import com.evistek.vr.net.NetWorkService;

import org.rajawali3d.vr.activity.GvrVideoActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by ZWX on 2016/8/7.
 */
public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int ITEM_TYPE_VIEW_PAGER = 0;
    public static final int ITEM_TYPE_GRID_VIEW = 1;
    public static final int ITEM_TYPE_VIEW_TAG = 2;
    public static final int ITEM_TYPE_LIST_ITEM_VIDEO = 3;
    public static final int ITEM_TYPE_LIST_ITEM_GAME = 4;
    public static final int ITEM_TYPE_LIST_ITEM_VR_ONLINE = 5;
    public static final int ITEM_TYPE_END_TAG = 6;

    public static final int TOTAL_SPAN_SIZE = 2;
    public static final int SPAN_SIZE_VIEW_PAGER = TOTAL_SPAN_SIZE;
    public static final int SPAN_SIZE_GRID_VIEW = TOTAL_SPAN_SIZE;
    public static final int SPAN_SIZE_VIEW_TAG = TOTAL_SPAN_SIZE;
    public static final int SPAN_SIZE_LIST_ITEM_VIDEO = 1;
    public static final int SPAN_SIZE_LIST_ITEM_GAME = 1;
    public static final int SPAN_SIZE_LIST_ITEM_VR_ONLINE = 1;

    public static final int ITEM_NUM_VIEW_PAGER = 1;
    public static final int ITEM_NUM_GRID_VIEW = 1;
    public static final int ITEM_NUM_VIEW_TAG = 1;
    public static final int ITEM_NUM_LIST_ITEM = 6;

    public static final int ITEM_INDEX_VIEW_PAGER = 0;
    public static final int ITEM_INDEX_GRID_VIEW = ITEM_INDEX_VIEW_PAGER + ITEM_NUM_VIEW_PAGER;

    public static final int ITEM_INDEX_VIEW_TAG_VR = ITEM_INDEX_GRID_VIEW + ITEM_NUM_GRID_VIEW;
    public static final int ITEM_INDEX_LIST_ITEM_VR = ITEM_INDEX_VIEW_TAG_VR + ITEM_NUM_VIEW_TAG;

    public static final int ITEM_INDEX_VIEW_TAG_3D = ITEM_INDEX_LIST_ITEM_VR + ITEM_NUM_LIST_ITEM;
    public static final int ITEM_INDEX_LIST_ITEM_3D = ITEM_INDEX_VIEW_TAG_3D + ITEM_NUM_VIEW_TAG;

    public static final int ITEM_INDEX_VIEW_TAG_VR_ONLINE = ITEM_INDEX_LIST_ITEM_3D + ITEM_NUM_LIST_ITEM;
    public static final int ITEM_INDEX_LIST_ITEM_VR_ONLINE = ITEM_INDEX_VIEW_TAG_VR_ONLINE + ITEM_NUM_VIEW_TAG;

    public static final int ITEM_INDEX_VIEW_TAG_GAME = ITEM_INDEX_LIST_ITEM_VR_ONLINE + ITEM_NUM_LIST_ITEM;
    public static final int ITEM_INDEX_LIST_ITEM_GAME = ITEM_INDEX_VIEW_TAG_GAME + ITEM_NUM_VIEW_TAG;

    public static final int ITEM_INDEX_END_TAG = ITEM_INDEX_LIST_ITEM_GAME + ITEM_NUM_LIST_ITEM;

    private static final int ITEM_NUM = ITEM_INDEX_END_TAG + ITEM_NUM_VIEW_TAG;

    private static final int VIEW_PAGER_NUM = 4;
    private static final int VIEW_PAGER_UPDATE_INTERVAL = 5; //seconds

    private static final int MSG_UPDATE_VIEW_PAGER = 0;

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ViewPagerHolder mViewPagerHolder;

    private ArrayList<Video> mStereoVideoList = new ArrayList<Video>();
    private ArrayList<Video> mPanoVideoList = new ArrayList<Video>();
    private ArrayList<Application> mGameList = new ArrayList<Application>();
    private ArrayList<Video> mViewPagerList = new ArrayList<Video>();
    private ArrayList<VrOnline> mVrOnlineList = new ArrayList<>();

    private ScheduledThreadPoolExecutor mScheduledThread;
    private int mViewPagerIndex = 0;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_UPDATE_VIEW_PAGER:
                    mViewPagerHolder.mViewPager.setCurrentItem(mViewPagerIndex);
                    if (mViewPagerList.size() > 0) {
                        mViewPagerHolder.mTextView.setText(mViewPagerList.get(mViewPagerIndex).getContentName());
                    }
                    break;
            }

            return true;
        }
    });

    public HomeAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);

        initScheduledThread();
    }

    public void setStereoVideoList(ArrayList<Video> list) {
        mStereoVideoList = list;
    }

    public void setPanoVideoList(ArrayList<Video> list) {
        mPanoVideoList = list;
    }

    public void setGameList(ArrayList<Application> list) {
        mGameList = list;
    }

    public void setViewPagerList(ArrayList<Video> list) {
        mViewPagerList = list;
    }

    public void setVrOnlineList(ArrayList<VrOnline> list) {
        mVrOnlineList = list;
    }

    public interface OnGridViewClickListener {
        void onGridViewClick(int position);
    }

    private OnGridViewClickListener mOnGridViewClickListener;

    public void setOnGridViewClickListener(OnGridViewClickListener l) {
        mOnGridViewClickListener = l;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_VIEW_PAGER) {
            mViewPagerHolder = new ViewPagerHolder(mLayoutInflater.inflate(R.layout.viewpager, parent, false));
            return mViewPagerHolder;
        } else if (viewType == ITEM_TYPE_GRID_VIEW) {
            return new GridViewHolder(mLayoutInflater.inflate(R.layout.gridview, parent, false));
        } else if (viewType == ITEM_TYPE_VIEW_TAG) {
            return new ViewTagHolder(mLayoutInflater.inflate(R.layout.view_tag, parent, false));
        } else if (viewType == ITEM_TYPE_LIST_ITEM_VIDEO) {
            return new ViewItemHolder(mLayoutInflater.inflate(R.layout.view_item, parent, false));
        } else if (viewType == ITEM_TYPE_LIST_ITEM_VR_ONLINE) {
            return new ViewItemHolder(mLayoutInflater.inflate(R.layout.view_square_item, parent, false));
        } else if (viewType == ITEM_TYPE_LIST_ITEM_GAME) {
            return new ViewItemHolder(mLayoutInflater.inflate(R.layout.view_item, parent, false));
        } else if (viewType == ITEM_TYPE_END_TAG) {
            return new EndTagHolder(mLayoutInflater.inflate(R.layout.end_tag, parent, false));
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewPagerHolder) {
            bindViewPagerData((ViewPagerHolder) holder, position);
        } else if (holder instanceof GridViewHolder) {
            bindGridViewData((GridViewHolder) holder, position);
        } else if (holder instanceof ViewTagHolder) {
            bindViewTagData((ViewTagHolder)holder, position);
        } else if (holder instanceof ViewItemHolder) {
            bindViewItemData((ViewItemHolder)holder, position);
        } else if (holder instanceof EndTagHolder) {
            ((EndTagHolder)holder).mTextView.setText("~~没有更多了~~");
        }
    }

    @Override
    public int getItemCount() {
        return ITEM_NUM;
    }

    @Override
    public int getItemViewType(int position) {

        if (position >= ITEM_INDEX_VIEW_PAGER && position < ITEM_INDEX_VIEW_PAGER + ITEM_NUM_VIEW_PAGER) {
            return ITEM_TYPE_VIEW_PAGER;
        } else if (position >= ITEM_INDEX_GRID_VIEW && position < ITEM_INDEX_GRID_VIEW + ITEM_NUM_GRID_VIEW) {
            return ITEM_TYPE_GRID_VIEW;
        } else if (position >= ITEM_INDEX_VIEW_TAG_VR && position < ITEM_INDEX_VIEW_TAG_VR + ITEM_NUM_VIEW_TAG) {
            return ITEM_TYPE_VIEW_TAG;
        } else if (position >= ITEM_INDEX_LIST_ITEM_VR && position < ITEM_INDEX_LIST_ITEM_VR + ITEM_NUM_LIST_ITEM) {
            return ITEM_TYPE_LIST_ITEM_VIDEO;
        } else if (position >= ITEM_INDEX_VIEW_TAG_3D && position < ITEM_INDEX_VIEW_TAG_3D + ITEM_NUM_VIEW_TAG) {
            return ITEM_TYPE_VIEW_TAG;
        } else if (position >= ITEM_INDEX_LIST_ITEM_3D && position < ITEM_INDEX_LIST_ITEM_3D + ITEM_NUM_LIST_ITEM) {
            return ITEM_TYPE_LIST_ITEM_VIDEO;
        } else if (position >= ITEM_INDEX_VIEW_TAG_VR_ONLINE && position < ITEM_INDEX_VIEW_TAG_VR_ONLINE + ITEM_NUM_VIEW_TAG) {
            return ITEM_TYPE_VIEW_TAG;
        } else if (position >= ITEM_INDEX_LIST_ITEM_VR_ONLINE && position < ITEM_INDEX_LIST_ITEM_VR_ONLINE + ITEM_NUM_LIST_ITEM) {
            return ITEM_TYPE_LIST_ITEM_VR_ONLINE;
        } else if (position >= ITEM_INDEX_VIEW_TAG_GAME && position < ITEM_INDEX_VIEW_TAG_GAME + ITEM_NUM_VIEW_TAG) {
            return ITEM_TYPE_VIEW_TAG;
        } else if (position >= ITEM_INDEX_LIST_ITEM_GAME && position < ITEM_INDEX_LIST_ITEM_GAME + ITEM_NUM_LIST_ITEM) {
            return ITEM_TYPE_LIST_ITEM_GAME;
        } else if (position >= ITEM_INDEX_END_TAG && position < ITEM_INDEX_END_TAG + ITEM_NUM_VIEW_TAG) {
            return ITEM_TYPE_END_TAG;
        }

        return super.getItemViewType(position);
    }

    private void bindViewPagerData(ViewPagerHolder holder, int position) {
        holder.mViewPager.setAdapter(new ViewPagerAdapter(null));
        holder.mViewPager.setCurrentItem(mViewPagerIndex);
        if (mViewPagerList.size() > 0) {
            holder.mTextView.setText(mViewPagerList.get(mViewPagerIndex).getContentName());
        }
    }

    private void bindGridViewData(GridViewHolder holder, int position) {
        holder.mGridView.setAdapter(new GridViewAdapter());
    }

    private void bindViewTagData(ViewTagHolder holder, int position) {
        switch (position) {
            case ITEM_INDEX_VIEW_TAG_VR:
                holder.mTextView.setText(R.string.vr);
                holder.mIndicatorView.setBackgroundResource(R.color.tag_indicator_vr);
                break;
            case ITEM_INDEX_VIEW_TAG_3D:
                holder.mTextView.setText(R.string.video_3d);
                holder.mIndicatorView.setBackgroundResource(R.color.tag_indicator_3d);
                break;
            case ITEM_INDEX_VIEW_TAG_VR_ONLINE:
                holder.mTextView.setText(R.string.vr_online);
                holder.mIndicatorView.setBackgroundResource(R.color.tag_indicator_vr_online);
                break;
            case ITEM_INDEX_VIEW_TAG_GAME:
                holder.mTextView.setText(R.string.game);
                holder.mIndicatorView.setBackgroundResource(R.color.tag_indicator_game);
                break;
        }
    }

    private void bindViewItemData(ViewItemHolder holder, int position) {
        if (position >= ITEM_INDEX_LIST_ITEM_VR && position < ITEM_INDEX_LIST_ITEM_VR + ITEM_NUM_LIST_ITEM) {
            bindPanoVideo(holder, position);
        } else if (position >= ITEM_INDEX_LIST_ITEM_3D && position < ITEM_INDEX_LIST_ITEM_3D + ITEM_NUM_LIST_ITEM) {
            bindStereoVideo(holder, position);
        } else if (position >= ITEM_INDEX_LIST_ITEM_VR_ONLINE && position < ITEM_INDEX_LIST_ITEM_VR_ONLINE + ITEM_NUM_LIST_ITEM) {
            bindVrOnline(holder, position);
        } else if (position >= ITEM_INDEX_LIST_ITEM_GAME && position < ITEM_INDEX_LIST_ITEM_GAME + ITEM_NUM_LIST_ITEM) {
            bindGame(holder, position);
        } else {
            holder.mImageView.setImageResource(R.drawable.home_place_holder);
            holder.mTextView.setText("");
        }
    }

    private void bindStereoVideo(ViewItemHolder holder, int position) {
        if (mStereoVideoList.size() >= ITEM_NUM_LIST_ITEM) {
            final Video video = mStereoVideoList.get(position - ITEM_INDEX_LIST_ITEM_3D);
            String name = video.getContentName();
            String coverUrl = "";
            if (video.getPreview1Url() != null ) {
                coverUrl = video.getPreview1Url();
            } else {
                coverUrl = video.getCoverUrl();
            }
            holder.mTextView.setText(name);
            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startVideoDetailActivity(video, GvrVideoActivity.PATTERN_TYPE_3, mStereoVideoList);
                }
            });

            BitmapLoadManager.display(holder.mImageView, coverUrl);
        }
    }

    private void bindPanoVideo(ViewItemHolder holder, int position) {
        if (mPanoVideoList.size() >= ITEM_NUM_LIST_ITEM) {
            final Video video = mPanoVideoList.get(position - ITEM_INDEX_LIST_ITEM_VR);
            String name = video.getContentName();
            String coverUrl = "";
            if (video.getPreview1Url() != null ) {
                coverUrl = video.getPreview1Url();
            } else {
                coverUrl = video.getCoverUrl();
            }
            holder.mTextView.setText(name);
            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startVideoDetailActivity(video, GvrVideoActivity.PATTERN_TYPE_2, mPanoVideoList);
                }
            });

            BitmapLoadManager.display(holder.mImageView, coverUrl);
        }
    }

    private void bindVrOnline(ViewItemHolder holder, int position) {
        if (mVrOnlineList.size() >= ITEM_NUM_LIST_ITEM) {
            final VrOnline vrOnline = mVrOnlineList.get(position - ITEM_INDEX_LIST_ITEM_VR_ONLINE);
            final String url = vrOnline.getUrl();
            holder.mTextView.setText(vrOnline.getName());
            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NetWorkService.updateVrOnlinePlaycount(vrOnline.getId());
                    startWebViewActivity(url);
                }
            });

            BitmapLoadManager.display(holder.mImageView, vrOnline.getCoverUrl(), BitmapLoadManager.URI_TYPE_REMOTE);
        }
    }

    private void bindGame(ViewItemHolder holder, int position) {
        int index = position - ITEM_INDEX_LIST_ITEM_GAME;
        if (index < mGameList.size()) {
            final Application app = mGameList.get(index);
            String name = app.getName();
            String coverUrl = "";
            if (app.getPreview1Url() != null && !app.getPreview1Url().equals("")) {
                coverUrl = app.getPreview1Url();
            } else {
                coverUrl = app.getIconUrl();
            }
            holder.mTextView.setText(name);
            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startGameDetailedActivity(app);
                }
            });
            BitmapLoadManager.display(holder.mImageView, coverUrl);
        } else {
            holder.mImageView.setImageResource(R.drawable.home_place_holder);
            holder.mTextView.setText("");
        }
    }

    private void initScheduledThread() {
        mScheduledThread = new ScheduledThreadPoolExecutor(1);

        mScheduledThread.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                int currentItem = mViewPagerHolder.mViewPager.getCurrentItem();
                mViewPagerIndex = (currentItem + 1) % VIEW_PAGER_NUM;
                sendMessage(MSG_UPDATE_VIEW_PAGER);
            }
        }, VIEW_PAGER_UPDATE_INTERVAL, VIEW_PAGER_UPDATE_INTERVAL, TimeUnit.SECONDS);
    }

    private void sendMessage(int what) {
        Message message = mHandler.obtainMessage();
        message.what = what;
        mHandler.sendMessage(message);
    }

    // TODO: goto StereoVideoDetailActivity for all videos for now
    private void startVideoDetailActivity(Video video, int pattern, ArrayList<Video> videoList) {
        Intent intent = new Intent(E3DApplication.getInstance(), StereoVideoDetailActivity.class);
        intent.putExtra(StereoVideoDetailActivity.INTENT_VIDEO, video);
        intent.putExtra(StereoVideoDetailActivity.INTENT_PATTERN, pattern);
        intent.putExtra(StereoVideoDetailActivity.INTENT_VIDEO_LIST, videoList);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.getApplicationContext().startActivity(intent);
    }

    private void startGameDetailedActivity(Application mApp) {
        Intent intent = new Intent(E3DApplication.getInstance(), ApplicationDetailedActivity.class);
        intent.putExtra(ApplicationDetailedActivity.APPLICATION, mApp);
        mContext.startActivity(intent);
    }

    private void startWebViewActivity(String url) {
        Intent intent = new Intent(E3DApplication.getInstance(), WebViewActivity.class);
        intent.putExtra(WebViewActivity.INTENT_URL, url);
        mContext.startActivity(intent);
    }

    public class ViewPagerHolder extends RecyclerView.ViewHolder {

        public ViewPager mViewPager;
        public LinearLayout mIndicatorLayout;
        public TextView mTextView;

        public ViewPagerHolder(View view) {
            super(view);
            mViewPager = (ViewPager) view.findViewById(R.id.view_pager);
            mIndicatorLayout = (LinearLayout) view.findViewById(R.id.view_pager_indicator);
            mTextView = (TextView) view.findViewById(R.id.view_pager_name);

            View indicatiorView = null;
            for (int i = 0; i < VIEW_PAGER_NUM; i++) {
                indicatiorView = new View(mContext);
                indicatiorView.setBackgroundResource(R.drawable.view_pager_seclect);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(15, 15);
                if (i != 0) {
                    params.leftMargin = 15;
                }
                indicatiorView.setEnabled(i == 0 ? true : false);
                indicatiorView.setLayoutParams(params);
                mIndicatorLayout.addView(indicatiorView);
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

                    if (mViewPagerList.size() > 0) {
                        mTextView.setText(mViewPagerList.get(position).getContentName());
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
    }

    public class GridViewHolder extends RecyclerView.ViewHolder {
        public GridView mGridView;
        public GridViewHolder(View view) {
            super(view);

            mGridView = (GridView) view.findViewById(R.id.grid_view);
            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (mOnGridViewClickListener != null) {
                        mOnGridViewClickListener.onGridViewClick(position);
                    }
                }
            });
        }
    }

    public class ViewTagHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public View mIndicatorView;

        public ViewTagHolder(View view) {
            super(view);

            mTextView = (TextView) view.findViewById(R.id.view_tag);
            mIndicatorView = (View) view.findViewById(R.id.view_tag_indicator);
        }
    }

    public class EndTagHolder extends RecyclerView.ViewHolder {
        public  TextView mTextView;

        public EndTagHolder(View view) {
            super(view);
            mTextView = (TextView) view.findViewById(R.id.end_tag);
        }
    }

    public class ViewItemHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;
        public TextView mTextView;

        public  ViewItemHolder(View view) {
            super(view);

            mImageView = (ImageView) view.findViewById(R.id.view_item_image);
            mTextView = (TextView) view.findViewById(R.id.view_item_name);
        }
    }

    private void generateViewPagerData() {
        if (mStereoVideoList.size() >= VIEW_PAGER_NUM / 2 &&
                mPanoVideoList.size() >= VIEW_PAGER_NUM / 2) {

            Comparator comparator = new Comparator<Video>() {
                @Override
                public int compare(Video lhs, Video rhs) {
                    if (lhs.getDownloadCount().intValue() > rhs.getDownloadCount().intValue()) {
                        return 1;
                    } else if (lhs.getDownloadCount().intValue() == rhs.getDownloadCount().intValue()) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
            };

            List<Video> stereoVideoCopy = new ArrayList<Video>(Arrays.asList(new Video[mStereoVideoList.size()]));
            List<Video> panoVideoCopy = new ArrayList<Video>(Arrays.asList(new Video[mPanoVideoList.size()]));
            Collections.copy(stereoVideoCopy, mStereoVideoList);
            Collections.copy(panoVideoCopy, mPanoVideoList);
            Collections.sort(stereoVideoCopy, comparator);
            Collections.sort(panoVideoCopy, comparator);

            mViewPagerList.clear();
            for (int i = 0; i < VIEW_PAGER_NUM / 2; i++) {
                mViewPagerList.add(panoVideoCopy.get(i));
            }
            for (int i = 0; i < VIEW_PAGER_NUM / 2; i++) {
                mViewPagerList.add(stereoVideoCopy.get(i));
            }
        }
    }

    public class ViewPagerAdapter extends PagerAdapter {

        private List<View> mViewList;

        public ViewPagerAdapter(List<View> views) {
            if (mViewPagerList.size() < VIEW_PAGER_NUM) {
                generateViewPagerData();
            }

            mViewList = new ArrayList<View>();
            if (mViewPagerList.size() >= VIEW_PAGER_NUM) {
                for (int i = 0; i < VIEW_PAGER_NUM; i++) {
                    final Video video = mViewPagerList.get(i);

                    View view = mLayoutInflater.inflate(R.layout.viewpager_item, null);
                    ImageView imageView = (ImageView) view.findViewById(R.id.view_pager_image);

                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int pattern = GvrVideoActivity.PATTERN_TYPE_2;
                            for(Video panoVideo: mStereoVideoList) {
                                if (panoVideo.getUrl().equals(video.getUrl())) {
                                    pattern = GvrVideoActivity.PATTERN_TYPE_3;
                                }
                            }

                            if (pattern == GvrVideoActivity.PATTERN_TYPE_2) {
                                startVideoDetailActivity(video, pattern, mStereoVideoList);
                            } else if (pattern == GvrVideoActivity.PATTERN_TYPE_3) {
                                startVideoDetailActivity(video, pattern, mPanoVideoList);
                            }
                        }
                    });

                    BitmapLoadManager.display(imageView, video.getPreview1Url());
                    mViewList.add(imageView);
                }
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //super.destroyItem(container, position, object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            ImageView view = (ImageView) mViewList.get(position);
            //如果View已经在之前添加到了一个父组件，则必须先remove，否则会抛出IllegalStateException。
            ViewParent vp =view.getParent();
            if (vp!=null){
                ViewGroup parent = (ViewGroup)vp;
                parent.removeView(view);
            }
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return mViewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    public class GridViewAdapter extends BaseAdapter {
        private List<Pair<Integer, Integer>> mData;

        public GridViewAdapter() {
            mData = new ArrayList<Pair<Integer, Integer>>();

            mData.add(new Pair<Integer, Integer>(R.drawable.icon_game, R.string.game));
            mData.add(new Pair<Integer, Integer>(R.drawable.icon_3d, R.string.video_3d));
            mData.add(new Pair<Integer, Integer>(R.drawable.icon_vr, R.string.vr));
            mData.add(new Pair<Integer, Integer>(R.drawable.icon_local, R.string.local_video));
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();

                convertView = View.inflate(mContext, R.layout.gridview_item, null);
                holder.mIcon = (ImageView) convertView.findViewById(R.id.grid_view_icon);
                holder.mName = (TextView) convertView.findViewById(R.id.grid_view_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            holder.mIcon.setImageResource(mData.get(position).first);
            holder.mName.setText(mData.get(position).second);
            return convertView;
        }

        class ViewHolder {
            public ImageView mIcon;
            public TextView mName;
        }
    }
}
