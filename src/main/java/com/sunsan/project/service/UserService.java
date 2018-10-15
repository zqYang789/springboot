package com.sunsan.project.service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.beetl.sql.core.db.KeyHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.sunsan.project.dao.UserDao;
import com.sunsan.project.entity.User;
@Service
public class UserService {
	
	
	@Autowired UserDao userDao;
	public List<JSONObject> all(){
		Map<String,Object> para = new HashMap<String,Object>();
		List<JSONObject> objs = this.userDao.getSQLManager().select("user.all", JSONObject.class);
		return objs;
	}


	public User getTestUser(){
		return userDao.createLambdaQuery().andEq(User::getUsername, "test").single();
	}
	
	
	public User unique(int userid) {
		
		return this.userDao.unique(userid);
	}
	public int updateById(User user) {
		// TODO Auto-generated method stub
		return this.userDao.updateById(user);
	}
	public KeyHolder insertReturnKey(User user) {
		// TODO Auto-generated method stub
		return this.userDao.insertReturnKey(user);
	}
	public int deleteById(int userid) {
		// TODO Auto-generated method stub
		return this.userDao.deleteById(userid);
	}
}
