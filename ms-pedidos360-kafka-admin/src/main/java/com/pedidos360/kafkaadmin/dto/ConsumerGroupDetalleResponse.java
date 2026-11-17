package com.pedidos360.kafkaadmin.dto;

import java.util.List;

public record ConsumerGroupDetalleResponse(
        String groupId,
        String state,
        List<MiembroInfo> members,
        List<OffsetInfo> offsets) {

    public record MiembroInfo(String memberId, String clientId, String host, List<Integer> assignedPartitions) { }

    public record OffsetInfo(String topic, int partition, Long committedOffset, Long endOffset, Long lag) { }
}
