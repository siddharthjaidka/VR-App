package com.evistek.vr.net.json;

import java.util.ArrayList;

import com.evistek.vr.model.ContentInfo;

public class JsonRespContent extends JsonRespBase {
    private ArrayList<ContentInfo> contentList;

    public ArrayList<ContentInfo> getContentList() {
        return contentList;
    }

    public void setContentList(ArrayList<ContentInfo> contentList) {
        this.contentList = contentList;
    }

}
