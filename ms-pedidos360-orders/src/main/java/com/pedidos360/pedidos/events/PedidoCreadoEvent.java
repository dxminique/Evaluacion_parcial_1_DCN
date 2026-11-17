package com.pedidos360.pedidos.events;

import com.pedidos360.pedidos.entity.EstadoPedido;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PedidoCreadoEvent(
        @NotNull UUID eventId,
        @NotNull Long pedidoId,
        @NotBlank @Email String clienteEmail,
        @NotBlank String descripcion,
        @NotNull BigDecimal total,
        @NotNull EstadoPedido estado,
        @NotNull Instant occurredAt) { }
