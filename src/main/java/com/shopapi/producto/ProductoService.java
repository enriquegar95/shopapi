package com.shopapi.producto;

import com.shopapi.categoria.Categoria;
import com.shopapi.categoria.CategoriaRepository;
import com.shopapi.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    @Transactional
    public ProductoResponseDTO crear(ProductoRequestDTO dto) {
        Categoria categoria = categoriaRepository.findById(dto.categoriaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoria no encontrada con id " + dto.categoriaId()));

        Producto producto = ProductoMapper.toEntity(dto, categoria);
        Producto guardado = productoRepository.save(producto);
        return ProductoMapper.toResponseDTO(guardado);
    }

    @Transactional(readOnly = true)
    public Page<ProductoResponseDTO> listar(Pageable pageable) {
        return productoRepository.findAll(pageable)
                .map(ProductoMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ProductoResponseDTO obtenerPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado con id " + id));
        return ProductoMapper.toResponseDTO(producto);
    }

    @Transactional
    public ProductoResponseDTO actualizar(Long id, ProductoRequestDTO dto) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado con id " + id));

        Categoria categoria = categoriaRepository.findById(dto.categoriaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoria no encontrada con id " + dto.categoriaId()));

        producto.setNombre(dto.nombre());
        producto.setPrecio(dto.precio());
        producto.setStock(dto.stock());
        producto.setCategoria(categoria);

        return ProductoMapper.toResponseDTO(producto);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Producto no encontrado con id " + id);
        }
        productoRepository.deleteById(id);
    }
}