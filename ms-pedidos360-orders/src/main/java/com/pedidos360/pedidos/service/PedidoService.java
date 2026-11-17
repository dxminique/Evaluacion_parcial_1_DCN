package com.pedidos360.pedidos.service;

import com.pedidos360.pedidos.dto.*;
import com.pedidos360.pedidos.entity.EstadoPedido;
import com.pedidos360.pedidos.entity.Pedido;
import com.pedidos360.pedidos.events.PedidoCreadoAppEvent;
import com.pedidos360.pedidos.events.PedidoEstadoAppEvent;
import com.pedidos360.pedidos.exception.RecursoNoEncontradoException;
import com.pedidos360.pedidos.messaging.PedidoEventPublisher;
import com.pedidos360.pedidos.repository.PedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class PedidoService {
    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

    private final PedidoRepository repository;
    private final PedidoEventPublisher eventPublisher;
    private final ApplicationEventPublisher applicationEventPublisher;

    public PedidoService(PedidoRepository repository, PedidoEventPublisher eventPublisher,
                          ApplicationEventPublisher applicationEventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public PedidoResponse crear(CrearPedidoRequest request) {
        Pedido pedido = new Pedido();
        pedido.setClienteEmail(request.clienteEmail());
        pedido.setDescripcion(request.descripcion());
        pedido.setTotal(request.total());
        Pedido guardado = repository.save(pedido);
        eventPublisher.publicarPedidoCreado(guardado);
        publicarEventoDeDominio(new PedidoCreadoAppEvent(guardado));
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
        EstadoPedido estadoAnterior = pedido.getEstado();
        pedido.setEstado(request.estado());
        Pedido actualizado = repository.save(pedido);
        publicarEventoDeDominio(new PedidoEstadoAppEvent(actualizado, estadoAnterior));
        return PedidoResponse.from(actualizado);
    }
    private Pedido encontrar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Pedido " + id + " no encontrado"));
    }

    /**
     * Publica el evento interno que, tras el AFTER_COMMIT de la transaccion, dispara
     * la publicacion en Kafka. Un fallo aqui (por ejemplo, un error inesperado al
     * registrar el evento) jamas debe impedir que el pedido quede creado/actualizado.
     */
    private void publicarEventoDeDominio(Object evento) {
        try {
            applicationEventPublisher.publishEvent(evento);
        } catch (Exception e) {
            log.error("No se pudo publicar el evento de dominio {} (Kafka no se notificara): {}", evento, e.getMessage(), e);
        }
    }
}
