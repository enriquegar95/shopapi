package com.shopapi.categoria;

public class CategoriaMapper {

    public static Categoria toEntity(CategoriaRequestDTO dto) {
        return Categoria.builder()
                .nombre(dto.nombre())
                .build();
    }

    public static CategoriaResponseDTO toResponseDTO(Categoria categoria) {
        return new CategoriaResponseDTO(categoria.getId(), categoria.getNombre());
    }
}