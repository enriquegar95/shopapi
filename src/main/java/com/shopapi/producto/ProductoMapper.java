package com.shopapi.producto;

import com.shopapi.categoria.Categoria;

public class ProductoMapper {

    public static Producto toEntity(ProductoRequestDTO dto, Categoria categoria) {
        return Producto.builder()
                .nombre(dto.nombre())
                .precio(dto.precio())
                .stock(dto.stock())
                .categoria(categoria)
                .build();
    }

    public static ProductoResponseDTO toResponseDTO(Producto producto) {
        return new ProductoResponseDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getPrecio(),
                producto.getStock(),
                producto.getCategoria().getNombre()
        );
    }
}