package com.pedidos360.kafkaadmin.controller;

import com.pedidos360.kafkaadmin.dto.ConsumerGroupDetalleResponse;
import com.pedidos360.kafkaadmin.dto.ConsumerGroupResumenResponse;
import com.pedidos360.kafkaadmin.service.KafkaAdminService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/kafka/consumer-groups")
@Validated
public class ConsumerGroupController {
    private final KafkaAdminService service;

    public ConsumerGroupController(KafkaAdminService service) {
        this.service = service;
    }

    @GetMapping
    public List<ConsumerGroupResumenResponse> listar() {
        return service.listarConsumerGroups();
    }

    @GetMapping("/{id}")
    public ConsumerGroupDetalleResponse obtener(@PathVariable @NotBlank String id) {
        return service.obtenerConsumerGroup(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable @NotBlank String id) {
        service.eliminarConsumerGroup(id);
        return ResponseEntity.noContent().build();
    }
}
