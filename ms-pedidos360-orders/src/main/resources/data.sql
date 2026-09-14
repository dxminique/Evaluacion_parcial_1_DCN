INSERT INTO pedidos (cliente_email, descripcion, total, estado, fecha_creacion)
SELECT 'cliente1@pedidos360.com', 'Pedido de prueba - laptop y accesorios', 895000.00, 'PENDIENTE', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM pedidos
    WHERE cliente_email = 'cliente1@pedidos360.com'
      AND descripcion = 'Pedido de prueba - laptop y accesorios'
);

INSERT INTO pedidos (cliente_email, descripcion, total, estado, fecha_creacion)
SELECT 'cliente2@pedidos360.com', 'Pedido de prueba - monitor', 320000.00, 'EN_PREPARACION', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM pedidos
    WHERE cliente_email = 'cliente2@pedidos360.com'
      AND descripcion = 'Pedido de prueba - monitor'
);
