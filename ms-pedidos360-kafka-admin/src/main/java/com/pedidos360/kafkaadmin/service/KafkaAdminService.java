package com.pedidos360.kafkaadmin.service;

import com.pedidos360.kafkaadmin.dto.ActualizarConfigTopicoRequest;
import com.pedidos360.kafkaadmin.dto.ActualizarParticionesRequest;
import com.pedidos360.kafkaadmin.dto.ConsumerGroupDetalleResponse;
import com.pedidos360.kafkaadmin.dto.ConsumerGroupResumenResponse;
import com.pedidos360.kafkaadmin.dto.CrearTopicoRequest;
import com.pedidos360.kafkaadmin.dto.TopicoDetalleResponse;
import com.pedidos360.kafkaadmin.exception.ConflictoException;
import com.pedidos360.kafkaadmin.exception.RecursoNoEncontradoException;
import com.pedidos360.kafkaadmin.exception.ValidacionException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.ListOffsetsResult;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.GroupIdNotFoundException;
import org.apache.kafka.common.errors.GroupNotEmptyException;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class KafkaAdminService {
    private static final Logger log = LoggerFactory.getLogger(KafkaAdminService.class);
    private static final long TIMEOUT_SEG = 10;

    private final AdminClient adminClient;

    public KafkaAdminService(AdminClient adminClient) {
        this.adminClient = adminClient;
    }

    // ----- Topicos -----

    public List<String> listarTopicos() {
        try {
            return new ArrayList<>(adminClient.listTopics().names().get(TIMEOUT_SEG, TimeUnit.SECONDS));
        } catch (Exception e) {
            throw new RuntimeException("No se pudieron listar los topicos", e);
        }
    }

    public TopicoDetalleResponse obtenerTopico(String name) {
        TopicDescription descripcion = describirTopico(name);
        Map<String, String> configs = obtenerConfigsTopico(name);

        List<TopicoDetalleResponse.ParticionInfo> particiones = descripcion.partitions().stream()
                .map(p -> new TopicoDetalleResponse.ParticionInfo(
                        p.partition(),
                        p.leader() != null ? p.leader().id() : null,
                        p.replicas().stream().map(Node::id).toList(),
                        p.isr().stream().map(Node::id).toList()))
                .toList();

        return new TopicoDetalleResponse(name, particiones, configs);
    }

    public TopicoDetalleResponse crearTopico(CrearTopicoRequest request) {
        NewTopic nuevo = new NewTopic(request.name(), request.partitions(), request.replicationFactor());
        if (request.configs() != null && !request.configs().isEmpty()) {
            nuevo.configs(request.configs());
        }
        try {
            adminClient.createTopics(List.of(nuevo)).all().get(TIMEOUT_SEG, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            if (causaEs(e, TopicExistsException.class)) {
                throw new ConflictoException("El topico '" + request.name() + "' ya existe");
            }
            throw new RuntimeException("No se pudo crear el topico " + request.name(), e);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo crear el topico " + request.name(), e);
        }
        log.info("Topico creado: {} (particiones={}, replicas={})", request.name(), request.partitions(), request.replicationFactor());
        return obtenerTopico(request.name());
    }

    public void actualizarConfig(String name, ActualizarConfigTopicoRequest request) {
        describirTopico(name);

        ConfigResource recurso = new ConfigResource(ConfigResource.Type.TOPIC, name);
        Collection<AlterConfigOp> operaciones = request.configs().entrySet().stream()
                .map(e -> new AlterConfigOp(new ConfigEntry(e.getKey(), e.getValue()), AlterConfigOp.OpType.SET))
                .toList();
        try {
            adminClient.incrementalAlterConfigs(Map.of(recurso, operaciones)).all().get(TIMEOUT_SEG, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo actualizar la configuracion del topico " + name, e);
        }
        log.info("Configuracion actualizada para el topico {}: {}", name, request.configs().keySet());
    }

    public void aumentarParticiones(String name, ActualizarParticionesRequest request) {
        TopicDescription descripcion = describirTopico(name);
        int actuales = descripcion.partitions().size();
        if (request.partitions() <= actuales) {
            throw new ValidacionException("partitions",
                    "debe ser mayor que el numero actual de particiones (" + actuales + "); Kafka no permite disminuirlas");
        }
        try {
            adminClient.createPartitions(Map.of(name, NewPartitions.increaseTo(request.partitions())))
                    .all().get(TIMEOUT_SEG, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo aumentar las particiones del topico " + name, e);
        }
        log.info("Particiones de {} aumentadas de {} a {}", name, actuales, request.partitions());
    }

    public void eliminarTopico(String name) {
        describirTopico(name);
        try {
            adminClient.deleteTopics(List.of(name)).all().get(TIMEOUT_SEG, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo eliminar el topico " + name, e);
        }
        log.info("Topico eliminado: {}", name);
    }

    // ----- Consumer groups -----

    public List<ConsumerGroupResumenResponse> listarConsumerGroups() {
        try {
            Collection<ConsumerGroupListing> listados = adminClient.listConsumerGroups().all().get(TIMEOUT_SEG, TimeUnit.SECONDS);
            return listados.stream()
                    .map(g -> new ConsumerGroupResumenResponse(g.groupId(), g.state().map(Enum::toString).orElse("UNKNOWN")))
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("No se pudieron listar los consumer groups", e);
        }
    }

    public ConsumerGroupDetalleResponse obtenerConsumerGroup(String id) {
        ConsumerGroupDescription descripcion;
        try {
            descripcion = adminClient.describeConsumerGroups(List.of(id)).describedGroups().get(id).get(TIMEOUT_SEG, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo describir el consumer group " + id, e);
        }

        Map<TopicPartition, OffsetAndMetadata> offsetsComprometidos;
        try {
            offsetsComprometidos = adminClient.listConsumerGroupOffsets(id).partitionsToOffsetAndMetadata().get(TIMEOUT_SEG, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("No se pudieron leer los offsets del consumer group " + id, e);
        }

        boolean existeRealmente = !descripcion.state().toString().equals("DEAD") || !offsetsComprometidos.isEmpty();
        if (!existeRealmente) {
            throw new RecursoNoEncontradoException("El consumer group '" + id + "' no existe");
        }

        List<ConsumerGroupDetalleResponse.MiembroInfo> miembros = descripcion.members().stream()
                .map(m -> new ConsumerGroupDetalleResponse.MiembroInfo(
                        m.consumerId(),
                        m.clientId(),
                        m.host(),
                        m.assignment().topicPartitions().stream().map(TopicPartition::partition).toList()))
                .toList();

        List<ConsumerGroupDetalleResponse.OffsetInfo> offsets = calcularOffsetsConLag(offsetsComprometidos);

        return new ConsumerGroupDetalleResponse(id, descripcion.state().toString(), miembros, offsets);
    }

    public void eliminarConsumerGroup(String id) {
        try {
            adminClient.deleteConsumerGroups(List.of(id)).all().get(TIMEOUT_SEG, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            if (causaEs(e, GroupIdNotFoundException.class)) {
                throw new RecursoNoEncontradoException("El consumer group '" + id + "' no existe");
            }
            if (causaEs(e, GroupNotEmptyException.class)) {
                throw new ConflictoException("El consumer group '" + id + "' tiene miembros activos, no se puede eliminar");
            }
            throw new RuntimeException("No se pudo eliminar el consumer group " + id, e);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo eliminar el consumer group " + id, e);
        }
        log.info("Consumer group eliminado: {}", id);
    }

    // ----- Helpers -----

    private List<ConsumerGroupDetalleResponse.OffsetInfo> calcularOffsetsConLag(
            Map<TopicPartition, OffsetAndMetadata> offsetsComprometidos) {
        if (offsetsComprometidos.isEmpty()) {
            return List.of();
        }

        Map<TopicPartition, OffsetSpec> specs = offsetsComprometidos.keySet().stream()
                .collect(Collectors.toMap(tp -> tp, tp -> OffsetSpec.latest()));

        Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> ultimosOffsets;
        try {
            ultimosOffsets = adminClient.listOffsets(specs).all().get(TIMEOUT_SEG, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("No se pudieron leer los offsets finales para calcular el lag", e);
        }

        List<ConsumerGroupDetalleResponse.OffsetInfo> resultado = new ArrayList<>();
        for (var entry : offsetsComprometidos.entrySet()) {
            TopicPartition tp = entry.getKey();
            long comprometido = entry.getValue().offset();
            Long ultimo = ultimosOffsets.containsKey(tp) ? ultimosOffsets.get(tp).offset() : null;
            Long lag = ultimo != null ? Math.max(0, ultimo - comprometido) : null;
            resultado.add(new ConsumerGroupDetalleResponse.OffsetInfo(tp.topic(), tp.partition(), comprometido, ultimo, lag));
        }
        return resultado;
    }

    private TopicDescription describirTopico(String name) {
        try {
            Map<String, TopicDescription> resultado = adminClient.describeTopics(List.of(name))
                    .allTopicNames().get(TIMEOUT_SEG, TimeUnit.SECONDS);
            return resultado.get(name);
        } catch (ExecutionException e) {
            if (causaEs(e, UnknownTopicOrPartitionException.class)) {
                throw new RecursoNoEncontradoException("El topico '" + name + "' no existe");
            }
            throw new RuntimeException("No se pudo describir el topico " + name, e);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo describir el topico " + name, e);
        }
    }

    private Map<String, String> obtenerConfigsTopico(String name) {
        try {
            ConfigResource recurso = new ConfigResource(ConfigResource.Type.TOPIC, name);
            Map<ConfigResource, Config> resultado = adminClient.describeConfigs(List.of(recurso))
                    .all().get(TIMEOUT_SEG, TimeUnit.SECONDS);
            Config config = resultado.get(recurso);
            return config.entries().stream()
                    .collect(Collectors.toMap(ConfigEntry::name, ConfigEntry::value, (a, b) -> a, java.util.LinkedHashMap::new));
        } catch (Exception e) {
            throw new RuntimeException("No se pudieron leer las configuraciones del topico " + name, e);
        }
    }

    private boolean causaEs(Throwable e, Class<? extends Throwable> tipo) {
        Throwable actual = e;
        while (actual != null) {
            if (tipo.isInstance(actual)) return true;
            actual = actual.getCause();
        }
        return false;
    }
}
