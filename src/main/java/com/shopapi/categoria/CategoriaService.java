package com.shopapi.categoria;

import com.shopapi.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Transactional
    public CategoriaResponseDTO crear(CategoriaRequestDTO dto) {
        Categoria guardada = categoriaRepository.save(CategoriaMapper.toEntity(dto));
        return CategoriaMapper.toResponseDTO(guardada);
    }

    @Transactional(readOnly = true)
    public Page<CategoriaResponseDTO> listar(Pageable pageable) {
        return categoriaRepository.findAll(pageable).map(CategoriaMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public CategoriaResponseDTO obtenerPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada con id " + id));
        return CategoriaMapper.toResponseDTO(categoria);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoria no encontrada con id " + id);
        }
        categoriaRepository.deleteById(id);
    }
}