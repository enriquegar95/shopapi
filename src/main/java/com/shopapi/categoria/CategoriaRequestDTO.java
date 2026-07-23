package com.shopapi.categoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequestDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String nombre
) {}