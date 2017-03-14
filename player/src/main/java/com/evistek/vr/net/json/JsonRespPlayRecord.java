package com.evistek.vr.net.json;

import com.evistek.vr.model.PlayRecord;

import java.util.List;


public class JsonRespPlayRecord extends JsonRespBase {

    private List<PlayRecord> results;

    public List<PlayRecord> getResults() {
        return results;
    }

    public void setResults(List<PlayRecord> results) {
        this.results = results;
    }

}
