package com.evistek.vr.user;

import com.evistek.vr.model.Favorite;
import com.evistek.vr.net.NetWorkService;
import com.evistek.vr.net.callback.FavoriteCallback;
import com.evistek.vr.net.json.JsonRespFavorite;
import com.evistek.vr.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by evis on 2016/8/17.
 */
public class User {
    public static final String SOURCE_QQ = "qq";
    public String name;
    public String nickname;
    public String location;
    public String registerTime;
    public int id;
    public int type;
    public String source;
    public String headImgUrl;
    public boolean isLogin;
    public String vrDevice;
    public List<Favorite> favorites = new ArrayList<>();

    public User() {
        update();
    }

    public void update() {
        name = Utils.getValue(Utils.SHARED_USERNAME, null);
        isLogin = (name == null) ? false : true;
        nickname = Utils.getValue(Utils.SHARED_NICKNAME, null);
        location = Utils.getValue(Utils.SHARED_LOCATION, null);
        registerTime = Utils.getValue(Utils.SHARED_REGISTERTIME, null);
        id = Utils.getValue(Utils.SHARED_USERID, 0);
        type = Utils.getValue(Utils.SHARED_USERTYPE, 0);
        source = Utils.getValue(Utils.SHARED_SOURCE, null);
        headImgUrl = Utils.getValue(Utils.SHARED_HEAD_IMGURL, null);
        vrDevice = Utils.getValue(Utils.SHARED_VR_DEVICE, null);

        getFavorite();
    }

    private void getFavorite() {
        if (isLogin) {
            NetWorkService.getFavoriteByUserId(id, new FavoriteCallback() {
                @Override
                public void onResult(int code, JsonRespFavorite jsonResp) {
                    if (code == 200) {
                        favorites = jsonResp.getResults();
                    }
                }
            });
        }
    }
}
