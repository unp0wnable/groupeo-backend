package me.unp0wnable.groupeo.rest.http.jwt;

public interface JwtGenerator {
    
    /** Genera un Json Web Token a partir de una instancia */
    String generateJWT(JwtData data);
    
    /** Extrae la información del Json Web Token */
    JwtData extractInfo(String token);
}
