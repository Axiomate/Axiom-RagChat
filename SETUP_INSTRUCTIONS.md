# RAG Chat Storage Backend - Setup Instructions

## Project Structure Created

✅ Complete Maven Spring Boot project with 50+ files
✅ All Java source files with full implementations
✅ Docker and Docker Compose configuration
✅ Database migrations (Flyway)
✅ Complete configuration files
✅ Test suite

## Setup Steps

### 1. Create Project Directory Structure
```bash
mkdir -p rag-chat-storage-backend
cd rag-chat-storage-backend
```

### 2. Copy All Files

Copy all provided files into the appropriate directories according to the structure shown.

### 3. Configure Environment
```bash
cp .env.example .env
# Edit .env with your actual values
```

Required environment variables:
- `GEMINI_API_KEY`: Your Google Gemini API key
- `GEMINI_PROJECT_ID`: Your GCP project ID
- `JWT_SECRET`: Generate a secure random string (base64 encoded, 256+ bits)
- `API_KEY`: Your API key for service-to-service auth

### 4. Start Infrastructure
```bash
docker-compose up -d postgres redis qdrant
```

### 5. Build the Application
```bash
./mvnw clean install
```

### 6. Run the Application
```bash
./mvnw spring-boot:run
```

Or with Docker:
```bash
docker-compose up --build
```

### 7. Access the Application

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- PgAdmin: http://localhost:5050
- Qdrant Dashboard: http://localhost:6333/dashboard

## Quick Test
```bash
# Register user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123","name":"Test User"}'

# Login (save the token)
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'

# Create session (use token from login)
curl -X POST http://localhost:8080/api/v1/sessions \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"My First Chat"}'
```

## File Count by Category

- Java Source Files: 42
- Configuration Files: 10
- SQL Migrations: 5
- Test Files: 4
- Docker Files: 2
- Documentation: 3

**Total: 66 files**

## Next Steps

1. Review and customize configurations
2. Add your Gemini API credentials
3. Run tests: `./mvnw test`
4. Deploy to your environment
5. Monitor logs: `tail -f logs/ragchat.log`

## Support

For issues, refer to README.md or check application logs.