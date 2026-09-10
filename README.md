# Pedidos360

| Servicio | Persistencia | API/responsabilidad |
|---|---|---|
| `ms-pedidos360-orders` | Oracle | CRUD de pedidos y estados: `/api/orders/*` |
| `ms-pedidos360-catalog` | Oracle | CRUD de productos, stock y precios: `/api/catalog/*` |
| `ms-pedidos360-notify` | Sin BD | Consumidor RabbitMQ para email/web-push |
| `ms-pedidos360-audit` | Oracle | Lecturas de auditoría: `/api/audit/*` |
| `ms-pedidos360-report` | Oracle | Lecturas de KPIs: `/api/report/*` |

Kafka queda como siguiente fase para alimentar auditoría y reportes.
