package com.sunsan.project.service;


import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.sunsan.framework.model.ApiException;
import com.sunsan.framework.model.ErrCode;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.beetl.sql.core.db.KeyHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.sunsan.project.dao.UserDao;
import com.sunsan.project.entity.User;
import org.springframework.transaction.annotation.Transactional;

@Service
@CacheConfig(cacheNames = "user")
public class UserService {
	
	
	@Autowired
	private UserDao userDao;
	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	private int baseTimeout = 3600; //key默认失效时间

	@Cacheable(key = "'userList'")
	public List<JSONObject> all(){
		Map<String,Object> para = new HashMap<String,Object>();
		List<JSONObject> objs = this.userDao.getSQLManager().select("user.all", JSONObject.class);
		return objs;
	}

	@Transactional
	public void insert(User user){
		user.setPassword(encryptPassword("123456"));
		userDao.insert(user);
	}

	public User getTestUser(){
		return userDao.createLambdaQuery().andEq(User::getUsername, "zqy").single();
	}

	public User getUserByLoginNameAndPassword(String loginName, String password) throws ApiException {
		User user = getHzzUserByLoginName(loginName);
		if (user != null) {
			checkAlreadyLoginErrorPassword(user.getId());
			if (!encryptPassword(password).equals(user.getPassword())) {
				loginByErrorPassword(user.getId());
			}
			redisTemplate.delete(getLoginByErrorPasswordKey(user.getId()));
		}
		return user;
	}

	private String encryptPassword(String password) {
		return new String(DigestUtils.md5(DigestUtils.md5(password)));
	}

    private void loginByErrorPassword(Integer userId) throws ApiException {
        String value = getLoginByErrorPasswordErrCount(userId);
        int count;
        int timeoutSec;
        if (value == null) {
            count = 1;
            timeoutSec = baseTimeout;
        } else {
            count = Integer.parseInt(value) + 1;
            timeoutSec = getLoginByErrorPasswordTimeout(userId);
            if (timeoutSec < baseTimeout)
                timeoutSec = baseTimeout;
        }

        String msg = String.format("连续%s次密码错误，超过5次后会被锁定一段时间", count);
        // 输入错误超过5次后，开始冻结
        if (count == 5)
            timeoutSec += 60;
        if (count == 6)
            timeoutSec += 60 * 5;
        if (count > 6) {
            timeoutSec += 60 * Math.pow(2, count - 2);
        }
        if (count > 5)
            msg = String.format("连续%s次密码错误，请%s秒后再尝试登录", count, timeoutSec - baseTimeout);

        String key = getLoginByErrorPasswordKey(userId);
        redisTemplate.boundValueOps(key).set(String.valueOf(count), timeoutSec, TimeUnit.SECONDS);

        if (count > 3)
            throw new ApiException(ErrCode.errPassword, msg);
        else
            throw new ApiException(ErrCode.errPassword);
    }
	private void checkAlreadyLoginErrorPassword(Integer userId) throws ApiException {
		int seconds = getLoginByErrorPasswordTimeout(userId);
		if (seconds > baseTimeout) {
			String value = getLoginByErrorPasswordErrCount(userId);
			Integer errCount = Integer.valueOf(value);
			if (errCount < 5)
				return;
			String msg = String.format("连续%s次密码错误，请%s秒后再尝试登录", errCount, seconds - baseTimeout);
			throw new ApiException(ErrCode.errPassword, msg);
		}
	}

    private String getLoginByErrorPasswordErrCount(Integer userId) {
        String key = getLoginByErrorPasswordKey(userId);
        String value = redisTemplate.boundValueOps(key).get();
        return value;
    }
	private String getLoginByErrorPasswordKey(Integer userId) {
		return "admin_login_fail_" + userId;
	}
	private Integer getLoginByErrorPasswordTimeout(Integer userId) {
		String key = getLoginByErrorPasswordKey(userId);
		int timeoutSec = Math.toIntExact(redisTemplate.boundValueOps(key).getExpire());
		return timeoutSec;
	}
	public User getHzzUserByLoginName(String loginName) throws ApiException {
		User hzzUser = null;
		if (StringUtils.isEmpty(loginName))
			return hzzUser;

		// if (WebUtils.isMobileNumber(loginName))
		try {
			hzzUser = userDao.createLambdaQuery().orEq(User::getMobile, loginName).orEq(User::getUsername, loginName).unique();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hzzUser;
	}

	@Cacheable(key="#userid")
	public User unique(String userid) {
		User user = this.userDao.unique(userid);
		return user;
	}

}
