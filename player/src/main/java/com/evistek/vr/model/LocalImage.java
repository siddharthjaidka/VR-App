package com.evistek.vr.model;

import java.io.Serializable;

public class LocalImage implements Serializable{
    private static final long serialVersionUID = 6410562558210044978L;
    private String name;
    private String path;
    private long id;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
