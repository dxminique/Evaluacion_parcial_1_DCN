package com.pedidos360.rabbitadmin.service;

import com.pedidos360.rabbitadmin.dto.ColaResponse;
import com.pedidos360.rabbitadmin.dto.CrearBindingRequest;
import com.pedidos360.rabbitadmin.dto.CrearColaRequest;
import com.pedidos360.rabbitadmin.dto.CrearExchangeRequest;
import com.pedidos360.rabbitadmin.exception.RabbitPreconditionFailedException;
import com.pedidos360.rabbitadmin.exception.RecursoNoEncontradoException;
import com.pedidos360.rabbitadmin.exception.ValidacionException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ShutdownSignalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Service
public class RabbitAdminService {
    private static final Logger log = LoggerFactory.getLogger(RabbitAdminService.class);
    private static final String PREFIJO_RESERVADO = "amq.";
    private static final Set<String> TIPOS_EXCHANGE_PERMITIDOS = Set.of("direct", "topic", "fanout", "headers");
    private static final int REPLY_CODE_NOT_FOUND = 404;
    private static final int REPLY_CODE_PRECONDITION_FAILED = 406;

    private final AmqpAdmin amqpAdmin;
    private final RabbitTemplate rabbitTemplate;

    public RabbitAdminService(AmqpAdmin amqpAdmin, RabbitTemplate rabbitTemplate) {
        this.amqpAdmin = amqpAdmin;
        this.rabbitTemplate = rabbitTemplate;
    }

    // ----- Colas -----

    public ColaResponse crearCola(CrearColaRequest request) {
        validarNombrePermitido("name", request.name());
        if (request.deadLetterExchange() != null) {
            validarNombrePermitido("deadLetterExchange", request.deadLetterExchange());
        }

        Map<String, Object> argumentos = new HashMap<>();
        if (request.deadLetterExchange() != null && !request.deadLetterExchange().isBlank()) {
            argumentos.put("x-dead-letter-exchange", request.deadLetterExchange());
        }
        if (request.deadLetterRoutingKey() != null && !request.deadLetterRoutingKey().isBlank()) {
            argumentos.put("x-dead-letter-routing-key", request.deadLetterRoutingKey());
        }

        Queue queue = new Queue(request.name(), request.durable(), false, request.autoDelete(), argumentos);
        try {
            amqpAdmin.declareQueue(queue);
        } catch (AmqpException e) {
            throw traducirErrorDeDeclare(e);
        }
        log.info("Cola creada: {}", request.name());
        return obtenerCola(request.name());
    }

    public ColaResponse obtenerCola(String name) {
        Properties propiedades = amqpAdmin.getQueueProperties(name);
        if (propiedades == null) {
            throw new RecursoNoEncontradoException("La cola '" + name + "' no existe");
        }
        Integer mensajes = (Integer) propiedades.get(RabbitAdmin.QUEUE_MESSAGE_COUNT);
        Integer consumidores = (Integer) propiedades.get(RabbitAdmin.QUEUE_CONSUMER_COUNT);
        return new ColaResponse(name, mensajes, consumidores);
    }

    public void eliminarCola(String name) {
        if (amqpAdmin.getQueueProperties(name) == null) {
            throw new RecursoNoEncontradoException("La cola '" + name + "' no existe");
        }
        amqpAdmin.deleteQueue(name);
        log.info("Cola eliminada: {}", name);
    }

    // ----- Exchanges -----

    public void crearExchange(CrearExchangeRequest request) {
        validarNombrePermitido("name", request.name());

        String tipo = request.type() == null ? "" : request.type().toLowerCase();
        validarTipoPermitido(tipo);

        Exchange exchange = construirExchange(request.name(), tipo, request.durable(), request.autoDelete());
        try {
            amqpAdmin.declareExchange(exchange);
        } catch (AmqpException e) {
            throw traducirErrorDeDeclare(e);
        }
        log.info("Exchange creado: {} (tipo {})", request.name(), tipo);
    }

    public void eliminarExchange(String name) {
        validarNombrePermitido("name", name);

        if (!existeExchange(name)) {
            throw new RecursoNoEncontradoException("El exchange '" + name + "' no existe");
        }
        amqpAdmin.deleteExchange(name);
        log.info("Exchange eliminado: {}", name);
    }

    // ----- Bindings -----

    public void crearBinding(CrearBindingRequest request) {
        String routingKey = request.routingKey() == null ? "" : request.routingKey();
        Binding binding = new Binding(request.queue(), Binding.DestinationType.QUEUE, request.exchange(), routingKey, null);
        try {
            amqpAdmin.declareBinding(binding);
        } catch (AmqpException e) {
            throw traducirErrorDeDeclare(e);
        }
        log.info("Binding creado: cola={}, exchange={}, routingKey={}", request.queue(), request.exchange(), routingKey);
    }

    public void eliminarBinding(String queue, String exchange, String routingKey) {
        String key = routingKey == null ? "" : routingKey;
        Binding binding = new Binding(queue, Binding.DestinationType.QUEUE, exchange, key, null);
        amqpAdmin.removeBinding(binding);
        log.info("Binding eliminado: cola={}, exchange={}, routingKey={}", queue, exchange, key);
    }

    // ----- Helpers -----

    private void validarNombrePermitido(String campo, String nombre) {
        if (nombre != null && nombre.toLowerCase().startsWith(PREFIJO_RESERVADO)) {
            throw new ValidacionException(campo,
                    "no puede empezar con '" + PREFIJO_RESERVADO + "', ese prefijo esta reservado por RabbitMQ");
        }
    }

    private void validarTipoPermitido(String tipo) {
        if (!TIPOS_EXCHANGE_PERMITIDOS.contains(tipo)) {
            throw new ValidacionException("type", "debe ser uno de " + TIPOS_EXCHANGE_PERMITIDOS);
        }
    }

    private Exchange construirExchange(String name, String tipo, boolean durable, boolean autoDelete) {
        return switch (tipo) {
            case "direct" -> new DirectExchange(name, durable, autoDelete);
            case "topic" -> new TopicExchange(name, durable, autoDelete);
            case "fanout" -> new FanoutExchange(name, durable, autoDelete);
            case "headers" -> new HeadersExchange(name, durable, autoDelete);
            default -> throw new ValidacionException("type", "debe ser uno de " + TIPOS_EXCHANGE_PERMITIDOS);
        };
    }

    private boolean existeExchange(String name) {
        try {
            rabbitTemplate.execute(channel -> {
                channel.exchangeDeclarePassive(name);
                return null;
            });
            return true;
        } catch (AmqpException e) {
            if (extraerCodigoDeRespuesta(e) == REPLY_CODE_NOT_FOUND) {
                return false;
            }
            throw e;
        }
    }

    private RuntimeException traducirErrorDeDeclare(AmqpException e) {
        if (extraerCodigoDeRespuesta(e) == REPLY_CODE_PRECONDITION_FAILED) {
            return new RabbitPreconditionFailedException(
                    "Ya existe un recurso con ese nombre pero con una configuracion distinta");
        }
        return e;
    }

    private int extraerCodigoDeRespuesta(Throwable e) {
        Throwable actual = e;
        while (actual != null) {
            if (actual instanceof ShutdownSignalException sse && sse.getReason() instanceof AMQP.Channel.Close close) {
                return close.getReplyCode();
            }
            actual = actual.getCause();
        }
        return -1;
    }
}
