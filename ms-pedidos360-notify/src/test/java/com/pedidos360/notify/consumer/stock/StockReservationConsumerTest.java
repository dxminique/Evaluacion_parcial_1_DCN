package com.pedidos360.notify.consumer.stock;

import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class StockReservationConsumerTest {

    private final PedidoEventEnvelope.PedidoData pedido =
            new PedidoEventEnvelope.PedidoData(1L, "cliente@correo.cl", "PENDIENTE");
    private final PedidoEventEnvelope envelope =
            new PedidoEventEnvelope("evt-1", "PEDIDO_CREADO", Instant.now(), "corr-1", pedido);

    @Test
    void confirmaElMensajeCuandoLaReservaEsExitosa() throws Exception {
        StockReservaSimulada stockReserva = mock(StockReservaSimulada.class);
        Channel channel = mock(Channel.class);
        StockReservationConsumer consumer = new StockReservationConsumer(stockReserva);

        consumer.recibir(envelope, channel, 10L);

        verify(stockReserva, times(1)).reservar(pedido);
        verify(channel).basicAck(10L, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void rechazaSinReintentosCuandoElPayloadEsInvalido() throws Exception {
        StockReservaSimulada stockReserva = mock(StockReservaSimulada.class);
        doThrow(new StockInvalidoException("sin id de pedido")).when(stockReserva).reservar(pedido);
        Channel channel = mock(Channel.class);
        StockReservationConsumer consumer = new StockReservationConsumer(stockReserva);

        consumer.recibir(envelope, channel, 20L);

        verify(stockReserva, times(1)).reservar(pedido);
        verify(channel).basicNack(20L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    void reintentaYLuegoEnviaALaDlqCuandoElFalloEsTransitorioYPersistente() throws Exception {
        StockReservaSimulada stockReserva = mock(StockReservaSimulada.class);
        doThrow(new StockTransitorioException("servicio caido")).when(stockReserva).reservar(pedido);
        Channel channel = mock(Channel.class);
        StockReservationConsumer consumer = new StockReservationConsumer(stockReserva);

        consumer.recibir(envelope, channel, 30L);

        verify(stockReserva, times(3)).reservar(pedido);
        verify(channel).basicNack(30L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }
}
