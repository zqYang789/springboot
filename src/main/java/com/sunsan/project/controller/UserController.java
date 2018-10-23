package com.sunsan.project.controller;

import com.sunsan.framework.annotation.ApiFrequency;
import com.sunsan.framework.manager.TokenManager;
import com.sunsan.framework.model.TokenUser;
import com.sunsan.framework.util.WebUtils;
import com.sunsan.project.entity.LoginInfo;
import io.swagger.annotations.*;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.beetl.sql.core.db.KeyHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.DeviceUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.sunsan.project.entity.User;
import com.sunsan.framework.model.ApiException;
import com.sunsan.framework.model.ErrCode;
import com.sunsan.project.service.UserService;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value="/user",produces="application/json")
@Api(value="用户信息",description="用户信息表")
public class UserController {
	@Autowired
	private UserService userService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    TokenManager tokenManager;


	
	//查询所有
	@ApiOperation(value = "新增", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "successful")})
	@RequestMapping(value="/insert",method=RequestMethod.POST)
	public ResponseEntity<User> insert() {
	    User user = new User();
	    user.setUsername("yangzhiqiang");
	    user.setMobile("15101024650");
	    user.setPassword("123456");
	    user.setRealname("杨志强");
	    userService.insert(user);
		return ResponseEntity.ok(null);
	}

    @ApiOperation(value = "查询所有（不含分页）", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "successful")})
    @RequestMapping(value="/list",method=RequestMethod.POST)
    public ResponseEntity<List<JSONObject>> list() {
        List<JSONObject> results = userService.all();
        return ResponseEntity.ok(results);
    }

	
	//根据id查询
	@ApiOperation(value = "查询", notes = "",authorizations = {@Authorization(value = "api_key")})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "successful")})
	@RequestMapping(value="/findById" , method=RequestMethod.GET)
	public ResponseEntity<User> getUserById(@RequestParam(value="userid",required = true) Integer userid) throws Exception {
		User user = userService.unique(userid);
		return ResponseEntity.ok(user);
	}
	

	 



    @ApiFrequency(name = "login", time = 2, limit = 1) //限制验证接口访问频率2秒一次
    @ApiOperation(value = "登录", notes = "登录")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "successful")})
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<LoginInfo> login(
            @RequestParam(required = true) String loginName,
            @RequestParam(required = true) String password,
            @RequestParam(required = false) String authCode,
            @RequestParam(required = false) String devId) throws Exception {
        User user = userService.getUserByLoginNameAndPassword(loginName, password);
        if (user == null)
            throw new ApiException(ErrCode.noUser);
        TokenUser tokenUser = new TokenUser();
        if (StringUtils.isEmpty(devId))
            devId = DeviceUtils.getCurrentDevice(request).getDevicePlatform().name();
        tokenUser.setDevice(devId);
        tokenUser.setUsername(user.getUsername());
        String loginIp = WebUtils.getRemoteAddr(request);
        tokenUser.setLastIp(loginIp);
        tokenUser.setUserId(user.getId());
        String token = tokenManager.generateToken(tokenUser);

        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setToken(token);
        loginInfo.setRealName(user.getRealname());
        loginInfo.setUserId(user.getId());
        return ResponseEntity.ok(loginInfo);
    }
}
