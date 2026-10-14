package com.pedidos360.rabbitadmin.exception;

/** RabbitMQ rechazo declarar el recurso porque ya existe con argumentos distintos (PRECONDITION_FAILED, 406). */
public class RabbitPreconditionFailedException extends RuntimeException {
    public RabbitPreconditionFailedException(String message) { super(message); }
}
