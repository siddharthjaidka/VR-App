package com.evistek.vr.net.callback;

import com.evistek.vr.net.json.JsonRespUserName;

public abstract class UserNameListCallBack {
    public abstract void onResult(int code, JsonRespUserName JsonResp);
}
