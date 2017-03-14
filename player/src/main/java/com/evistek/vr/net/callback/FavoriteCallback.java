package com.evistek.vr.net.callback;

import com.evistek.vr.net.json.JsonRespFavorite;

/**
 * Created by evis on 2016/8/11.
 */

public abstract class FavoriteCallback {
    public abstract void onResult(int code, JsonRespFavorite jsonResp);
}
