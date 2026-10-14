package com.pedidos360.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearBindingRequest(
        @NotBlank String queue,
        @NotBlank String exchange,
        @Size(max = 255) String routingKey) { }
