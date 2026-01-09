package com.example.demo.utils;


import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.util.*;

import javax.crypto.SecretKey;

@Component
public class JwtTokenUtil {
    private final static  SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final static Long EXPIRATION_TIME = 864000000L;

    public static String generateToken(String email){
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY,SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token){
        return !isTokenExpired(token);
    }
    public String extractEmail(String token){
        JwtParser parser = Jwts.parserBuilder().setSigningKey(SECRET_KEY).build();
         return parser.parseClaimsJws(token).getBody().getSubject();
    }

    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token){
        JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(SECRET_KEY).build();
        return jwtParser.parseClaimsJws(token).getBody().getExpiration();
    }


}
