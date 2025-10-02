# Penny Control Backend

A comprehensive financial control and management system built with Spring Boot microservices architecture.

## 🏗️ Architecture

- **Microservices**: Modular service-based architecture
- **Shared Database**: Single PostgreSQL database (monolith-first approach)
- **Common Library**: Reusable components for all services
- **JWT Authentication**: Secure token-based authentication with refresh token rotation

## 📋 Services

| Service | Port | Description |
|---------|------|-------------|
| **Auth Service** | 8081 | Authentication, authorization, and user registration |
| **User Service** | 8082 | User profile management |

## 🚀 Tech Stack

- **Java 21**
- **Spring Boot 3.5.6**
- **Spring Security** with JWT
- **Spring Data JPA** + **Hibernate**
- **PostgreSQL** (Database)
- **Flyway** (Database migrations)
- **Swagger/OpenAPI 3** (API Documentation)
- **Lombok** (Code generation)
- **Gradle** (Build tool)
- **Docker & Docker Compose** (Containerization)

## 📚 API Documentation

### Interactive Swagger UI

All microservice APIs are documented and accessible through Swagger UI:

| Service | Swagger URL |
|---------|-------------|
| **Auth Service** | http://localhost:8081/swagger-ui.html |
| **User Service** | http://localhost:8082/swagger-ui.html |

### API Endpoints Summary

#### **Auth Service** (`/api/v1/auth`)

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/register` | POST | ❌ | Register new user |
| `/login` | POST | ❌ | Login and get tokens |
| `/refresh` | POST | ❌ | Refresh access token |
| `/logout` | POST | ❌ | Logout (revoke refresh token) |

#### **User Service** (`/api/v1/users`)

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/me` | GET | ✅ | Get current user profile |

## 🔐 Authentication Flow

### 1. Register
```bash
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePass123!",
    "name": "John Doe",
    "currency": "USD"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePass123!"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "userId": 1,
    "email": "user@example.com",
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "expiresIn": 3600
  }
}
```

### 3. Use Access Token
```bash
curl -X GET http://localhost:8082/api/v1/users/me \
  -H "Authorization: Bearer <access_token>"
```

### 4. Refresh Token (when access token expires)
```bash
curl -X POST http://localhost:8081/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "<refresh_token>"
  }'
```

### 5. Logout
```bash
curl -X POST http://localhost:8081/api/v1/auth/logout \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "<refresh_token>"
  }'
```

## 🛠️ Prerequisites

- **Java 21** or higher
- **Docker & Docker Compose**
- **Gradle** (or use included wrapper)

## ⚡ Quick Start

### 1. Clone the repository
```bash
git clone <repository-url>
cd penny-control-backend
```

### 2. Set up environment variables
Create a `.env` file in the project root:

```env
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=penny_control_db
DB_USERNAME=postgres
DB_PASSWORD=postgres

# Service Ports
AUTH_SERVICE_PORT=8081
USER_SERVICE_PORT=8082

# JWT Configuration
JWT_SECRET=your-secret-key-min-512-bits-for-hs512-algorithm-change-this-in-production

# CORS
ALLOWED_ORIGIN_1=http://localhost:3000
ALLOWED_ORIGIN_2=http://localhost:5173
```

### 3. Start PostgreSQL Database
```bash
docker-compose up -d
```

This will start PostgreSQL on port 5432.

### 4. Build the project
```bash
./gradlew build
```

### 5. Run the services

**Option A: Run all services**
```bash
# Terminal 1 - Auth Service
./gradlew :auth-service:bootRun

# Terminal 2 - User Service
./gradlew :user-service:bootRun
```

**Option B: Run individual service**
```bash
./gradlew :auth-service:bootRun
# or
./gradlew :user-service:bootRun
```

### 6. Access Swagger UI
- Auth Service API Docs: http://localhost:8081/swagger-ui.html
- User Service API Docs: http://localhost:8082/swagger-ui.html

## 🗄️ Database Schema

### Tables

#### `users`
- Core user authentication and profile data
- Managed by: **auth-service**

#### `roles`
- Master role definitions (ROLE_USER, ROLE_ADMIN)
- Managed by: **auth-service**

#### `user_roles`
- User-role assignments (Many-to-Many)
- Managed by: **auth-service**

