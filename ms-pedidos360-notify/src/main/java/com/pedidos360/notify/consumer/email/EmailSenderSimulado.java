package com.pedidos360.notify.consumer.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class EmailSenderSimulado {
    private static final Logger log = LoggerFactory.getLogger(EmailSenderSimulado.class);
    private static final double PROBABILIDAD_FALLO_TRANSITORIO = 0.3;

    public void enviar(PedidoEventEnvelope.PedidoData pedido) {
        if (pedido == null || pedido.clienteEmail() == null || pedido.clienteEmail().isBlank()) {
            throw new CorreoInvalidoException("El evento no trae clienteEmail, no se puede enviar el correo");
        }
        if (ThreadLocalRandom.current().nextDouble() < PROBABILIDAD_FALLO_TRANSITORIO) {
            throw new CorreoTransitorioException("Proveedor de correo no disponible momentaneamente");
        }
        log.info("Correo simulado enviado a {} para el pedido {} (estado {})",
                pedido.clienteEmail(), pedido.id(), pedido.estado());
    }
}
