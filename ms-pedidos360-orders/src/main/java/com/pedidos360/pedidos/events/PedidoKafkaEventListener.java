package com.pedidos360.pedidos.events;

import com.pedidos360.pedidos.entity.Pedido;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Traduce los eventos internos de Spring (publicados dentro de la transaccion de
 * PedidoService) a los eventos de Kafka, pero solo DESPUES de que la transaccion de
 * base de datos ya se confirmo (AFTER_COMMIT). fallbackExecution=true: si por algun
 * motivo no hay una transaccion activa (ej. en una prueba), se ejecuta igual al
 * instante en vez de perderse el evento silenciosamente.
 */
@Component
public class PedidoKafkaEventListener {
    private final PedidoKafkaPublisher publisher;

    public PedidoKafkaEventListener(PedidoKafkaPublisher publisher) {
        this.publisher = publisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void alCrearPedido(PedidoCreadoAppEvent evento) {
        Pedido pedido = evento.pedido();
        PedidoCreadoEvent dto = new PedidoCreadoEvent(
                UUID.randomUUID(), pedido.getId(), pedido.getClienteEmail(), pedido.getDescripcion(),
                pedido.getTotal(), pedido.getEstado(), Instant.now());
        publisher.publicarPedidoCreado(dto);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void alCambiarEstado(PedidoEstadoAppEvent evento) {
        Pedido pedido = evento.pedido();
        PedidoEstadoEvent dto = new PedidoEstadoEvent(
                UUID.randomUUID(), pedido.getId(), evento.estadoAnterior(), pedido.getEstado(), Instant.now());
        publisher.publicarCambioEstado(dto);
    }
}
