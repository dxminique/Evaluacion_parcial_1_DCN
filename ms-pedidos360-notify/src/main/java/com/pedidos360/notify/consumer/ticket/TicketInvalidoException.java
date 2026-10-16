package com.pedidos360.notify.consumer.ticket;

/** Error NO recuperable: el payload no trae los datos minimos para generar el ticket. */
public class TicketInvalidoException extends RuntimeException {
    public TicketInvalidoException(String message) { super(message); }
}
