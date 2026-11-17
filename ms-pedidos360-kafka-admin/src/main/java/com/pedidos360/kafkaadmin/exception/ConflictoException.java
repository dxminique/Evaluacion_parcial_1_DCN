package com.pedidos360.kafkaadmin.exception;

/** Conflicto con el estado actual de Kafka: topico que ya existe, consumer group con miembros activos, etc. */
public class ConflictoException extends RuntimeException {
    public ConflictoException(String message) { super(message); }
}
