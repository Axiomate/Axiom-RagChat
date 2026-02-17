# RAG Chat Storage Backend

Production-ready RAG Chat Storage Microservice with lifecycle-aware AI memory compression using Gemini 2.5 Flash.

## Features

- **Event-driven Architecture**: Modular monolith ready for microservice transition
- **Dual Authentication**: JWT and API Key support
- **Rate Limiting**: Redis-backed Bucket4j implementation
- **AI Memory Compression**: Rolling summary with lifecycle-aware optimization
- **Vector Storage**: Qdrant integration for semantic search
- **Session Management**: Automatic timeout, snapshot, and finalization
- **User Preferences**: Aggregated preference learning across sessions

## Architecture

### Storage
- **PostgreSQL**: Relational data with message partitioning
- **Qdrant**: Vector embeddings for semantic search
- **Redis**: Caching and rate limiting

### Memory Model
1. **Rolling Compression**: Periodic summary updates during active sessions
2. **Lifecycle Finalization**: Complete summary on session end
3. **Session Snapshots**: Quick restart capability
4. **Preference Aggregation**: User-level learning

## Quick Start

### Prerequisites
- Java 21+
- Docker & Docker Compose
- Maven 3.8+

### Using Docker Compose

1. Clone the repository
2. Copy `.env.example` to `.env` and configure:
```bash
cp .env.example .env
# Edit .env with your configuration
```

3. Start all services:
```bash
docker-compose up -d
```

The application will be available at `http://localhost:8080`

### Local Development

1. Start infrastructure services:
```bash
docker-compose up postgres qdrant redis -d
```

2. Copy and configure application properties:
```bash
cp .env.example .env
# Edit .env with your local settings
```

3. Run the application:
```bash
./mvnw spring-boot:run
```

## API Documentation

Once running, access Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

API documentation available at:
```
http://localhost:8080/v3/api-docs
```

## Core Endpoints

### Authentication
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - Login and receive JWT token
- `POST /api/v1/auth/logout` - Logout and invalidate token

### Sessions
- `POST /api/v1/sessions` - Create new chat session
- `GET /api/v1/sessions` - List user sessions
- `GET /api/v1/sessions/{id}` - Get session details with summary
- `DELETE /api/v1/sessions/{id}` - End session (triggers finalization)
- `POST /api/v1/sessions/{id}/restart` - Restart from snapshot

### Messages
- `POST /api/v1/sessions/{sessionId}/messages` - Send message
- `GET /api/v1/sessions/{sessionId}/messages` - Get messages (paginated)
- `GET /api/v1/sessions/{sessionId}/messages/search` - Semantic search

### Users
- `GET /api/v1/users/me` - Current user profile
- `GET /api/v1/users/me/preferences` - Aggregated preferences across sessions
- `PUT /api/v1/users/me` - Update user profile

## Configuration

Key configuration in `application.yml`:
```yaml
ragchat:
  summary:
    trigger-message-count: 20  # Messages before rolling summary
    max-tokens: 2000           # Max tokens per summary
  session:
    timeout-minutes: 30        # Inactivity timeout
    inactivity-check-minutes: 5  # Check interval
  rate-limit:
    capacity: 100              # Max requests
    refill-tokens: 100         # Refill amount
    refill-duration-minutes: 1  # Refill period
```

## Database Migrations

Flyway migrations in `src/main/resources/db/migration`:
- **V1**: Initial schema (users, sessions, messages)
- **V2**: Summary columns and indexes
- **V3**: Message partitioning by month
- **V4**: Token tracking and usage metrics
- **V5**: Session snapshots for restart capability

## Memory Compression Strategy

### Rolling Compression
Triggers every N messages (configurable):
1. Fetch recent messages since last summary
2. Generate incremental summary using Gemini
3. Update session rolling summary
4. Create embeddings for vector search

### Lifecycle Finalization
On session end/timeout:
1. Generate comprehensive final summary
2. Extract user preferences and patterns
3. Create session snapshot for restart
4. Aggregate to user-level preferences

### Session Restart
From snapshot:
1. Load session context and summary
2. Resume with full conversation awareness
3. Continue rolling compression

## Testing

Run all tests:
```bash
./mvnw test
```

Run specific test:
```bash
./mvnw test -Dtest=SessionServiceTest
```

## Monitoring

Health check endpoint:
```
GET /actuator/health
```

Metrics:
```
GET /actuator/metrics
```

Prometheus metrics:
```
GET /actuator/prometheus
```

## Performance Considerations

### Database Optimization
- Message partitioning by month (automatic)
- Indexed session queries
- Connection pooling configured

### Caching Strategy
- Session metadata cached (Redis)
- Rate limit buckets cached
- Eviction on session end

### Vector Search
- Batch embedding generation
- Qdrant collection optimized for chat
- Similarity threshold: 0.7

## Security

### Authentication
- JWT with RS256 signing
- API key for service-to-service
- Refresh token rotation

### Rate Limiting
- Per-user limits
- Global endpoint limits
- Redis-backed distributed

### Data Protection
- Password hashing (BCrypt)
- Sensitive data encryption at rest
- SQL injection prevention (JPA)

## Deployment

### Docker Production
```bash
docker-compose -f docker-compose.prod.yml up -d
```

### Environment Variables
Set all variables from `.env.example` in production.

### Health Checks
Application includes:
- Database connectivity
- Redis connectivity
- Qdrant connectivity
- Gemini API availability

## Troubleshooting

### Database Connection Issues
```bash
docker-compose logs postgres
# Check connection string in application.yml
```

### Qdrant Not Responding
```bash
docker-compose restart qdrant
# Check http://localhost:6333/dashboard
```

### Rate Limiting Too Aggressive
Adjust in `application.yml`:
```yaml
ragchat:
  rate-limit:
    capacity: 200  # Increase capacity
```

## Development

### Code Structure
- **Entities**: JPA entities with Lombok
- **Repositories**: Spring Data JPA
- **Services**: Business logic layer
- **Controllers**: REST endpoints
- **Events**: Application events for async processing
- **Listeners**: Event handlers

### Adding New Features
1. Create entity if needed
2. Add repository
3. Implement service
4. Create controller
5. Add tests
6. Update OpenAPI docs

## License

Proprietary - Axiom Systems

## Support

For issues and questions:
- Email: support@axiom-systems.com
- Issues: Create issue in repository
- Docs: Check /docs folder

## Roadmap

- [ ] Multi-tenant support
- [ ] WebSocket real-time updates
- [ ] Advanced analytics dashboard
- [ ] Custom AI model integration
- [ ] GraphQL API
- [ ] Kubernetes deployment configs