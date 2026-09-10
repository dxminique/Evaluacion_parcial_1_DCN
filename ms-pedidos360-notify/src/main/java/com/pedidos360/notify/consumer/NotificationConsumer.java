package com.pedidos360.notify.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Punto de integración para proveedores de correo y web-push. No expone HTTP ni persiste datos. */
@Component
public class NotificationConsumer {
    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);
    @RabbitListener(queues = "${app.rabbitmq.notifications-queue:order.notifications}")
    public void process(String payload) { log.info("Notificación recibida: {}", payload); }
}
