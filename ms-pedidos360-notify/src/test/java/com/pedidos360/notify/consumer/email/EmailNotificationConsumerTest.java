package com.pedidos360.notify.consumer.email;

import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class EmailNotificationConsumerTest {

    private final PedidoEventEnvelope.PedidoData pedido =
            new PedidoEventEnvelope.PedidoData(1L, "cliente@correo.cl", "PENDIENTE");
    private final PedidoEventEnvelope envelope =
            new PedidoEventEnvelope("evt-1", "PEDIDO_CREADO", Instant.now(), "corr-1", pedido);

    @Test
    void confirmaElMensajeCuandoElEnvioEsExitoso() throws Exception {
        EmailSenderSimulado emailSender = mock(EmailSenderSimulado.class);
        Channel channel = mock(Channel.class);
        EmailNotificationConsumer consumer = new EmailNotificationConsumer(emailSender);

        consumer.recibir(envelope, channel, 10L);

        verify(emailSender, times(1)).enviar(pedido);
        verify(channel).basicAck(10L, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void rechazaSinReintentosCuandoElPayloadEsInvalido() throws Exception {
        EmailSenderSimulado emailSender = mock(EmailSenderSimulado.class);
        doThrow(new CorreoInvalidoException("sin clienteEmail")).when(emailSender).enviar(pedido);
        Channel channel = mock(Channel.class);
        EmailNotificationConsumer consumer = new EmailNotificationConsumer(emailSender);

        consumer.recibir(envelope, channel, 20L);

        verify(emailSender, times(1)).enviar(pedido);
        verify(channel).basicNack(20L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    void reintentaYLuegoEnviaALaDlqCuandoElFalloEsTransitorioYPersistente() throws Exception {
        EmailSenderSimulado emailSender = mock(EmailSenderSimulado.class);
        doThrow(new CorreoTransitorioException("proveedor caido")).when(emailSender).enviar(pedido);
        Channel channel = mock(Channel.class);
        EmailNotificationConsumer consumer = new EmailNotificationConsumer(emailSender);

        consumer.recibir(envelope, channel, 30L);

        verify(emailSender, times(3)).enviar(pedido);
        verify(channel).basicNack(30L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }
}
