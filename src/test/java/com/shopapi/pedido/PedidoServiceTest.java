package com.shopapi.pedido;

import com.shopapi.common.exception.BusinessRuleException;
import com.shopapi.producto.Producto;
import com.shopapi.producto.ProductoRepository;
import com.shopapi.producto.ProductoService;
import com.shopapi.usuario.RolUsuario;
import com.shopapi.usuario.Usuario;
import com.shopapi.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock private PedidoRepository pedidoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private ProductoService productoService;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void crear_conStockInsuficiente_lanzaExcepcionYNoTocaElStock() {
        Usuario cliente = Usuario.builder()
                .id(1L).email("ana@test.com").rol(RolUsuario.CLIENTE).build();
        Producto producto = Producto.builder()
                .id(1L).nombre("Teclado").precio(BigDecimal.TEN).stock(2).build();

        when(usuarioRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(cliente));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        PedidoRequestDTO dto = new PedidoRequestDTO(null, List.of(new LineaPedidoRequestDTO(1L, 5)));

        assertThatThrownBy(() -> pedidoService.crear(dto, "ana@test.com"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Stock insuficiente");

        assertThat(producto.getStock()).isEqualTo(2);
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void cambiarEstado_transicionNoValida_lanzaExcepcion() {
        Pedido pedido = Pedido.builder()
                .id(1L).estado(EstadoPedido.PENDIENTE).lineas(new ArrayList<>()).build();

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.cambiarEstado(1L, EstadoPedido.ENVIADO))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("PENDIENTE");
    }

    @Test
    void cambiarEstado_aCancelado_reponeElStockDeCadaLinea() {
        Usuario usuario = Usuario.builder().id(1L).nombre("Kike Admin").build();
        Producto producto = Producto.builder().id(1L).nombre("Teclado").stock(5).build();
        LineaPedido linea = LineaPedido.builder()
                .producto(producto).cantidad(3).precioUnitario(BigDecimal.TEN).build();
        Pedido pedido = Pedido.builder()
                .id(1L).usuario(usuario).estado(EstadoPedido.PENDIENTE)
                .total(BigDecimal.valueOf(30))
                .lineas(new ArrayList<>(List.of(linea))).build();

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        pedidoService.cambiarEstado(1L, EstadoPedido.CANCELADO);

        assertThat(producto.getStock()).isEqualTo(8);
        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.CANCELADO);
    }
}