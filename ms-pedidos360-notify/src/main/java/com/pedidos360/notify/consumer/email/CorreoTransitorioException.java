package com.pedidos360.notify.consumer.email;

/** Error recuperable: falla transitoria simulada (por ejemplo, proveedor de correo caido). */
public class CorreoTransitorioException extends RuntimeException {
    public CorreoTransitorioException(String message) { super(message); }
}
