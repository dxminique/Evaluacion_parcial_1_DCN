package com.pedidos360.productos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "productos")
public class Producto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 150) private String nombre;
    @Column(nullable = false, length = 500) private String descripcion;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal precio;
    @Column(nullable = false) private Integer stock;
    @Column(nullable = false) private Boolean activo;
    @PrePersist void antesDeGuardar() { if (activo == null) activo = true; }
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Boolean getActivo() { return activo; }
}
