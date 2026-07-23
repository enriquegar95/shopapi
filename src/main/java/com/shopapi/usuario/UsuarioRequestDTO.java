package com.shopapi.usuario;

import jakarta.validation.constraints.*;

public record UsuarioRequestDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150)
        String nombre,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato valido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password,

        @NotNull(message = "El rol es obligatorio")
        RolUsuario rol
) {}