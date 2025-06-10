# Financial Service

## Repositorio
https://github.com/JereMedr/puente.git

## Descripción
Servicio financiero que proporciona información en tiempo real sobre instrumentos financieros, con capacidades de autenticación de usuarios y gestión de favoritos.

## Notas Importantes
- El archivo `.env` ha sido incluido intencionalmente en el repositorio para facilitar las pruebas y la evaluación del código.
- Se ha implementado un sistema extensivo de logging como decisión de diseño para facilitar el debugging durante las pruebas en vivo.
- No es necesario crear la base de datos manualmente, la aplicación maneja esto automáticamente.
- Se incluye una API key de Alpha Vantage para pruebas básicas. Para pruebas más extensivas, se recomienda obtener una API key propia en: https://www.alphavantage.co/support/#api-key

## Requisitos Previos
- Java 21
- Maven 3.8+
- API Key de Alpha Vantage

## Instalación y Configuración

### 1. Clonar el Repositorio
```bash
git clone https://github.com/JereMedr/puente.git
cd financialservice
```

### 2. Variables de Entorno
El archivo `.env` ya está incluido con la configuración necesaria para ejecutar el proyecto:
```properties
ALPHA_VANTAGE_API_KEY=your_api_key  # Se provee una key, pero puedes usar tu propia key de https://www.alphavantage.co/
POSTGRES_URL=jdbc:postgresql://localhost:5432/financialdb
POSTGRES_USER=your_user
POSTGRES_PASSWORD=your_password
JWT_SECRET=your_jwt_secret
```

Nota sobre Alpha Vantage API:
- La key proporcionada tiene el límite gratuito de 25 llamadas/día
- Para pruebas más extensivas, puedes obtener tu propia key gratuita en https://www.alphavantage.co/support/#api-key
- Simplemente reemplaza el valor de ALPHA_VANTAGE_API_KEY en el archivo `.env` con tu nueva key

### 3. Compilar el Proyecto
```bash
mvn clean package
```

### 4. Ejecutar con Docker Compose
```bash
docker-compose up --build
```

Este comando:
- Construye las imágenes necesarias
- Inicia todos los servicios definidos
- Configura la red entre los contenedores
- Maneja las dependencias entre servicios

Para detener la aplicación:
```bash
docker-compose down
```

### 5. Inspeccionar la Base de Datos
Para acceder a la base de datos PostgreSQL y examinar su estructura:

```bash
# Conectarse a la base de datos
docker exec -it financialservice-db-1 psql -U postgres -d financialservice

# Comandos útiles dentro de PostgreSQL:
\dt        # Listar todas las tablas
\d+ TABLA  # Describir estructura de una tabla específica
\q         # Salir del cliente psql
```

## Arquitectura
La aplicación implementa una arquitectura hexagonal (ports and adapters), permitiendo una clara separación de responsabilidades y facilitando el mantenimiento y testing del código.

### Componentes Principales
- **Controllers**: Manejo de endpoints REST y DTOs
- **Services**: Lógica de negocio y mappers
- **Domain**: Entidades y ports
- **Infrastructure**: Repositories, seguridad, servicios externos y caché

## Sistema de Logging
Se ha implementado un sistema extensivo de logging que incluye:
- Logging detallado de todas las operaciones
- Trazabilidad de requests y responses
- Logging de errores y excepciones
- Monitoreo de llamadas a la API externa
- Logging de operaciones de caché
- Logging de operaciones de base de datos

## Decisiones Técnicas

### 1. Sistema de Caché
- **Decisión**: Implementación de caché en memoria con ConcurrentHashMap.
- **Justificación**: 
  - Límites de la API de Alpha Vantage (25 llamadas/día en plan gratuito)
  - Necesidad de respuesta rápida
- **Beneficios**:
  - Reducción de llamadas a la API externa
  - Mejor tiempo de respuesta
  - Control sobre la actualización de datos

### 2. Seguridad
- **Decisión**: Implementación de JWT para autenticación.
- **Justificación**: 
  - Necesidad de stateless authentication
  - Soporte para microservicios
- **Beneficios**:
  - Escalabilidad
  - No requiere estado en servidor
  - Fácil integración con otros servicios

### 3. Persistencia
- **Decisión**: PostgreSQL con JPA/Hibernate.
- **Justificación**:
  - Necesidad de persistencia relacional
  - Soporte para transacciones
- **Beneficios**:
  - ACID compliance
  - Buen soporte para relaciones
  - Herramientas maduras



## Documentación API con Swagger
La aplicación incluye Swagger UI para facilitar la prueba y documentación de los endpoints. Para acceder a la interfaz de Swagger:

1. Inicia la aplicación
2. Abre en tu navegador: `http://localhost:8080/swagger-ui.html`

Beneficios de usar Swagger UI:
- Interfaz interactiva para probar todos los endpoints
- Documentación detallada de request/response schemas
- No requiere Postman u otras herramientas externas
- Facilita la demostración y prueba en vivo del API
- Incluye campos de autorización para probar endpoints protegidos

## Testing
La aplicación incluye:
- Tests unitarios para componentes clave
- Tests de integración para controllers y repositorios
- Tests de servicios externos

## Limitaciones Conocidas
1. Límite de llamadas a Alpha Vantage API (25/día en plan gratuito)
2. Caché en memoria (se pierde al reiniciar)
3. Sin soporte para websockets (datos no en tiempo real) 