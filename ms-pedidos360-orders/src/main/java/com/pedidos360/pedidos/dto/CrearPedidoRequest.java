package com.pedidos360.pedidos.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CrearPedidoRequest(
        @NotBlank @Email @Size(max = 150) String clienteEmail,
        @NotBlank @Size(max = 500) String descripcion,
        @NotNull @DecimalMin(value = "0.01") BigDecimal total) { }
