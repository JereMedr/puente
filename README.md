# Financial Service - Backend Challenge

Este proyecto es un microservicio RESTful desarrollado en Java 21 con Spring Boot 3.x para gestionar datos de instrumentos financieros.

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