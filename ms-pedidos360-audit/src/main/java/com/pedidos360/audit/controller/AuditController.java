package com.pedidos360.audit.controller;

import com.pedidos360.audit.repository.AuditEventRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/** API de solo lectura. Los eventos serán recibidos desde Kafka. */
@RestController @RequestMapping("/api/audit")
public class AuditController {
    private final AuditEventRepository repository;
    public AuditController(AuditEventRepository repository) { this.repository = repository; }
    @GetMapping("/timeline") public List<?> timeline() { return repository.findAllByOrderByOccurredAtDesc(); }
}
