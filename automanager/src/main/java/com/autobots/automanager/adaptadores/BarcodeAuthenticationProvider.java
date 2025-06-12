package com.autobots.automanager.adaptadores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class BarcodeAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

     
        if (username.startsWith("BARCODE:")) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
               
                if (userDetails.getPassword().equals(password)) {
                    return new UsernamePasswordAuthenticationToken(
                            userDetails, password, userDetails.getAuthorities());
                } else {
                    throw new BadCredentialsException("Invalid barcode");
                }
            } catch (Exception e) {
                throw new BadCredentialsException("Barcode authentication failed", e);
            }
        }
        
        return null; 
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}