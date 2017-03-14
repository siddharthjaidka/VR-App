package com.evistek.vr.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.evistek.vr.R;
import com.evistek.vr.activity.fragment.AboutFragment;
import com.evistek.vr.activity.fragment.ApplicationFragment;
import com.evistek.vr.activity.fragment.HomeFragment;
import com.evistek.vr.activity.fragment.VideoFragment;
import com.evistek.vr.activity.fragment.VrOnlineFragment;
import com.evistek.vr.utils.Utils;

public class MainActivity extends FragmentActivity
        implements TabHost.OnTabChangeListener,
        HomeFragment.OnHomeFragmentInteractionListener,
        VideoFragment.OnFragmentInteractionListener,
        VrOnlineFragment.OnFragmentInteractionListener,
        ApplicationFragment.OnFragmentInteractionListener,
        AboutFragment.OnFragmentInteractionListener
{
    private static final int INDEX_HOME_FRAGMENT = 0;
    private static final int INDEX_VIDEO_FRAGMENT = 1;
    private static final int INDEX_APP_FRAGMENT = 3;
    private static final int INDEX_ABOUT_FRAGMENT = 4;

    private static final Class[] mFragmentArray = {
            HomeFragment.class,
            VideoFragment.class,
            VrOnlineFragment.class,
            ApplicationFragment.class,
            AboutFragment.class
    };

    private static final int[] mTabNameResourceIds = {
            R.string.tabname_home,
            R.string.tabname_video,
            R.string.tabname_vr_online,
            R.string.tabname_app,
            R.string.tabname_about
    };

    private static final int[] mTabIconResourceIds = {
            R.drawable.tab_home_selector,
            R.drawable.tab_video_selector,
            R.drawable.tab_vrol_selector,
            R.drawable.tab_game_selector,
            R.drawable.tab_about_selector
    };

    private Context mContext;
    private Toolbar mToolbar;
    private FragmentTabHost mTabHost;
    private TextView mToolbarTitle;
    private static final long WAIT_TIME = 2000;
    private long mClickTime = 0;
    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mContext = this;
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbarTitle = (TextView) findViewById(R.id.toolbar_title);

        initTabs();
        checkVrDevice();
    }

    public void hideToolbar(){
        if (mToolbar != null) {
            //mToolbar.setVisibility(View.GONE);
        }
    }

    public void showToolbar(){
        if (mToolbar != null) {
           // mToolbar.setVisibility(View.VISIBLE);
        }
    }

    private void initTabs() {
        mTabHost = (FragmentTabHost) findViewById(R.id.tab_host);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.container);
        mTabHost.setOnTabChangedListener(this);
        mTabHost.getTabWidget().setDividerDrawable(null);

        int count = mFragmentArray.length;
        for (int i = 0; i < count; i++) {
            mTabHost.addTab(mTabHost.newTabSpec(getTabItemName(i)).setIndicator(getTabItemView(i)), mFragmentArray[i], null);
        }

    }

    private String getTabItemName(int index) {
        return getResources().getString(mTabNameResourceIds[index]);
    }

    private View getTabItemView(int index) {
        LayoutInflater inflater = LayoutInflater.from(this);

        View view = inflater.inflate(R.layout.tab_item, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.tab_icon);
        imageView.setImageResource(mTabIconResourceIds[index]);

        TextView textView = (TextView) view.findViewById(R.id.tab_name);
        textView.setText(mTabNameResourceIds[index]);

        return view;
    }

    private void checkVrDevice() {
        String device = Utils.getValue(Utils.SHARED_VR_DEVICE, null);
        if (device == null) {
            showAlertDialog();
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.v2_need_device_msg)
                .setTitle(R.string.v2_need_device_title);
        builder.setPositiveButton(R.string.v2_go_setting, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(mContext, DeviceActivity.class);
                mContext.startActivity(intent);
            }
        });
        builder.setNegativeButton(R.string.v2_continue, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onTabChanged(String tabId) {
        TabWidget tw = mTabHost.getTabWidget();
        int count = mFragmentArray.length;
        for (int i = 0; i < count; i++) {
            View view = tw.getChildAt(i);
            if (view != null) {
                if (mTabHost.getCurrentTab() == i) {
                    mToolbarTitle.setText(mTabNameResourceIds[i]);
                }
            }
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onHomeFragmentInteraction(int position) {
        switch (position) {
            case HomeFragment.INDEX_GRIDVIEW_GAME:
                mTabHost.setCurrentTab(INDEX_APP_FRAGMENT);
                break;
            case HomeFragment.INDEX_GRIDVIEW_STERO_VIDEO:
                VideoFragment.setCurrentPage(VideoFragment.PAGE_STEREO_VIDEO);
                mTabHost.setCurrentTab(INDEX_VIDEO_FRAGMENT);
                break;
            case HomeFragment.INDEX_GRIDVIEW_PANO_VIDEO:
                VideoFragment.setCurrentPage(VideoFragment.PAGE_PANO_VIDEO);
                mTabHost.setCurrentTab(INDEX_VIDEO_FRAGMENT);
                break;
            case HomeFragment.INDEX_GRIDVIEW_LOCAL_VIDEO:
                VideoFragment.setCurrentPage(VideoFragment.PAGE_LOCAL_VIDEO);
                mTabHost.setCurrentTab(INDEX_VIDEO_FRAGMENT);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        onDoubleClickToExit();
        //super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        /* intercept keycode for media player to prevent user from launching
           system media player, e.g Music player, in this activity
          */
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
            case KeyEvent.KEYCODE_MEDIA_NEXT:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        /* intercept keycode for media player to prevent user from launching
           system media player, e.g Music player, in this activity
          */
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
            case KeyEvent.KEYCODE_MEDIA_NEXT:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    private void onDoubleClickToExit() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - mClickTime) >= WAIT_TIME) {
            mToast = Toast.makeText(this, getString(R.string.exit_toast), Toast.LENGTH_SHORT);
            mToast.show();
            mClickTime = currentTime;
        } else {
            if (mToast != null) {
                mToast.cancel();
            }
            finish();
        }
    }
}
