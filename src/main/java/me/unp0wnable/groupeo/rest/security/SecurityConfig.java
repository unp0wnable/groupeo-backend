package me.unp0wnable.groupeo.rest.security;

import me.unp0wnable.groupeo.rest.http.jwt.JwtGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.*;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private JwtGenerator jwtGenerator;
    
    /** Configura los endpoints expuestos públicamente y qué métodos HTTP pueden hacer peticiones a cada uno */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        JwtFilter jwtAuthFilter = new JwtFilter(authenticationManager(), jwtGenerator);
        
        http
                .cors().and().csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilter(jwtAuthFilter).authorizeRequests()
                // ACCOUNTS ENDPOINTS
                .antMatchers(HttpMethod.POST,   "/api/users/signUp").permitAll()                    // signUp
                .antMatchers(HttpMethod.POST,   "/api//users/login").permitAll()                     // login
                .antMatchers(HttpMethod.POST,   "/api//users/tokenLogin").permitAll()                // loginFromServiceToken
                .antMatchers(HttpMethod.POST,   "/api//users/*/changePassword").permitAll()          // changePassword
                .antMatchers(HttpMethod.PUT,    "/api//users/*/update").permitAll()                  // updateUserProfile
                .antMatchers(HttpMethod.POST,    "/api//users/*/address").permitAll()                // assignAddressToUser
                .antMatchers(HttpMethod.PUT,    "/api//users/*/address").permitAll()                 // updateUserAddress
                .antMatchers(HttpMethod.DELETE, "/api//users/*").permitAll()                         // deleteUser
                // EVENTS ENDPOINTS
                
                // DENY ALL UNAUTHORIZED REQUESTS
                .anyRequest().denyAll();
    }
    
    
    /**
     * Configuración de seguridad para permitir peticiones CORS.
     * También controla qué tipo de contenido aceptar en las peticiones (origen, cabeceras, método HTTP, etc)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        UrlBasedCorsConfigurationSource corsUrlSource = new UrlBasedCorsConfigurationSource();
        
        corsConfiguration.addAllowedOrigin("*");            // Permite peticiones de cualquier origen
        corsConfiguration.addAllowedMethod("*");            // Permite peticiones con cualquier verbo HTTP
        corsConfiguration.addAllowedHeader("*");            // Permite peticiones con cualquier cabecera
        
        // Aplica la configuración a todas las URL expuestas por el servicio
        corsUrlSource.registerCorsConfiguration("/**", corsConfiguration);
        
        return corsUrlSource;
    }
}
