# ms-pedidos

Microservicio Spring Boot para administrar los pedidos de Pedidos360.

## Ejecución local

1. Cree la base de datos: `CREATE DATABASE pedidos360;`.
2. Ajuste las credenciales de MySQL mediante `DB_USERNAME` y `DB_PASSWORD` si no utiliza `root` / `1234`.
3. Ejecute `mvn spring-boot:run` desde esta carpeta. El servicio queda disponible en `http://localhost:8081`.

La API requiere un JWT válido de Azure AD en las rutas `/api/pedidos/**`. El endpoint `GET /actuator/health` es público.

## Endpoints

- `POST /api/pedidos`
- `GET /api/pedidos`
- `GET /api/pedidos/{id}`
- `PATCH /api/pedidos/{id}/estado`