#### `refresh_tokens`
- Refresh token storage for session management
- Enables true logout functionality
- Tracks IP address, user agent, and usage
- Managed by: **auth-service**

### Migrations

Database migrations are managed by **Flyway** in the **auth-service**:

```
auth-service/src/main/resources/db/migration/
├── V1__create_users_table.sql
├── V2__create_user_roles_table.sql
├── V3__create_roles_table.sql
├── V4__migrate_user_roles_to_foreign_key.sql
└── V5__create_refresh_tokens_table.sql
```

## 🏗️ Project Structure

```
penny-control-backend/
├── auth-service/                # Authentication & authorization
│   ├── src/main/
│   │   ├── java/.../authservice/
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── service/         # Business logic
│   │   │   ├── repository/      # Data access
│   │   │   ├── entity/          # JPA entities
│   │   │   └── dto/             # Request/Response DTOs
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/    # Flyway migrations
│   └── build.gradle
│
├── user-service/                # User profile management
│   ├── src/main/
│   │   ├── java/.../userservice/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── entity/
│   │   │   └── dto/
│   │   └── resources/
│   │       └── application.yml
│   └── build.gradle
│
├── common-library/              # Shared components
│   ├── src/main/
│   │   └── java/.../common/
│   │       ├── config/          # Spring configurations
│   │       ├── security/        # JWT & security
│   │       ├── exception/       # Error handling
│   │       └── dto/             # Shared DTOs
│   └── build.gradle
│
├── docker-compose.yml           # PostgreSQL container
├── build.gradle                 # Root build configuration
├── settings.gradle              # Multi-module setup
└── README.md
```

## 🔧 Development

### Run Tests
```bash
./gradlew test
```

### Build without Tests
```bash
./gradlew build -x test
```

### Clean Build
```bash
./gradlew clean build
```

### Format Code
```bash
./gradlew spotlessApply
```

## 🔒 Security Features

- ✅ **JWT Authentication** with HS512 algorithm
- ✅ **Token Rotation** for refresh tokens (prevents replay attacks)
- ✅ **Role-Based Access Control** (RBAC)
- ✅ **Password Hashing** with BCrypt
- ✅ **Session Management** with refresh token tracking
- ✅ **True Logout** via token revocation
- ✅ **CORS Configuration** for frontend integration
- ✅ **Request Validation** with Jakarta Validation

## 📝 Token Lifecycle

| Token Type | Validity | Storage | Purpose |
|------------|----------|---------|---------|
| **Access Token** | 1 hour | Client-side only | API authentication |
| **Refresh Token** | 30 days | Database + Client | Get new access tokens |

## 🌐 Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | PostgreSQL host | localhost |
| `DB_PORT` | PostgreSQL port | 5432 |
| `DB_NAME` | Database name | penny_control_db |
| `DB_USERNAME` | Database username | postgres |
| `DB_PASSWORD` | Database password | postgres |
| `AUTH_SERVICE_PORT` | Auth service port | 8081 |
| `USER_SERVICE_PORT` | User service port | 8082 |
| `JWT_SECRET` | JWT signing key (min 512 bits) | - |
| `ALLOWED_ORIGIN_1` | CORS origin 1 | http://localhost:3000 |
| `ALLOWED_ORIGIN_2` | CORS origin 2 | http://localhost:5173 |

## 📊 API Response Format

All APIs return responses in this standard format:

### Success Response
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { },
  "timestamp": "2025-10-02T10:00:00"
}
```

### Error Response
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Email already registered",
    "details": null
  },
  "timestamp": "2025-10-02T10:00:00"
}
```

## 🐛 Common Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `VALIDATION_ERROR` | 400 | Invalid request data |
| `INVALID_CREDENTIALS` | 401 | Wrong email/password |
| `INVALID_TOKEN` | 401 | Invalid or expired token |
| `ACCESS_DENIED` | 403 | Insufficient permissions |
| `RESOURCE_NOT_FOUND` | 404 | Resource not found |
| `INTERNAL_SERVER_ERROR` | 500 | Server error |

## 🤝 Contributing

1. Create a feature branch
2. Make your changes
3. Add tests
4. Submit a pull request

## 📄 License

All rights reserved.

## 📧 Support

For issues and questions, please contact the development team.

---

**Built with ❤️ using Spring Boot**
