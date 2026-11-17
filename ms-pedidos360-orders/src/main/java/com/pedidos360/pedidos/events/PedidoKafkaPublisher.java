package com.pedidos360.pedidos.events;

import com.pedidos360.pedidos.config.KafkaTopicsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publicador de Kafka, SEPARADO del publicador de RabbitMQ (PedidoEventPublisher).
 * La clave de cada mensaje es el id del pedido como String, para que todos los
 * eventos de un mismo pedido caigan en la misma particion y se procesen en orden.
 * Nunca lanza una excepcion hacia afuera: un fallo de Kafka solo se registra en log
 * y no debe afectar la creacion/actualizacion del pedido ni a RabbitMQ.
 */
@Component
public class PedidoKafkaPublisher {
    private static final Logger log = LoggerFactory.getLogger(PedidoKafkaPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties properties;

    public PedidoKafkaPublisher(KafkaTemplate<String, Object> kafkaTemplate, KafkaTopicsProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    public void publicarPedidoCreado(PedidoCreadoEvent evento) {
        publicar(properties.getTopics().getPedidoCreado(), evento.pedidoId(), evento.eventId(), evento);
    }

    public void publicarCambioEstado(PedidoEstadoEvent evento) {
        publicar(properties.getTopics().getPedidoEstado(), evento.pedidoId(), evento.eventId(), evento);
    }

    private void publicar(String topic, Long pedidoId, java.util.UUID eventId, Object payload) {
        String key = String.valueOf(pedidoId);
        try {
            kafkaTemplate.send(topic, key, payload).whenComplete((resultado, error) -> {
                if (error != null) {
                    log.error("Fallo al publicar en Kafka: topico={}, pedidoId={}, eventId={}, causa={}",
                            topic, pedidoId, eventId, error.getMessage(), error);
                } else {
                    var metadata = resultado.getRecordMetadata();
                    log.info("Publicado en Kafka: topico={}, particion={}, offset={}, pedidoId={}, eventId={}",
                            metadata.topic(), metadata.partition(), metadata.offset(), pedidoId, eventId);
                }
            });
        } catch (Exception e) {
            log.error("No se pudo publicar en Kafka (fallo sincrono, ej. brokers no disponibles): topico={}, pedidoId={}, eventId={}, causa={}",
                    topic, pedidoId, eventId, e.getMessage(), e);
        }
    }
}
