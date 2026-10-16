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

    /**
     * Publica los 3 eventos asociados a la creacion de un pedido: notificacion por
     * correo, generacion de ticket y reserva de stock simulada. Cada publicacion es
     * independiente: si una falla, se registra en log y no afecta a las demas ni a
     * la creacion del pedido (que ya ocurrio antes de llamar a este metodo).
     */
    public void publicarPedidoCreado(Pedido pedido) {
        PedidoEventEnvelope envelope = new PedidoEventEnvelope(
                UUID.randomUUID().toString(),
                "PEDIDO_CREADO",
                Instant.now(),
                UUID.randomUUID().toString(),
                new PedidoEventEnvelope.PedidoData(pedido.getId(), pedido.getClienteEmail(), pedido.getEstado().name()));

        publicar(properties.getExchange(), properties.getEmailRoutingKey(), envelope);
        publicar(properties.getExchange(), properties.getTicketRoutingKey(), envelope);
        publicar(properties.getTopicExchange(), properties.getStockRoutingKey(), envelope);
    }

    private void publicar(String exchange, String routingKey, PedidoEventEnvelope envelope) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, envelope);
        } catch (Exception e) {
            log.error("No se pudo publicar en exchange={} routingKey={} para el pedido {}: {}",
                    exchange, routingKey, envelope.pedido().id(), e.getMessage(), e);
        }
    }
}
