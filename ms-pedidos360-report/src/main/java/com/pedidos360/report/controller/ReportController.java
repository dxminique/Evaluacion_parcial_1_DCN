package com.pedidos360.report.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/** API de lectura. Los indicadores serán materializados desde eventos Kafka. */
@RestController @RequestMapping("/api/report")
public class ReportController {
    @GetMapping("/kpis") public ResponseEntity<Map<String, Object>> kpis() {
        return ResponseEntity.ok(Map.of("status", "ready", "ordersProcessed", 0, "source", "Kafka (pendiente de integración)"));
    }
}
