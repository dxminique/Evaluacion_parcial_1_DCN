package com.pedidos360.notify.consumer.stock;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Reglas de confirmacion/reintento:
 * - Error NO recuperable (payload invalido, por ejemplo sin id de pedido): se rechaza
 *   de inmediato con basicNack(requeue=false), sin reintentar, y el mensaje cae en la DLQ.
 * - Error recuperable (falla transitoria simulada): se reintenta hasta MAX_INTENTOS veces,
 *   con una espera corta entre intentos y un log por cada intento fallido; si se agotan
 *   los intentos, tambien se nack a la DLQ.
 * - Si la reserva es exitosa (en cualquier intento), se confirma con basicAck.
 */
@Component
public class StockReservationConsumer {
    private static final Logger log = LoggerFactory.getLogger(StockReservationConsumer.class);
    private static final int MAX_INTENTOS = 3;
    private static final long ESPERA_ENTRE_INTENTOS_MS = 200;

    private final StockReservaSimulada stockReserva;

    public StockReservationConsumer(StockReservaSimulada stockReserva) {
        this.stockReserva = stockReserva;
    }

    @RabbitListener(queues = "${app.rabbit.stock-queue}", containerFactory = "manualAckContainerFactory")
    public void recibir(PedidoEventEnvelope envelope, Channel channel,
                         @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        String eventId = envelope != null ? envelope.eventId() : null;
        PedidoEventEnvelope.PedidoData pedido = envelope != null ? envelope.pedido() : null;

        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
            try {
                stockReserva.reservar(pedido);
                channel.basicAck(deliveryTag, false);
                return;
            } catch (StockInvalidoException e) {
                log.error("Evento {} con payload invalido, va directo a la DLQ sin reintentos (causa: {})",
                        eventId, e.getMessage());
                channel.basicNack(deliveryTag, false, false);
                return;
            } catch (StockTransitorioException e) {
                log.warn("Intento {}/{} fallido para el evento {} (causa: {})",
                        intento, MAX_INTENTOS, eventId, e.getMessage());
                if (intento == MAX_INTENTOS) {
                    log.error("Se agotaron los reintentos para el evento {}. Se envia a la DLQ.", eventId);
                    channel.basicNack(deliveryTag, false, false);
                    return;
                }
                esperarAntesDeReintentar();
            }
        }
    }

    private void esperarAntesDeReintentar() {
        try {
            Thread.sleep(ESPERA_ENTRE_INTENTOS_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
