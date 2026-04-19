package com.example.hosptial.auth;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Service;
@Service
public class JwtService {
    private String secret = "mysecretkeymysecretkeymysecretkey12";
    private long jwtExpirationMs= 1000*60*60*20;
public String generateToken(String username) {
    return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
}
public String extractUsername(String token){
    return Jwts.parserBuilder()
    .setSigningKey(getSigningKey())
    .build()
    .parseClaimsJws(token)
    .getBody()
    .getSubject();
}
public boolean isTokenValid(String token,String username){
    String extractedUsername=extractUsername(token);
    return extractedUsername.equals(username) && !isTokenExpired(token);
}
public Date extractExpiration(String token){
    return Jwts.parserBuilder()
    .setSigningKey(getSigningKey())
    .build()
    .parseClaimsJws(token)
    .getBody()
    .getExpiration();
}
private boolean isTokenExpired(String token){
    return extractExpiration(token).before(new Date());
}
private Key getSigningKey(){

    return Keys.hmacShaKeyFor(secret.getBytes());
}
}
