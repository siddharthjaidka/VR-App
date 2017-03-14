package com.evistek.vr.net.callback;

import com.evistek.vr.net.json.JsonRespUser;

public abstract class UserNameCallBack {
    public abstract void onResult(int code, JsonRespUser JsonResp);
}
