package com.sunsan.framework.manager.impl;

import com.sunsan.framework.manager.JwtTokenManager;
import com.sunsan.framework.manager.TokenManager;
import com.sunsan.framework.model.ApiException;
import com.sunsan.framework.model.ErrCode;
import com.sunsan.framework.model.TokenUser;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 通过Redis存储和验证token的实现类
 *
 * @author ScienJus
 * @date 2015/7/31.
 */
@Primary
@Component
public class RedisTokenManager implements TokenManager {

    private String KEY_PREFIX = "login_";
    // token持续有效时间，如果没有api访问，在设置数值秒后失效，优先从spring配置获取
    @Value("${token.persistSeconds:3600}")
    private long tokenPersistSecond;
    // token到期时间，如果一直有api访问，在设置数值秒后失效，优先从spring配置获取
    @Value("${token.expiresSeconds:86400}")
    private long tokenExpiresSecond;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JwtTokenManager jwtTokenManager;


    @Override
    public String generateToken(TokenUser tokenUser) throws Exception {
        String token = jwtTokenManager.generateToken(tokenUser, tokenExpiresSecond);
        String key = getKey(tokenUser);
        //存储到redis并设置过期时间
        redisTemplate.boundValueOps(key).set(token, tokenPersistSecond, TimeUnit.SECONDS);
        return token;
    }

    @Override
    public TokenUser getTokenUserFromToken(String token) throws Exception {
        TokenUser tokenUser = new TokenUser();
        try {
            tokenUser = jwtTokenManager.getTokenUserFromToken(token);
        } catch (ExpiredJwtException e) {
            throw new ApiException(ErrCode.expireToken);
        } catch (Exception e) {
            throw new ApiException(ErrCode.invalidToken);
        }
        String curToken = getToken(tokenUser);
        if (curToken == null)
            throw new ApiException(ErrCode.invalidToken);
        if (!curToken.equals(token))
            throw new ApiException(ErrCode.beTakenToken);
        return tokenUser;
    }

    private String getKey(TokenUser tokenUser) throws ApiException {
        String key = KEY_PREFIX;
        if (tokenUser.getUserId() != null) {
            key = key + tokenUser.getUserId() + "_" + tokenUser.getDevice();
        } else {
            throw new ApiException(ErrCode.invalidToken);
        }
        return key;
    }

    @Override
    @Transactional
    public String updateToken(TokenUser tokenUser, String oldToken) throws Exception {
        TokenUser oldTokenUser = getTokenUserFromToken(oldToken);
        String token;
        if (oldTokenUser.getUserId().equals(tokenUser.getUserId()) && oldTokenUser.getLastIp().equals(tokenUser.getLastIp()) && oldTokenUser.getDevice().equals(tokenUser.getDevice()))
            token = oldToken;
        else
            token = jwtTokenManager.generateToken(tokenUser, tokenExpiresSecond);
        tokenUser.setToken(token);
        String key = getKey(tokenUser);
        //存储到redis并设置过期时间
        redisTemplate.boundValueOps(key).set(token, tokenPersistSecond, TimeUnit.SECONDS);
        return token;
    }

    @Override
    @Transactional
    public void refreshToken(TokenUser tokenUser) throws ApiException {
        String key = getKey(tokenUser);
        redisTemplate.boundValueOps(key).expire(tokenPersistSecond, TimeUnit.SECONDS);
    }

    @Override
    public String getToken(TokenUser tokenUser) throws ApiException {
        String key = getKey(tokenUser);
        return redisTemplate.boundValueOps(key).get();
    }

    @Override
    public void deleteToken(TokenUser tokenUser) throws ApiException {
        String key = getKey(tokenUser);
        redisTemplate.delete(key);
    }

    @Override
    public List<String> getAllTokens() {
        Set<String> keys = redisTemplate.keys("^" + KEY_PREFIX + "*");
        List<String> allTokens = new ArrayList<>();
        for (String key : keys) {
            allTokens.add(redisTemplate.boundValueOps(key).get());
        }
        return allTokens;
    }

    @Override
    public List<TokenUser> getAllTokenUsers() {
        return null;
    }
}
