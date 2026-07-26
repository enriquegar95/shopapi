package com.shopapi.pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponseDTO(
        Long id,
        String usuarioNombre,
        LocalDateTime fecha,
        EstadoPedido estado,
        BigDecimal total,
        List<LineaPedidoResponseDTO> lineas
) {}