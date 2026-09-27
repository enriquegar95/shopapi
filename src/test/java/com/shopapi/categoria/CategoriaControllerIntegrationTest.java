package com.shopapi.categoria;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopapi.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class CategoriaControllerIntegrationTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser(roles = "ADMIN")
    void crear_conRolAdmin_devuelve201() throws Exception {
        CategoriaRequestDTO dto = new CategoriaRequestDTO("Electronica");

        mockMvc.perform(post("/api/v1/categorias")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Electronica"));
    }

    @Test
    void crear_sinAutenticar_devuelve401() throws Exception {
        CategoriaRequestDTO dto = new CategoriaRequestDTO("Electronica");

        mockMvc.perform(post("/api/v1/categorias")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void crear_conRolCliente_devuelve403() throws Exception {
        CategoriaRequestDTO dto = new CategoriaRequestDTO("Electronica");

        mockMvc.perform(post("/api/v1/categorias")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crear_conNombreVacio_devuelve400() throws Exception {
        CategoriaRequestDTO dto = new CategoriaRequestDTO("");

        mockMvc.perform(post("/api/v1/categorias")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "VENDEDOR")
    void eliminar_conRolVendedor_devuelve403() throws Exception {
        // VENDEDOR puede crear (arriba) pero no puede borrar — confirma
        // que la autorizacion se evalua endpoint por endpoint, no por controller entero.
        mockMvc.perform(delete("/api/v1/categorias/1"))
                .andExpect(status().isForbidden());
    }
}