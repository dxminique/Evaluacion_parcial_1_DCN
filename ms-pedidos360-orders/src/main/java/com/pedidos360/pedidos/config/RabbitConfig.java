package com.pedidos360.pedidos.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declara la topologia de RabbitMQ (exchanges, colas de negocio, DLX y DLQ).
 * Esta configuracion debe coincidir con el otro servicio (orders/notify):
 * ambos declaran los mismos nombres y argumentos para que la topologia quede
 * correctamente armada sin importar cual de los dos arranque primero.
 *
 * Casos de uso (todas las colas, de negocio y DLQ, son colas quorum para
 * que la replicacion funcione en el cluster de RabbitMQ):
 * - email: notificacion por correo al crear un pedido (cmd.direct / email.send).
 * - ticket: generacion de ticket al crear un pedido (cmd.direct / ticket.generate).
 * - stock: reserva de stock simulada al crear un pedido (evt.topic / pedido.creado.#).
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
    public TopicExchange evtTopicExchange() {
        return new TopicExchange(properties.getTopicExchange());
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(properties.getDeadLetterExchange());
    }

    // ----- email -----

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(properties.getEmailQueue())
                .quorum()
                .withArgument("x-dead-letter-exchange", properties.getDeadLetterExchange())
                .build();
    }

    @Bean
    public Queue emailDlq() {
        return QueueBuilder.durable(properties.getEmailDlq()).quorum().build();
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue()).to(cmdExchange()).with(properties.getEmailRoutingKey());
    }

    @Bean
    public Binding emailDlqBinding() {
        return BindingBuilder.bind(emailDlq()).to(deadLetterExchange()).with(properties.getEmailRoutingKey());
    }

    // ----- ticket -----

    @Bean
    public Queue ticketQueue() {
        return QueueBuilder.durable(properties.getTicketQueue())
                .quorum()
                .withArgument("x-dead-letter-exchange", properties.getDeadLetterExchange())
                .build();
    }

    @Bean
    public Queue ticketDlq() {
        return QueueBuilder.durable(properties.getTicketDlq()).quorum().build();
    }

    @Bean
    public Binding ticketBinding() {
        return BindingBuilder.bind(ticketQueue()).to(cmdExchange()).with(properties.getTicketRoutingKey());
    }

    @Bean
    public Binding ticketDlqBinding() {
        return BindingBuilder.bind(ticketDlq()).to(deadLetterExchange()).with(properties.getTicketRoutingKey());
    }

    // ----- stock -----

    @Bean
    public Queue stockQueue() {
        return QueueBuilder.durable(properties.getStockQueue())
                .quorum()
                .withArgument("x-dead-letter-exchange", properties.getDeadLetterExchange())
                .build();
    }

    @Bean
    public Queue stockDlq() {
        return QueueBuilder.durable(properties.getStockDlq()).quorum().build();
    }

    @Bean
    public Binding stockBinding() {
        return BindingBuilder.bind(stockQueue()).to(evtTopicExchange()).with(properties.getStockBindingPattern());
    }

    @Bean
    public Binding stockDlqBinding() {
        // El dead-lettering conserva la routing key original del mensaje (pedido.creado.nuevo),
        // no el patron de binding de la cola de origen.
        return BindingBuilder.bind(stockDlq()).to(deadLetterExchange()).with(properties.getStockRoutingKey());
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
