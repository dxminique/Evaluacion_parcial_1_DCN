package com.pedidos360.audit.entity;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name = "audit_events")
public class AuditEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 100) private String topic;
    @Lob @Column(nullable = false) private String payload;
    @Column(nullable = false) private Instant occurredAt;
    protected AuditEvent() { }
    public AuditEvent(String topic, String payload) { this.topic = topic; this.payload = payload; this.occurredAt = Instant.now(); }
    public Long getId() { return id; } public String getTopic() { return topic; } public String getPayload() { return payload; } public Instant getOccurredAt() { return occurredAt; }
}
