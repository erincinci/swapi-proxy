# SWAPI Proxy API

- [SWAPI Docs](https://swapi.dev/documentation)
- [Swagger UI for auto-generated docs](http://localhost:8080/swagger-ui/index.html)
- [JSON Generated API Docs](http://localhost:8080/api-docs)

## Design Choices

- Java - TODO
- Spring Boot - TODO
- Retrofit HTTP Client - TODO
- Using Spring Cache Manager with Caffeine cache, as it's a near-optimal caching lib ([reference](https://github.com/ben-manes/caffeine))
- Bucket4j for rate limiting our API - using Caffeine in-memory cache for buckets, and rate limiting per remote address
- Swagger OpenAPI docs used for automatic API documentation generation (can be found in [/api-docs](http://localhost:8080/swagger-ui/index.html))

### Rate Limits

Considered following options:

- **Spring-Starter-Bucket4j**: Very easy to integrate and only requires yaml config, but unfortunately doesn't support dynamic rate consuming, which is required in our case.
- **Custom Bucket management through ConcurrentHashMap**: Fairly easy as well, since only one service would be required, but doesn't support switching to another cache source easily.
- **Custom RateLimit Service using Spring Cache**: Decided on this approach, as it gives flexibility on token consumption, and also supports easy switching to another cache source (ex. Redis) when needed through Spring configuration.

### Notes:

- On 21st of April, SWAPI Let's Encrypt certificate got expired, still waiting for it to be renewed so that Java PKIX doesn't give out error.