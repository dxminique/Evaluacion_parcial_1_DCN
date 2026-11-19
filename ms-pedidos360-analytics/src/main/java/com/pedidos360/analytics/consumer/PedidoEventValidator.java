package com.pedidos360.analytics.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Component
public class PedidoEventValidator {
    private static final Set<String> ESTADOS = Set.of("PENDIENTE", "EN_PREPARACION", "ENTREGADO", "CANCELADO");

    private final ObjectMapper objectMapper;

    public PedidoEventValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String validar(String topic, String payload) {
        if (payload == null || payload.isBlank()) {
            throw new EventoAnalyticsInvalidoException("El mensaje está vacío");
        }
        try {
            JsonNode event = objectMapper.readTree(payload);
            if (event == null || !event.isObject()) {
                throw new EventoAnalyticsInvalidoException("El evento debe ser un objeto JSON");
            }
            validarIdentidad(event);
            if ("pedidos.creados".equals(topic)) {
                validarCreado(event);
            } else if ("pedidos.estado".equals(topic)) {
                validarEstado(event);
            } else {
                throw new EventoAnalyticsInvalidoException("Topic no soportado: " + topic);
            }
            return event.path("eventId").asText();
        } catch (JsonProcessingException e) {
            throw new EventoAnalyticsInvalidoException("JSON inválido", e);
        }
    }

    private void validarIdentidad(JsonNode event) {
        try {
            UUID.fromString(textoRequerido(event, "eventId"));
        } catch (IllegalArgumentException e) {
            throw new EventoAnalyticsInvalidoException("eventId debe ser un UUID válido", e);
        }
        if (!event.path("pedidoId").canConvertToLong() || event.path("pedidoId").asLong() <= 0) {
            throw new EventoAnalyticsInvalidoException("pedidoId debe ser un entero positivo");
        }
        try {
            Instant.parse(textoRequerido(event, "occurredAt"));
        } catch (DateTimeException e) {
            throw new EventoAnalyticsInvalidoException("occurredAt debe ser una fecha ISO-8601 válida", e);
        }
    }

    private void validarCreado(JsonNode event) {
        String email = textoRequerido(event, "clienteEmail");
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new EventoAnalyticsInvalidoException("clienteEmail debe ser válido");
        }
        textoRequerido(event, "descripcion");
        JsonNode total = event.get("total");
        if (total == null || !total.isNumber() || total.decimalValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new EventoAnalyticsInvalidoException("total debe ser un número no negativo");
        }
        estadoRequerido(textoRequerido(event, "estado"));
    }

    private void validarEstado(JsonNode event) {
        estadoRequerido(textoRequerido(event, "estadoNuevo"));
        JsonNode anterior = event.get("estadoAnterior");
        if (anterior != null && !anterior.isNull()) {
            estadoRequerido(anterior.asText());
        }
    }

    private String textoRequerido(JsonNode event, String field) {
        JsonNode value = event.get(field);
        if (value == null || !value.isTextual() || value.asText().isBlank()) {
            throw new EventoAnalyticsInvalidoException(field + " es obligatorio");
        }
        return value.asText();
    }

    private void estadoRequerido(String estado) {
        if (!ESTADOS.contains(estado)) {
            throw new EventoAnalyticsInvalidoException("Estado de pedido no reconocido: " + estado);
        }
    }
}
