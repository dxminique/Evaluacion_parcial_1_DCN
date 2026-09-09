package com.pedidos360.pedidos.controller;

import com.pedidos360.pedidos.dto.*;
import com.pedidos360.pedidos.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    private final PedidoService service;
    public PedidoController(PedidoService service) { this.service = service; }
    @PostMapping
    public ResponseEntity<PedidoResponse> crear(@Valid @RequestBody CrearPedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }
    @GetMapping public List<PedidoResponse> listar() { return service.listar(); }
    @GetMapping("/{id}") public PedidoResponse buscar(@PathVariable Long id) { return service.buscarPorId(id); }
    @PatchMapping("/{id}/estado")
    public PedidoResponse actualizarEstado(@PathVariable Long id, @Valid @RequestBody ActualizarEstadoPedidoRequest request) {
        return service.actualizarEstado(id, request);
    }
}
