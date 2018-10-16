package com.sunsan.project.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 登录信息
 * 备注：2018-08-27，陈彦名，增加（行政级别：areaLevel,电话：mobile,行政职务：areaRoles,河道职务：riverDuty）
 */
@ApiModel(description = "登录信息")

public class LoginInfo implements Serializable {
    @ApiModelProperty(value = "token")
    private String token;
    @ApiModelProperty(value = "realName")
    private String realName;
    @ApiModelProperty(value = "roles")
    private String[] roles;
    @ApiModelProperty(value = "perms")
    private String[] perms;
    @ApiModelProperty(value = "passRemainSeconds", notes = "密码剩余的有效时间，单位秒")
    private Integer passRemainSeconds;
    @ApiModelProperty(value = "areaLevel")
    private String areaLevel;
    @ApiModelProperty(value = "mobile")
    private String mobile;
    @ApiModelProperty(value = "areaRoles")
    private String areaRoles;
    @ApiModelProperty(value = "riverDuty")
    private String riverDuty;
    @ApiModelProperty(value = "userId")
    private Integer userId;


    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String[] getPerms() {
        return perms;
    }

    public void setPerms(String[] perms) {
        this.perms = perms;
    }

    public Integer getPassRemainSeconds() {
        return passRemainSeconds;
    }

    public void setPassRemainSeconds(Integer passRemainSeconds) {
        this.passRemainSeconds = passRemainSeconds;
    }

    public String getAreaLevel() {
        return areaLevel;
    }

    public void setAreaLevel(String areaLevel) {
        this.areaLevel = areaLevel;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAreaRoles() {
        return areaRoles;
    }

    public void setAreaRoles(String areaRoles) {
        this.areaRoles = areaRoles;
    }

    public String getRiverDuty() {
        return riverDuty;
    }

    public void setRiverDuty(String riverDuty) {
        this.riverDuty = riverDuty;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}

