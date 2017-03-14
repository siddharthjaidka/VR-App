package com.evistek.vr.model;

import java.io.Serializable;
import java.util.Date;

public class Video extends ContentInfo implements Serializable {

    private static final long serialVersionUID = -7577396294341529027L;

    private Integer contentId;

    private String format;

    private Integer duration;

    private String actors;

    private String location;

    private Date date;

    private String introduction;

    private String preview1Url;

    private String preview2Url;

    private String preview3Url;

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

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getActors() {
        return actors;
    }

    public void setActors(String actors) {
        this.actors = actors;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getPreview1Url() {
        return preview1Url;
    }

    public void setPreview1Url(String preview1Url) {
        this.preview1Url = preview1Url;
    }

    public String getPreview2Url() {
        return preview2Url;
    }

    public void setPreview2Url(String preview2Url) {
        this.preview2Url = preview2Url;
    }

    public String getPreview3Url() {
        return preview3Url;
    }

    public void setPreview3Url(String preview3Url) {
        this.preview3Url = preview3Url;
    }

    public Video() {
    }

    public Video(Integer contentId, String format, Integer duration, String actors, String location, Date date, String introduction) {
        super();
        this.contentId = contentId;
        this.format = format;
        this.duration = duration;
        this.actors = actors;
        this.location = location;
        this.date = date;
        this.introduction = introduction;
    }

    public Video(String format, Integer duration, String actors, String location, Date date, String introduction, String contentName, String contentType, Integer categoryId,
            Integer source, Date createTime, Integer auditStatus, Date updateTime, String url, String coverUrl,
            Integer userId, Integer downloadCount, Integer height, Integer width, Integer size) {
        super(contentName, contentType, categoryId, source, createTime, auditStatus, updateTime, url, coverUrl, userId,
                downloadCount, height, width, size);
        this.format = format;
        this.duration = duration;
        this.actors = actors;
        this.location = location;
        this.date = date;
        this.introduction = introduction;
    }

    public Video(Integer contentId, String format, Integer duration, String actors, String location, Date date, String introduction, String contentName, String contentType,
            Integer categoryId, Integer source, Date createTime, Integer auditStatus, Date updateTime, String url,
            String coverUrl, Integer userId, Integer downloadCount, Integer height, Integer width, Integer size) {
        super(contentName, contentType, categoryId, source, createTime, auditStatus, updateTime, url, coverUrl, userId,
                downloadCount, height, width, size);
        this.contentId = contentId;
        this.format = format;
        this.duration = duration;
        this.actors = actors;
        this.location = location;
        this.date = date;
        this.introduction = introduction;
    }
}