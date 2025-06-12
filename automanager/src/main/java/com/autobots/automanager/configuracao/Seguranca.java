
package com.autobots.automanager.configuracao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.autobots.automanager.adaptadores.BarcodeAuthenticationProvider;
import com.autobots.automanager.adaptadores.UserDetailsServiceImpl;
import com.autobots.automanager.filtros.Autenticador;
import com.autobots.automanager.filtros.AutenticadorCodigoBarra;
import com.autobots.automanager.filtros.Autorizador;
import com.autobots.automanager.jwt.ProvedorJwt;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class Seguranca extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private ProvedorJwt provedorJwt;
    
    @Autowired
    private BarcodeAuthenticationProvider barcodeAuthenticationProvider;

    private static final String[] rotasPublicas = {
        "/", 
        "/login",                    // Original filter endpoint 
        "/login/barcode",           // Original filter endpoint
        "/auth/login",              // Controller endpoint for username/password
        "/auth/login-barcode",      // Controller endpoint for barcode
        "/auth/registrar-cliente",  // Public client registration
        "/auth/test-token",         // Test endpoint
        "/h2-console/**",           // H2 database console
        "/swagger-ui/**",           // Swagger UI
        "/api-docs/**",             // OpenAPI docs
        "/swagger-ui.html",         // Swagger UI main page
        "/v3/api-docs/**"           // OpenAPI v3 docs
    };

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable();
        
       
        http.headers().frameOptions().disable();
        
        http.authorizeHttpRequests()
            .antMatchers(rotasPublicas).permitAll()
            .anyRequest().authenticated();

     
        http.addFilter(new Autenticador(authenticationManager(), provedorJwt));
        http.addFilter(new AutenticadorCodigoBarra(authenticationManager(), provedorJwt));
        http.addFilter(new Autorizador(authenticationManager(), provedorJwt, userDetailsService));
        
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());

        auth.authenticationProvider(barcodeAuthenticationProvider);
    }
    
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
        return fonte;
    }
}