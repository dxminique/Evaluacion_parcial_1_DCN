package com.pedidos360.pedidos.events;

import com.pedidos360.pedidos.entity.EstadoPedido;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record PedidoEstadoEvent(
        @NotNull UUID eventId,
        @NotNull Long pedidoId,
        EstadoPedido estadoAnterior,
        @NotNull EstadoPedido estadoNuevo,
        @NotNull Instant occurredAt) { }
