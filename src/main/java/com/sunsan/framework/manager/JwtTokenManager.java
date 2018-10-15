package com.sunsan.framework.manager;

import com.sunsan.framework.model.TokenUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenManager implements Serializable {

    private static final long serialVersionUID = -3301605591108950415L;

    static final String CLAIM_KEY_USERNAME = "sub";
    static final String CLAIM_KEY_AUDIENCE = "audience";
    static final String CLAIM_KEY_CREATED = "created";
    static final String CLAIM_KEY_USERID = "id";
    static final String CLAIM_KEY_LOGINIP = "ip";

    @Value("${jwt.secret:dfc438ec1d5c4cc50ea6796352edc7f5}")
    private String secret;

    private Long expiration;

    public String getUsernameFromToken(String token) {
        String username;
        final Claims claims = getClaimsFromToken(token);
        username = claims.getSubject();
        return username;
    }

    public Integer getUserIdFromToken(String token) {
        Integer userId;
        final Claims claims = getClaimsFromToken(token);
        Object id = claims.get(CLAIM_KEY_USERID);
        userId = Integer.valueOf((String) id);
        return userId;
    }

    public LocalDateTime getCreatedDateFromToken(String token) {
        LocalDateTime created;
        final Claims claims = getClaimsFromToken(token);
        Long milliseconds = (Long) claims.get(CLAIM_KEY_CREATED);
        created = Instant.ofEpochMilli(milliseconds).atZone(ZoneId.systemDefault()).toLocalDateTime();
        return created;
    }

    public Date getExpirationDateFromToken(String token) {
        Date expiration;
        final Claims claims = getClaimsFromToken(token);
        expiration = claims.getExpiration();
        return expiration;
    }

    public String getAudienceFromToken(String token) {
        String audience;
        final Claims claims = getClaimsFromToken(token);
        audience = (String) claims.get(CLAIM_KEY_AUDIENCE);
        return audience;
    }

    public String getLoginIpFromToken(String token) {
        String loginIp;
        final Claims claims = getClaimsFromToken(token);
        loginIp = (String) claims.get(CLAIM_KEY_LOGINIP);
        return loginIp;
    }

    public TokenUser getTokenUserFromToken(String token) {
        final Claims claims = getClaimsFromToken(token);
        Object id = claims.get(CLAIM_KEY_USERID);
        Integer userId = Integer.valueOf((String) id);
        TokenUser tokenUser = new TokenUser();
        tokenUser.setLastIp((String) claims.get(CLAIM_KEY_LOGINIP));
        tokenUser.setUserId(userId);
        tokenUser.setUsername((String) claims.get(CLAIM_KEY_USERNAME));
        tokenUser.setDevice((String) claims.get(CLAIM_KEY_AUDIENCE));
        tokenUser.setToken(token);
        return tokenUser;
    }

    private Claims getClaimsFromToken(String token) {
        Claims claims;
        claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
        return claims;
    }

    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + expiration * 1000);
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    private Boolean isCreatedBeforeLastPasswordReset(LocalDateTime created, LocalDateTime lastPasswordReset) {
        return (lastPasswordReset != null && created.isBefore(lastPasswordReset));
    }

    public String generateToken(TokenUser tokenUser, Long expirationSeconds) {
        expiration = expirationSeconds;
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_USERNAME, tokenUser.getUsername());
        claims.put(CLAIM_KEY_USERID, String.valueOf(tokenUser.getUserId()));
        claims.put(CLAIM_KEY_LOGINIP, tokenUser.getLastIp());
        claims.put(CLAIM_KEY_AUDIENCE, tokenUser.getDevice());
        claims.put(CLAIM_KEY_CREATED, LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return generateToken(claims);
    }

    String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(generateExpirationDate())
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public Boolean canTokenBeRefreshed(String token, LocalDateTime lastPasswordReset) {
        final LocalDateTime created = getCreatedDateFromToken(token);
        return !isCreatedBeforeLastPasswordReset(created, lastPasswordReset)
                && (!isTokenExpired(token));
    }

    public String refreshToken(String token) {
        String refreshedToken;
        final Claims claims = getClaimsFromToken(token);
        claims.put(CLAIM_KEY_CREATED, new Date());
        refreshedToken = generateToken(claims);
        return refreshedToken;
    }

    public Boolean validateToken(String token, TokenUser user) {
        final String username = getUsernameFromToken(token);
        final LocalDateTime created = getCreatedDateFromToken(token);
        //final Date expiration = getExpirationDateFromToken(token);
        return (username.equals(user.getUsername()) && !isTokenExpired(token)
                && !isCreatedBeforeLastPasswordReset(created, LocalDateTime.now()));
        // && !isCreatedBeforeLastPasswordReset(created, user.getLastPasswordResetDate().toLocalDateTime()));
    }
}
