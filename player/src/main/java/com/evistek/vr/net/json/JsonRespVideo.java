package com.evistek.vr.net.json;

import java.util.ArrayList;

import com.evistek.vr.model.Video;

//import com.evistek.mediaserver.model.Video;

public class JsonRespVideo extends JsonRespBase {
    private int pageNo;
    private int pageSize;
    private ArrayList<Video> results;
    private int totalCount;
    private int totalPage;

    public int getPageNo() {
        return pageNo;
    }
    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }
    public int getPageSize() {
        return pageSize;
    }
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    public ArrayList<Video> getResults() {
        return results;
    }
    public void setResults(ArrayList<Video> results) {
        this.results = results;
    }
    public int getTotalCount() {
        return totalCount;
    }
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
    public int getTotalPage() {
        return totalPage;
    }
    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }
}
