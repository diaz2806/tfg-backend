# 💰 Control de Gastos — Backend

REST API desarrollada con **Spring Boot** para una aplicación de gestión financiera personal con inteligencia artificial integrada.

## ✨ Funcionalidades

- **Autenticación** — Registro y login de usuarios con sesiones seguras
- **Gestión de gastos** — CRUD completo de gastos con categorías personalizadas
- **Eventos financieros** — Seguimiento de eventos y gastos recurrentes
- **IA integrada** — Análisis y recomendaciones financieras mediante la API de Groq (LLM)
- **Exportación** — Generación de reportes en Excel (Apache POI) y PDF (iText7)

## 🛠️ Stack tecnológico

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje principal |
| Spring Boot 3.5 | Framework REST |
| Spring Data JPA | Persistencia de datos |
| MySQL | Base de datos |
| Spring WebFlux | Cliente reactivo para Groq API |
| Apache POI | Exportación a Excel |
| iText7 | Generación de PDF |
| Groq API | Modelos de lenguaje (IA) |

## 🏗️ Arquitectura

```
src/main/java/com/tfg/app/
├── controller/       # Endpoints REST (Auth, Usuario, Gasto, Categoria, Evento, IA)
├── service/          # Lógica de negocio
├── repository/       # Acceso a datos (Spring Data JPA)
├── model/            # Entidades JPA
└── config/           # Configuración CORS
```

## 🚀 Instalación y ejecución

### Requisitos
- Java 21+
- MySQL 8+
- Maven

### Configuración

1. Clona el repositorio:
```bash
git clone https://github.com/diaz2806/tfg-backend.git
cd tfg-backend
```

2. Crea la base de datos:
```sql
CREATE DATABASE control_gastos;
```

3. Crea el archivo `.env` en la raíz con tus credenciales:
```env
GROQ_API_KEY=tu_api_key_aqui
```

4. Configura `src/main/resources/application.properties` con tus datos de MySQL.

5. Ejecuta la aplicación:
```bash
./mvnw spring-boot:run
```

La API estará disponible en `http://localhost:8081`

## 📡 Endpoints principales

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/auth/register` | Registro de usuario |
| POST | `/auth/login` | Login |
| GET | `/gastos` | Listar gastos |
| POST | `/gastos` | Crear gasto |
| GET | `/categorias` | Listar categorías |
| POST | `/ia/analizar` | Análisis IA de gastos |
| GET | `/gastos/export/excel` | Exportar a Excel |
| GET | `/gastos/export/pdf` | Exportar a PDF |

## 🔗 Repositorio frontend

El frontend de esta aplicación está en [tfg-frontend](https://github.com/diaz2806/tfg-frontend) — desarrollado con Angular.

---

*Trabajo de Fin de Grado — Alberto Díaz*
