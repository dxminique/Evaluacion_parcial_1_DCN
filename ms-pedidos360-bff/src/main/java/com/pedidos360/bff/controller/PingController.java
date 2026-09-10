package com.pedidos360.bff.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    @GetMapping("/api/ping")
    public String ping() {
        return "pong";
    }

    @GetMapping("/api/secure-ping")
    public String securePing() {
        return "pong seguro";
    }
}