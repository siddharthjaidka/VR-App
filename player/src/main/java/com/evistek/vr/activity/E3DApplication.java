package com.evistek.vr.activity;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.evistek.vr.net.Config;
import com.evistek.vr.service.DownloadService;
import com.evistek.vr.user.User;

import org.xutils.x;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class E3DApplication extends Application {
    private static final String TAG = "E3DApplication";
    private static final String INTENT_START_SERVICE = "com.evistek.vr.ACTION_SERVICE";
    private static E3DApplication instance;
    private List<Activity> mActivityList = new LinkedList<Activity>();
    private DownloadService mDownloadService;
    private boolean mBind;
    private Object mDownloadLock;
    private boolean mVersionLimited = false;
    private User mUser;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        mDownloadLock = new Object();
        mBind = false;
        mUser = new User();
        x.Ext.init(this);

        File cacheDir = new File(Config.DISK_CACHE_PATH);
        if (!cacheDir.exists()) {
            cacheDir.mkdir();
            if (!cacheDir.exists()) {
                Log.e(TAG, "Failed to make dir: " + Config.DISK_CACHE_PATH);
            }
        }

        if (!mBind) {
            bindDownloadService();
        }
    }

    public static E3DApplication getInstance() {
        return instance;
    }

    public DownloadService getDownloadService() {
        synchronized (mDownloadLock) {
            try {
                if (!mBind) {
                    mDownloadLock.wait(500);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return mDownloadService;
        }
    }

    public void addActivity(Activity activity) {
        mActivityList.add(activity);
    }

    public void setVersionLimited(boolean versionLimited) {
        mVersionLimited = versionLimited;
    }

    public boolean getVersionLimited() {
        return mVersionLimited;
    }

    public User getUser() {
        return mUser;
    }

    public void exit() {
        for (Activity activity : mActivityList) {
            activity.finish();
        }

        unbindDownloadService();
        System.exit(0);
    }

    private ServiceConnection mConn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (mDownloadLock) {
                DownloadService.DownloadServiceBinder binder = (DownloadService.DownloadServiceBinder) service;
                mDownloadService = binder.getService();
                mBind = true;
                mDownloadLock.notifyAll();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "Download Service Disconnected!");
            mBind = false;
        }
    };

    private void bindDownloadService() {
        Intent intent = new Intent();
        intent.setAction(INTENT_START_SERVICE);
        intent.setPackage(getPackageName());
        bindService(intent, mConn, Context.BIND_AUTO_CREATE);
    }

    private void unbindDownloadService() {
        if (mBind) {
            mDownloadService.cancelCurrentTask();
            mDownloadService.removeDownloadNotify();
            mDownloadService.saveTask();
            unbindService(mConn);
            mBind = false;
        }
    }
}
