# Financial Service - Backend Challenge

Este proyecto es un microservicio RESTful desarrollado en Java 21 con Spring Boot 3.x para gestionar datos de instrumentos financieros.

## Estado Actual del Proyecto

El proyecto ha completado las siguientes fases de implementación:

1. ✅ Configuración Inicial
   - Estructura del proyecto con arquitectura hexagonal
   - Configuración de Java 21 y Spring Boot 3.x
   - Configuración de dependencias en pom.xml

2. ✅ Configuración de Docker
   - Dockerfile y docker-compose.yml
   - Configuración de PostgreSQL
   - Gestión de variables de entorno

3. ✅ Módulo de Usuarios
   - Modelo de dominio y DTOs
   - Servicios de autenticación y registro
   - Endpoints REST con seguridad JWT

4. ✅ Módulo de Instrumentos Financieros
   - Integración con Alpha Vantage API
   - Caché de datos financieros
   - Endpoints para consulta de instrumentos

5. ✅ Módulo de Favoritos
   - Implementación de modelo con clave compuesta
   - Gestión eficiente de favoritos por usuario
   - Endpoints para agregar/eliminar/listar favoritos

Próximos pasos:
- [ ] Implementación de pruebas unitarias
- [ ] Implementación de pruebas de integración
- [ ] Documentación completa de la API
- [ ] Despliegue en ambiente de producción

## Características Principales

- Consumo de datos de instrumentos financieros de Alpha Vantage API
- Gestión de usuarios con autenticación JWT
- Sistema de favoritos para instrumentos financieros
- Arquitectura hexagonal (ports and adapters)
- Docker y Docker Compose para containerización
- Pruebas unitarias y de integración
- Caché para optimización de rendimiento
- Manejo seguro de contraseñas con BCrypt

## Requisitos Previos

- Java 21
- Maven
- Docker y Docker Compose
- API Key de Alpha Vantage (opcional para desarrollo)
- PostgreSQL (si se ejecuta localmente sin Docker)

## Estructura del Proyecto

```
src/main/java/com/puente/financialservice/
├── user/                           # Módulo de usuarios
│   ├── domain/                     # Capa de dominio
│   │   ├── model/                  # Entidades del dominio
│   │   └── port/                   # Puertos (interfaces)
│   ├── application/                # Capa de aplicación
│   │   ├── service/                # Servicios de aplicación
│   │   └── dto/                    # Objetos de transferencia de datos
│   └── infrastructure/             # Capa de infraestructura
│       ├── persistence/            # Implementaciones de repositorios
│       └── security/               # Configuración de seguridad
├── financialinstrument/            # Módulo de instrumentos financieros
│   ├── domain/
│   │   ├── model/
│   │   └── port/
│   ├── application/
│   │   ├── service/
│   │   └── dto/
│   └── infrastructure/
│       ├── persistence/
│       └── external/               # Cliente Alpha Vantage
├── favorite/                       # Módulo de favoritos
│   ├── domain/
│   │   ├── model/                  # Entidades Favorite y FavoriteId
│   │   └── port/                   # Interfaces de repositorio
│   ├── application/
│   │   ├── service/                # Servicio de gestión de favoritos
│   │   └── dto/                    # DTOs para favoritos
│   └── infrastructure/
│       ├── persistence/            # Implementación JPA del repositorio
│       └── controller/             # Controlador REST
└── common/                         # Componentes comunes
    ├── config/                     # Configuraciones globales
    └── exception/                  # Manejo de excepciones
```

## Configuración del Entorno

1. Clona el repositorio:
```bash
git clone https://github.com/JereMedr/puente.git
cd financial-service
```

2. Configura las variables de entorno:
   - Copia el archivo `.env.example` a `.env`
   - Ajusta los valores según tu entorno:
     ```
     DB_HOST=localhost
     DB_PORT=5432
     DB_NAME=financialservice
     DB_USER=postgres
     DB_PASSWORD=postgres
     ALPHA_VANTAGE_API_KEY=your_api_key
     JWT_SECRET=your_jwt_secret
     ```

3. Construye el proyecto:
```bash
./mvnw clean package
```

## Ejecución con Docker

1. Construye y levanta los contenedores:
```bash
docker-compose up --build
```

2. Para detener los contenedores:
```bash
docker-compose down
```

3. Para ver los logs:
```bash
docker-compose logs -f
```

4. Para ejecutar en modo detached:
```bash
docker-compose up -d
```

## Ejecución Local (sin Docker)

1. Asegúrate de tener PostgreSQL instalado y corriendo
2. Configura las variables de entorno en el archivo `.env`
3. Ejecuta la aplicación:
```bash
./mvnw spring-boot:run
```

## Ejecución de Pruebas

1. Ejecutar todas las pruebas:
```bash
./mvnw test
```

2. Ejecutar pruebas con cobertura:
```bash
./mvnw verify
```

3. Ver el reporte de cobertura:
   - Abre `target/site/jacoco/index.html` en tu navegador

## Endpoints Disponibles

### Usuarios
- POST /api/users/register - Registro de usuarios
  ```json
  {
    "email": "user@example.com",
    "password": "password123",
    "name": "John Doe"
  }
  ```
- POST /api/users/login - Login y generación de JWT
  ```json
  {
    "email": "user@example.com",
    "password": "password123"
  }
  ```
- GET /api/users/profile - Consulta de perfil propio
- PUT /api/users/profile - Actualización de perfil

### Instrumentos Financieros
- GET /api/instruments - Lista de instrumentos predefinidos
- GET /api/instruments/{symbol} - Detalles de un instrumento
- POST /api/favorites/{symbol} - Marcar como favorito
- DELETE /api/favorites/{symbol} - Eliminar de favoritos
- GET /api/favorites - Listar favoritos del usuario

