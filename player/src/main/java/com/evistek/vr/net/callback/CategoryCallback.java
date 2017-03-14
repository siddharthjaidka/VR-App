package com.evistek.vr.net.callback;

import com.evistek.vr.net.json.JsonRespCategory;

public abstract class CategoryCallback {
	public abstract void onResult( int code,JsonRespCategory JsonResp);
}
