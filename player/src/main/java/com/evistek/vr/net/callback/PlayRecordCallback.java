package com.evistek.vr.net.callback;

import com.evistek.vr.net.json.JsonRespPlayRecord;

/**
 * Created by evis on 2016/8/11.
 */

public abstract class PlayRecordCallback {
    public abstract void onResult(int code, JsonRespPlayRecord jsonResp);
}
