package com.pedidos360.pedidos.events;

import com.pedidos360.pedidos.entity.Pedido;

/** Evento interno de Spring: se publica dentro de la transaccion, pero el listener solo actua AFTER_COMMIT. */
public record PedidoCreadoAppEvent(Pedido pedido) { }
