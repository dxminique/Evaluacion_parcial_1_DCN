package com.pedidos360.pedidos.dto;

import com.pedidos360.pedidos.entity.EstadoPedido;
import jakarta.validation.constraints.NotNull;

public record ActualizarEstadoPedidoRequest(@NotNull EstadoPedido estado) { }
