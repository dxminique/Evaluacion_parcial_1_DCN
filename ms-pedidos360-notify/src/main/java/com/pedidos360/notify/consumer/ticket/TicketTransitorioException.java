package com.pedidos360.notify.consumer.ticket;

/** Error recuperable: falla transitoria simulada (por ejemplo, servicio de tickets caido). */
public class TicketTransitorioException extends RuntimeException {
    public TicketTransitorioException(String message) { super(message); }
}
