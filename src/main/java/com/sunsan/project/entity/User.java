package com.sunsan.project.entity;

public class User {
	private Integer userid;
	private String username;
	private Integer userage;
	private Integer usersex;
	private String userpwd;
	private String userdesc;
	public Integer getUserid() {
		return userid;
	}
	public void setUserid(Integer userid) {
		this.userid = userid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public Integer getUserage() {
		return userage;
	}
	public void setUserage(Integer userage) {
		this.userage = userage;
	}
	public Integer getUsersex() {
		return usersex;
	}
	public void setUsersex(Integer usersex) {
		this.usersex = usersex;
	}
	public String getUserpwd() {
		return userpwd;
	}
	public void setUserpwd(String userpwd) {
		this.userpwd = userpwd;
	}
	public String getUserdesc() {
		return userdesc;
	}
	public void setUserdesc(String userdesc) {
		this.userdesc = userdesc;
	}
	
	@Override
	public String toString() {
		return "USER{用户编号:"+this.userid+ ",用户名称:"+this.username+
				",密码:"+this.userpwd+
				",年龄:"+this.userage+
				",性别:"+this.usersex+
				",备注:"+this.userdesc+"}";
	}
}
