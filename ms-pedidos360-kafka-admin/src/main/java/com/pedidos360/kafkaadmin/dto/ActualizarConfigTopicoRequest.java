package com.pedidos360.kafkaadmin.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Map;

public record ActualizarConfigTopicoRequest(@NotEmpty Map<String, String> configs) { }
