package com.pedidos360.notify.consumer.stock;

/** Error NO recuperable: el payload no trae los datos minimos para reservar el stock. */
public class StockInvalidoException extends RuntimeException {
    public StockInvalidoException(String message) { super(message); }
}
