package com.evistek.vr.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.evistek.vr.R;
import com.evistek.vr.utils.Utils;

public class AboutUsActivity extends Activity {

    private ImageView mBack;
    private TextView mTVversion;
    private String version;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        mBack = (ImageView) findViewById(R.id.v2_user_aboutus_backbt);
        mTVversion = (TextView) findViewById(R.id.tv_version);
        version = Utils.getVersion(getApplicationContext());
        mTVversion.setText(version);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
