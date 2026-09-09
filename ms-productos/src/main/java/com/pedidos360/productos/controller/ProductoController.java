package com.pedidos360.productos.controller;

import com.pedidos360.productos.dto.*;
import com.pedidos360.productos.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    private final ProductoService service;
    public ProductoController(ProductoService service) { this.service = service; }
    @PostMapping public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody CrearProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }
    @GetMapping public List<ProductoResponse> listar() { return service.listar(); }
    @GetMapping("/{id}") public ProductoResponse buscar(@PathVariable Long id) { return service.buscarPorId(id); }
    @PatchMapping("/{id}/stock") public ProductoResponse actualizarStock(@PathVariable Long id, @Valid @RequestBody ActualizarStockRequest request) {
        return service.actualizarStock(id, request);
    }
}
