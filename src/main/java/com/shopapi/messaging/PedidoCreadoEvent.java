package com.shopapi.messaging;

import java.math.BigDecimal;

public record PedidoCreadoEvent(Long pedidoId, String usuarioEmail, BigDecimal total) {}