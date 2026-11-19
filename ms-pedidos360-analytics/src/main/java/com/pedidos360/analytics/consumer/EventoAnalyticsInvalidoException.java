package com.pedidos360.analytics.consumer;

public class EventoAnalyticsInvalidoException extends RuntimeException {
    public EventoAnalyticsInvalidoException(String message) {
        super(message);
    }

    public EventoAnalyticsInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}
