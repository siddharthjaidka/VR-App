package com.evistek.vr.model;

import java.io.Serializable;

public class Category implements Serializable{

    private static final long serialVersionUID = -2874896142294755295L;

    private Integer categoryId;

    private String categoryName;

    private Integer parentId;

    private String coverUrl;

    private String contentType;

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName == null ? null : categoryName.trim();
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl == null ? null : coverUrl.trim();
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType == null ? null : contentType.trim();
    }

    /**
     * 无参构造
     */
    public Category() {
    }

    /**
     * 有参构造
     */

    public Category(String categoryName, String coverUrl, String contentType) {
        super();
        this.categoryName = categoryName;
        this.coverUrl = coverUrl;
        this.contentType = contentType;
    }

    /**
     * 重载构造
     */

    public Category(Integer categoryId, String categoryName, String coverUrl, String contentType) {
        super();
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.coverUrl = coverUrl;
        this.contentType = contentType;
    }
}