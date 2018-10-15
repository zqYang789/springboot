package com.sunsan.framework.model;


import java.util.HashMap;
import java.util.Map;


public enum ErrCode {
    success("success", 200),
    unknown("未知的错误", 400),
    unauthorized("没有登录,无权限", 401),

    param("参数错误", 99),
    frequency("访问频率过快", 100),

    // about user
    alreadyLogin("已经登录", 102),
    errPassword("密码错误", 103),
    userLocked("用户已经被锁定", 104),
    noUser("用户不存在", 105),
    expireUser("用户已到期", 106),
    noPermission("无权限", 107),
    userExists("用户已经存在", 108),
    passwordMinLen("密码长度不符合要求", 109),
    expirePassword("密码过期", 110),
    noSupportLoginMethod("不支持此方法登录", 111),
    invalidMobileNumber("无效的手机号码", 112),
    existMobileNumber("手机号码已经存在", 113),
    existUserName("用户名已经存在", 114),
   
   
    encodingExists("编码已存在",119),

    // token
    nullToken("token为空", 201),
    expireToken("token已经过期", 202),
    invalidToken("无效的token", 203),
    beTakenToken("该账户已经其他地方登录", 204),
    emptyAuthCode("验证码为空", 205),
    errAuthCode("验证码错误", 206),
    invalidAuthCode("请重新申请验证码", 207),
    createTokenFail("创建token失败", 208),
    tokenIpChange("ip变化", 209),
    tokenDeviceChange("设备变化", 210),
    verificationCodeExpired("验证码过期", 214),

    //web
    noApiOperation("方法没有ApiOperation注解", 301),

    //upload
    fileExist("文件已经存在", 600),

    max("max error code", 10000);

    private int code;
    private String message;

    ErrCode(String message, int code) {
        this.code = code;
        this.message = message;
    }

    public static String getMessage(int code) {
        for (ErrCode err : ErrCode.values()) {
            if (code == err.getCode()) {
                return err.getMessage();
            }
        }
        return unknown.getMessage();
    }

    public static void main(String[] args) {
        // 检查是否有相同的code定义的项
        String str;
        Map<Integer, String> errCodeMap = new HashMap<Integer, String>();
        for (ErrCode value : ErrCode.values()) {
            if (errCodeMap.containsKey(value.getCode())) {
                System.out.println("----------------------------");
                str = String.format("%5s : %s", value.getCode(), value.getMessage());
                System.out.println(str);
                str = String.format("%5s : %s", value.getCode(), errCodeMap.get(value.getCode()));
                System.out.println(str);
            }
            errCodeMap.put(value.getCode(), value.message);
            // str = String.format("%5s : %s", value.getCode(), value.getMessage());
            // System.out.println(out);
        }
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }
}
