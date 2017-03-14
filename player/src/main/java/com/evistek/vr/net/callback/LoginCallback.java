package com.evistek.vr.net.callback;

import com.evistek.vr.net.json.JsonRespUser;

public abstract class LoginCallback {
	public abstract void onResult(int code, String msg,JsonRespUser respLogin);
}
