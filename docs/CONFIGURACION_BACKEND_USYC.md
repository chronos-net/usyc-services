# Configuración Backend USYC

## 1. Propósito

Este backend expone la API HTTP del sistema **USYC** para operación administrativa y consumo por el frontend. En términos funcionales cubre, entre otros aspectos:

- Autenticación basada en sesión/cookie con JWT (según configuración de seguridad).
- Catálogos institucionales (escolaridad, carrera, plantel, conceptos, tipos de pago, estatus de recibo, etc.).
- Gestión de alumnos y pagos/recibos (incluye generación/validación relacionada con QR según módulos del proyecto).
- Reportes operativos (por ejemplo, reportes de corte).
- Administración de usuarios (según controladores expuestos).

## 2. Stack tecnológico

Verificado desde `pom.xml` + estructura del proyecto:

| Componente | Evidencia / versión |
|---|---|
| Java (target del proyecto) | **17** (`<java.version>17</java.version>`) |
| Spring Boot | **3.5.3** (`spring-boot-starter-parent`) |
| Build | **Maven** (`pom.xml`, `mvnw` / `mvnw.cmd`) |
| Spring Web | `spring-boot-starter-web` |
| Spring Security | `spring-boot-starter-security` |
| Spring Data JPA | `spring-boot-starter-data-jpa` |
| Hibernate | vía Spring Data JPA / Boot |
| PostgreSQL | driver `postgresql` (runtime) |
| Pool JDBC | **HikariCP** (por defecto en Spring Boot) |
| JWT | `jjwt-*` (`io.jsonwebtoken`) |
| OpenAPI / Swagger UI | `springdoc-openapi-starter-webmvc-ui` (`org.springdoc`) |
| QR (ZXing) | `com.google.zxing:core` / `javase` |
| Lombok | `org.projectlombok:lombok` |

## 3. Estructura general del proyecto

Organización típica por capas en `src/main/java/com/io/usyc/`:

| Carpeta | Rol probable |
|---|---|
| `Controller/` | Controladores REST (`@RestController`), mapping HTTP |
| `Service/` | Contratos de servicio |
| `Service/Impl/` | Implementaciones |
| `Repository/` | Acceso a datos (Spring Data JPA) |
| `Domain/` | Entidades JPA (`@Entity`, `@Table`) |
| `Dto/` | DTOs request/response |
| `Config/` | Seguridad/JWT/filtros/config Spring |

Configuración Spring Boot:

| Recurso | Rol |
|---|---|
| `src/main/resources/application.yaml` | Propiedades principales (puerto, datasource, JWT/cookies vía variables de entorno, springdoc, rutas de almacenamiento QR) |

## 4. Configuración por variables de entorno

El archivo `application.yaml` parametriza valores sensibles vía variables de entorno (no se documentan secretos productivos aquí).

| Variable | Propósito |
|---|---|
| `APP_PORT` | Puerto HTTP del backend (por defecto **8000** si no se define). |
| `DB_URL` | JDBC URL de PostgreSQL. |
| `DB_USER` | Usuario de BD. |
| `DB_PASS` | Contraseña de BD (**solo entorno local/dev**; nunca productiva en docs). |
| `JWT_SECRET` | Secreto para firmar JWT (**rotar si se expone**). |
| `JWT_MINUTES` | TTL del token en minutos (según implementación). |
| `QR_DIR` | Directorio base para artefactos QR en filesystem (según configuración `app.storage.qr-dir`). |
| `JWT_COOKIE_NAME` | Nombre de cookie JWT (si la configuración lo utiliza de forma consistente). |
| `JWT_COOKIE_SAMESITE` | Política SameSite de cookie. |
| `JWT_COOKIE_SECURE` | Cookie solo HTTPS (`true/false`). |

**Ejemplo local (no productivo, solo para desarrollo):**

```powershell
$env:APP_PORT="8000"
$env:DB_URL="jdbc:postgresql://127.0.0.1:5432/USYC_DB_LOCAL"
$env:DB_USER="usyc_local"
$env:DB_PASS="***LOCAL_ONLY***"
$env:JWT_SECRET="***LOCAL_ONLY_LONG_RANDOM***"
$env:JWT_MINUTES="480"
$env:QR_DIR="C:\proyectos\usyc-services\storage\qr"
$env:JWT_COOKIE_NAME="JWT"
$env:JWT_COOKIE_SAMESITE="Lax"
$env:JWT_COOKIE_SECURE="false"
```

## 5. Base de datos local

### Parámetros locales confirmados por micrometa (entorno de pruebas)

| Parámetro | Valor |
|---|---|
| Base | `USYC_DB_LOCAL` |
| Host | `127.0.0.1` |
| Puerto | `5432` |
| Usuario | `usyc_local` |

### Tablas: expectativa vs evidencia en código

Se solicitó como referencia operativa que existan **18** tablas y una lista de nombres (incluyendo secuencias como si fueran tablas).

**Evidencia verificable en el código (entidades JPA):** existen **13** mappings `@Table(...)` en `Domain/` (lista inferida directamente de `@Table(name=...)`):

- `alumno`
- `recibo`
- `app_user`
- `app_role`
- `app_user_role`
- `cat_plantel`
- `cat_carrera`
- `cat_concepto_pago`
- `cat_tipo_pago`
- `cat_escolaridad`
- `cat_estatus_recibo`
- `alumno_folio_seq`
- `carrera_concepto_config`

**No verificado en el repositorio (sin ocurrencias en búsqueda de código):**

- `recibo_folio_seq` (no aparece como tabla en entidades escaneadas).

