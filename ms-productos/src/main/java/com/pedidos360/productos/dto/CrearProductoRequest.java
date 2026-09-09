package com.pedidos360.productos.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CrearProductoRequest(
        @NotBlank @Size(max = 150) String nombre,
        @NotBlank @Size(max = 500) String descripcion,
        @NotNull @DecimalMin(value = "0.01") BigDecimal precio,
        @NotNull @PositiveOrZero Integer stock) { }
