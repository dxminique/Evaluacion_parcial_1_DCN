# Pedidos360

## 1. Descripción del proyecto

Pedidos360 es una aplicación desarrollada bajo una arquitectura de microservicios para gestionar el proceso de pedidos y productos de forma centralizada.

> **Importante:** todos los endpoints requieren un token JWT de Azure AD. Una llamada sin token responde **401 Unauthorized**; es el comportamiento esperado. Para probar, obtén un token con los usuarios de prueba (ver sección Autenticación) o importa la colección de Postman incluida en `docs/postman/`.

El sistema permite administrar productos, stock y precios mediante el microservicio de catálogo, además de crear, consultar y actualizar pedidos y sus estados mediante el microservicio de pedidos. La notificación por correo ante la creación de un pedido se procesa de forma asíncrona vía RabbitMQ, y existe un microservicio administrador de RabbitMQ (colas, exchanges y bindings) solo para el rol Admin.

La solución cuenta con un frontend para la interacción con los usuarios, un Backend for Frontend (BFF) que centraliza la comunicación entre la interfaz y los servicios backend, un API Gateway local (nginx) para pruebas con Postman sin depender de infraestructura en AWS, y mecanismos de autenticación y seguridad (Azure AD / JWT) para proteger el acceso a las funcionalidades.

### Componentes principales

- **frontend-pedidos360:** interfaz de usuario de la aplicación (opcional para evaluar el backend).
- **ms-pedidos360-bff:** Backend for Frontend que centraliza las solicitudes del frontend.
- **ms-pedidos360-catalog:** administra productos, stock y precios.
- **ms-pedidos360-orders:** administra la creación, consulta y actualización de pedidos y sus estados; publica un evento en RabbitMQ al crear un pedido.
- **ms-pedidos360-notify:** consume el evento de pedido creado y simula el envío de un correo (con reintentos y dead-letter queue).
- **ms-pedidos360-rabbit-admin:** administra colas, exchanges y bindings de RabbitMQ vía API REST (solo rol Admin).
- **api-gateway:** proxy nginx local que enruta a catalog, orders, rabbit-admin y al bff bajo un único puerto (8090), para probar con Postman sin el API Gateway de AWS.

| Servicio | Persistencia | API/responsabilidad |
|---|---|---|
| `ms-pedidos360-orders` | MySQL | CRUD de pedidos y estados: `/api/orders/*` |
| `ms-pedidos360-catalog` | MySQL | CRUD de productos, stock y precios: `/api/catalog/*` |
| `ms-pedidos360-notify` | sin persistencia | Consumidor RabbitMQ de notificaciones de pedido |
| `ms-pedidos360-rabbit-admin` | sin persistencia | Administración de RabbitMQ: `/api/queues`, `/api/exchanges`, `/api/bindings` |
| `ms-pedidos360-bff` | MySQL | Backend for Frontend |

## 2. Arquitectura

```
                        ┌───────────────────────┐
  Postman / Frontend →  │  api-gateway (nginx)   │  :8090
                        └───────────┬───────────┘
                 ┌──────────────────┼──────────────────┬──────────────┐
                 ▼                  ▼                  ▼              ▼
          /catalog/...        /orders/...       /rabbit-admin/...     /...
                 │                  │                  │              │
                 ▼                  ▼                  ▼              ▼
             catalog              orders          rabbit-admin       bff
             :8086                :8087              :8088          :8083
                 │                  │                                 │
                 └────────┬─────────┘                                 │
                          ▼                                           │
                        mysql :3306 (3307 en el host) ←────────────────┘

             orders ──(publica evento)──► rabbitmq :5672 (panel :15672) ──► notify
```

Cada microservicio valida su propio JWT y sus propios roles (no hay una validación centralizada en el gateway). El API Gateway local es un **equivalente de desarrollo** del API Gateway de AWS: solo enruta y reenvía la cabecera `Authorization`, el `Host` y `X-Forwarded-For`; no agrega ni retira seguridad. Todos los servicios siguen siendo accesibles directo por su puerto si se prefiere.

## 3. Tecnologías

- **Backend:** Spring Boot 3.3.4, Java 21, Spring Security (OAuth2 Resource Server, JWT de Azure AD), Spring Data JPA, Spring AMQP (RabbitMQ).
- **Persistencia:** MySQL 8.
- **Mensajería:** RabbitMQ 3 (con plugin de administración).
- **Frontend (opcional):** React 19 + Vite + MSAL (`@azure/msal-browser`, `@azure/msal-react`).
- **Infraestructura:** Docker, Docker Compose, nginx (API Gateway local y, opcionalmente, servidor del frontend).

