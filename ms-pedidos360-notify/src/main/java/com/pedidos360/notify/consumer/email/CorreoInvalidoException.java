package com.pedidos360.notify.consumer.email;

/** Error NO recuperable: el payload no trae los datos minimos para enviar el correo. */
public class CorreoInvalidoException extends RuntimeException {
    public CorreoInvalidoException(String message) { super(message); }
}
