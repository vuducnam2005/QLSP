package com.example.productmanagement.auth.dto;

public record AuthUserResponse(String username, String role, boolean authenticated) {}
