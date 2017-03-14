package com.evistek.vr.net.callback;

import com.evistek.vr.net.json.JsonRespVideo;

public abstract class VideoCallback {
	public abstract void onResult( int code,JsonRespVideo JsonResp);
}
