package com.shopapi.pedido;

import java.math.BigDecimal;
import java.util.List;

public class PedidoMapper {

    public static PedidoResponseDTO toResponseDTO(Pedido pedido) {
        List<LineaPedidoResponseDTO> lineasDTO = pedido.getLineas().stream()
                .map(linea -> new LineaPedidoResponseDTO(
                        linea.getId(),
                        linea.getProducto().getNombre(),
                        linea.getCantidad(),
                        linea.getPrecioUnitario(),
                        linea.getPrecioUnitario().multiply(BigDecimal.valueOf(linea.getCantidad()))
                ))
                .toList();

        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getUsuario().getNombre(),
                pedido.getFecha(),
                pedido.getEstado(),
                pedido.getTotal(),
                lineasDTO
        );
    }
}