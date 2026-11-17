package com.pedidos360.kafkaadmin.dto;

import jakarta.validation.constraints.*;

import java.util.Map;

public record CrearTopicoRequest(
        @NotBlank @Size(max = 249)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "solo se permiten letras, numeros, punto, guion y guion bajo")
        String name,

        @NotNull @Min(1) Integer partitions,

        @NotNull @Min(1) Short replicationFactor,

        Map<String, String> configs) { }
