package com.pedidos360.rabbitadmin.dto;

import jakarta.validation.constraints.*;

public record CrearExchangeRequest(
        @NotBlank @Size(max = 255)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "solo se permiten letras, numeros, punto, guion y guion bajo")
        String name,

        @NotBlank
        @Pattern(regexp = "direct|topic|fanout|headers", message = "type debe ser direct, topic, fanout o headers")
        String type,

        @NotNull Boolean durable,

        @NotNull Boolean autoDelete) { }
