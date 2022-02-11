package me.unp0wnable.groupeo.rest.security;

import me.unp0wnable.groupeo.rest.http.jwt.JwtData;
import me.unp0wnable.groupeo.rest.http.jwt.JwtGenerator;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class JwtFilter extends BasicAuthenticationFilter {
    private final String AUTH_TOKEN_PREFIX = "Bearer ";
    private final String ROLE_PREFIX = "ROLE_";
    
    private final JwtGenerator jwtGenerator;
    
    
    public JwtFilter(AuthenticationManager authManager, JwtGenerator jwtGenerator) {
        super(authManager);
        this.jwtGenerator = jwtGenerator;
    }
    
    @Override
    /**
     * Obtiene el JWT de la cabecera HTTP AUTHORIZATION y se lo comunica a Spring para que configure el contexto del usuario
     */
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // Comprueba si se ha recibido el JWT en la cabecera de la petición
        String authHeaderValue = request.getHeader(HttpHeaders.AUTHORIZATION);
        if ( authHeaderValue == null || !authHeaderValue.startsWith(AUTH_TOKEN_PREFIX) ){
            chain.doFilter(request, response);
            return;
        }
        
        // Al obtener el JWT, extrae sus atributos y se los comunica a Spring
        UsernamePasswordAuthenticationToken authToken = getAuthentication(request);       // Obtiene los datos de acceso
        SecurityContextHolder.getContext()
                             .setAuthentication(authToken);                                            // Configura Spring con los datos recibidos
        
        // Filtra la petición según la configuración recién establecida
        chain.doFilter(request, response);
    }
    
    
    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        // Elimina el "Bearer " de la cabecera
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = authHeader.replace(AUTH_TOKEN_PREFIX, "");
        
        // Extraer los datos del token
        JwtData data = jwtGenerator.extractInfo(token);
        
        // Si se obtienen los datos, se registran
        if (data != null) {
            request.setAttribute("token", token);
            request.setAttribute("userId", data.getUserID());
            // Asigna rol al usuario
            Set<GrantedAuthority> authorities = new HashSet<>();
            authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + data.getRole()));
            
            return new UsernamePasswordAuthenticationToken(data, null, authorities);
        } else
            return null;
        
    }
}
