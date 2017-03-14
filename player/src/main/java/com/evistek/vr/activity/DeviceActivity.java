package com.evistek.vr.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.evistek.vr.R;
import com.evistek.vr.activity.adapter.DeviceAdapter;
import com.evistek.vr.model.UserInfo;
import com.evistek.vr.net.NetWorkService;
import com.evistek.vr.net.callback.UserCallBack;
import com.evistek.vr.user.User;

public class DeviceActivity extends Activity {
    private static final String TAG = "DeviceActivity";
    private RecyclerView mRecyclerView;
    private ImageView mBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        mRecyclerView = (RecyclerView) findViewById(R.id.v2_user_device_recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new DeviceAdapter(this));

        mBack = (ImageView) findViewById(R.id.v2_user_device_back);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        updateUserInfo();
        super.onBackPressed();
    }

    private void updateUserInfo() {
        User user = E3DApplication.getInstance().getUser();
        if (user.isLogin) {

            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(user.id);
            userInfo.setVrDevice(user.vrDevice);

            NetWorkService.updateUserInfo(userInfo, new UserCallBack() {
                @Override
                public void onResult(int code, String msg) {
                    if (code != 200) {
                        Log.e(TAG, "code: " + code + " msg: " + msg);
                    }
                }
            });
        }
    }
}
