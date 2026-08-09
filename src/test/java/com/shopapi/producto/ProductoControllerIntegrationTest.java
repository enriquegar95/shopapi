package com.shopapi.producto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopapi.IntegrationTestBase;
import com.shopapi.categoria.Categoria;
import com.shopapi.categoria.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ProductoControllerIntegrationTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired private CategoriaRepository categoriaRepository;

    private Long categoriaId;

    @BeforeEach
    void setUp() {
        Categoria categoria = categoriaRepository.save(
                Categoria.builder().nombre("Electronica").build());
        categoriaId = categoria.getId();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crear_conDatosValidosYRolAdmin_devuelve201() throws Exception {
        ProductoRequestDTO dto = new ProductoRequestDTO(
                "Teclado", BigDecimal.valueOf(59.99), 10, categoriaId);

        mockMvc.perform(post("/api/v1/productos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Teclado"));
    }

    @Test
    void crear_sinAutenticar_devuelve401() throws Exception {
        ProductoRequestDTO dto = new ProductoRequestDTO(
                "Teclado", BigDecimal.valueOf(59.99), 10, categoriaId);

        mockMvc.perform(post("/api/v1/productos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void crear_conRolCliente_devuelve403() throws Exception {
        ProductoRequestDTO dto = new ProductoRequestDTO(
                "Teclado", BigDecimal.valueOf(59.99), 10, categoriaId);

        mockMvc.perform(post("/api/v1/productos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crear_conPrecioNegativo_devuelve400() throws Exception {
        ProductoRequestDTO dto = new ProductoRequestDTO(
                "Teclado", BigDecimal.valueOf(-5), 10, categoriaId);

        mockMvc.perform(post("/api/v1/productos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}