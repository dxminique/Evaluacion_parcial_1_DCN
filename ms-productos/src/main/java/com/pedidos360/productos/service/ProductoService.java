package com.pedidos360.productos.service;

import com.pedidos360.productos.dto.*;
import com.pedidos360.productos.entity.Producto;
import com.pedidos360.productos.exception.RecursoNoEncontradoException;
import com.pedidos360.productos.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class ProductoService {
    private final ProductoRepository repository;
    public ProductoService(ProductoRepository repository) { this.repository = repository; }
    public ProductoResponse crear(CrearProductoRequest request) {
        Producto producto = new Producto();
        producto.setNombre(request.nombre()); producto.setDescripcion(request.descripcion());
        producto.setPrecio(request.precio()); producto.setStock(request.stock());
        return ProductoResponse.from(repository.save(producto));
    }
    @Transactional(readOnly = true)
    public List<ProductoResponse> listar() { return repository.findAll().stream().map(ProductoResponse::from).toList(); }
    @Transactional(readOnly = true)
    public ProductoResponse buscarPorId(Long id) { return ProductoResponse.from(encontrar(id)); }
    public ProductoResponse actualizarStock(Long id, ActualizarStockRequest request) {
        Producto producto = encontrar(id); producto.setStock(request.stock());
        return ProductoResponse.from(repository.save(producto));
    }
    private Producto encontrar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Producto " + id + " no encontrado"));
    }
}
