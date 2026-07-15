# Luxury Corporate Unificado

Proyecto unificado que integra:

- `frontend/`: aplicacion Angular 19 de `Luxury_CorporateF`.
- `backend/`: API Spring Boot/Maven de `Luxury_Corporate_B`.

El frontend consume el backend por `/api`. En desarrollo, Angular usa `frontend/proxy.conf.json` para reenviar esas llamadas a `http://localhost:8080`, evitando problemas de CORS y manteniendo una sola URL logica dentro del codigo.

## Requisitos

- Node.js y npm.
- Java 21.
- PostgreSQL con una base llamada `luxury`.

El backend usa estas variables opcionales:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/luxury"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="tu_clave"
```

Si no se configuran, toma los valores por defecto definidos en `backend/src/main/resources/application.properties`.

## Instalacion

Desde la raiz del proyecto:

```powershell
npm install
npm run install:frontend
```

## Ejecutar frontend y backend juntos

```powershell
npm run dev
```

Servicios:

- Frontend: `http://localhost:4200`
- Backend/API: `http://localhost:8080/api`
- Swagger: `http://localhost:8080/swagger-ui.html`

## Ejecutarlos por separado

Backend:

```powershell
npm run start:backend
```

Frontend:

```powershell
npm run start:frontend
```

## Compilar y probar

Frontend:

```powershell
npm run build:frontend
```

Backend:

```powershell
npm run test:backend
```

## Credenciales de prueba

El backend siembra un usuario administrador:

```txt
admin@luxury.com / admin123
```

Tambien puedes registrar usuarios desde el frontend; se crean inicialmente como operador.

## Estructura

```txt
Luxury_Corporate_Unificado/
  backend/
  frontend/
  scripts/
  package.json
  README.md
```