**Conclusión práctica:** el conteo **18** y nombres adicionales deben validarse contra el esquema real de PostgreSQL local (`\dt`, `information_schema`, dump autorizado, etc.). Este documento **no afirma** el número final del esquema fuera de lo verificado arriba.

## 6. Arranque local del backend

### Pre-requisitos operativos

- PostgreSQL local accesible en `127.0.0.1:5432` con la base `USYC_DB_LOCAL`.
- Carpeta local para QR (si se usa generación/almacenamiento en disco):

`C:\proyectos\usyc-services\storage\qr`

> Nota: crear la carpeta antes del arranque si el sistema intenta escribir archivos QR.

### Comandos PowerShell (ejemplo local explícito)

> Valores **solo para desarrollo local**. Ajustar secretos/credenciales locales según tu política interna.

```powershell
cd C:\proyectos\usyc-services

$env:APP_PORT="8000"
$env:DB_URL="jdbc:postgresql://127.0.0.1:5432/USYC_DB_LOCAL"
$env:DB_USER="usyc_local"
$env:DB_PASS="usyc_local_123456"
$env:JWT_SECRET="clave_local_larga_para_desarrollo_usyc_123456789_abcdefghi"
$env:JWT_MINUTES="480"
$env:QR_DIR="C:\proyectos\usyc-services\storage\qr"
$env:JWT_COOKIE_NAME="JWT"
$env:JWT_COOKIE_SAMESITE="Lax"
$env:JWT_COOKIE_SECURE="false"

.\mvnw.cmd spring-boot:run
```

## 7. Validación del backend

Smoke checks recomendados (sin asumir datos):

| Verificación | URL / comportamiento esperado |
|---|---|
| OpenAPI docs | `http://localhost:8000/api-docs` |
| Swagger UI | `http://localhost:8000/swagger` |
| Sesión | `GET /api/auth/me` puede responder **401** si no hay cookie/sesión válida (esperado). |

Endpoints de autenticación expuestos por código (referencia):

- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/auth/logout`

## 8. Integración con frontend local

| Componente | Valor |
|---|---|
| Frontend local | `http://localhost:3000` |
| Backend local | `http://localhost:8000` |
| Variable frontend | `NEXT_PUBLIC_API_BASE_URL=http://localhost:8000` |

**Git:** el archivo `.env.local` del frontend debe permanecer **fuera del control de versiones** (normalmente ignorado). No pegar secretos en documentación ni en commits.

## 9. Producción (solo datos no sensibles)

| Elemento | Dato |
|---|---|
| SO | Debian (según contexto operativo) |
| Artefacto | `/opt/usyc/usyc-services.jar` |
| Servicio systemd | `usyc.service` |
| Ejecución típica | `java -jar /opt/usyc/usyc-services.jar` |
| Puerto backend | **8000** |
| PostgreSQL | **17.7** |
| Puerto PostgreSQL usado por backend | **5438** |
| Base | `USYC_DB` |
| Frontend estático | Nginx sirve desde `/var/www/usyc` |
| Proxy API | Nginx → `http://127.0.0.1:8000` |

**No documentar:** contraseñas de BD, `JWT_SECRET` productivo, tokens, ni cadenas de conexión completas con credenciales.

## 10. Diferencias local vs producción

| Aspecto | Local (desarrollo) | Producción |
|---|---|---|
| Base de datos | `USYC_DB_LOCAL` @ `127.0.0.1:5432` | `USYC_DB` (PostgreSQL **17.7**) |
| Puerto PostgreSQL | `5432` | `5438` |
| `QR_DIR` | Ruta Windows local (`...\storage\qr`) | Típicamente ruta Linux (`/var/usyc/qr` por default en YAML; confirmar env en servidor) |
| `JWT_COOKIE_SECURE` | `false` típico en HTTP local | En HTTPS detrás de dominio real suele requerir `true` (validar política de cookies + HTTPS) |
| JDK | Alinear a **17** (target Maven) | OpenJDK **17.x** en servidor (según micrometa) |

## 11. Seguridad

- No versionar `.env` ni archivos con secretos.
- No pegar contraseñas reales en chats, tickets ni documentación.
- No usar credenciales productivas en máquinas locales.
- Rotar secretos si hubo exposición accidental (JWT, DB, etc.).
- Validar `JWT_COOKIE_SECURE`, `SameSite` y CORS según el dominio real (Cloudflare/Nginx/HTTPS).

## 12. Problemas comunes

- **401 en `/api/auth/me` sin sesión**: comportamiento esperado si no hay cookie JWT válida.
- **CORS**: si el frontend apunta a otro host/puerto distinto al configurado en `SecurityConfig`, fallará el flujo con credenciales.
- **BD**: error típico si `DB_URL` usa puerto/host equivocado vs instancia local.
- **`QR_DIR`**: fallos al escribir QR si la carpeta no existe o no tiene permisos.
- **JDK**: una máquina local con **Java 21** puede compilar/ejecutar distinto a **Java 17** objetivo; conviene alinear toolchain.
- **Frontend export estático**: `next start` no aplica cuando el frontend usa `output: "export"` (sirve estático); esto afecta cómo se prueba contra API local (origen/puerto).

## 13. Reglas de trabajo

- No trabajar directamente sobre `main` sin política explícita del repositorio/proyecto.
- Revisar `git status` antes de cada cambio.
- No usar `git add .` (preferir rutas explícitas cuando aplique política del Mtro.).
- No `commit` ni `push` sin autorización explícita.
- Separar cambios: documentación vs configuración vs cambios funcionales.
- Probar local antes de tocar producción.
