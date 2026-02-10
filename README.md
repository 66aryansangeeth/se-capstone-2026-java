# E-Commerce Microservices Platform

A comprehensive e-commerce platform built with Spring Boot microservices architecture, featuring user authentication, product management, order processing, and payment integration with Stripe.

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Services](#services)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Local Setup](#local-setup)
    - [1. Database Setup (PostgreSQL with Podman)](#1-database-setup-postgresql-with-podman)
    - [2. Redis Setup (with Podman)](#2-redis-setup-with-podman)
    - [3. Environment Variables](#3-environment-variables)
    - [4. Running Services Locally](#4-running-services-locally)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Deployment](#deployment)
- [Project Structure](#project-structure)

## 🎯 Overview

This is a microservices-based e-commerce platform that handles:
- **User Authentication & Authorization** - JWT-based authentication with role-based access control (USER/ADMIN)
- **Product Management** - CRUD operations, inventory management, and product search
- **Order Processing** - Order creation, validation, stock management, and order history
- **Payment Processing** - Stripe integration for secure payment processing
- **API Gateway** - Centralized routing, rate limiting, and authentication

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    API Gateway (Port 8080)                   │
│  - Routing, Rate Limiting, JWT Validation, Swagger UI      │
└──────────────┬──────────────────────────────────────────────┘
               │
    ┌──────────┼──────────┬──────────┬──────────┐
    │          │          │          │          │
    ▼          ▼          ▼          ▼          ▼
┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐
│  Auth  │ │Product │ │ Order  │ │Payment │ │ Redis  │
│Service │ │Service │ │Service │ │Service │ │(Cache) │
│ :8081  │ │ :8082  │ │ :8083  │ │ :8084  │ │ :6379  │
└───┬────┘ └───┬────┘ └───┬────┘ └───┬────┘ └────────┘
    │          │          │          │
    └──────────┴──────────┴──────────┘
               │
               ▼
        ┌──────────────┐
        │  PostgreSQL  │
        │   Database    │
        └──────────────┘
```

### Service Communication Flow

1. **Client** → API Gateway (with JWT token)
2. **API Gateway** → Validates JWT, applies rate limiting
3. **API Gateway** → Routes to appropriate microservice
4. **Microservices** → Communicate via WebClient (reactive HTTP)
5. **Payment Service** → Integrates with Stripe API
6. **Order Service** → Updates product stock via Product Service
7. **Payment Service** → Notifies Order Service via webhook

## 🔧 Services

### 1. **Auth Service** (Port 8081)
- User registration and authentication
- JWT token generation and validation
- Password management (change password, admin reset)
- User management (admin operations)
- Role-based access control (USER, ADMIN)

### 2. **Product Service** (Port 8082)
- Product CRUD operations
- Product search (by name and/or category)
- Inventory management (stock reduction)
- Product audit logging

### 3. **Order Service** (Port 8083)
- Order creation and management
- Order history retrieval
- Stock validation and reduction
- Payment session creation
- Order status management (PENDING, CONFIRMED, CANCELLED)

### 4. **Payment Service** (Port 8084)
- Stripe checkout session creation
- Webhook handling for payment events
- Payment status updates
- Order confirmation/cancellation notifications

### 5. **API Gateway** (Port 8080)
- Request routing to microservices
- JWT authentication and authorization
- Rate limiting (Redis-based)
- Unified Swagger UI documentation
- CORS configuration

## 🛠️ Tech Stack

- **Framework**: Spring Boot 3.5.10
- **Language**: Java 17
- **Build Tool**: Maven
- **Database**: PostgreSQL
- **Cache/Rate Limiting**: Redis
- **API Gateway**: Spring Cloud Gateway
- **Security**: Spring Security, JWT (HS384)
- **Payment**: Stripe API
- **Documentation**: Swagger/OpenAPI 3
- **Reactive Programming**: Project Reactor (WebFlux)
- **HTTP Client**: Spring WebClient

## 📦 Prerequisites

Before running the project locally, ensure you have:

- **Java 17** or higher
- **Maven 3.6+**
- **Podman** (or Docker) for containerized services
- **Stripe Account** (for payment processing)
    - Get your Stripe Secret Key from [Stripe Dashboard](https://dashboard.stripe.com/apikeys)
    - Get your Stripe Webhook Secret (after setting up webhook endpoint)

## 🚀 Local Setup

### 1. Database Setup (PostgreSQL with Podman)

Run PostgreSQL in a Podman container:

```bash
podman run --name ecommerce-postgres \
  -e POSTGRES_USER=ecommerce_user \
  -e POSTGRES_PASSWORD=ecommerce_password \
  -e POSTGRES_DB=ecommerce_db \
  -p 5432:5432 \
  -d postgres:15-alpine
```

**Verify PostgreSQL is running:**
```bash
podman ps
# Should show ecommerce-postgres container running
```

**Connect to PostgreSQL (optional):**
```bash
podman exec -it ecommerce-postgres psql -U ecommerce_user -d ecommerce_db
```

**Stop PostgreSQL (when needed):**
```bash
podman stop ecommerce-postgres
```

**Start PostgreSQL again:**
```bash
podman start ecommerce-postgres
```

**Remove PostgreSQL container (if needed):**
```bash
podman stop ecommerce-postgres
podman rm ecommerce-postgres
```

### 2. Redis Setup (with Podman)

Run Redis in a Podman container:

```bash
podman run --name ecommerce-redis \
  -p 6379:6379 \
  -d redis:7-alpine
```

**Verify Redis is running:**
```bash
podman ps
# Should show ecommerce-redis container running
```

**Test Redis connection (optional):**
```bash
podman exec -it ecommerce-redis redis-cli ping
# Should return: PONG
```

**Stop Redis (when needed):**
```bash
podman stop ecommerce-redis
```

**Start Redis again:**
```bash
podman start ecommerce-redis
```

**Remove Redis container (if needed):**
```bash
podman stop ecommerce-redis
podman rm ecommerce-redis
```

### 3. Environment Variables

Create a `.env` file in the project root (or export these variables in your shell):

```bash
# Database Configuration
export DB_URL=jdbc:postgresql://localhost:5432/ecommerce_db
export DB_USERNAME=ecommerce_user
export DB_PASSWORD=ecommerce_password

# JWT Configuration (generate a secure key)
export JWT_SECRET_KEY=your-base64-encoded-secret-key-here-minimum-32-bytes

# Redis Configuration
export REDIS_HOST=localhost
export REDIS_PORT=6379

# Service URLs (for local development)
export AUTH_SERVICE_URL=http://localhost:8081
export PRODUCT_SERVICE_URL=http://localhost:8082
export ORDER_SERVICE_URL=http://localhost:8083
export PAYMENT_SERVICE_URL=http://localhost:8084

# Stripe Configuration
export STRIPE_SECRET_KEY=sk_test_your_stripe_secret_key
export STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret

# Gateway Configuration
export GATEWAY_URL=http://localhost:8080
```

**Generate JWT Secret Key:**
```bash
# Generate a secure random key (Base64 encoded, minimum 32 bytes)
openssl rand -base64 48
```

**Note**: Each service uses the same `JWT_SECRET_KEY` for token validation. Make sure it's the same across all services.

### 4. Running Services Locally

#### Option A: Run All Services from Root (Recommended)

From the project root directory:

```bash
# Build all services
mvn clean install

# Run Auth Service
cd auth-service
mvn spring-boot:run

# In a new terminal, run Product Service
cd product-service
mvn spring-boot:run

# In a new terminal, run Order Service
cd order-service
mvn spring-boot:run

# In a new terminal, run Payment Service
cd payment-service
mvn spring-boot:run

# In a new terminal, run API Gateway
cd api-gateway
mvn spring-boot:run
```

#### Option B: Run Services Individually

**1. Auth Service (Port 8081)**
```bash
cd auth-service
export DB_URL=jdbc:postgresql://localhost:5432/ecommerce_db
export DB_USERNAME=ecommerce_user
export DB_PASSWORD=ecommerce_password
export JWT_SECRET_KEY=your-jwt-secret-key
mvn spring-boot:run
```

**2. Product Service (Port 8082)**
```bash
cd product-service
export DB_URL=jdbc:postgresql://localhost:5432/ecommerce_db
export DB_USERNAME=ecommerce_user
export DB_PASSWORD=ecommerce_password
export JWT_SECRET_KEY=your-jwt-secret-key
mvn spring-boot:run
```

**3. Order Service (Port 8083)**
```bash
cd order-service
export DB_URL=jdbc:postgresql://localhost:5432/ecommerce_db
export DB_USERNAME=ecommerce_user
export DB_PASSWORD=ecommerce_password
export JWT_SECRET_KEY=your-jwt-secret-key
export PRODUCT_SERVICE_URL=http://localhost:8082
export PAYMENT_SERVICE_URL=http://localhost:8084
mvn spring-boot:run
```

**4. Payment Service (Port 8084)**
```bash
cd payment-service
export DB_URL=jdbc:postgresql://localhost:5432/ecommerce_db
export DB_USERNAME=ecommerce_user
export DB_PASSWORD=ecommerce_password
export JWT_SECRET_KEY=your-jwt-secret-key
export STRIPE_SECRET_KEY=sk_test_your_stripe_key
export STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret
export ORDER_SERVICE_URL=http://localhost:8083
mvn spring-boot:run
```

**5. API Gateway (Port 8080)**
```bash
cd api-gateway
export JWT_SECRET_KEY=your-jwt-secret-key
export REDIS_HOST=localhost
export REDIS_PORT=6379
export AUTH_SERVICE_URL=http://localhost:8081
export PRODUCT_SERVICE_URL=http://localhost:8082
export ORDER_SERVICE_URL=http://localhost:8083
export PAYMENT_SERVICE_URL=http://localhost:8084
export GATEWAY_URL=http://localhost:8080
mvn spring-boot:run
```

#### Verify Services are Running

Check each service health:
- Auth Service: http://localhost:8081
- Product Service: http://localhost:8082
- Order Service: http://localhost:8083
- Payment Service: http://localhost:8084
- API Gateway: http://localhost:8080

## 📚 API Documentation

### Swagger UI

Access the unified Swagger UI documentation at:
- **Local**: http://localhost:8080/swagger-ui.html
- **Cloud Run**: https://your-gateway-url.run.app/swagger-ui.html

The Swagger UI shows all endpoints from all services in one unified interface.

### API Endpoints

#### Authentication Service
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login
- `PUT /api/auth/change-password` - Change password (authenticated)
- `PUT /api/auth/admin-reset` - Admin password reset (admin only)
- `GET /api/auth/admin/users` - Get all users (admin only)
- `DELETE /api/auth/admin/delete` - Delete user (admin only)

#### Product Service
- `GET /api/products` - Get all products (optional category filter)
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create product (admin only)
- `PUT /api/products/{id}` - Update product (admin only)
- `DELETE /api/products/{id}` - Delete product (admin only)
- `POST /api/products/search` - Search products (name and/or category)
- `PATCH /api/products/{id}/reduce-stock` - Reduce stock (internal use)

#### Order Service
- `POST /api/orders` - Place new order (authenticated)
- `GET /api/orders/my-orders` - Get user's order history (authenticated)
- `GET /api/orders/{id}` - Get order by ID (admin only)

#### Payment Service
- `POST /api/payments/create-session` - Create Stripe checkout session (authenticated)
- `POST /api/payments/webhook` - Stripe webhook endpoint (public, Stripe only)

## 🧪 Testing

### 1. Register a User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "role": "USER"
  }'
```

### 2. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

Save the `token` from the response.

### 3. Create a Product (Admin)

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '{
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 99900,
    "stockQuantity": 10,
    "category": "Electronics"
  }'
```

**Note**: Price is in cents (99900 = $999.00)

### 4. Place an Order

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_USER_TOKEN" \
  -d '{
    "items": [
      {
        "productId": 1,
        "quantity": 2
      }
    ]
  }'
```

### 5. Access Swagger UI

Open http://localhost:8080/swagger-ui.html in your browser to:
- View all API endpoints
- Test endpoints interactively
- See request/response schemas
- Authenticate with JWT token

## 🚢 Deployment

### Cloud Run Deployment

The project is configured for Google Cloud Run deployment. Each service has a Dockerfile:

- `Dockerfile.auth` - Auth Service
- `Dockerfile.product` - Product Service
- `Dockerfile.order` - Order Service
- `Dockerfile.payment` - Payment Service
- `Dockerfile.gateway` - API Gateway

### Environment Variables for Cloud Run

Set these in Cloud Run for each service:

```bash
# Database (CloudSQL)
DB_URL=jdbc:postgresql://your-cloudsql-instance/dbname
DB_USERNAME=your-username
DB_PASSWORD=your-password

# JWT
JWT_SECRET_KEY=your-jwt-secret-key

# Redis 
REDIS_HOST=your-redis-host
REDIS_PORT=6379

# Service URLs (Cloud Run URLs)
AUTH_SERVICE_URL=https://auth-service-url.run.app
PRODUCT_SERVICE_URL=https://product-service-url.run.app
ORDER_SERVICE_URL=https://order-service-url.run.app
PAYMENT_SERVICE_URL=https://payment-service-url.run.app
GATEWAY_URL=https://api-gateway-url.run.app

# Stripe
STRIPE_SECRET_KEY=sk_live_your_stripe_key
STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret
```

## 📁 Project Structure

```
se-capstone-2026-java/
├── api-gateway/          # API Gateway service
├── auth-service/         # Authentication service
├── product-service/      # Product management service
├── order-service/        # Order processing service
├── payment-service/      # Payment processing service
├── pom.xml               # Parent POM
├── Dockerfile.*          # Dockerfiles for each service
├── database_migration_script.sql  # Database migration script
└── README.md            # This file
```

## 🔐 Security Notes

1. **JWT Secret Key**: Must be the same across all services
2. **Database Credentials**: Use strong passwords in production
3. **Stripe Keys**: Never commit Stripe keys to version control
4. **Internal Secret**: Used for service-to-service communication (currently: `my-app-secret-123`)

## 🐛 Troubleshooting

### Database Connection Issues
- Verify PostgreSQL container is running: `podman ps`
- Check connection string: `jdbc:postgresql://localhost:5432/ecommerce_db`
- Verify credentials match container environment variables

### Redis Connection Issues
- Verify Redis container is running: `podman ps`
- Check Redis host and port: `localhost:6379`
- Test connection: `podman exec -it ecommerce-redis redis-cli ping`

### Service Communication Issues
- Verify all services are running on correct ports
- Check service URLs in environment variables
- Verify JWT secret key is the same across all services

### Swagger UI Not Loading
- Access via: http://localhost:8080/swagger-ui.html
- Check browser console for errors
- Verify API Gateway is running

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 📄 License

See LICENSE file for details.


---

