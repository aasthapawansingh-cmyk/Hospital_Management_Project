package com.example.hosptial.auth;

public class LoginResponse {
    private String token;
    private String refreshToken;
    public LoginResponse(){

    };
    public LoginResponse(String token,String refreshToken){
        this.token=token;
        this.refreshToken = refreshToken;
    }
    public void setToken(String token){
        this.token=token;
    }
    public String getToken(){
        return token;

    }
    public void setrefreshToken(String refreshToken){
        this.refreshToken=refreshToken;
    }
    public String getrefreshToken(){
        return refreshToken ;

    }
}
