package com.pedidos360.pedidos.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Solo registra las propiedades de nombres de topico (app.kafka.topics.*), para que
 * no queden strings sueltos en el codigo de negocio. orders NO crea los topicos:
 * eso lo hace ms-pedidos360-kafka-admin al arrancar.
 */
@Configuration
@EnableConfigurationProperties(KafkaTopicsProperties.class)
public class KafkaConfig {
}
