package com.shopapi.pedido;

import java.math.BigDecimal;

public record LineaPedidoResponseDTO(
        Long id,
        String productoNombre,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {}