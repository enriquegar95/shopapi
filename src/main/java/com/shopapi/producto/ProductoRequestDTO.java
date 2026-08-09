package com.shopapi.producto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductoRequestDTO(

        @Schema(description = "Nombre del producto", example = "Teclado mecanico")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
        String nombre,

        @Schema(description = "Precio de venta en euros", example = "59.99")
        @NotNull(message = "El precio es obligatorio")
        @Positive(message = "El precio debe ser mayor que cero")
        BigDecimal precio,

        @Schema(description = "Unidades disponibles en stock", example = "25")
        @NotNull(message = "El stock es obligatorio")
        @PositiveOrZero(message = "El stock no puede ser negativo")
        Integer stock,

        @Schema(description = "Id de la categoria a la que pertenece el producto", example = "1")
        @NotNull(message = "La categoria es obligatoria")
        Long categoriaId
) {}