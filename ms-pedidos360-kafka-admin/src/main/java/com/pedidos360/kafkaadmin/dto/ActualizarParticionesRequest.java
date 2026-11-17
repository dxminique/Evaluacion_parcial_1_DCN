package com.pedidos360.kafkaadmin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ActualizarParticionesRequest(@NotNull @Min(1) Integer partitions) { }
