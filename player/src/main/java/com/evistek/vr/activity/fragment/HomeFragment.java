package com.evistek.vr.activity.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.evistek.vr.R;
import com.evistek.vr.activity.adapter.ErrorAdapter;
import com.evistek.vr.activity.adapter.HomeAdapter;
import com.evistek.vr.activity.MainActivity;
import com.evistek.vr.model.Application;
import com.evistek.vr.model.Category;
import com.evistek.vr.model.Video;
import com.evistek.vr.model.VrOnline;
import com.evistek.vr.net.Config;
import com.evistek.vr.net.NetWorkService;
import com.evistek.vr.net.callback.ApplicationCallback;
import com.evistek.vr.net.callback.CategoryCallback;
import com.evistek.vr.net.callback.VideoCallback;
import com.evistek.vr.net.callback.VrOnlineCallback;
import com.evistek.vr.net.json.JsonRespApplication;
import com.evistek.vr.net.json.JsonRespCategory;
import com.evistek.vr.net.json.JsonRespVideo;
import com.evistek.vr.net.json.JsonRespVrOnline;
import com.evistek.vr.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnHomeFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment{
    private static final String TAG = "HomeFragment";
    private static final String CATEGORY_VR = "VR全景";
    private static final String CATEGORY_MOVIE = "电影";
    private static final String CATEGORY_GAME = "VR游戏";
    private static final String CATEGORY_VRONLINE = "VR在线实景";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final int MSG_GET_CATEGORY_DONE = 0;
    private static final int MSG_GET_STEREO_VIDEO_DONE = 1;
    private static final int MSG_GET_PANO_VIDEO_DONE = 2;
    private static final int MSG_GET_GAME_DONE = 3;
    private static final int MSG_GET_VR_ONLINE_DONE = 4;
    private static final int MSG_REFRESH_DATA = 5;

    // This should be the same with the GridView item in HomeAdapter.
    public static final int INDEX_GRIDVIEW_GAME = 0;
    public static final int INDEX_GRIDVIEW_STERO_VIDEO = 1;
    public static final int INDEX_GRIDVIEW_PANO_VIDEO = 2;
    public static final int INDEX_GRIDVIEW_LOCAL_VIDEO = 3;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private MainActivity mActivity;

    private Context mContext;
    private OnHomeFragmentInteractionListener mListener;
    private View mRootView;
    private RecyclerView mRecyclerView;
    private HomeAdapter mHomeAdapter;
    private GridLayoutManager mGridLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ErrorAdapter mErrorAdapter;

    private List<Category> mCategoryList = new ArrayList<Category>();
    private static ArrayList<Video> mStereoVideoList = new ArrayList<Video>();
    private static ArrayList<Video> mPanoVideoList = new ArrayList<Video>();
    private ArrayList<Application> mGameList = new ArrayList<Application>();
    private ArrayList<VrOnline> mVrOnlineList = new ArrayList<>();

    private boolean mIsStereoVideoGot = false;
    private boolean mIsPanoVideoGot = false;
    private boolean mIsGameGot = false;
    private boolean mIsVrOnlineGot = false;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GET_CATEGORY_DONE:
                    fetchVrOnlineList(CATEGORY_VRONLINE);
                    fetchVideoList(CATEGORY_MOVIE);
                    fetchVideoList(CATEGORY_VR);
                    break;
                case MSG_GET_STEREO_VIDEO_DONE:
                    mIsStereoVideoGot = true;
                    break;
                case MSG_GET_PANO_VIDEO_DONE:
                    mIsPanoVideoGot = true;
                    break;
                case MSG_GET_GAME_DONE:
                    mIsGameGot = true;
                    break;
                case MSG_GET_VR_ONLINE_DONE:
                    mIsVrOnlineGot = true;
                    break;
                case MSG_REFRESH_DATA:
                    if(Utils.isNetworkAvailable()) {
                        mRecyclerView.setAdapter(mHomeAdapter);
                        fetchData();
                    } else {
                        mRecyclerView.setAdapter(mErrorAdapter);
                        if (mSwipeRefreshLayout != null) {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }

                    break;
            }

            if (mIsStereoVideoGot && mIsPanoVideoGot && mIsGameGot && mIsVrOnlineGot) {
                mHomeAdapter.setStereoVideoList(mStereoVideoList);
                mHomeAdapter.setPanoVideoList(mPanoVideoList);
                mHomeAdapter.setGameList(mGameList);
                mHomeAdapter.setVrOnlineList(mVrOnlineList);
                mHomeAdapter.notifyDataSetChanged();
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }

                mIsStereoVideoGot = false;
                mIsPanoVideoGot = false;
                mIsGameGot = false;
                mIsVrOnlineGot = false;
            }

            return false;
        }
    });

    private HomeAdapter.OnGridViewClickListener mOnGridViewClickListener = new HomeAdapter.OnGridViewClickListener() {
        @Override
        public void onGridViewClick(int position) {
            if (mListener != null) {
                mListener.onHomeFragmentInteraction(position);
            }
        }
    };

    public HomeFragment() {
        mContext = getContext();
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mIsGameGot = false;
        mIsPanoVideoGot = false;
        mIsStereoVideoGot = false;

        mHomeAdapter = new HomeAdapter(getContext());
        mHomeAdapter.setOnGridViewClickListener(mOnGridViewClickListener);

        mErrorAdapter = new ErrorAdapter(getContext());

        mActivity =(MainActivity) getActivity();
        mActivity.showToolbar();

        fetchData();
    }

    @Override
    public void onPause() {
        mSwipeRefreshLayout.clearAnimation();
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_home, container, false);
        }
        mSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                sendMessage(MSG_REFRESH_DATA);
            }
        });

        // This is a workaround to avoid that circle indicator doesn't hide when switch
        // between fragments
        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(true);
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mSwipeRefreshLayout.setRefreshing(false);
        }

        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.home_recyclerview);

        mGridLayoutManager = new GridLayoutManager(mContext, HomeAdapter.TOTAL_SPAN_SIZE);
        mGridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int size = 0;
                switch(mHomeAdapter.getItemViewType(position)) {
                    case HomeAdapter.ITEM_TYPE_VIEW_PAGER:
                        size = HomeAdapter.SPAN_SIZE_VIEW_PAGER;
                        break;
                    case HomeAdapter.ITEM_TYPE_GRID_VIEW:
                        size = HomeAdapter.SPAN_SIZE_GRID_VIEW;
                        break;
                    case HomeAdapter.ITEM_TYPE_VIEW_TAG:
                        size = HomeAdapter.SPAN_SIZE_VIEW_TAG;
                        break;
                    case HomeAdapter.ITEM_TYPE_LIST_ITEM_VIDEO:
                        size = HomeAdapter.SPAN_SIZE_LIST_ITEM_VIDEO;
                        break;
                    case HomeAdapter.ITEM_TYPE_LIST_ITEM_GAME:
                        size = HomeAdapter.SPAN_SIZE_LIST_ITEM_GAME;
                        break;
                    case HomeAdapter.ITEM_TYPE_LIST_ITEM_VR_ONLINE:
                        size = HomeAdapter.SPAN_SIZE_LIST_ITEM_VR_ONLINE;
                        break;
                    case HomeAdapter.ITEM_INDEX_END_TAG:
                        size = HomeAdapter.SPAN_SIZE_VIEW_TAG;
                        break;
                    default:
                        size = HomeAdapter.TOTAL_SPAN_SIZE;
                }

                return size;
            }
        });
        mRecyclerView.setLayoutManager(mGridLayoutManager);

        if (Utils.isNetworkAvailable()) {
            mRecyclerView.setAdapter(mHomeAdapter);
        } else {
            mRecyclerView.setAdapter(mErrorAdapter);
        }

        mActivity.showToolbar();

        // To avoid refresh when switch back to this fragment
        ViewGroup parent = (ViewGroup) mRootView.getParent();
        if (parent != null) {
            parent.removeView(mRootView);
        }
        return mRootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnHomeFragmentInteractionListener) {
            mListener = (OnHomeFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public static List<Video> getStereoVideoList() {
        return mStereoVideoList;
    }

    public static List<Video> getPanoVideoList() {
        return mPanoVideoList;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnHomeFragmentInteractionListener {
        void onHomeFragmentInteraction(int position);
    }

    private void filterCategory(List<Category> list) {
        for(Category c: list) {
            if (c.getCategoryName().equals(CATEGORY_VR) ||
                    c.getCategoryName().equals(CATEGORY_MOVIE) ||
                    c.getCategoryName().equals(CATEGORY_GAME) ||
                    c.getCategoryName().equals(CATEGORY_VRONLINE)) {
                mCategoryList.add(c);
            }
        }
    }

    private void fetchCategoryList() {
        String contentType = "all";

        NetWorkService.getCateGoryList(Config.FIRST_PAGE, Config.PAGE_SIZE, contentType, new CategoryCallback() {
            @Override
            public void onResult(int code, JsonRespCategory JsonResp) {
                if (code == 200) {
                    filterCategory(JsonResp.getResults());
                    sendMessage(MSG_GET_CATEGORY_DONE);
                } else {
                    Log.e(TAG, "Failed to fetch category list, code: " + code);
                }
            }
        });

    }

    private void fetchVideoList(final String categoryName) {
        String contentType = "video";
        int pageSize = 6;
        int categoryId = -1;

        for (Category c: mCategoryList) {
            if (c.getCategoryName().equals(categoryName)) {
                categoryId = c.getCategoryId().intValue();
                break;
            }
        }

        if (categoryId != -1) {
            NetWorkService.getVideoList(Config.FIRST_PAGE, Config.PAGE_SIZE, contentType, categoryId, new VideoCallback() {
                @Override
                public void onResult(int code, JsonRespVideo JsonResp) {
                    if (code == 200) {
                        switch (categoryName) {
                            case CATEGORY_MOVIE:
                                mStereoVideoList = JsonResp.getResults();
                                sendMessage(MSG_GET_STEREO_VIDEO_DONE);
                                break;
                            case CATEGORY_VR:
                                mPanoVideoList = JsonResp.getResults();
                                sendMessage(MSG_GET_PANO_VIDEO_DONE);
                                break;
                        }
                    } else {
                        Log.e(TAG, "Failed to fetch video list, code: " + code);
                    }
                }
            });
        }
    }

    private void fetchGameList() {
        NetWorkService.getApplicationList(Config.FIRST_PAGE, Config.PAGE_SIZE, new ApplicationCallback() {
            @Override
            public void onResult(int code, JsonRespApplication JsonResp) {
                if (code == 200) {
                    mGameList = JsonResp.getResults();
                    sendMessage(MSG_GET_GAME_DONE);
                } else {
                    Log.e(TAG, "Failed to fetch game list, code: " + code);
                }
            }
        });
    }

    private void fetchVrOnlineList(String categoryName) {
        String contentType = "webview";
        int categoryId = -1;

        for (Category c: mCategoryList) {
            if (c.getCategoryName().equals(categoryName)) {
                categoryId = c.getCategoryId().intValue();
                break;
            }
        }
        if (categoryId != -1) {
            NetWorkService.getVrOnline(Config.FIRST_PAGE, Config.PAGE_SIZE, categoryId, new VrOnlineCallback() {
                @Override
                public void onResult(int code, JsonRespVrOnline JsonResp) {
                    if (code == 200) {
                        mVrOnlineList = JsonResp.getResults();
                        sendMessage(MSG_GET_VR_ONLINE_DONE);
                    }
                }
            });
        }
    }

    private void fetchData() {
        fetchCategoryList();
        fetchGameList();
    }

    private void sendMessage(int what) {
        Message message = mHandler.obtainMessage();
        message.what = what;
        mHandler.sendMessage(message);
    }
}
