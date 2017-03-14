package com.evistek.vr.net.callback;

import com.evistek.vr.net.json.JsonRespContentComment;

public abstract class ContentCommentCallback {
	public abstract void onResult( int code,JsonRespContentComment JsonResp);
}
