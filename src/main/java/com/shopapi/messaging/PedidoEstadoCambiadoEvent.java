package com.shopapi.messaging;

public record PedidoEstadoCambiadoEvent(
        Long pedidoId, String usuarioEmail, String estadoAnterior, String estadoNuevo) {}