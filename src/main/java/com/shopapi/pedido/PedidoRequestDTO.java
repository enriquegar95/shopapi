package com.shopapi.pedido;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PedidoRequestDTO(
        @NotNull(message = "El usuario es obligatorio")
        Long usuarioId,

        @NotEmpty(message = "El pedido debe tener al menos una linea")
        @Valid
        List<LineaPedidoRequestDTO> lineas
) {}