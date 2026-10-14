package com.pedidos360.rabbitadmin.controller;

import com.pedidos360.rabbitadmin.dto.ColaResponse;
import com.pedidos360.rabbitadmin.dto.CrearColaRequest;
import com.pedidos360.rabbitadmin.service.RabbitAdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/queues")
@Validated
public class QueueController {
    private static final String NOMBRE_REGEX = "^[A-Za-z0-9._-]+$";

    private final RabbitAdminService service;

    public QueueController(RabbitAdminService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ColaResponse> crear(@Valid @RequestBody CrearColaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearCola(request));
    }

    @GetMapping("/{name}")
    public ColaResponse obtener(
            @PathVariable @NotBlank @Pattern(regexp = NOMBRE_REGEX) String name) {
        return service.obtenerCola(name);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> eliminar(
            @PathVariable @NotBlank @Pattern(regexp = NOMBRE_REGEX) String name) {
        service.eliminarCola(name);
        return ResponseEntity.noContent().build();
    }
}
