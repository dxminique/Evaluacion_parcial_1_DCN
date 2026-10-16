# Plan de pruebas funcionales – Pedidos360

## 1. Objetivo

Definir los casos de prueba necesarios para verificar el funcionamiento de los principales componentes de Pedidos360.

## 2. Alcance

Las pruebas contemplan:

- Autenticación y control de acceso.
- Gestión de productos y catálogo.
- Gestión de pedidos.
- Comunicación entre microservicios.
- Mensajería mediante RabbitMQ.

## 3. Entorno de pruebas

- Frontend: React y Vite.
- Backend: Java 21 y Spring Boot 3.3.4.
- Base de datos: MySQL 8.0.
- Mensajería: RabbitMQ.
- Ejecución local: Docker Compose.

## 4. Casos de prueba

| ID | Módulo | Caso de prueba | Resultado esperado | Estado |
|---|---|---|---|---|
| CP-01 | Infraestructura | Iniciar servicios mediante Docker Compose | Los servicios se inician correctamente | Pendiente |
| CP-02 | Frontend | Acceder a la aplicación web | La interfaz se carga sin errores | Pendiente |
| CP-03 | Autenticación | Intentar acceder sin autenticación a un recurso protegido | El backend rechaza el acceso no autorizado | Pendiente |
| CP-04 | Catálogo | Consultar productos | Se muestran los productos disponibles | Pendiente |
| CP-05 | Catálogo | Crear un producto con permisos suficientes | El producto queda registrado | Pendiente |
| CP-06 | Catálogo | Actualizar información de un producto | Los cambios se guardan correctamente | Pendiente |
| CP-07 | Pedidos | Crear un pedido válido | El pedido queda registrado | Pendiente |
| CP-08 | Pedidos | Consultar un pedido existente | Se recupera la información del pedido | Pendiente |
| CP-09 | Pedidos | Actualizar el estado de un pedido | El nuevo estado se registra correctamente | Pendiente |
| CP-10 | RabbitMQ | Crear un pedido y verificar la publicación del mensaje | El servicio publica el mensaje correspondiente | Pendiente |
| CP-11 | Notificaciones | Verificar el consumo de un mensaje válido | El servicio de notificaciones procesa el mensaje | Pendiente |
| CP-12 | Responsive | Visualizar la aplicación en tablet | La interfaz mantiene una presentación utilizable | Pendiente |

## 5. Criterios de aceptación

Un caso de prueba se considerará aprobado cuando el resultado obtenido coincida con el resultado esperado.

Los resultados deberán respaldarse mediante capturas de pantalla, registros de ejecución o evidencias equivalentes.

## 6. Registro de resultados

Para cada caso ejecutado se recomienda registrar:

- Fecha de ejecución.
- Responsable de la prueba.
- Resultado obtenido.
- Estado: Aprobado, Fallido o Bloqueado.
- Evidencia correspondiente.

## 7. Observaciones

Este documento constituye un plan inicial de pruebas. Los casos deben ajustarse a las funcionalidades efectivamente implementadas antes de su ejecución.

Los estados permanecerán pendientes hasta realizar las validaciones correspondientes.