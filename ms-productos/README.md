# ms-productos

Microservicio de catálogo y stock de Pedidos360. Se ejecuta en el puerto `8082` y requiere JWT de Azure AD en `/api/productos/**`.

## Ejecución local

1. Cree la base `pedidos360` en MySQL.
2. Configure opcionalmente `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y `AZURE_AD_ISSUER_URI`.
3. Ejecute `mvn spring-boot:run`.

## API

- `POST /api/productos`
- `GET /api/productos`
- `GET /api/productos/{id}`
- `PATCH /api/productos/{id}/stock`
