# E-Commerce Backend API

A RESTful e-commerce backend application developed using Java, Spring Boot, and PostgreSQL. The project follows a layered architecture and implements secure authentication, database management, and containerized application deployment.

## Technology Stack

| Category | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.0 |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA, Hibernate |
| Security | Spring Security, JWT |
| Build Tool | Maven |
| Containerization | Docker, Docker Compose |
| API Documentation | OpenAPI / Swagger |

## Features

- RESTful API architecture
- User authentication and authorization
- JWT-based authentication
- Role-based access control
- Password encryption using password hashing
- Product and category management
- PostgreSQL database integration
- Global exception handling
- Request validation
- Environment-based configuration
- Docker-based application deployment

## Project Architecture

The application follows a layered architecture to maintain separation of concerns and improve maintainability.

```text
src/main/java/com/ecommerce/
│
├── config/
│   └── Application and security configurations
│
├── controller/
│   └── REST API endpoints
│
├── dto/
│   └── Data Transfer Objects
│
├── entity/
│   └── Database entities
│
├── exception/
│   └── Global exception handling
│
├── repository/
│   └── Data access layer
│
└── service/
    └── Business logic
```

## Prerequisites

Ensure the following tools are installed:

- Java Development Kit (JDK) 21
- Docker Desktop
- Git
- Maven (optional when using Maven Wrapper)

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/Subrata-05/ECommerce.git
cd ECommerce
```

### 2. Configure Environment Variables

Create a `.env` file in the project root and configure the required database and application environment variables.

Do not commit `.env` files, database credentials, JWT secrets, or other sensitive information to version control.

### 3. Start the Application

Build and start the application using Docker Compose:

```bash
docker compose up --build -d
```

### 4. Verify Running Containers

```bash
docker compose ps
```

### 5. View Application Logs

```bash
docker compose logs -f
```

### 6. Access the Application

The backend application runs at:

```text
http://localhost:8080
```

## Database Configuration

The application uses PostgreSQL as its relational database.

| Configuration | Value |
|---|---|
| Database | ecommerce_db |
| Database Version | PostgreSQL 16 |
| Persistence | Spring Data JPA |
| ORM | Hibernate |

Database connection details are managed through environment variables.

## Docker Commands

Start the application:

```bash
docker compose up -d
```

Rebuild and start:

```bash
docker compose up --build -d
```

Stop containers:

```bash
docker compose down
```

View application logs:

```bash
docker compose logs -f
```

Check container status:

```bash
docker compose ps
```

## Security

The application incorporates the following security mechanisms:

- JWT-based stateless authentication
- Role-based access control
- Password hashing
- Spring Security integration
- Environment-based secret management

## Future Enhancements

- Complete order management workflow
- Payment gateway integration
- Automated unit and integration testing
- Cloud deployment
- Frontend integration
- Improved API documentation

## Author

**Subrata Ghosh**

GitHub: [Subrata-05](https://github.com/Subrata-05)

Project Repository: [E-Commerce Backend API](https://github.com/Subrata-05/ECommerce)

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
