package com.pedidos360.pedidos.messaging;

import com.pedidos360.pedidos.config.RabbitProperties;
import com.pedidos360.pedidos.entity.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class PedidoEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(PedidoEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final RabbitProperties properties;

    public PedidoEventPublisher(RabbitTemplate rabbitTemplate, RabbitProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    public void publicarPedidoCreado(Pedido pedido) {
        PedidoEventEnvelope envelope = new PedidoEventEnvelope(
                UUID.randomUUID().toString(),
                "PEDIDO_CREADO",
                Instant.now(),
                UUID.randomUUID().toString(),
                new PedidoEventEnvelope.PedidoData(pedido.getId(), pedido.getClienteEmail(), pedido.getEstado().name()));

        try {
            rabbitTemplate.convertAndSend(properties.getExchange(), properties.getEmailRoutingKey(), envelope);
        } catch (Exception e) {
            log.error("No se pudo publicar el evento PEDIDO_CREADO para el pedido {}: {}", pedido.getId(), e.getMessage(), e);
        }
    }
}
