package com.pedidos360.notify.consumer.stock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/** Solo registra en log los mensajes que terminaron en la DLQ de stock; no reprocesa. */
@Component
public class StockDeadLetterConsumer {
    private static final Logger log = LoggerFactory.getLogger(StockDeadLetterConsumer.class);

    @RabbitListener(queues = "${app.rabbit.stock-dlq}")
    public void recibirDeDlq(PedidoEventEnvelope envelope,
                              @Header(name = "x-death", required = false) List<Map<String, Object>> xDeath) {
        Map<String, Object> muerte = (xDeath != null && !xDeath.isEmpty()) ? xDeath.get(0) : Map.of();

        log.error("Mensaje en la DLQ de stock -> eventId={}, correlationId={}, contenido={}, colaOrigen={}, motivo={}",
                envelope != null ? envelope.eventId() : null,
                envelope != null ? envelope.correlationId() : null,
                envelope,
                muerte.get("queue"),
                muerte.get("reason"));
    }
}
