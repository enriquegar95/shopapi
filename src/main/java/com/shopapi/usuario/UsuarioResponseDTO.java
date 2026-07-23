package com.shopapi.usuario;

public record UsuarioResponseDTO(
        Long id,
        String nombre,
        String email,
        RolUsuario rol
) {}