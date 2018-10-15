package com.sunsan.project.controller;

import io.swagger.annotations.*;

import java.util.List;

import org.beetl.sql.core.db.KeyHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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


	
	//查询所有
	@ApiOperation(value = "查询所有（不含分页）", notes = "",authorizations = {@Authorization(value = "api_key")})
	@ResponseBody
    @ApiResponses(value = {@ApiResponse(code = 200, message = "successful")})
	@RequestMapping(value="/list",method=RequestMethod.POST)
	public ResponseEntity<List<JSONObject>> list() {
		
		List<JSONObject> results = userService.all();
		
		return ResponseEntity.ok(results);
	}
	
	//根据id查询
	@ApiOperation(value = "查询", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "successful")})
	@RequestMapping(value="/findById" , method=RequestMethod.GET)
	public ResponseEntity<User> getUserById(@RequestParam(value="userid",required = true) Integer userid) throws Exception {
		
		if(true){
			throw new ApiException(ErrCode.alreadyLogin);
		}
		User user = userService.unique(userid);
		return ResponseEntity.ok(user);
	}
	
	//根据id修改
	 @RequestMapping(value = "/updateUser",method = RequestMethod.POST)
	    public  String updateUser(@RequestBody User user){
	        int u = userService.updateById(user);
	        if(u==1){
	            return user.toString();
	        }else {
	            return "fail";
	        }
	    }
	 
	 	//添加
	    @RequestMapping(value = "/insertUser",method = RequestMethod.POST)
	    public  String postUser(
	    		@RequestBody User user) {
	      
	        KeyHolder u = userService.insertReturnKey(user);
	        if (u.getInt() > 0) {
	            return user.toString();
	        } else {
	            return "fail";
	        }
	    }
	    
	    //删除
	    @RequestMapping(value = "/delete",method=RequestMethod.POST)
	    public String delUser(@RequestParam("userid")int userid) {
	    	int u = userService.deleteById(userid);
	    	if(u>0) {
	    		return "删除成功";
	    	}else {
	    		return "fail";
	    	}
	    }
}
