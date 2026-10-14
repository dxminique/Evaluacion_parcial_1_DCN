package com.pedidos360.pedidos.service;

import com.pedidos360.pedidos.dto.*;
import com.pedidos360.pedidos.entity.Pedido;
import com.pedidos360.pedidos.exception.RecursoNoEncontradoException;
import com.pedidos360.pedidos.messaging.PedidoEventPublisher;
import com.pedidos360.pedidos.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class PedidoService {
    private final PedidoRepository repository;
    private final PedidoEventPublisher eventPublisher;

    public PedidoService(PedidoRepository repository, PedidoEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    public PedidoResponse crear(CrearPedidoRequest request) {
        Pedido pedido = new Pedido();
        pedido.setClienteEmail(request.clienteEmail());
        pedido.setDescripcion(request.descripcion());
        pedido.setTotal(request.total());
        Pedido guardado = repository.save(pedido);
        eventPublisher.publicarPedidoCreado(guardado);
        return PedidoResponse.from(guardado);
    }
    @Transactional(readOnly = true)
    public List<PedidoResponse> listar() {
        return repository.findAll().stream().map(PedidoResponse::from).toList();
    }
    @Transactional(readOnly = true)
    public PedidoResponse buscarPorId(Long id) { return PedidoResponse.from(encontrar(id)); }
    public PedidoResponse actualizarEstado(Long id, ActualizarEstadoPedidoRequest request) {
        Pedido pedido = encontrar(id);
        pedido.setEstado(request.estado());
        return PedidoResponse.from(repository.save(pedido));
    }
    private Pedido encontrar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Pedido " + id + " no encontrado"));
    }
}
