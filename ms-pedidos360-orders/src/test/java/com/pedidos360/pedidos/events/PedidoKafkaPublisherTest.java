package com.pedidos360.pedidos.events;

import com.pedidos360.pedidos.config.KafkaTopicsProperties;
import com.pedidos360.pedidos.entity.EstadoPedido;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PedidoKafkaPublisherTest {

    private KafkaTopicsProperties propiedades() {
        KafkaTopicsProperties properties = new KafkaTopicsProperties();
        properties.getTopics().setPedidoCreado("pedidos.creados");
        properties.getTopics().setPedidoEstado("pedidos.estado");
        return properties;
    }

    private PedidoCreadoEvent eventoDePrueba() {
        return new PedidoCreadoEvent(UUID.randomUUID(), 1L, "cliente@correo.cl", "Pedido de prueba",
                new BigDecimal("12000"), EstadoPedido.PENDIENTE, Instant.now());
    }

    @Test
    @SuppressWarnings("unchecked")
    void publicaPedidoCreadoEnElTopicoCorrectoConLaClaveYElDtoCorrectos() {
        KafkaTemplate<String, Object> kafkaTemplate = mock(KafkaTemplate.class);
        CompletableFuture<SendResult<String, Object>> futuro = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(futuro);

        PedidoKafkaPublisher publisher = new PedidoKafkaPublisher(kafkaTemplate, propiedades());
        PedidoCreadoEvent evento = eventoDePrueba();

        publisher.publicarPedidoCreado(evento);

        verify(kafkaTemplate).send(eq("pedidos.creados"), eq("1"), eq(evento));
    }

    @Test
    @SuppressWarnings("unchecked")
    void noPropagaLaExcepcionCuandoElEnvioFallaDeFormaSincrona() {
        KafkaTemplate<String, Object> kafkaTemplate = mock(KafkaTemplate.class);
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenThrow(new org.apache.kafka.common.errors.TimeoutException("brokers no disponibles"));

        PedidoKafkaPublisher publisher = new PedidoKafkaPublisher(kafkaTemplate, propiedades());

        assertThatCode(() -> publisher.publicarPedidoCreado(eventoDePrueba())).doesNotThrowAnyException();
    }

    @Test
    @SuppressWarnings("unchecked")
    void noPropagaLaExcepcionCuandoElFuturoCompletaConError() {
        KafkaTemplate<String, Object> kafkaTemplate = mock(KafkaTemplate.class);
        CompletableFuture<SendResult<String, Object>> futuro = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(futuro);

        PedidoKafkaPublisher publisher = new PedidoKafkaPublisher(kafkaTemplate, propiedades());

        assertThatCode(() -> {
            publisher.publicarPedidoCreado(eventoDePrueba());
            futuro.completeExceptionally(new RuntimeException("fallo asincronico al enviar"));
        }).doesNotThrowAnyException();
    }
}
