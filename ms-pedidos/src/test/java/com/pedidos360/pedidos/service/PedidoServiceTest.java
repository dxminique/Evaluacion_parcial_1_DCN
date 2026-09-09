package com.pedidos360.pedidos.service;

import com.pedidos360.pedidos.dto.CrearPedidoRequest;
import com.pedidos360.pedidos.entity.Pedido;
import com.pedidos360.pedidos.repository.PedidoRepository;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PedidoServiceTest {
    @Test void creaPedidoPendiente() {
        PedidoRepository repository = mock(PedidoRepository.class);
        when(repository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            var method = Pedido.class.getDeclaredMethod("antesDeGuardar");
            method.setAccessible(true);
            method.invoke(pedido);
            return pedido;
        });
        var response = new PedidoService(repository).crear(new CrearPedidoRequest("cliente@correo.cl", "Pedido de prueba", new BigDecimal("12000")));
        assertThat(response.estado()).isEqualTo(com.pedidos360.pedidos.entity.EstadoPedido.PENDIENTE);
        verify(repository).save(any(Pedido.class));
    }
}
