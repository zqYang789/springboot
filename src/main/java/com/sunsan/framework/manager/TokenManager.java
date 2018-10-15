package com.sunsan.framework.manager;


import com.sunsan.framework.model.TokenUser;

import java.util.List;

/**
 * 对Token进行操作的接口
 */
public interface TokenManager {
    // 生成token
    String generateToken(TokenUser tokenUser) throws Exception;

    //更新token
    String updateToken(TokenUser tokenUser, String token) throws Exception;

    //更新token的生存周期
    void refreshToken(TokenUser tokenUser) throws Exception;

    TokenUser getTokenUserFromToken(String token) throws Exception;

    String getToken(TokenUser tokenUser) throws Exception;

    void deleteToken(TokenUser tokenUser) throws Exception;

    List<String> getAllTokens();

    List<TokenUser> getAllTokenUsers();
}
