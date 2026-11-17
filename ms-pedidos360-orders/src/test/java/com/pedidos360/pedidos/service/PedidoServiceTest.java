package com.pedidos360.pedidos.service;

import com.pedidos360.pedidos.dto.CrearPedidoRequest;
import com.pedidos360.pedidos.entity.EstadoPedido;
import com.pedidos360.pedidos.entity.Pedido;
import com.pedidos360.pedidos.messaging.PedidoEventPublisher;
import com.pedidos360.pedidos.repository.PedidoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PedidoServiceTest {

    private PedidoRepository repositorioConGuardadoSimulado() throws Exception {
        PedidoRepository repository = mock(PedidoRepository.class);
        when(repository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            var method = Pedido.class.getDeclaredMethod("antesDeGuardar");
            method.setAccessible(true);
            method.invoke(pedido);
            return pedido;
        });
        return repository;
    }

    @Test
    void creaPedidoPendiente() throws Exception {
        PedidoRepository repository = repositorioConGuardadoSimulado();
        PedidoEventPublisher eventPublisher = mock(PedidoEventPublisher.class);
        ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);

        var response = new PedidoService(repository, eventPublisher, applicationEventPublisher)
                .crear(new CrearPedidoRequest("cliente@correo.cl", "Pedido de prueba", new BigDecimal("12000")));

        assertThat(response.estado()).isEqualTo(EstadoPedido.PENDIENTE);
        verify(repository).save(any(Pedido.class));
        verify(eventPublisher).publicarPedidoCreado(any(Pedido.class));
        verify(applicationEventPublisher).publishEvent(any(Object.class));
    }

    @Test
    void creaPedidoIgualYSiguePublicandoEnRabbitMqAunqueFalleLaPublicacionDelEventoDeDominio() throws Exception {
        PedidoRepository repository = repositorioConGuardadoSimulado();
        PedidoEventPublisher eventPublisher = mock(PedidoEventPublisher.class);
        ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);
        doThrow(new RuntimeException("fallo simulado al publicar el evento de dominio (Kafka no disponible)"))
                .when(applicationEventPublisher).publishEvent(any(Object.class));

        var response = new PedidoService(repository, eventPublisher, applicationEventPublisher)
                .crear(new CrearPedidoRequest("cliente@correo.cl", "Pedido de prueba", new BigDecimal("12000")));

        assertThat(response.estado()).isEqualTo(EstadoPedido.PENDIENTE);
        verify(repository).save(any(Pedido.class));
        verify(eventPublisher).publicarPedidoCreado(any(Pedido.class));
        verify(applicationEventPublisher).publishEvent(any(Object.class));
    }
}
