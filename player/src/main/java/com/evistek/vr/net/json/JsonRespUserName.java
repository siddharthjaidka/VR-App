package com.evistek.vr.net.json;

import java.util.ArrayList;

import com.evistek.vr.model.UserInfo;

public class JsonRespUserName extends JsonRespBase {
    private ArrayList<UserInfo> userList;

    public ArrayList<UserInfo> getUserList() {
        return userList;
    }

    public void setUserList(ArrayList<UserInfo> userList) {
        this.userList = userList;
    }
}
