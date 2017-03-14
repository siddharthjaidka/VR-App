package com.evistek.vr.activity.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.astuetz.PagerSlidingTabStrip;
import com.evistek.vr.R;
import com.evistek.vr.activity.MainActivity;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VideoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link VideoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoFragment extends Fragment {

    public static final int PAGE_PANO_VIDEO = 0;
    public static final int PAGE_STEREO_VIDEO = 1;
    public static final int PAGE_LOCAL_VIDEO = 2;
    public static final int PAGE_NUMBER = 3;

    private static final int MSG_SET_PAGE = 0;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private MainActivity mActivity;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View mRootView;
    private ViewPager mViewPager;
    private VideoPagerAdapter mPagerAdapter;
    private PagerSlidingTabStrip mPagerSlidingTabStrip;
    private static int mCurrentPage;

    // mAutoSwitch is true when enter this fragment by click gridview item
    // on HomeFragment
    private static boolean mAutoSwitch;

    private OnFragmentInteractionListener mListener;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SET_PAGE:
                    mAutoSwitch = false;
                    mViewPager.setCurrentItem(mCurrentPage, true);
                    break;
            }

            return true;
        }
    });

    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (position != mCurrentPage && mAutoSwitch) {
                sendMessage(MSG_SET_PAGE);
            }
        }

        @Override
        public void onPageSelected(int position) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    public VideoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VideoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VideoFragment newInstance(String param1, String param2) {
        VideoFragment fragment = new VideoFragment();
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
        mActivity =(MainActivity) getActivity();
        mActivity.showToolbar();

        mPagerAdapter = new VideoPagerAdapter(getChildFragmentManager());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_video, container, false);
        }

        mViewPager = (ViewPager) mRootView.findViewById(R.id.video_view_pager);
        mViewPager.setAdapter(mPagerAdapter);

        mPagerSlidingTabStrip = (PagerSlidingTabStrip) mRootView.findViewById(R.id.video_tabs);
        mPagerSlidingTabStrip.setViewPager(mViewPager);
        mPagerSlidingTabStrip.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources()
                .getDisplayMetrics()));
        //mPagerSlidingTabStrip.setTextColorResource(R.color.colorPrimary);
        mPagerSlidingTabStrip.setOnPageChangeListener(mPageChangeListener);

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
            mListener.onFragmentInteraction(uri);
        }
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

    public static void setCurrentPage(int page) {
        mCurrentPage = page > PAGE_NUMBER ? PAGE_NUMBER - 1 : page;
        mAutoSwitch = true;
    }

    private void sendMessage(int what) {
        Message message = mHandler.obtainMessage();
        message.what = what;
        mHandler.sendMessage(message);
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

    private class VideoPagerAdapter extends FragmentPagerAdapter {

        private Fragment[] mFragments = new Fragment[3];

        private final String[] TITLES = {
                getString(R.string.vr),
                getString(R.string.video_3d),
                getString(R.string.local_video)
        };

        public VideoPagerAdapter(FragmentManager fm) {
            super(fm);
            PanoVideoFragment panoVideoFragment = new PanoVideoFragment();
            StereoVideoFragment stereoVideoFragment = new StereoVideoFragment();
            LocalVideoFragment localVideoFragment = new LocalVideoFragment();
            mFragments[0] = panoVideoFragment;
            mFragments[1] = stereoVideoFragment;
            mFragments[2] = localVideoFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }
    }
}
