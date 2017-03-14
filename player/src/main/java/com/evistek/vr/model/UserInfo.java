package com.evistek.vr.model;

import java.util.Date;

/**
 * 用户对象
 */
public class UserInfo {
    private Integer userId;

    private String username;

    private String password;

    private String nickName;

    private Integer type;

    private Integer level;

    private Date registerTime;

    private String location;

    private String sex;

    private String headImgurl;

    private String source;

    private String telephone;

    private String email;

    private String phoneDevice;

    private String vrDevice;

    private String phoneSystem;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName == null ? null : nickName.trim();
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Date getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(Date registerTime) {
        this.registerTime = registerTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location == null ? null : location.trim();
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getHeadImgurl() {
        return headImgurl;
    }

    public void setHeadImgurl(String headImgurl) {
        this.headImgurl = headImgurl;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneDevice() {
        return phoneDevice;
    }

    public void setPhoneDevice(String phoneDevice) {
        this.phoneDevice = phoneDevice;
    }

    public String getVrDevice() {
        return vrDevice;
    }

    public void setVrDevice(String vrDevice) {
        this.vrDevice = vrDevice;
    }

    public String getPhoneSystem() {
        return phoneSystem;
    }

    public void setPhoneSystem(String phoneSystem) {
        this.phoneSystem = phoneSystem;
    }

    /**
     * 无参构造
     */
    public UserInfo() {
    }

    /**
     * 构造重载
     */

    public UserInfo(String username, String password, Date registerTime) {
        this.username = username;
        this.password = password;
        this.registerTime = registerTime;
    }

    public UserInfo(String username, String password, Integer type, Integer level, Date registerTime, String sex,
                    String headImgurl, String source, String telephone, String email) {
        super();
        this.username = username;
        this.password = password;
        this.type = type;
        this.level = level;
        this.registerTime = registerTime;
        this.sex = sex;
        this.headImgurl = headImgurl;
        this.source = source;
        this.telephone = telephone;
        this.email = email;
    }

    public UserInfo(String username, String password, Integer type, Integer level, Date registerTime, String location,
                    String sex, String headImgurl, String source, String telephone, String email) {
        super();
        this.username = username;
        this.password = password;
        this.type = type;
        this.level = level;
        this.registerTime = registerTime;
        this.location = location;
        this.sex = sex;
        this.headImgurl = headImgurl;
        this.source = source;
        this.telephone = telephone;
        this.email = email;
    }

    public UserInfo(String username, String password, String nickName, String location, String sex, String headImgurl,
                    String source) {
        super();
        this.username = username;
        this.password = password;
        this.nickName = nickName;
        this.location = location;
        this.sex = sex;
        this.headImgurl = headImgurl;
        this.source = source;
    }

    public UserInfo(String username, String password, String nickName, Integer type, Integer level, Date registerTime,
                    String location, String sex, String headImgurl, String source) {
        super();
        this.username = username;
        this.password = password;
        this.nickName = nickName;
        this.type = type;
        this.level = level;
        this.registerTime = registerTime;
        this.location = location;
        this.sex = sex;
        this.headImgurl = headImgurl;
        this.source = source;
    }
}