package com.pedidos360.bff;

import com.pedidos360.bff.config.SecurityConfig;
import com.pedidos360.bff.controller.PingController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba la cadena de seguridad real (SecurityConfig) sin depender de internet ni de
 * Azure AD: el JwtDecoder se reemplaza por un mock, de modo que el token "simulado"
 * nunca se valida contra el JWK Set real.
 */
@WebMvcTest(PingController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void pingEsPublicoSinToken() throws Exception {
        mockMvc.perform(get("/api/ping"))
                .andExpect(status().isOk());
    }

    @Test
    void securePingSinTokenRespondeNoAutorizado() throws Exception {
        mockMvc.perform(get("/api/secure-ping"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void securePingConJwtSimuladoRespondeOk() throws Exception {
        Jwt jwtSimulado = Jwt.withTokenValue("token-simulado")
                .header("alg", "none")
                .claim("sub", "usuario-de-prueba")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        when(jwtDecoder.decode(anyString())).thenReturn(jwtSimulado);

        mockMvc.perform(get("/api/secure-ping").header("Authorization", "Bearer token-simulado"))
                .andExpect(status().isOk());
    }
}
