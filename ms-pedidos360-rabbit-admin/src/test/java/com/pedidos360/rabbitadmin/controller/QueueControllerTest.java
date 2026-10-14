package com.pedidos360.rabbitadmin.controller;

import com.pedidos360.rabbitadmin.exception.ApiExceptionHandler;
import com.pedidos360.rabbitadmin.service.RabbitAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QueueController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class QueueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RabbitAdminService service;

    @Test
    void rechazaCrearColaConNombreVacio() throws Exception {
        mockMvc.perform(post("/api/queues")
                        .contentType("application/json")
                        .content("{\"name\":\"\",\"durable\":true,\"autoDelete\":false}"))
                .andExpect(status().isBadRequest());
    }
}
