package com.pedidos360.kafkaadmin.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CrearTopicoRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void rechazaNombreVacio() {
        var request = new CrearTopicoRequest("", 3, (short) 2, null);
        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void rechazaNombreConCaracteresInvalidos() {
        var request = new CrearTopicoRequest("pedidos creados", 3, (short) 2, null);
        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void rechazaParticionesMenorA1() {
        var request = new CrearTopicoRequest("pedidos.prueba", 0, (short) 2, null);
        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void rechazaReplicationFactorMenorA1() {
        var request = new CrearTopicoRequest("pedidos.prueba", 3, (short) 0, null);
        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void aceptaUnaSolicitudValidaConConfigsOpcionales() {
        var request = new CrearTopicoRequest("pedidos.prueba", 3, (short) 2, Map.of("retention.ms", "60000"));
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void aceptaUnaSolicitudValidaSinConfigs() {
        var request = new CrearTopicoRequest("pedidos.prueba", 3, (short) 2, null);
        assertThat(validator.validate(request)).isEmpty();
    }
}
