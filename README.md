# Pedidos360

## 1. Descripción del proyecto

Pedidos360 es una aplicación web desarrollada bajo una arquitectura de microservicios, orientada a la gestión centralizada de productos y pedidos.

El sistema contempla la administración de productos, stock y precios, así como la creación, consulta y actualización de pedidos y sus estados.

La solución incorpora una interfaz web, servicios backend, autenticación, una base de datos relacional y mensajería mediante RabbitMQ para apoyar el procesamiento de notificaciones.

## 2. Arquitectura del sistema

El proyecto está organizado en los siguientes componentes:

| Componente | Responsabilidad | Puerto local |
|---|---|---|
| `frontend-pedidos360` | Interfaz web de usuario | 5173 |
| `ms-pedidos360-bff` | Backend for Frontend | 8083 |
| `ms-pedidos360-catalog` | Gestión de productos, stock y precios | 8086 |
| `ms-pedidos360-orders` | Gestión de pedidos y estados | 8087 |
| `ms-pedidos360-notify` | Procesamiento de notificaciones mediante RabbitMQ | Interno |
| `ms-pedidos360-rabbit-admin` | Administración de recursos de RabbitMQ | 8088 |
| MySQL | Persistencia de datos | 3307 |
| RabbitMQ | Mensajería entre servicios | 5672 |
| RabbitMQ Management | Panel web de administración | 15672 |

## 3. Tecnologías utilizadas

### Frontend

- React 19
- Vite 8
- React Router
- Axios
- Microsoft Authentication Library (MSAL)

### Backend

- Java 21
- Spring Boot 3.3.4
- Maven
- Arquitectura de microservicios

### Infraestructura

- MySQL 8.0
- RabbitMQ
- Docker
- Docker Compose

## 4. Requisitos previos

Para ejecutar el proyecto localmente se requiere:

- Git
- Docker Desktop con Docker Compose
- Node.js y npm
- Un navegador web actualizado

Los microservicios se construyen y ejecutan mediante Docker, por lo que no es necesario iniciar cada aplicación Java manualmente.

## 5. Instalación y ejecución local

### 5.1. Obtener el repositorio

Clonar el repositorio y acceder al directorio principal:

```bash
git clone https://github.com/dxminique/Evaluacion_parcial_1_DCN.git
cd Evaluacion_parcial_1_DCN
```

### 5.2. Iniciar los servicios con Docker

Desde el directorio principal:

```bash
docker compose up -d --build mysql rabbitmq backend catalog orders notify rabbit-admin
```

Este comando construye los microservicios y levanta los contenedores necesarios.

Verificar su estado:

```bash
docker compose ps
```

MySQL y RabbitMQ deben alcanzar el estado `healthy`, y los microservicios deben permanecer en ejecución.

### 5.3. Configurar el frontend

Ingresar al directorio del frontend:

```bash
cd frontend-pedidos360
```

Crear el archivo `.env` a partir de `.env.example`.

En PowerShell:

```powershell
Copy-Item .env.example .env
```

Configuración local:

```env
VITE_API_URL=http://localhost:8083
VITE_CATALOG_URL=http://localhost:8086
VITE_ORDERS_URL=http://localhost:8087
```

### 5.4. Instalar dependencias y ejecutar

```bash
npm install
npm run dev
```

Si PowerShell bloquea la ejecución de `npm.ps1`, utilizar:

```powershell
npm.cmd install
npm.cmd run dev
```

Abrir la dirección local indicada por Vite, normalmente:

http://localhost:5173/

Mantener abierta la terminal mientras se utiliza el frontend.

## 6. RabbitMQ

RabbitMQ permite intercambiar mensajes entre los microservicios.

En la implementación del proyecto, el servicio de pedidos publica mensajes relacionados con la creación de pedidos y el servicio de notificaciones los consume.

El proyecto incorpora además un microservicio destinado a la administración de RabbitMQ.

### Panel de administración

Dirección local:

http://localhost:15672/

Las credenciales de desarrollo se configuran mediante las variables `RABBITMQ_DEFAULT_USER` y `RABBITMQ_DEFAULT_PASS` del archivo Docker Compose.

No deben utilizarse credenciales de desarrollo en entornos productivos.

## 7. Seguridad

El frontend incorpora Microsoft MSAL para la integración con mecanismos de autenticación.

Los servicios de catálogo y pedidos contemplan controles de acceso mediante JWT y roles de usuario, de acuerdo con las funcionalidades implementadas.

Los permisos deben validarse en los servicios backend, independientemente de los controles visuales del frontend.

## 8. Comandos útiles

Consultar contenedores activos:

```bash
docker compose ps
```

Consultar registros de un servicio:

```bash
docker compose logs --tail=100 rabbitmq
```

Iniciar los servicios backend:

```bash
docker compose up -d --build mysql rabbitmq backend catalog orders notify rabbit-admin
```

Detener los contenedores:

```bash
docker compose down
```

**Advertencia:** Evitar `docker compose down -v` si se necesita conservar la información persistida en los volúmenes de MySQL y RabbitMQ.

## 9. Pruebas y validación

Se recomienda verificar:

- Inicio de los contenedores y conexión a MySQL.
- Disponibilidad del panel de RabbitMQ.
- Comunicación del frontend con los servicios backend.
- Funcionalidades de catálogo y pedidos.
- Controles de autenticación y autorización.
- Publicación y consumo de mensajes.
- Adaptación de la interfaz a pantallas de escritorio y tablets.

## 10. Consideraciones

La ejecución local del frontend se realiza mediante Vite, de forma independiente del contenedor de frontend definido en Docker Compose.

Las configuraciones de despliegue en AWS pueden utilizar direcciones y variables diferentes a las del entorno local.

El proyecto debe utilizar variables de entorno y mecanismos adecuados para proteger credenciales y configuraciones sensibles.