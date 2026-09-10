package com.pedidos360.pedidos.dto;

import com.pedidos360.pedidos.entity.EstadoPedido;
import com.pedidos360.pedidos.entity.Pedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoResponse(Long id, String clienteEmail, String descripcion, BigDecimal total,
                             EstadoPedido estado, LocalDateTime fechaCreacion) {
    public static PedidoResponse from(Pedido pedido) {
        return new PedidoResponse(pedido.getId(), pedido.getClienteEmail(), pedido.getDescripcion(),
                pedido.getTotal(), pedido.getEstado(), pedido.getFechaCreacion());
    }
}
