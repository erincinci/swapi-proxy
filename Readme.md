# SWAPI Proxy API

- [SWAPI Docs](https://swapi.dev/documentation)
- [Swagger UI for auto-generated docs](http://localhost:8080/swagger-ui/index.html)
- [JSON Generated API Docs](http://localhost:8080/api-docs)

## Test & Run
```shell
> ./gradlew test
> ./gradlew bootRun
```

## Design Choices

- `Java` - Familiar with the language
- `Spring Boot` - Easy to boilerplate production-ready APIs
- `Retrofit` HTTP Client - Efficient & clean client, backed with OkHTTP (using together with `RetroMock`)
- Using `Spring Cache` Manager with `Caffeine` cache, as it's a near-optimal caching lib ([reference](https://github.com/ben-manes/caffeine))
- `Bucket4j` for rate limiting our API - using `Caffeine` in-memory cache for buckets, and rate limiting per remote address
- `Swagger` OpenAPI docs used for automatic API documentation generation (can be found in [/api-docs](http://localhost:8080/swagger-ui/index.html))
- Decided not to use `BlockingBucket` for async entity enrichment tasks, for better performance. Please note that this may result in some incorrect usage of rate limits in corner cases
- `Spring Async` Task Executor is used for parallel SWAPI enrichment tasks

### Rate Limits

Considered following options & implemented last option:

- **Spring-Starter-Bucket4j**: Very easy to integrate and only requires yaml config, but unfortunately doesn't support dynamic rate consuming, which is required in our case.
- **Custom Bucket management through ConcurrentHashMap**: Fairly easy as well, since only one service would be required, but doesn't support switching to another cache source easily.
- **Custom RateLimit Service using Spring Cache**: Decided on this approach, as it gives flexibility on token consumption, and also supports easy switching to another cache source (ex. Redis) when needed through Spring configuration.
- By default, single endpoint with no enrichment uses 1 rate limit from bucket, whereas multi endpoint tries to consume rate limit from bucket after parsing the request.
- Instead of using a Spring Interceptor approach for rate limit checking & header setting, went with a custom in-controller approach since endpoints requires custom rate-limit calculations.

## Notes:

- On 21st of April, SWAPI Let's Encrypt certificate got expired, still waiting for it to be renewed so that Java PKIX doesn't give out error. (issue fixed around May 1st)
- For entity enrichment functionality, only 1 level of enriching operation is supported in nested entities in order to avoid any stack overflow issues.
- Even though in-memory cache is being used for efficiency, cache hits are not excluded from rate limits by design simplicity
- Some of the numeric fields text fields in SWAPI are also left as strings in our API, due to the incomplete documentation and non-standard examples seen.
- Defining a map `ApiResponse` description in Swagger is quite tedious, so bypassing for the sake of complexity in this case.