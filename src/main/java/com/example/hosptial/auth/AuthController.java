package com.example.hosptial.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.hosptial.PatientRepository;
import com.example.hosptial.DoctorRepository;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private RefreshTokenService refreshTokenService;
    private RefreshTokenRepository refreshTokenRepository;
    private UserRepository userRepository;
    private PatientRepository patientRepository;
    private DoctorRepository doctorRepository;
    public AuthController(AuthenticationManager authenticationmanager,JwtService jwtService, RefreshTokenService refreshTokenService,RefreshTokenRepository refreshTokenRepository, UserRepository userRepository, PatientRepository patientRepository, DoctorRepository doctorRepository){
        this.authenticationManager=authenticationmanager;
        this.jwtService=jwtService;
        this.refreshTokenService=refreshTokenService;
        this.refreshTokenRepository=refreshTokenRepository;
        this.userRepository=userRepository; this.patientRepository=patientRepository; this.doctorRepository=doctorRepository;
    }
    @GetMapping("/me")
    public java.util.Map<String,Object> me(@AuthenticationPrincipal UserDetails principal) {
        AuthUser user = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        var result = new java.util.HashMap<String,Object>();
        result.put("username", user.getUsername());
        result.put("roles", user.getRoles().stream().map(r -> r.getName().name()).toList());
        result.put("patientId", patientRepository.findByAccountUsername(user.getUsername()).map(p -> p.getId()).orElse(null));
        result.put("doctorId", doctorRepository.findByAccountUsername(user.getUsername()).map(d -> d.getId()).orElse(null));
        return result;
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
