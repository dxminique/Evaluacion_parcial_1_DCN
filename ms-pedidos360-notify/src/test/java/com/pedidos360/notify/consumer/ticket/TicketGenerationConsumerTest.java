package com.pedidos360.notify.consumer.ticket;

import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class TicketGenerationConsumerTest {

    private final PedidoEventEnvelope.PedidoData pedido =
            new PedidoEventEnvelope.PedidoData(1L, "cliente@correo.cl", "PENDIENTE");
    private final PedidoEventEnvelope envelope =
            new PedidoEventEnvelope("evt-1", "PEDIDO_CREADO", Instant.now(), "corr-1", pedido);

    @Test
    void confirmaElMensajeCuandoLaGeneracionEsExitosa() throws Exception {
        TicketGeneratorSimulado ticketGenerator = mock(TicketGeneratorSimulado.class);
        Channel channel = mock(Channel.class);
        TicketGenerationConsumer consumer = new TicketGenerationConsumer(ticketGenerator);

        consumer.recibir(envelope, channel, 10L);

        verify(ticketGenerator, times(1)).generar(pedido);
        verify(channel).basicAck(10L, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void rechazaSinReintentosCuandoElPayloadEsInvalido() throws Exception {
        TicketGeneratorSimulado ticketGenerator = mock(TicketGeneratorSimulado.class);
        doThrow(new TicketInvalidoException("sin id de pedido")).when(ticketGenerator).generar(pedido);
        Channel channel = mock(Channel.class);
        TicketGenerationConsumer consumer = new TicketGenerationConsumer(ticketGenerator);

        consumer.recibir(envelope, channel, 20L);

        verify(ticketGenerator, times(1)).generar(pedido);
        verify(channel).basicNack(20L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    void reintentaYLuegoEnviaALaDlqCuandoElFalloEsTransitorioYPersistente() throws Exception {
        TicketGeneratorSimulado ticketGenerator = mock(TicketGeneratorSimulado.class);
        doThrow(new TicketTransitorioException("servicio caido")).when(ticketGenerator).generar(pedido);
        Channel channel = mock(Channel.class);
        TicketGenerationConsumer consumer = new TicketGenerationConsumer(ticketGenerator);

        consumer.recibir(envelope, channel, 30L);

        verify(ticketGenerator, times(3)).generar(pedido);
        verify(channel).basicNack(30L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }
}
