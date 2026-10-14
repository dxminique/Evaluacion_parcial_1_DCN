package com.pedidos360.rabbitadmin.service;

import com.pedidos360.rabbitadmin.dto.CrearColaRequest;
import com.pedidos360.rabbitadmin.exception.RecursoNoEncontradoException;
import com.pedidos360.rabbitadmin.exception.ValidacionException;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RabbitAdminServiceTest {

    private final AmqpAdmin amqpAdmin = mock(AmqpAdmin.class);
    private final RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    private final RabbitAdminService service = new RabbitAdminService(amqpAdmin, rabbitTemplate);

    @Test
    void creaUnaColaValida() {
        Properties propiedades = new Properties();
        propiedades.put(RabbitAdmin.QUEUE_MESSAGE_COUNT, 0);
        propiedades.put(RabbitAdmin.QUEUE_CONSUMER_COUNT, 0);
        when(amqpAdmin.getQueueProperties(eq("q.pedidos"))).thenReturn(propiedades);

        var response = service.crearCola(new CrearColaRequest("q.pedidos", true, false, null, null));

        assertThat(response.name()).isEqualTo("q.pedidos");
        assertThat(response.messageCount()).isZero();
        assertThat(response.consumerCount()).isZero();
        verify(amqpAdmin).declareQueue(any(Queue.class));
    }

    @Test
    void eliminarColaInexistenteLanza404() {
        when(amqpAdmin.getQueueProperties(eq("q.no-existe"))).thenReturn(null);

        assertThatThrownBy(() -> service.eliminarCola("q.no-existe"))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(amqpAdmin, never()).deleteQueue(any());
    }

    @Test
    void rechazaNombreReservado() {
        var request = new CrearColaRequest("amq.reservada", true, false, null, null);

        assertThatThrownBy(() -> service.crearCola(request))
                .isInstanceOf(ValidacionException.class);

        verify(amqpAdmin, never()).declareQueue(any());
    }
}
