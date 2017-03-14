package com.evistek.vr.net.callback;

import com.evistek.vr.net.json.JsonRespContent;

public abstract class ContentCallBack {
    public abstract void onResult(int code, JsonRespContent JsonResp);
}
