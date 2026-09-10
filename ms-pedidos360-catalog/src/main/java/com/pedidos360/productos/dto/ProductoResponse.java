package com.pedidos360.productos.dto;

import com.pedidos360.productos.entity.Producto;
import java.math.BigDecimal;

public record ProductoResponse(Long id, String nombre, String descripcion, BigDecimal precio, Integer stock, Boolean activo) {
    public static ProductoResponse from(Producto producto) {
        return new ProductoResponse(producto.getId(), producto.getNombre(), producto.getDescripcion(),
                producto.getPrecio(), producto.getStock(), producto.getActivo());
    }
}