## Seguridad

- Autenticación basada en JWT
- Contraseñas hasheadas con BCrypt
- Endpoints protegidos con Spring Security
- Validación de datos de entrada
- Manejo seguro de errores

## Notas de Desarrollo

- La aplicación usa una API key de demostración de Alpha Vantage por defecto
- Los datos se actualizan cada 5 minutos
- Se implementa caché para optimizar el consumo de la API externa
- Las pruebas usan una base de datos H2 en memoria
- El proyecto sigue los principios SOLID y Clean Architecture

## Tecnologías Utilizadas

- Java 21
- Spring Boot 3.x
- Spring Security + JWT
- PostgreSQL
- Docker + Docker Compose
- Maven
- Lombok
- JUnit 5
- Mockito
- JaCoCo (cobertura de código)
- H2 Database (pruebas)

## Contribución

1. Fork el repositorio
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## API Documentation

### Authentication

La API utiliza autenticación JWT. Para obtener un token, primero debes registrarte y luego iniciar sesión.

#### Registro de Usuario
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "securePassword123"
  }'
```

Respuesta:
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "role": "USER"
}
```

#### Inicio de Sesión
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "securePassword123"
  }'
```

Respuesta:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer"
}
```

### Instrumentos Financieros

#### Obtener Lista de Instrumentos
```bash
curl -X GET http://localhost:8080/api/v1/instruments \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

Respuesta:
```json
{
  "content": [
    {
      "symbol": "AAPL",
      "name": "Apple Inc.",
      "type": "STOCK",
      "currency": "USD",
      "price": 150.25
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1
}
```

#### Buscar Instrumento por Símbolo
```bash
curl -X GET http://localhost:8080/api/v1/instruments/AAPL \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

Respuesta:
```json
{
  "symbol": "AAPL",
  "name": "Apple Inc.",
  "type": "STOCK",
  "currency": "USD",
  "price": 150.25
}
```

### Gestión de Favoritos

#### Símbolos Disponibles
Los siguientes símbolos están predefinidos en el sistema y pueden ser agregados a favoritos:

```
AAPL  - Apple Inc
MSFT  - Microsoft Corporation
GOOGL - Alphabet Inc
AMZN  - Amazon.com Inc
META  - Meta Platforms Inc
TSLA  - Tesla Inc
JPM   - JPMorgan Chase & Co
V     - Visa Inc
PG    - Procter & Gamble Co
JNJ   - Johnson & Johnson
WMT   - Walmart Inc
BAC   - Bank of America Corp
KO    - Coca-Cola Co
DIS   - Walt Disney Co
NFLX  - Netflix Inc
INTC  - Intel Corporation
VZ    - Verizon Communications Inc
T     - AT&T Inc
PFE   - Pfizer Inc
MRK   - Merck & Co Inc
```

**Nota sobre límites de API:** 
- El sistema utiliza Alpha Vantage API que tiene un límite de 25 llamadas por día en su versión gratuita
- Los datos de los instrumentos se actualizan en ciclos de 5 minutos, 4 símbolos por ciclo
- Cuando se alcanza el límite diario de la API, solo se pueden agregar a favoritos los símbolos que ya tienen datos cargados
- Los símbolos nuevos agregados como favoritos obtendrán sus datos actualizados en el próximo ciclo de actualización disponible

#### Agregar Instrumento a Favoritos
```bash
curl -X POST http://localhost:8080/api/v1/favorites/AAPL \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

Respuesta (200 OK):
```json
{
  "symbol": "AAPL",
  "createdAt": "2024-03-19T10:30:00"
}
```

Respuestas de Error:
```json
// 400 Bad Request - Símbolo no válido
{
  "error": "Invalid financial instrument symbol - not in predefined list",
  "status": 400
}

// 400 Bad Request - Ya está en favoritos
{
  "error": "Symbol is already in favorites",
  "status": 400
}
```

#### Eliminar Instrumento de Favoritos
```bash
curl -X DELETE http://localhost:8080/api/v1/favorites/AAPL \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

Respuesta (200 OK):
```json
{}
```

#### Listar Favoritos del Usuario
```bash
curl -X GET http://localhost:8080/api/v1/favorites \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

Respuesta:
```json
[
  {
    "symbol": "AAPL",
    "createdAt": "2024-03-19T10:30:00"
  },
  {
    "symbol": "GOOGL",
    "createdAt": "2024-03-19T11:15:00"
  }
]
```

### Notas sobre Favoritos

- Los favoritos son específicos para cada usuario autenticado
- La operación de agregar verifica que el símbolo exista en el sistema
- No es necesario llamar a la API externa al listar favoritos
- Las operaciones son atómicas y manejan concurrencia
- Se implementa caché para optimizar el rendimiento

### Notas Importantes

1. **Autenticación**: Todas las peticiones (excepto registro y login) requieren el token JWT en el header `Authorization: Bearer YOUR_JWT_TOKEN`

2. **Paginación**: Las listas de instrumentos y favoritos soportan paginación con los siguientes parámetros:
   - `page`: Número de página (comienza en 0)
   - `size`: Tamaño de la página
   - `sort`: Campo por el cual ordenar

   Ejemplo:
   ```bash
   curl -X GET "http://localhost:8080/api/v1/instruments?page=0&size=20&sort=symbol,asc" \
     -H "Authorization: Bearer YOUR_JWT_TOKEN"
   ```

3. **Caché**: Las consultas de instrumentos financieros están cacheadas por 5 minutos para optimizar el rendimiento.

4. **Límites de API**: La integración con Alpha Vantage tiene un límite de 5 llamadas por minuto y 500 por día en el plan gratuito. 