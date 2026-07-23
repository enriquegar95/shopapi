package com.shopapi.producto;

import java.math.BigDecimal;

public record ProductoResponseDTO(
        Long id,
        String nombre,
        BigDecimal precio,
        Integer stock,
        String categoriaNombre
) {}