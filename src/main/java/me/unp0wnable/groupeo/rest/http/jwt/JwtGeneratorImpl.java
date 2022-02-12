package me.unp0wnable.groupeo.rest.http.jwt;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class JwtGeneratorImpl implements JwtGenerator {
    @Value("${project.jwt.signKey}")
    private String signKey;
    
    @Value("${project.jwt.expirationMinutes}")
    private long expirationInMinutes;
    
    
    @Override
    public String generateJWT(JwtData data) {
        long expirationInMillis = expirationInMinutes * 60 * 1000;
        Date tokenExpirationDate = new Date(System.currentTimeMillis() + expirationInMillis);
        
        return Jwts.builder()
                   .setExpiration(tokenExpirationDate)
                   .signWith(SignatureAlgorithm.HS512, signKey.getBytes())
                   // Información a almacenar en el JWT
                   .claim("nickname", data.getNickName())
                   .claim("userID", data.getUserID())
                   .claim("role", data.getRole())
                   .compact();
    }
    
    @Override
    public JwtData extractInfo(String token) {
        Claims claims = Jwts.parser()
                            .setSigningKey(signKey.getBytes())
                            .parseClaimsJws(token)
                            .getBody();
        
        // Parsear datos obtenidos del JWT
        UUID userID = UUID.fromString(claims.get("userID").toString());
        String userNickname = claims.get("nickName").toString();
        String userRole = claims.get("role").toString();
        
        
        return new JwtData(userID, userNickname, userRole);
    }
}
