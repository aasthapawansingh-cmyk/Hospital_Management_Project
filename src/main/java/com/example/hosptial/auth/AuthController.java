package com.example.hosptial.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private RefreshTokenService refreshTokenService;
    private RefreshTokenRepository refreshTokenRepository;
    public AuthController(AuthenticationManager authenticationmanager,JwtService jwtService, RefreshTokenService refreshTokenService,RefreshTokenRepository refreshTokenRepository){
        this.authenticationManager=authenticationmanager;
        this.jwtService=jwtService;
        this.refreshTokenService=refreshTokenService;
        this.refreshTokenRepository=refreshTokenRepository;
    }
    @PostMapping("/login")
   
    public LoginResponse login(@RequestBody LoginRequest request){
        authenticationManager.authenticate(
    new UsernamePasswordAuthenticationToken(
        request.getUsername(),
        request.getPassword()
    )
);

        String token = jwtService.generateToken(request.getUsername());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(request.getUsername());

        return new LoginResponse(token,refreshToken.getToken());
    }
    @PostMapping("/refresh")
public LoginResponse refresh(@RequestBody RefreshTokenRequest request) {
    RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
        .map(refreshTokenService::verifyExpiration)
        .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

    String newAccessToken = jwtService.generateToken(refreshToken.getusername());

    return new LoginResponse(newAccessToken, refreshToken.getToken());
}



}
