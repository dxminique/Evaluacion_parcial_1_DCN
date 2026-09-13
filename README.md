# Pedidos360

## Descripción del proyecto

Pedidos360 es una aplicación desarrollada bajo una arquitectura de microservicios para gestionar el proceso de pedidos y productos de forma centralizada.

El sistema permite administrar productos, stock y precios mediante el microservicio de catálogo, además de crear, consultar y actualizar pedidos y sus estados mediante el microservicio de pedidos.

La solución cuenta con un frontend para la interacción con los usuarios, un Backend for Frontend (BFF) que centraliza la comunicación entre la interfaz y los servicios backend, y mecanismos de autenticación y seguridad para proteger el acceso a las funcionalidades.

### Componentes principales

- **frontend-pedidos360:** interfaz de usuario de la aplicación.
- **ms-pedidos360-bff:** Backend for Frontend que centraliza las solicitudes del frontend.
- **ms-pedidos360-catalog:** administra productos, stock y precios.
- **ms-pedidos360-orders:** administra la creación, consulta y actualización de pedidos y sus estados.

| Servicio | Persistencia | API/responsabilidad |
|---|---|---|
| `ms-pedidos360-orders` | Oracle | CRUD de pedidos y estados: `/api/orders/*` |
| `ms-pedidos360-catalog` | Oracle | CRUD de productos, stock y precios: `/api/catalog/*` |
