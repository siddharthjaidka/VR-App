package com.evistek.vr.activity.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.evistek.vr.R;
import com.evistek.vr.activity.adapter.VrOnlineAdapter;
import com.evistek.vr.model.Category;
import com.evistek.vr.model.VrOnline;
import com.evistek.vr.net.Config;
import com.evistek.vr.net.NetWorkService;
import com.evistek.vr.net.callback.CategoryCallback;
import com.evistek.vr.net.callback.VrOnlineCallback;
import com.evistek.vr.net.json.JsonRespCategory;
import com.evistek.vr.net.json.JsonRespVrOnline;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VrOnlineFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link VrOnlineFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VrOnlineFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String CATEGORY_VRONLINE = "VR在线实景";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Context mContext;
    private View mRootView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private VrOnlineAdapter mAdapter;
    private ArrayList<VrOnline> mVrOnlineList = new ArrayList<>();
    private ProgressBar mProgressBar;

    private OnFragmentInteractionListener mListener;

    List<Category> mCategoryList = new ArrayList<Category>();
    private int mCategoryId = -1;

    private static final int MSG_REFRESH_DATA = 0;
    private static final int MSG_REFRESH_DATA_DONE = 1;
    private static final int MSG_FETCH_LIST = 2;
    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_DATA:
                    fetchData();
                    break;
                case MSG_REFRESH_DATA_DONE:
                    mAdapter.setVrOnlineList(mVrOnlineList);
                    mAdapter.notifyDataSetChanged();
                    if (mSwipeRefreshLayout != null) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                    if (mProgressBar != null) {
                        mProgressBar.setVisibility(View.INVISIBLE);
                    }
                    break;
                case MSG_FETCH_LIST:
                    fetchData();
                    break;
            }

            return true;
        }
    });


    public VrOnlineFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VrOnlineFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VrOnlineFragment newInstance(String param1, String param2) {
        VrOnlineFragment fragment = new VrOnlineFragment();
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

        mContext = getContext();
        mAdapter = new VrOnlineAdapter(mContext);

        fetchCategory(CATEGORY_VRONLINE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_vr_online, container, false);
        }
        mSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.vronline_swipe_refresh_layout);
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

        RecyclerView recyclerView = (RecyclerView) mRootView.findViewById(R.id.vronline_recyclerview);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 2);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (mAdapter.getItemViewType(position) == VrOnlineAdapter.ITEM_TYPE_END_TAG) {
                    return VrOnlineAdapter.END_TAG_SPAN_SIZE;
                }
                return VrOnlineAdapter.ITEM_SPAN_SIZE;
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator() {
            @Override
            public boolean animateChange (RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromX, int fromY, int toX, int toY){
                return false;
            }
        });

        mProgressBar = (ProgressBar) mRootView.findViewById(R.id.vronline_progress_bar);
        return mRootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        sendMessage(MSG_FETCH_LIST);
    }

    @Override
    public void onPause() {
        mSwipeRefreshLayout.clearAnimation();
        super.onPause();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void fetchCategory (final String categoryName) {
        String contentType = "webview";
        NetWorkService.getVRCategoryList(contentType, new CategoryCallback() {
            @Override
            public void onResult(int code, JsonRespCategory JsonResp) {
                if (code == 200) {
                    mCategoryList = JsonResp.getResults();
                    for (Category c: mCategoryList) {
                        if (c.getCategoryName().equals(categoryName)) {
                            mCategoryId = c.getCategoryId().intValue();
                            sendMessage(MSG_FETCH_LIST);
                            break;
                        }
                    }
                }
            }
        });


    }

    private void fetchData() {
        if (mCategoryId != -1) {
            NetWorkService.getVrOnline(Config.FIRST_PAGE, Config.PAGE_SIZE, mCategoryId, new VrOnlineCallback() {
                @Override
                public void onResult(int code, JsonRespVrOnline JsonResp) {
                    if (code == 200) {
                        mVrOnlineList = JsonResp.getResults();
                        sendMessage(MSG_REFRESH_DATA_DONE);
                    }
                }
            });
        }
    }

    private void sendMessage(int what) {
        Message msg = mHandler.obtainMessage();
        msg.what = what;
        mHandler.sendMessage(msg);
    }
}
