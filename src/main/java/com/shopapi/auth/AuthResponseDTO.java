package com.shopapi.auth;

public record AuthResponseDTO(
        String token,
        String tipo
) {}