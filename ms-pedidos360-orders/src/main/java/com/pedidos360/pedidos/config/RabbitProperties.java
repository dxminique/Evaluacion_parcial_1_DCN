package com.pedidos360.pedidos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rabbit")
public class RabbitProperties {
    private String exchange;
    private String topicExchange;
    private String deadLetterExchange;
    private String emailQueue;
    private String emailRoutingKey;
    private String emailDlq;
    private String ticketQueue;
    private String ticketRoutingKey;
    private String ticketDlq;
    private String stockQueue;
    private String stockRoutingKey;
    private String stockBindingPattern;
    private String stockDlq;

    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }
    public String getTopicExchange() { return topicExchange; }
    public void setTopicExchange(String topicExchange) { this.topicExchange = topicExchange; }
    public String getDeadLetterExchange() { return deadLetterExchange; }
    public void setDeadLetterExchange(String deadLetterExchange) { this.deadLetterExchange = deadLetterExchange; }
    public String getEmailQueue() { return emailQueue; }
    public void setEmailQueue(String emailQueue) { this.emailQueue = emailQueue; }
    public String getEmailRoutingKey() { return emailRoutingKey; }
    public void setEmailRoutingKey(String emailRoutingKey) { this.emailRoutingKey = emailRoutingKey; }
    public String getEmailDlq() { return emailDlq; }
    public void setEmailDlq(String emailDlq) { this.emailDlq = emailDlq; }
    public String getTicketQueue() { return ticketQueue; }
    public void setTicketQueue(String ticketQueue) { this.ticketQueue = ticketQueue; }
    public String getTicketRoutingKey() { return ticketRoutingKey; }
    public void setTicketRoutingKey(String ticketRoutingKey) { this.ticketRoutingKey = ticketRoutingKey; }
    public String getTicketDlq() { return ticketDlq; }
    public void setTicketDlq(String ticketDlq) { this.ticketDlq = ticketDlq; }
    public String getStockQueue() { return stockQueue; }
    public void setStockQueue(String stockQueue) { this.stockQueue = stockQueue; }
    public String getStockRoutingKey() { return stockRoutingKey; }
    public void setStockRoutingKey(String stockRoutingKey) { this.stockRoutingKey = stockRoutingKey; }
    public String getStockBindingPattern() { return stockBindingPattern; }
    public void setStockBindingPattern(String stockBindingPattern) { this.stockBindingPattern = stockBindingPattern; }
    public String getStockDlq() { return stockDlq; }
    public void setStockDlq(String stockDlq) { this.stockDlq = stockDlq; }
}
