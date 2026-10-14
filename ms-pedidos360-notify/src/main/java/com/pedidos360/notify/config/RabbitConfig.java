package com.pedidos360.notify.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declara la topologia de RabbitMQ (exchange, cola principal, DLX y DLQ).
 * Esta configuracion debe coincidir con el otro servicio (orders/notify):
 * ambos declaran los mismos nombres y argumentos para que la topologia quede
 * correctamente armada sin importar cual de los dos arranque primero.
 */
@Configuration
@EnableConfigurationProperties(RabbitProperties.class)
public class RabbitConfig {

    private final RabbitProperties properties;

    public RabbitConfig(RabbitProperties properties) {
        this.properties = properties;
    }

    @Bean
    public DirectExchange cmdExchange() {
        return new DirectExchange(properties.getExchange());
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(properties.getDeadLetterExchange());
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(properties.getEmailQueue())
                .withArgument("x-dead-letter-exchange", properties.getDeadLetterExchange())
                .build();
    }

    @Bean
    public Queue emailDlq() {
        return QueueBuilder.durable(properties.getEmailDlq()).build();
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue()).to(cmdExchange()).with(properties.getEmailRoutingKey());
    }

    @Bean
    public Binding emailDlqBinding() {
        return BindingBuilder.bind(emailDlq()).to(deadLetterExchange()).with(properties.getEmailRoutingKey());
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // A partir de aqui: infraestructura propia del lado consumidor (no forma parte
    // de la topologia que debe coincidir con orders, orders no consume mensajes).
    @Bean
    public SimpleRabbitListenerContainerFactory manualAckContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jackson2JsonMessageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
