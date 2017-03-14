package com.evistek.vr.activity.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.evistek.vr.R;
import com.evistek.vr.activity.AboutUsActivity;
import com.evistek.vr.activity.DeviceActivity;
import com.evistek.vr.activity.E3DApplication;
import com.evistek.vr.activity.FavoriteActivity;
import com.evistek.vr.activity.HistoryActivity;
import com.evistek.vr.activity.LoginActivity;
import com.evistek.vr.activity.MainActivity;
import com.evistek.vr.view.RoundImageView;
import com.evistek.vr.user.User;
import com.evistek.vr.net.BitmapLoadManager;
import com.evistek.vr.net.NetWorkService;
import com.evistek.vr.net.UpdateManager;
import com.evistek.vr.net.callback.DeviceCallback;
import com.evistek.vr.net.callback.UserCallBack;
import com.evistek.vr.utils.Utils;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AboutFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AboutFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AboutFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TAG = "AboutFragment";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static String VERSION;
    private MainActivity mActivity;
    private Context mContext;
    private LocationClient mLocationClient;
    private AlertDialog mDialog;
    private UpdateManager mUpdateManager;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private View mRootView;
    private RoundImageView mUserIcon;
    private TextView mUserName;
    private TextView mVerson;
    private RelativeLayout mHistoryLayout;
    private RelativeLayout mSaveLayout;
    private RelativeLayout mAboutLayout;
    private RelativeLayout mCheckAndUpdateLayout;
    private RelativeLayout mDeviceLayout;
    private TextView mLocation;
    private Button mLogoutButton;
    private OnFragmentInteractionListener mListener;
    private User mUser;
    private TextView mDeviceView;
    private String mDevice;

    public AboutFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AboutFragment newInstance(String param1, String param2) {
        AboutFragment fragment = new AboutFragment();
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

        mActivity = (MainActivity) getActivity();
        mContext = mActivity;
        mActivity.hideToolbar();
        mUpdateManager = new UpdateManager(mContext);
        mUser = E3DApplication.getInstance().getUser();
        VERSION = getString(R.string.v2_current_version) + " " + Utils.getVersion(mActivity);
        getLocation();
    }

    @Override
    public void onResume() {
        super.onResume();
        mUser = E3DApplication.getInstance().getUser();
        updateViewData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_user, container, false);
        }

        initView(mRootView);
        updateViewData();

        if (null != mUser.location) {
            uploadDeviceInfo(mUser.location);
        }
        return mRootView;
    }

    private void updateViewData() {
        if (mUser.isLogin) {
            mLogoutButton.setVisibility(View.VISIBLE);
            mLocation.setText(mUser.location);
            if (mUser.source != null && mUser.source.equals(User.SOURCE_QQ)) {
                BitmapLoadManager.display(mUserIcon, mUser.headImgUrl);
            } else {
                mUserIcon.setImageResource(R.drawable.v2_user_icon);
            }

            mUserName.setText(mUser.nickname != null ? mUser.nickname : mUser.name);
        } else {
            mLogoutButton.setVisibility(View.GONE);
            mUserName.setText(R.string.v2_clicktologin);
            mLocation.setText("");
        }

        mDevice = mUser.vrDevice;
        if (mDevice == null) {
            mDeviceView.setText(R.string.v2_device_none);
        } else {
            mDeviceView.setText(mDevice);
        }

    }

    private void initView(View layoutView) {
        mUserIcon = (RoundImageView) layoutView.findViewById(R.id.v2_user_icon);
        mUserName = (TextView) layoutView.findViewById(R.id.v2_user_name);
        mVerson = (TextView) layoutView.findViewById(R.id.v2_user_checkandupdate_version);
        mHistoryLayout = (RelativeLayout) layoutView.findViewById(R.id.v2_user_history_layout);
        mSaveLayout = (RelativeLayout) layoutView.findViewById(R.id.v2_user_save_layout);
        mCheckAndUpdateLayout = (RelativeLayout) layoutView.findViewById(R.id.v2_user_checkandupdate_layout);
        mAboutLayout = (RelativeLayout) layoutView.findViewById(R.id.v2_user_about_layout);
        mDeviceLayout = (RelativeLayout) layoutView.findViewById(R.id.v2_user_device_layout);
        mLocation = (TextView) layoutView.findViewById(R.id.v2_user_location);
        mLogoutButton = (Button) layoutView.findViewById(R.id.v2_user_logout);
        mDeviceView = (TextView) layoutView.findViewById(R.id.v2_user_current_device);
        mVerson.setText(VERSION);
        mUserIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mUser.isLogin) {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    startActivity(intent);
                }
            }
        });
        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog = new AlertDialog.Builder(mActivity)
                        .setTitle(R.string.v2_clicktologout)
                        .setMessage(R.string.v2_logout_tips)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                addLogoutRecord(mUser.id);
                                List<String> keyList = new ArrayList<String>();
                                keyList.add(Utils.SHARED_USERNAME);
                                keyList.add(Utils.SHARED_NICKNAME);
                                keyList.add(Utils.SHARED_USERTYPE);
                                keyList.add(Utils.SHARED_USERLEVEL);
                                keyList.add(Utils.SHARED_REGISTERTIME);
                                keyList.add(Utils.SHARED_USERID);
                                keyList.add(Utils.SHARED_SOURCE);
                                keyList.add(Utils.SHARED_HEAD_IMGURL);
                                Utils.deleteValue(keyList);
                                E3DApplication.getInstance().getUser().update();
                                mLocation.setText("");
                                mUserIcon.setImageResource(R.drawable.v2_user_icon);
                                mUserName.setText(R.string.v2_clicktologin);
                                mLogoutButton.setVisibility(View.GONE);
                                mLocationClient.stop();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });
        mHistoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUser.isLogin) {
                    Intent intent = new Intent(mContext, HistoryActivity.class);
                    intent.putExtra("userId", mUser.id);
                    startActivity(intent);
                } else {
                    mDialog = new AlertDialog.Builder(mActivity)
                            .setTitle(R.string.not_login)
                            .setMessage(R.string.need_login)
                            .setPositiveButton(R.string.ok,null)
                           .show();
//                    Toast.makeText(mActivity, R.string.PleaseLogin, Toast.LENGTH_SHORT).show();
                }
            }
        });
        mSaveLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUser.isLogin) {
                    Intent intent = new Intent(mContext, FavoriteActivity.class);
                    intent.putExtra("userId", mUser.id);
                    startActivity(intent);
                } else {
                    mDialog = new AlertDialog.Builder(mActivity)
                            .setTitle(R.string.not_login)
                            .setMessage(R.string.need_login)
                            .setPositiveButton(R.string.ok,null)
                            .show();
//                    Toast.makeText(mActivity, R.string.PleaseLogin, Toast.LENGTH_SHORT).show();
                }
            }
        });
        mAboutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, AboutUsActivity.class);
                startActivity(intent);
            }
        });
        mCheckAndUpdateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUpdateManager.checkUpdate(true);
            }
        });

        mDeviceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, DeviceActivity.class);
                startActivity(intent);
            }
        });
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

    @Override
    public void onStop() {
        if (mLocationClient != null) {
            mLocationClient.stop();
        }
        super.onStop();
    }

    public void addLogoutRecord (int userId) {
        //1：login ; 0:logout
        NetWorkService.addLoginRecord(userId, 0, new UserCallBack() {

            @Override
            public void onResult(int code, String msg) {
                if (code == 200) {
                    Log.i(TAG, "add loginRecord successfully");
                } else if (code == 400) {
                    Log.i(TAG, "failed to add loginRecord");
                }
            }
        });
    }

    private void uploadDeviceInfo(String myLocation) {
        String deviceId = Utils.getDeviceId(mContext);
        String deviceModel = Utils.getDeviceModel();
        String system = Utils.getDeviceSystem();
        Log.e(TAG, deviceId);
        Log.e(TAG, deviceModel);
        Log.e(TAG, system);
        if (deviceId != null && deviceModel != null && system != null && myLocation != null) {
            NetWorkService.uploadDeviceInfo(deviceId, deviceModel, system, myLocation, new DeviceCallback() {
                @Override
                public void onResult(int code, String msg) {
                }
            });
        }
    }

    private void getLocation() {
        mLocationClient = new LocationClient(mContext);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);
        option.setCoorType("bd09ll");
        option.disableCache(true);
        option.setAddrType("all");
        option.setProdName("E3DGarllery");
        mLocationClient.setLocOption(option);
        mLocationClient.registerLocationListener(new BDLocationListener() {

            @Override
            public void onReceiveLocation(BDLocation arg0) {
                String myLocation = arg0.getCity();
                Utils.saveValue(Utils.SHARED_LOCATION, myLocation);
            }
        });
        mLocationClient.start();
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
}
