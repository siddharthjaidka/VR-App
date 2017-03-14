package com.evistek.vr.activity.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.evistek.vr.R;
import com.evistek.vr.activity.adapter.ErrorAdapter;
import com.evistek.vr.activity.adapter.StereoVideoAdapter;
import com.evistek.vr.model.Category;
import com.evistek.vr.model.Video;
import com.evistek.vr.net.Config;
import com.evistek.vr.net.NetWorkService;
import com.evistek.vr.net.callback.CategoryCallback;
import com.evistek.vr.net.callback.VideoCallback;
import com.evistek.vr.net.json.JsonRespCategory;
import com.evistek.vr.net.json.JsonRespVideo;
import com.evistek.vr.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StereoVideoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StereoVideoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StereoVideoFragment extends Fragment {
    private static final String TAG = "StereoVideoFragment";
    private static final String CATEGORY_MOVIE = "电影";

    private static final int MSG_GET_CATEGORY_DONE = 0;
    private static final int MSG_GET_STEREO_VIDEO_DONE = 1;
    private static final int MSG_REFRESH_DATA = 2;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View mRootView;
    private OnFragmentInteractionListener mListener;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private StereoVideoAdapter mAdapter;
    private ErrorAdapter mErrorAdapter;
    private ProgressBar mProgessBar;

    private List<Category> mCategoryList = new ArrayList<Category>();
    private ArrayList<Video> mStereoVideoList = new ArrayList<Video>();

    private boolean mIsStereoVideoGot = false;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GET_CATEGORY_DONE:
                    fetchVideoList(CATEGORY_MOVIE);
                    break;
                case MSG_GET_STEREO_VIDEO_DONE:
                    mIsStereoVideoGot = true;
                    break;
                case MSG_REFRESH_DATA:
                    if (Utils.isNetworkAvailable()) {
                        mRecyclerView.setAdapter(mAdapter);
                        fetchData();
                    } else {
                        mRecyclerView.setAdapter(mErrorAdapter);
                        if (mSwipeRefreshLayout != null) {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                    break;
            }

            if (mIsStereoVideoGot) {
                mAdapter.setVideoList(mStereoVideoList);
                mAdapter.notifyDataSetChanged();
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }

                if (mProgessBar != null) {
                    mProgessBar.setVisibility(View.INVISIBLE);
                }

                mIsStereoVideoGot = false;
            }

            return true;
        }
    });

    public StereoVideoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StereoVideoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StereoVideoFragment newInstance(String param1, String param2) {
        StereoVideoFragment fragment = new StereoVideoFragment();
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

        mAdapter = new StereoVideoAdapter(getContext());
        mErrorAdapter = new ErrorAdapter(getContext());

        mIsStereoVideoGot = false;

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
            mRootView = inflater.inflate(R.layout.fragment_stereo_video, container, false);
        }
        mSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.stereo_video_swipe_refresh_layout);
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

        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.stereo_video_recyclerview);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        if (Utils.isNetworkAvailable()) {
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mRecyclerView.setAdapter(mErrorAdapter);
        }

        mProgessBar = (ProgressBar) mRootView.findViewById(R.id.stereo_video_progress_bar);
        return mRootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void filterCategory(List<Category> list) {
        for(Category c: list) {
            if (c.getCategoryName().equals(CATEGORY_MOVIE)) {
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
                        }
                    } else {
                        Log.e(TAG, "Failed to fetch video list, code: " + code);
                    }
                }
            });
        }
    }

    private void fetchData() {
        fetchCategoryList();
    }

    private void sendMessage(int what) {
        Message message = mHandler.obtainMessage();
        message.what = what;
        mHandler.sendMessage(message);
    }
}
