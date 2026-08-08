package com.shopapi.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(
                jwtService, "secretKey", "clave-de-pruebas-de-al-menos-32-caracteres-1234");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3_600_000L);
    }

    @Test
    void generarYValidarToken_funcionaDePrincipioAFin() {
        UserDetails userDetails = User.withUsername("ana@test.com")
                .password("x").authorities("ROLE_CLIENTE").build();

        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.extractUsername(token)).isEqualTo("ana@test.com");
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void isTokenValid_conUsuarioDistinto_devuelveFalse() {
        UserDetails ana = User.withUsername("ana@test.com").password("x").authorities("ROLE_CLIENTE").build();
        UserDetails luis = User.withUsername("luis@test.com").password("x").authorities("ROLE_CLIENTE").build();

        String token = jwtService.generateToken(ana);

        assertThat(jwtService.isTokenValid(token, luis)).isFalse();
    }
}