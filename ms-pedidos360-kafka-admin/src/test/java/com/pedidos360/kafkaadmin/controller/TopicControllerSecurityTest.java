package com.pedidos360.kafkaadmin.controller;

import com.pedidos360.kafkaadmin.config.SecurityConfig;
import com.pedidos360.kafkaadmin.service.KafkaAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba la cadena de seguridad real (SecurityConfig) sin depender de internet ni de
 * Azure AD: el JwtDecoder se reemplaza por un mock, de modo que los tokens "simulados"
 * nunca se validan contra el JWK Set real.
 */
@WebMvcTest(TopicController.class)
@Import(SecurityConfig.class)
class TopicControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private KafkaAdminService service;

    @Test
    void sinTokenRespondeNoAutorizado() throws Exception {
        mockMvc.perform(get("/api/kafka/topics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void conRolDistintoDeAdminRespondeProhibido() throws Exception {
        when(jwtDecoder.decode("token-cliente")).thenReturn(construirJwt("token-cliente", List.of("Cliente")));

        mockMvc.perform(get("/api/kafka/topics").header("Authorization", "Bearer token-cliente"))
                .andExpect(status().isForbidden());
    }

    @Test
    void conRolAdminRespondeOk() throws Exception {
        when(service.listarTopicos()).thenReturn(List.of());
        when(jwtDecoder.decode("token-admin")).thenReturn(construirJwt("token-admin", List.of("Admin")));

        mockMvc.perform(get("/api/kafka/topics").header("Authorization", "Bearer token-admin"))
                .andExpect(status().isOk());
    }

    private Jwt construirJwt(String valor, List<String> roles) {
        return Jwt.withTokenValue(valor)
                .header("alg", "none")
                .claim("sub", "usuario-de-prueba")
                .claim("roles", roles)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }
}
