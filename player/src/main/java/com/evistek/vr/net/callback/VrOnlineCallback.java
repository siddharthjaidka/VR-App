package com.evistek.vr.net.callback;

import com.evistek.vr.net.json.JsonRespVrOnline;

/**
 * Created by ymzhao on 2016/11/7.
 */

public abstract class VrOnlineCallback {
    public abstract void onResult(int code, JsonRespVrOnline JsonResp);
}
