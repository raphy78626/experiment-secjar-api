package com.secjar.secjarapi.controllers;

import com.secjar.secjarapi.dtos.LoginRequestDTO;
import com.secjar.secjarapi.dtos.responses.LoginResponseDTO;
import com.secjar.secjarapi.services.JwtTokenService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final JwtTokenService jwtTokenService;
    private final AuthenticationManager authenticationManager;

    public AuthController(JwtTokenService jwtTokenService, AuthenticationManager authenticationManager) {
        this.jwtTokenService = jwtTokenService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> token(@RequestBody LoginRequestDTO userLogin, HttpServletResponse response) throws AuthenticationException {

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userLogin.username(), userLogin.password()));

        return ResponseEntity.ok(new LoginResponseDTO(jwtTokenService.generateToken(authentication)));
    }
}
