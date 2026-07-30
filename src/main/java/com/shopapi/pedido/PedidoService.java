package com.shopapi.pedido;

import com.shopapi.common.exception.BusinessRuleException;
import com.shopapi.common.exception.ResourceNotFoundException;
import com.shopapi.producto.Producto;
import com.shopapi.producto.ProductoRepository;
import com.shopapi.usuario.RolUsuario;
import com.shopapi.usuario.Usuario;
import com.shopapi.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    @Transactional
    public PedidoResponseDTO crear(PedidoRequestDTO dto, String emailAutenticado) {
        Usuario usuarioAutenticado = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con email " + emailAutenticado));

        Usuario usuarioPedido = resolverUsuarioDelPedido(dto, usuarioAutenticado);

        Pedido pedido = Pedido.builder()
                .usuario(usuarioPedido)
                .fecha(LocalDateTime.now())
                .estado(EstadoPedido.PENDIENTE)
                .lineas(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (LineaPedidoRequestDTO lineaDto : dto.lineas()) {
            Producto producto = productoRepository.findById(lineaDto.productoId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto no encontrado con id " + lineaDto.productoId()));

            if (producto.getStock() < lineaDto.cantidad()) {
                throw new BusinessRuleException(
                        "Stock insuficiente para '" + producto.getNombre() +
                                "' (disponible: " + producto.getStock() +
                                ", solicitado: " + lineaDto.cantidad() + ")");
            }

            producto.setStock(producto.getStock() - lineaDto.cantidad());

            LineaPedido linea = LineaPedido.builder()
                    .pedido(pedido)
                    .producto(producto)
                    .cantidad(lineaDto.cantidad())
                    .precioUnitario(producto.getPrecio())
                    .build();

            pedido.getLineas().add(linea);
            total = total.add(producto.getPrecio().multiply(BigDecimal.valueOf(lineaDto.cantidad())));
        }

        pedido.setTotal(total);

        Pedido guardado = pedidoRepository.save(pedido);
        return PedidoMapper.toResponseDTO(guardado);
    }

    private Usuario resolverUsuarioDelPedido(PedidoRequestDTO dto, Usuario usuarioAutenticado) {
        if (usuarioAutenticado.getRol() == RolUsuario.CLIENTE) {
            return usuarioAutenticado;
        }

        Long usuarioIdObjetivo = dto.usuarioId() != null ? dto.usuarioId() : usuarioAutenticado.getId();
        return usuarioRepository.findById(usuarioIdObjetivo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con id " + usuarioIdObjetivo));
    }

    @Transactional(readOnly = true)
    public Page<PedidoResponseDTO> listar(Pageable pageable) {
        return pedidoRepository.findAll(pageable).map(PedidoMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public PedidoResponseDTO obtenerPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id " + id));
        return PedidoMapper.toResponseDTO(pedido);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!pedidoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Pedido no encontrado con id " + id);
        }
        pedidoRepository.deleteById(id);
    }
}