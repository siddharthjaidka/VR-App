package com.evistek.vr.model;

public class DeviceInfo{
    private String deviceid;

    private String deviceModel;

    private String system;

    private String location;

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid == null ? null : deviceid.trim();
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel == null ? null : deviceModel.trim();
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system == null ? null : system.trim();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location == null ? null : location.trim();
    }

    /**
     * 无参构造
     */
    public DeviceInfo() {
    }

    /**
     * 有参构造
     */

    public DeviceInfo(String deviceid, String deviceModel, String system, String location) {
        super();
        this.deviceid = deviceid;
        this.deviceModel = deviceModel;
        this.system = system;
        this.location = location;
    }
    @Override
    public String toString() {
        return "DeviceInfo [deviceid=" +deviceid+", deviceModel=" +deviceModel+ ",system=" +system+ ",location=" +location+ "]";
    }
}