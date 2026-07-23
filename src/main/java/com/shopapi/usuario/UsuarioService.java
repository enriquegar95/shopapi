package com.shopapi.usuario;

import com.shopapi.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Transactional
    public UsuarioResponseDTO crear(UsuarioRequestDTO dto) {
        Usuario guardado = usuarioRepository.save(UsuarioMapper.toEntity(dto));
        return UsuarioMapper.toResponseDTO(guardado);
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> listar(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(UsuarioMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id " + id));
        return UsuarioMapper.toResponseDTO(usuario);
    }

    @Transactional
    public UsuarioResponseDTO actualizar(Long id, UsuarioRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id " + id));

        usuario.setNombre(dto.nombre());
        usuario.setEmail(dto.email());
        usuario.setPassword(dto.password());
        usuario.setRol(dto.rol());

        return UsuarioMapper.toResponseDTO(usuario);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado con id " + id);
        }
        usuarioRepository.deleteById(id);
    }
}