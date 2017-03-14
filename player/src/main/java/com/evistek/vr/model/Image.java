package com.evistek.vr.model;

import java.util.Date;

public class Image extends ContentInfo {
    private Integer contentId;

    private String format;

    public Integer getContentId() {
        return contentId;
    }

    public void setContentId(Integer contentId) {
        this.contentId = contentId;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format == null ? null : format.trim();
    }

    /**
     * 无参构造
     */
    public Image() {
    }

    /**
     * 有参构造
     */

    public Image(Integer contentId, String format) {
        super();
        this.contentId = contentId;
        this.format = format;
    }

    public Image(String format, String contentName, String contentType, Integer categoryId, Integer source,
            Date createTime, Integer auditStatus, Date updateTime, String url, String coverUrl, Integer userId,
            Integer downloadCount, Integer height, Integer width, Integer size) {
        super(contentName, contentType, categoryId, source, createTime, auditStatus, updateTime, url, coverUrl, userId,
                downloadCount, height, width, size);
        this.format = format;
    }

    public Image(Integer contentId, String format, String contentName, String contentType, Integer categoryId,
            Integer source, Date createTime, Integer auditStatus, Date updateTime, String url, String coverUrl,
            Integer userId, Integer downloadCount, Integer height, Integer width, Integer size) {
        super(contentName, contentType, categoryId, source, createTime, auditStatus, updateTime, url, coverUrl, userId,
                downloadCount, height, width, size);
        this.contentId = contentId;
        this.format = format;
    }
}