
package com.autobots.automanager.controles;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.dto.AutenticacaoCodigoBarraDTO;
import com.autobots.automanager.entidades.CredencialUsuario;
import com.autobots.automanager.jwt.ProvedorJwt;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthControle {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ProvedorJwt provedorJwt;

    @Operation(summary = "Login with username and password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody CredencialUsuario credencial) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    credencial.getNomeUsuario(), 
                    credencial.getSenha()
                )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = provedorJwt.proverJwtComAutoridades(userDetails.getUsername(), userDetails.getAuthorities());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("token", jwt);
            response.put("username", userDetails.getUsername());
            response.put("authorities", userDetails.getAuthorities());

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid credentials");
            error.put("message", "Username or password is incorrect");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @Operation(summary = "Login with barcode")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid barcode")
    })
    @PostMapping("/login-barcode")
    public ResponseEntity<?> loginWithBarcode(@RequestBody AutenticacaoCodigoBarraDTO barcodeAuth) {
        try {
            String barcodeUsername = "BARCODE:" + barcodeAuth.getCodigo();
            
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    barcodeUsername, 
                    barcodeAuth.getCodigo()
                )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = provedorJwt.proverJwtComAutoridades(userDetails.getUsername(), userDetails.getAuthorities());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful with barcode");
            response.put("token", jwt);
            response.put("barcode", barcodeAuth.getCodigo());
            response.put("authorities", userDetails.getAuthorities());

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid barcode");
            error.put("message", "Barcode not found or inactive");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    // @Operation(summary = "Test JWT token validity")
    // @PostMapping("/test-token")
    // public ResponseEntity<?> testToken() {
    //     Map<String, String> response = new HashMap<>();
    //     response.put("message", "Token is valid! You are authenticated.");
    //     return ResponseEntity.ok(response);
    // }
}