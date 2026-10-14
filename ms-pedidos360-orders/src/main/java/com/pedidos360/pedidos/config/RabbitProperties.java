package com.pedidos360.pedidos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rabbit")
public class RabbitProperties {
    private String exchange;
    private String deadLetterExchange;
    private String emailQueue;
    private String emailRoutingKey;
    private String emailDlq;

    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }
    public String getDeadLetterExchange() { return deadLetterExchange; }
    public void setDeadLetterExchange(String deadLetterExchange) { this.deadLetterExchange = deadLetterExchange; }
    public String getEmailQueue() { return emailQueue; }
    public void setEmailQueue(String emailQueue) { this.emailQueue = emailQueue; }
    public String getEmailRoutingKey() { return emailRoutingKey; }
    public void setEmailRoutingKey(String emailRoutingKey) { this.emailRoutingKey = emailRoutingKey; }
    public String getEmailDlq() { return emailDlq; }
    public void setEmailDlq(String emailDlq) { this.emailDlq = emailDlq; }
}
