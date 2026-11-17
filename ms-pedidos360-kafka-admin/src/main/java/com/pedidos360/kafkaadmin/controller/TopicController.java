package com.pedidos360.kafkaadmin.controller;

import com.pedidos360.kafkaadmin.dto.ActualizarConfigTopicoRequest;
import com.pedidos360.kafkaadmin.dto.ActualizarParticionesRequest;
import com.pedidos360.kafkaadmin.dto.CrearTopicoRequest;
import com.pedidos360.kafkaadmin.dto.TopicoDetalleResponse;
import com.pedidos360.kafkaadmin.service.KafkaAdminService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/kafka/topics")
@Validated
public class TopicController {
    private static final String NOMBRE_REGEX = "^[A-Za-z0-9._-]+$";

    private final KafkaAdminService service;

    public TopicController(KafkaAdminService service) {
        this.service = service;
    }

    @GetMapping
    public List<String> listar() {
        return service.listarTopicos();
    }

    @GetMapping("/{name}")
    public TopicoDetalleResponse obtener(@PathVariable @NotBlank @Pattern(regexp = NOMBRE_REGEX) String name) {
        return service.obtenerTopico(name);
    }

    @PostMapping
    public ResponseEntity<TopicoDetalleResponse> crear(@Valid @RequestBody CrearTopicoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearTopico(request));
    }

    @PutMapping("/{name}/config")
    public ResponseEntity<Void> actualizarConfig(
            @PathVariable @NotBlank @Pattern(regexp = NOMBRE_REGEX) String name,
            @Valid @RequestBody ActualizarConfigTopicoRequest request) {
        service.actualizarConfig(name, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{name}/partitions")
    public ResponseEntity<Void> aumentarParticiones(
            @PathVariable @NotBlank @Pattern(regexp = NOMBRE_REGEX) String name,
            @Valid @RequestBody ActualizarParticionesRequest request) {
        service.aumentarParticiones(name, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> eliminar(@PathVariable @NotBlank @Pattern(regexp = NOMBRE_REGEX) String name) {
        service.eliminarTopico(name);
        return ResponseEntity.noContent().build();
    }
}
