package com.shopapi.pedido;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class TransicionEstadoValidator {

    private static final Map<EstadoPedido, Set<EstadoPedido>> TRANSICIONES_VALIDAS =
            new EnumMap<>(EstadoPedido.class);

    static {
        TRANSICIONES_VALIDAS.put(EstadoPedido.PENDIENTE,
                EnumSet.of(EstadoPedido.CONFIRMADO, EstadoPedido.CANCELADO));
        TRANSICIONES_VALIDAS.put(EstadoPedido.CONFIRMADO,
                EnumSet.of(EstadoPedido.ENVIADO, EstadoPedido.CANCELADO));
        TRANSICIONES_VALIDAS.put(EstadoPedido.ENVIADO,
                EnumSet.of(EstadoPedido.ENTREGADO));
        TRANSICIONES_VALIDAS.put(EstadoPedido.ENTREGADO,
                EnumSet.noneOf(EstadoPedido.class));
        TRANSICIONES_VALIDAS.put(EstadoPedido.CANCELADO,
                EnumSet.noneOf(EstadoPedido.class));
    }

    public static boolean esTransicionValida(EstadoPedido actual, EstadoPedido nuevo) {
        return TRANSICIONES_VALIDAS
                .getOrDefault(actual, EnumSet.noneOf(EstadoPedido.class))
                .contains(nuevo);
    }
}