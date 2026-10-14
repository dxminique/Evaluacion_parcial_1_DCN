package com.pedidos360.rabbitadmin.exception;

/** Error de validacion de negocio (no cubierto por las anotaciones del DTO), p. ej. nombre reservado o type invalido. */
public class ValidacionException extends RuntimeException {
    private final String field;

    public ValidacionException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() { return field; }
}
