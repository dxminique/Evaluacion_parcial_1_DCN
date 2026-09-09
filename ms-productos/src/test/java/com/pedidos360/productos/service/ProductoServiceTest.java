package com.pedidos360.productos.service;

import com.pedidos360.productos.dto.CrearProductoRequest;
import com.pedidos360.productos.entity.Producto;
import com.pedidos360.productos.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductoServiceTest {
    @Test void creaProductoConDatosIngresados() {
        ProductoRepository repository = mock(ProductoRepository.class);
        when(repository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var response = new ProductoService(repository).crear(new CrearProductoRequest("Pizza", "Pizza familiar", new BigDecimal("8990"), 10));
        assertThat(response.nombre()).isEqualTo("Pizza");
        assertThat(response.stock()).isEqualTo(10);
        verify(repository).save(any(Producto.class));
    }
}
