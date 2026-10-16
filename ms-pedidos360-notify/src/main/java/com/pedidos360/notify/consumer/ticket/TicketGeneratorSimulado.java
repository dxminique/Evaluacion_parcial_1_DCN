package com.pedidos360.notify.consumer.ticket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class TicketGeneratorSimulado {
    private static final Logger log = LoggerFactory.getLogger(TicketGeneratorSimulado.class);
    private static final double PROBABILIDAD_FALLO_TRANSITORIO = 0.3;

    public void generar(PedidoEventEnvelope.PedidoData pedido) {
        if (pedido == null || pedido.id() == null) {
            throw new TicketInvalidoException("El evento no trae el id del pedido, no se puede generar el ticket");
        }
        if (ThreadLocalRandom.current().nextDouble() < PROBABILIDAD_FALLO_TRANSITORIO) {
            throw new TicketTransitorioException("Servicio de generacion de tickets no disponible momentaneamente");
        }
        log.info("Ticket simulado generado para el pedido {} (estado {})", pedido.id(), pedido.estado());
    }
}