## 4. Requisitos

- Docker y Docker Compose (todo el backend corre en contenedores, no se necesita instalar Java, Maven ni MySQL en el host).
- Postman (o similar) para probar la API.
- Opcional, solo si se quiere trabajar fuera de Docker: Java 21 + Maven para los microservicios, y Node 20+ para el frontend.

## 5. Instalación y ejecución

```bash
git clone <url-del-repo>
cd ms-pedidos360-bff
docker compose up -d --build
```

Ese único comando levanta **todo el backend**: `mysql`, `rabbitmq`, `backend` (bff), `catalog`, `orders`, `notify`, `rabbit-admin` y el `api-gateway` local en el puerto `8090`. No se necesita ningún paso manual adicional ni certificados.

El **frontend es opcional** y no es necesario para evaluar el backend con Postman. Si se quiere levantar igual:

```bash
# Dentro de Docker (requiere certificados HTTPS vía docker-compose.override.yml, no versionado):
docker compose --profile frontend up -d --build

# O en modo desarrollo, sin Docker:
cd frontend-pedidos360
npm install
npm run dev
```

Para detener los servicios:

```bash
docker compose down
```

> No uses `docker compose down -v` en el uso normal: borra los datos de MySQL y de RabbitMQ (colas, usuarios del vhost, etc.). Solo tiene sentido si RabbitMQ no levanta por un volumen viejo (ver [Problemas comunes](#10-problemas-comunes)).

### 5.1 Puertos y rutas

| Servicio | Puerto host | Ruta vía API Gateway local (`:8090`) | Ruta directa |
|---|---|---|---|
| `api-gateway` (nginx) | `8090` | — | `http://localhost:8090` |
| `ms-pedidos360-bff` | `8083` | `/` → bff | `http://localhost:8083` |
| `ms-pedidos360-catalog` | `8086` | `/catalog/...` → catalog | `http://localhost:8086` |
| `ms-pedidos360-orders` | `8087` | `/orders/...` → orders | `http://localhost:8087` |
| `ms-pedidos360-rabbit-admin` | `8088` | `/rabbit-admin/...` → rabbit-admin | `http://localhost:8088` |
| `rabbitmq` (AMQP, nodo 1) | `5672` | — | — |
| `rabbitmq` (panel de administración, nodo 1) | `15672` | — | `http://localhost:15672` |
| `rabbitmq2` (panel de administración, nodo 2) | `15673` | — | `http://localhost:15673` |
| `mysql` | `3307` (mapeado al `3306` del contenedor) | — | — |
| `frontend` (opcional, profile `frontend`) | `80` | — | `http://localhost` |

Ejemplo: `GET http://localhost:8090/catalog/api/catalog` llega a `catalog` como `GET /api/catalog` (el prefijo `/catalog/` se descarta al reenviar). Lo mismo aplica para `/orders/...` y `/rabbit-admin/...`. La ruta `/` sin prefijo va directa al `bff`.

## 6. RabbitMQ

### 6.1 Clúster (2 nodos)

RabbitMQ corre como un **clúster de 2 nodos** para esta evaluación: `rabbitmq` (nodo 1) y `rabbitmq2` (nodo 2). Ambos comparten el mismo `RABBITMQ_ERLANG_COOKIE` (requisito para que dos nodos puedan formar un clúster) y tienen `hostname` fijo (`rabbitmq`/`rabbitmq2`), porque el nombre del nodo de Erlang se deriva del hostname (`rabbit@rabbitmq`, `rabbit@rabbitmq2`).

El nodo 2 se une al clúster solo mediante su propio `command` en `docker-compose.yml` (sin pasos manuales): arranca `rabbitmq-server -detached`, espera a que el nodo esté listo (`rabbitmqctl await_startup`), y luego hace `stop_app` → `join_cluster rabbit@rabbitmq` → `start_app`.

Todas las colas de negocio (y sus DLQ) son **colas quorum** (`x-queue-type: quorum`), que es el tipo de cola replicado entre nodos en RabbitMQ moderno — las colas clásicas no replican. Las colas quorum son compatibles con `x-dead-letter-exchange`: el dead-lettering funciona igual que con colas clásicas.

Los microservicios (`orders`, `notify`, `rabbit-admin`) se conectan con **ambas direcciones** (`spring.rabbitmq.addresses: rabbitmq:5672,rabbitmq2:5672`), para seguir funcionando si uno de los dos nodos no está disponible.

- Paneles: nodo 1 en `http://localhost:15672`, nodo 2 en `http://localhost:15673` (mismas credenciales en ambos, es el mismo clúster).
- Verificar el clúster: `docker exec ms-pedidos360-bff-rabbitmq-1 rabbitmqctl cluster_status`.

### 6.2 Credenciales de desarrollo

Usuario `pedidos`, contraseña `1234` (mismos valores que `RABBITMQ_DEFAULT_USER`/`RABBITMQ_DEFAULT_PASS` en `docker-compose.yml`, iguales en los dos nodos). AMQP en el puerto `5672` (nodo 1); el nodo 2 no expone AMQP al host, solo se usa dentro de la red de Docker.

### 6.3 Topología

Dos exchanges de negocio y uno de errores, con tres colas (todas quorum, cada una con su propia DLQ):

| Exchange | Tipo | Cola | Routing key | DLQ |
|---|---|---|---|---|
| `cmd.direct` | direct | `q.cmd.email` | `email.send` | `q.cmd.email.dlq` |
| `cmd.direct` | direct | `q.cmd.ticket` | `ticket.generate` | `q.cmd.ticket.dlq` |
| `evt.topic` | topic | `q.evt.stock` | patrón `pedido.creado.#` (orders publica con `pedido.creado.nuevo`) | `q.evt.stock.dlq` |

El exchange de errores `cmd.dead.dlx` (direct) recibe las 3 DLQ, cada una enlazada con la routing key original del mensaje (`email.send`, `ticket.generate`, `pedido.creado.nuevo`).

### 6.4 Flujo

1. `orders` publica **3 eventos** (mismo envelope, 3 destinos) cuando se crea un pedido: notificación por correo, generación de ticket y reserva de stock simulada.
2. `notify` tiene **3 consumidores independientes**, uno por dominio (`consumer/email`, `consumer/ticket`, `consumer/stock`), cada uno con **confirmación manual** (ack/nack).
3. Si el error es **no recuperable** (por ejemplo, el evento no trae el dato mínimo requerido), el consumidor hace `nack` sin reencolar y el mensaje cae en su DLQ.
4. Si el error es **transitorio** (simulado), el consumidor reintenta un número limitado de veces antes de, si persiste, enviarlo también a su DLQ.
5. Cada DLQ tiene su propio consumidor que solo registra el mensaje en log (no reprocesa).

### 6.5 Endpoints de `rabbit-admin` (solo rol Admin)

| Método y ruta | Respuesta |
|---|---|
| `POST /api/queues` | `201` |
| `GET /api/queues/{name}` | `200` / `404` si no existe |
| `DELETE /api/queues/{name}` | `204` / `404` si no existe |
| `POST /api/exchanges` | `201` (tipos: `direct`, `topic`, `fanout`, `headers`) |
| `DELETE /api/exchanges/{name}` | `204` / `404` si no existe |
| `POST /api/bindings` | `201` |
| `DELETE /api/bindings?queue=&exchange=&routingKey=` | `204` |

Los errores de validación devuelven `400` con el formato `{"errors":[{"field": "...", "message": "..."}]}`. Los nombres que empiezan con `amq.` están reservados por RabbitMQ y se rechazan. Si se intenta declarar una cola o exchange que ya existe con argumentos distintos, responde `409`. Esto sigue funcionando igual con el clúster activo (`rabbit-admin` administra recursos propios de prueba, no las colas quorum de negocio).

### 6.6 Cómo probarlo

1. **Caso exitoso:** crea un pedido con `POST /api/orders` (ver [Roles y permisos](#71-roles-y-permisos) para el token) y revisa los 3 consumidores con:
   ```bash
   docker compose logs -f notify
   ```
2. **Caso de error por cada cola (termina en su DLQ):** en el panel (`http://localhost:15672`), `Publish message` con `content_type: application/json` y un body sin el dato mínimo requerido:
   - Exchange `cmd.direct`, routing key `email.send` → cae en `q.cmd.email.dlq` (payload sin `clienteEmail`):
     ```json
     {"eventId":"test-1","type":"PEDIDO_CREADO","timestamp":"2026-01-01T00:00:00Z","correlationId":"test-1","pedido":{"id":1,"clienteEmail":"","estado":"PENDIENTE"}}
     ```
   - Exchange `cmd.direct`, routing key `ticket.generate` → cae en `q.cmd.ticket.dlq` (payload sin `id` de pedido):
     ```json
     {"eventId":"test-2","type":"PEDIDO_CREADO","timestamp":"2026-01-01T00:00:00Z","correlationId":"test-2","pedido":{"id":null,"clienteEmail":"cliente@correo.cl","estado":"PENDIENTE"}}
     ```
   - Exchange `evt.topic`, routing key `pedido.creado.nuevo` → cae en `q.evt.stock.dlq` (mismo payload sin `id`).

   Verificable en el panel o con `GET /api/queues/q.cmd.email.dlq` (o `q.cmd.ticket.dlq` / `q.evt.stock.dlq`) vía `rabbit-admin`.

## 7. Seguridad

Cada microservicio (`catalog`, `orders`, `rabbit-admin`, `bff`) valida de forma independiente el JWT emitido por Azure AD: firma contra el JWK Set de Azure AD v2, `issuer` esperado en formato v1 (`https://sts.windows.net/<tenant>/`), `audience` esperada (`api://6fc9e12e-949d-4672-8126-89ab30c6f757`), y los roles se leen del claim `roles` del token, convertidos a authorities `ROLE_*` de Spring Security. El API Gateway local **no** valida nada de esto; solo reenvía la cabecera `Authorization` tal cual la mande el cliente.

### 7.1 Roles y permisos

| Servicio | Operación | Roles permitidos | Rol sin permiso |
|---|---|---|---|
| `catalog` | `GET /api/catalog/**` | Admin, Operador, Cliente | — |
| `catalog` | `POST`/`PUT`/`PATCH`/`DELETE` `/api/catalog/**` | Admin, Operador | Cliente → `403` |
| `orders` | `GET /api/orders/**` y `POST /api/orders` | Admin, Operador, Cliente | — |
| `orders` | `PATCH /api/orders/{id}/estado` | Admin, Operador | Cliente → `403` |
| `rabbit-admin` | `/api/**` | Admin | Operador y Cliente → `403` |
| `rabbit-admin` | `/actuator/health` | público | — |

Cómo leer las respuestas:
- **`200`/`201`/`204`**: el token es válido y el rol tiene permiso para esa operación.
- **`401`**: no se envió token, el token es inválido, expiró, o el `issuer`/`audience` no coinciden con lo esperado.
- **`403`**: el token es válido, pero el rol del usuario no tiene permiso para esa operación puntual.

### 7.2 Usuarios de prueba

| Usuario (correo completo) | Contraseña | Rol |
|---|---|---|
| admin.test@ejemploduoc123.onmicrosoft.com | @Raho261580 | Admin |
| operador.test@ejemploduoc123.onmicrosoft.com | @Duvu910761 | Operador |
| cliente.test@ejemploduoc123.onmicrosoft.com | @Xuta475515 | Cliente |

Estas son cuentas de prueba del tenant `ejemploduoc123.onmicrosoft.com` creadas solo para evaluar el proyecto. El usuario es el correo completo y la contraseña incluye el `@` inicial.

### 7.3 Cómo obtener un token para Postman

```
POST https://login.microsoftonline.com/ejemploduoc123.onmicrosoft.com/oauth2/v2.0/token
Content-Type: application/x-www-form-urlencoded

grant_type=password
client_id=6fc9e12e-949d-4672-8126-89ab30c6f757
scope=api://6fc9e12e-949d-4672-8126-89ab30c6f757/access_as_user
username=<correo completo del usuario de prueba, por ejemplo admin.test@ejemploduoc123.onmicrosoft.com>
password=<contraseña de la tabla de usuarios de prueba>
```

El `access_token` de la respuesta se envía en cada request como `Authorization: Bearer <token>`. Dura aproximadamente **1 hora**; si expira, hay que pedir uno nuevo. Para probar los distintos niveles de acceso, pide un token por cada usuario de la tabla de arriba (uno para Admin, otro para Operador, otro para Cliente).

### 7.4 Probar con Postman

1. Importar `docs/postman/Pedidos360_Postman_Roles.postman_collection.json`.
2. Verificar la variable `base = http://localhost:8090`.
3. Ejecutar la carpeta **"0. Tokens"** (una petición por rol).
4. Ejecutar el resto de las carpetas, o usar **Run collection** para correr todo de una vez.

Resultados esperados:
- Sin token → `401`.
- Cliente intentando escribir (crear/editar/eliminar donde no tiene permiso) → `403`.
- Admin/Operador en las operaciones permitidas → `200`/`201`.
- `rabbit-admin` → solo Admin puede operar, el resto recibe `403`.

Los tokens duran cerca de **1 hora**; si una corrida falla con `401` a mitad de camino, vuelve a ejecutar la carpeta "0. Tokens" para renovarlos.

## 8. Comandos útiles

```bash
# Ver el estado de todos los servicios
docker compose ps

# Logs de un servicio puntual
docker compose logs -f orders
docker compose logs -f notify

# Reiniciar un solo servicio (por ejemplo, tras un cambio de configuración)
docker compose restart catalog

# Reconstruir solo un servicio
docker compose up -d --build rabbit-admin

# Apagar todo (conservando los datos)
docker compose down

# Compilar y probar un microservicio en forma local (sin Docker)
cd ms-pedidos360-orders
mvn -q compile
mvn -q test
```

## 9. Pruebas

Con el backend levantado (`docker compose up -d --build`), sin token todas estas llamadas deben responder `401` (no `404` ni `502`):

```bash
curl -i http://localhost:8090/catalog/api/catalog
curl -i http://localhost:8090/orders/api/orders
curl -i http://localhost:8090/rabbit-admin/api/queues/q.cmd.email
curl -i http://localhost:8086/api/catalog          # directo, sin pasar por el gateway
```

Y esta debe responder `200` sin token, porque es pública:

```bash
curl -i http://localhost:8090/rabbit-admin/actuator/health
```

Con un token válido (ver [Cómo obtener un token para Postman](#73-cómo-obtener-un-token-para-postman)), agrega la cabecera `Authorization: Bearer <token>` y compara el resultado contra la tabla de [Roles y permisos](#71-roles-y-permisos) según el rol del usuario usado.

## 10. Problemas comunes

| Síntoma | Causa probable | Solución |
|---|---|---|
| `rabbitmq` termina en `exited (1)` | Volumen (`rabbitmq_data`) con datos de una ejecución anterior incompatible (identidad del nodo, cookie de Erlang) | `docker compose down -v` y volver a levantar con `docker compose up -d --build` (solo en este caso puntual) |
| `catalog`/`orders` se reinician o fallan al arrancar | Intentaron conectarse a MySQL antes de que estuviera listo | Ya hay `healthcheck` en `mysql` y `depends_on: condition: service_healthy`; si persiste, revisa `docker compose logs mysql` |
| `401` inesperado en Postman | El token expiró (dura ~1 hora) | Pide un token nuevo (sección 7.3) |
| `403` en una operación que debería funcionar | El rol del usuario no tiene permiso para esa operación | Revisa la tabla de [Roles y permisos](#71-roles-y-permisos) y usa un usuario con el rol correcto |
| `PRECONDITION_FAILED` al crear una cola/exchange en `rabbit-admin` | Ya existe un recurso con ese nombre pero con argumentos distintos (`durable`, `autoDelete`, dead-letter, etc.) | Elimina el recurso existente o usa otro nombre |
| Mensaje marcado como "published, but not routed" en el panel de RabbitMQ | La routing key no es `email.send`, o se publicó contra un exchange distinto a `cmd.direct` | Revisa la routing key y el exchange usados al publicar |

## 11. Consideraciones

- **Datos de prueba:** `catalog` y `orders` insertan automáticamente datos de ejemplo al arrancar (`data.sql`): 3 productos en `catalog` y 2 pedidos en `orders`. Las inserciones son idempotentes (no se duplican si el servicio se reinicia).
- **`docker compose down -v`:** no lo uses en el uso normal, porque borra los datos de MySQL y de RabbitMQ. Resérvalo para el caso puntual en que `rabbitmq` no arranca por un volumen viejo (ver [Problemas comunes](#10-problemas-comunes)).
- **Credenciales de desarrollo:** las contraseñas de MySQL y RabbitMQ en `docker-compose.yml` (`1234`) son solo para entorno local/evaluación y están marcadas con `TODO` para cambiarlas antes de un uso productivo real.
- **Tokens:** este README no incluye ningún token de acceso. Las únicas credenciales que contiene son las contraseñas de las cuentas de prueba de la sección 7.2, exclusivamente para fines de evaluación del proyecto.
