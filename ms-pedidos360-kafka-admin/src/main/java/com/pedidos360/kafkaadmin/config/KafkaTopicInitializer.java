package com.pedidos360.kafkaadmin.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.errors.TopicExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Crea los topicos de negocio al arrancar, de forma idempotente (si ya existen,
 * no falla ni los modifica) y con reintento si los brokers todavia no responden
 * (por ejemplo, justo despues de "docker compose up" en frio).
 */
@Component
public class KafkaTopicInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(KafkaTopicInitializer.class);
    private static final int MAX_INTENTOS = 10;
    private static final long ESPERA_ENTRE_INTENTOS_MS = 3000;
    private static final long TIMEOUT_SEG = 10;

    private final AdminClient adminClient;

    public KafkaTopicInitializer(AdminClient adminClient) {
        this.adminClient = adminClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<NewTopic> requeridos = List.of(
                topicoPedidosCreados(),
                topicoPedidosEstado(),
                topicoDlt("pedidos.creados.DLT"),
                topicoDlt("pedidos.estado.DLT"));
        crearSiNoExistenConReintento(requeridos);
    }

    private NewTopic topicoPedidosCreados() {
        // pedidos.creados: evento de "pedido creado" para quien quiera reaccionar
        // (ej. futura analitica/reporting). 3 particiones para repartir el consumo
        // entre varias instancias de un mismo grupo; 2 replicas porque el cluster
        // tiene 2 brokers (es el maximo de redundancia posible, igual que las colas
        // quorum de RabbitMQ en este mismo proyecto). retention.ms = 7 dias
        // (604800000 ms): es un evento transaccional de vida corta, no hace falta
        // conservarlo mas. cleanup.policy=delete porque interesa el log completo de
        // eventos, no "el ultimo valor por clave".
        Map<String, String> configs = new HashMap<>();
        configs.put(TopicConfig.RETENTION_MS_CONFIG, "604800000");
        configs.put(TopicConfig.CLEANUP_POLICY_CONFIG, "delete");
        NewTopic topic = new NewTopic("pedidos.creados", 3, (short) 2);
        topic.configs(configs);
        return topic;
    }

    private NewTopic topicoPedidosEstado() {
        // pedidos.estado: cada cambio de estado de un pedido, publicado con el id
        // del pedido como key. 3 particiones (mismo paralelismo que pedidos.creados);
        // 2 replicas (maximo del cluster). cleanup.policy=compact,delete: se compacta
        // porque solo interesa el ULTIMO estado conocido por pedido (la key es el id
        // del pedido), y se agrega "delete" para que los registros/tombstones mas
        // viejos que retention.ms tambien se puedan purgar y no crezca sin limite.
        // retention.ms = 30 dias (2592000000 ms): mas largo que pedidos.creados
        // porque el historial de estados sirve para auditoria/soporte.
        Map<String, String> configs = new HashMap<>();
        configs.put(TopicConfig.CLEANUP_POLICY_CONFIG, "compact,delete");
        configs.put(TopicConfig.RETENTION_MS_CONFIG, "2592000000");
        NewTopic topic = new NewTopic("pedidos.estado", 3, (short) 2);
        topic.configs(configs);
        return topic;
    }

    private NewTopic topicoDlt(String nombre) {
        // DLT (dead letter topic) de cada topico de negocio: 1 sola particion porque
        // el volumen esperado de mensajes fallidos es bajo y no necesita paralelismo
        // de consumo; 2 replicas (igual que los demas, para no perder los mensajes
        // fallidos si cae un broker). retention.ms = 14 dias (1209600000 ms), mas que
        // el topico de origen, para dar tiempo a investigar/reprocesar manualmente
        // antes de que se pierdan. cleanup.policy=delete porque son eventos de error,
        // no "ultimo valor por clave".
        Map<String, String> configs = new HashMap<>();
        configs.put(TopicConfig.RETENTION_MS_CONFIG, "1209600000");
        configs.put(TopicConfig.CLEANUP_POLICY_CONFIG, "delete");
        NewTopic topic = new NewTopic(nombre, 1, (short) 2);
        topic.configs(configs);
        return topic;
    }

    private void crearSiNoExistenConReintento(List<NewTopic> requeridos) {
        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
            try {
                Set<String> existentes = adminClient.listTopics().names().get(TIMEOUT_SEG, TimeUnit.SECONDS);
                List<NewTopic> faltantes = requeridos.stream()
                        .filter(t -> !existentes.contains(t.name()))
                        .toList();

                if (faltantes.isEmpty()) {
                    log.info("Los topicos requeridos ya existen, no se crea ni modifica nada.");
                    return;
                }

                adminClient.createTopics(faltantes).all().get(TIMEOUT_SEG, TimeUnit.SECONDS);
                log.info("Topicos creados: {}", faltantes.stream().map(NewTopic::name).toList());
                return;
            } catch (Exception e) {
                if (causaEs(e, TopicExistsException.class)) {
                    log.info("Uno o mas topicos ya existian (creados por otra instancia en paralelo), se ignora.");
                    return;
                }
                log.warn("Intento {}/{} fallido al crear los topicos iniciales (causa: {})",
                        intento, MAX_INTENTOS, e.getMessage());
                if (intento == MAX_INTENTOS) {
                    log.error("No se pudieron crear los topicos iniciales tras {} intentos. " +
                            "El servicio sigue arriba igual; se puede reintentar manualmente via la API.",
                            MAX_INTENTOS, e);
                    return;
                }
                dormir();
            }
        }
    }

    private void dormir() {
        try {
            Thread.sleep(ESPERA_ENTRE_INTENTOS_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
