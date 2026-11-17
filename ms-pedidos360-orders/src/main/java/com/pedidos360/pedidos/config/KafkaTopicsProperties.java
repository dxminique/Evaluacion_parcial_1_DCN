package com.pedidos360.pedidos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public class KafkaTopicsProperties {
    private final Topics topics = new Topics();

    public Topics getTopics() { return topics; }

    public static class Topics {
        private String pedidoCreado;
        private String pedidoEstado;

        public String getPedidoCreado() { return pedidoCreado; }
        public void setPedidoCreado(String pedidoCreado) { this.pedidoCreado = pedidoCreado; }
        public String getPedidoEstado() { return pedidoEstado; }
        public void setPedidoEstado(String pedidoEstado) { this.pedidoEstado = pedidoEstado; }
    }
}
