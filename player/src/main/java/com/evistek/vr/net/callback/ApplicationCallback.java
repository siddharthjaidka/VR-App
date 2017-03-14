package com.evistek.vr.net.callback;

import com.evistek.vr.net.json.JsonRespApplication;

/**
 * Created by evis on 2016/7/29.
 */
public abstract class ApplicationCallback {
    public abstract void onResult( int code,JsonRespApplication JsonResp);
}
