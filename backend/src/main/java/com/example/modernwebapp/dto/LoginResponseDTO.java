package com.example.modernwebapp.dto;

/**
 * DTO for POST /login response body containing the generated JWT token.
 */
public class LoginResponseDTO {

    private String token;

    public LoginResponseDTO() {}

    public LoginResponseDTO(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
