package com.shopapi.pedido;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PedidoRequestDTO(
        Long usuarioId,

        @NotEmpty(message = "El pedido debe tener al menos una linea")
        @Valid
        List<LineaPedidoRequestDTO> lineas
) {}