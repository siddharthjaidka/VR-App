package com.evistek.vr.net.callback;

import com.evistek.vr.net.json.JsonRespImage;

public abstract class ImageCallback {
	public abstract void onResult( int code,JsonRespImage JsonResp);
}
