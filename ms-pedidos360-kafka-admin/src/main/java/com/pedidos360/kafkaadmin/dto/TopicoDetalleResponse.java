package com.pedidos360.kafkaadmin.dto;

import java.util.List;
import java.util.Map;

public record TopicoDetalleResponse(String name, List<ParticionInfo> partitions, Map<String, String> configs) {
    public record ParticionInfo(int partition, Integer leader, List<Integer> replicas, List<Integer> isr) { }
}
