package com.pedidos360.notify.consumer.ticket;

import java.time.Instant;

public record PedidoEventEnvelope(
        String eventId,
        String type,
        Instant timestamp,
        String correlationId,
        PedidoData pedido) {

    public record PedidoData(Long id, String clienteEmail, String estado) { }
}
