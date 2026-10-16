package com.pedidos360.notify.consumer.stock;

/** Error recuperable: falla transitoria simulada (por ejemplo, servicio de stock caido). */
public class StockTransitorioException extends RuntimeException {
    public StockTransitorioException(String message) { super(message); }
}
