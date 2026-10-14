package com.pedidos360.rabbitadmin.controller;

import com.pedidos360.rabbitadmin.dto.CrearBindingRequest;
import com.pedidos360.rabbitadmin.service.RabbitAdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bindings")
@Validated
public class BindingController {
    private final RabbitAdminService service;

    public BindingController(RabbitAdminService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> crear(@Valid @RequestBody CrearBindingRequest request) {
        service.crearBinding(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> eliminar(
            @RequestParam @NotBlank String queue,
            @RequestParam @NotBlank String exchange,
            @RequestParam(required = false, defaultValue = "") String routingKey) {
        service.eliminarBinding(queue, exchange, routingKey);
        return ResponseEntity.noContent().build();
    }
}
