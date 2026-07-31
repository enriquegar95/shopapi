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
import org.springframework.security.access.AccessDeniedException;
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
    public Page<PedidoResponseDTO> listar(Pageable pageable, String emailAutenticado) {
        Usuario usuarioActual = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (usuarioActual.getRol() == RolUsuario.CLIENTE) {
            return pedidoRepository.findByUsuarioId(usuarioActual.getId(), pageable)
                    .map(PedidoMapper::toResponseDTO);
        }

        return pedidoRepository.findAll(pageable).map(PedidoMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public PedidoResponseDTO obtenerPorId(Long id, String emailAutenticado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id " + id));

        Usuario usuarioActual = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        boolean esPropio = pedido.getUsuario().getId().equals(usuarioActual.getId());
        boolean esStaff = usuarioActual.getRol() != RolUsuario.CLIENTE;

        if (!esPropio && !esStaff) {
            throw new AccessDeniedException("No puedes ver pedidos de otro usuario");
        }

        return PedidoMapper.toResponseDTO(pedido);
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

    @Transactional
    public PedidoResponseDTO cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id " + id));

        if (!TransicionEstadoValidator.esTransicionValida(pedido.getEstado(), nuevoEstado)) {
            throw new BusinessRuleException(
                    "No se puede pasar de " + pedido.getEstado() + " a " + nuevoEstado);
        }

        if (nuevoEstado == EstadoPedido.CANCELADO) {
            reponerStock(pedido);
        }

        pedido.setEstado(nuevoEstado);
        return PedidoMapper.toResponseDTO(pedido);
    }

    private void reponerStock(Pedido pedido) {
        for (LineaPedido linea : pedido.getLineas()) {
            Producto producto = linea.getProducto();
            producto.setStock(producto.getStock() + linea.getCantidad());
        }
    }
}