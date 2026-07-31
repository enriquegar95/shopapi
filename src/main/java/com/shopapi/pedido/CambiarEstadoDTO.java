package com.shopapi.pedido;

import jakarta.validation.constraints.NotNull;

public record CambiarEstadoDTO(
        @NotNull(message = "El nuevo estado es obligatorio")
        EstadoPedido estado
) {}