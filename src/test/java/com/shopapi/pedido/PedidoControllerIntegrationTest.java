package com.shopapi.pedido;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopapi.IntegrationTestBase;
import com.shopapi.categoria.Categoria;
import com.shopapi.categoria.CategoriaRepository;
import com.shopapi.producto.Producto;
import com.shopapi.producto.ProductoRepository;
import com.shopapi.usuario.RolUsuario;
import com.shopapi.usuario.Usuario;
import com.shopapi.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class PedidoControllerIntegrationTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Long productoId;
    private Long pedidoDeClienteDosId;

    @BeforeEach
    void setUp() {
        Categoria categoria = categoriaRepository.save(Categoria.builder().nombre("Electronica").build());
        Producto producto = productoRepository.save(Producto.builder()
                .nombre("Teclado").precio(BigDecimal.TEN).stock(5).categoria(categoria).build());
        productoId = producto.getId();

        usuarioRepository.save(Usuario.builder()
                .nombre("Cliente Uno").email("cliente1@test.com")
                .password(passwordEncoder.encode("x")).rol(RolUsuario.CLIENTE).build());
        Usuario cliente2 = usuarioRepository.save(Usuario.builder()
                .nombre("Cliente Dos").email("cliente2@test.com")
                .password(passwordEncoder.encode("x")).rol(RolUsuario.CLIENTE).build());

        // Un pedido que pertenece a cliente2, creado directamente por repositorio
        // (sin pasar por HTTP) para poder probar que cliente1 no puede acceder a el.
        Pedido pedidoAjeno = Pedido.builder()
                .usuario(cliente2)
                .fecha(LocalDateTime.now())
                .estado(EstadoPedido.PENDIENTE)
                .total(producto.getPrecio())
                .lineas(new ArrayList<>())
                .build();
        LineaPedido linea = LineaPedido.builder()
                .pedido(pedidoAjeno).producto(producto).cantidad(1).precioUnitario(producto.getPrecio())
                .build();
        pedidoAjeno.getLineas().add(linea);
        pedidoDeClienteDosId = pedidoRepository.save(pedidoAjeno).getId();
    }

    @Test
    void crear_sinAutenticar_devuelve401() throws Exception {
        PedidoRequestDTO dto = new PedidoRequestDTO(null, List.of(new LineaPedidoRequestDTO(productoId, 1)));

        mockMvc.perform(post("/api/v1/pedidos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "cliente1@test.com", roles = "CLIENTE")
    void crear_conLineasVacias_devuelve400() throws Exception {
        PedidoRequestDTO dto = new PedidoRequestDTO(null, List.of());

        mockMvc.perform(post("/api/v1/pedidos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "cliente1@test.com", roles = "CLIENTE")
    void crear_conStockInsuficiente_devuelve409() throws Exception {
        PedidoRequestDTO dto = new PedidoRequestDTO(null, List.of(new LineaPedidoRequestDTO(productoId, 999)));

        mockMvc.perform(post("/api/v1/pedidos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "cliente1@test.com", roles = "CLIENTE")
    void obtenerPedidoAjeno_devuelve403() throws Exception {
        mockMvc.perform(get("/api/v1/pedidos/" + pedidoDeClienteDosId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void cambiarEstado_conRolCliente_devuelve403() throws Exception {
        CambiarEstadoDTO dto = new CambiarEstadoDTO(EstadoPedido.CONFIRMADO);

        mockMvc.perform(patch("/api/v1/pedidos/" + pedidoDeClienteDosId + "/estado")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }
}