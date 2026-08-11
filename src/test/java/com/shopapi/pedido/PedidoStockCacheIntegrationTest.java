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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureMockMvc
class PedidoStockCacheIntegrationTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser(username = "cliente@test.com", roles = "CLIENTE")
    void crearPedido_invalidaLaCacheDelProductoAfectado() throws Exception {
        Categoria categoria = categoriaRepository.save(Categoria.builder().nombre("Electronica").build());
        Producto producto = productoRepository.save(Producto.builder()
                .nombre("Teclado").precio(BigDecimal.TEN).stock(10).categoria(categoria).build());

        usuarioRepository.save(Usuario.builder()
                .nombre("Cliente Test").email("cliente@test.com")
                .password(passwordEncoder.encode("x")).rol(RolUsuario.CLIENTE).build());

        // Primera lectura: se cachea con stock = 10
        mockMvc.perform(get("/api/v1/productos/" + producto.getId()))
                .andExpect(jsonPath("$.stock").value(10));

        // Se crea un pedido que descuenta 4 unidades
        PedidoRequestDTO dto = new PedidoRequestDTO(null,
                List.of(new LineaPedidoRequestDTO(producto.getId(), 4)));

        mockMvc.perform(post("/api/v1/pedidos")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(dto)));

        // Segunda lectura: si la cache se invalido correctamente, refleja el stock real (6)
        mockMvc.perform(get("/api/v1/productos/" + producto.getId()))
                .andExpect(jsonPath("$.stock").value(6));
    }
}