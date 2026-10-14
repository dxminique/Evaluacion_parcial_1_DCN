package com.pedidos360.rabbitadmin.dto;

import jakarta.validation.constraints.*;

public record CrearColaRequest(
        @NotBlank @Size(max = 255)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "solo se permiten letras, numeros, punto, guion y guion bajo")
        String name,

        @NotNull Boolean durable,

        @NotNull Boolean autoDelete,

        @Size(max = 255) String deadLetterExchange,

        @Size(max = 255) String deadLetterRoutingKey) { }
