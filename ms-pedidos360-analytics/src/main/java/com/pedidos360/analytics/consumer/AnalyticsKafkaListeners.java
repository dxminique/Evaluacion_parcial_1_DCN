package com.pedidos360.analytics.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsKafkaListeners {
    private static final Logger log = LoggerFactory.getLogger(AnalyticsKafkaListeners.class);
    private final PedidoEventValidator validator;

    public AnalyticsKafkaListeners(PedidoEventValidator validator) {
        this.validator = validator;
    }

    @KafkaListener(topics = {"${app.kafka.topics.pedido-creado}", "${app.kafka.topics.pedido-estado}"}, groupId = "${app.kafka.groups.dashboard}")
    public void consumirDashboard(String payload, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Dashboard recibió evento {} desde {}", validator.validar(topic, payload), topic);
    }

    @KafkaListener(topics = {"${app.kafka.topics.pedido-creado}", "${app.kafka.topics.pedido-estado}"}, groupId = "${app.kafka.groups.ventas}")
    public void consumirVentas(String payload, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Analítica de ventas recibió evento {} desde {}", validator.validar(topic, payload), topic);
    }

    @KafkaListener(topics = {"${app.kafka.topics.pedido-creado}", "${app.kafka.topics.pedido-estado}"}, groupId = "${app.kafka.groups.auditoria}")
    public void consumirAuditoria(String payload, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Auditoría recibió evento {} desde {}", validator.validar(topic, payload), topic);
    }
}
