package com.pedidos360.notify.consumer.stock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class StockReservaSimulada {
    private static final Logger log = LoggerFactory.getLogger(StockReservaSimulada.class);
    private static final double PROBABILIDAD_FALLO_TRANSITORIO = 0.3;

    public void reservar(PedidoEventEnvelope.PedidoData pedido) {
        if (pedido == null || pedido.id() == null) {
            throw new StockInvalidoException("El evento no trae el id del pedido, no se puede reservar el stock");
        }
        if (ThreadLocalRandom.current().nextDouble() < PROBABILIDAD_FALLO_TRANSITORIO) {
            throw new StockTransitorioException("Servicio de inventario no disponible momentaneamente");
        }
        log.info("Stock reservado (simulado) para el pedido {} (estado {})", pedido.id(), pedido.estado());
    }
}
