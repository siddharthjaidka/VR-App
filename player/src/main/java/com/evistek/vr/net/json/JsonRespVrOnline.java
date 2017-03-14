package com.evistek.vr.net.json;

import com.evistek.vr.model.VrOnline;

import java.util.ArrayList;


public class JsonRespVrOnline extends JsonRespBase {
    private int pageNo;
    private int pageSize;
    private ArrayList<VrOnline> results;
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

    public ArrayList<VrOnline> getResults() {
        return results;
    }

    public void setResults(ArrayList<VrOnline> results) {
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
