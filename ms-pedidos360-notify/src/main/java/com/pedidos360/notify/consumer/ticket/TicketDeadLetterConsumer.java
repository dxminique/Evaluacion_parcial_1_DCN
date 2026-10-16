package com.pedidos360.notify.consumer.ticket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/** Solo registra en log los mensajes que terminaron en la DLQ de ticket; no reprocesa. */
@Component
public class TicketDeadLetterConsumer {
    private static final Logger log = LoggerFactory.getLogger(TicketDeadLetterConsumer.class);

    @RabbitListener(queues = "${app.rabbit.ticket-dlq}")
    public void recibirDeDlq(PedidoEventEnvelope envelope,
                              @Header(name = "x-death", required = false) List<Map<String, Object>> xDeath) {
        Map<String, Object> muerte = (xDeath != null && !xDeath.isEmpty()) ? xDeath.get(0) : Map.of();

        log.error("Mensaje en la DLQ de ticket -> eventId={}, correlationId={}, contenido={}, colaOrigen={}, motivo={}",
                envelope != null ? envelope.eventId() : null,
                envelope != null ? envelope.correlationId() : null,
                envelope,
                muerte.get("queue"),
                muerte.get("reason"));
    }
}
