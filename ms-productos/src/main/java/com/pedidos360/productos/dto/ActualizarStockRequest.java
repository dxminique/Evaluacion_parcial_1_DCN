package com.pedidos360.productos.dto;

import jakarta.validation.constraints.*;

public record ActualizarStockRequest(@NotNull @PositiveOrZero Integer stock) { }
