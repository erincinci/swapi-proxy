# SWAPI Proxy API

- [SWAPI Docs](https://swapi.dev/documentation)

> TODO

## Design Choices

- Java - TODO
- Spring Boot - TODO
- Retrofit HTTP Client - TODO
- Using Spring Cache Manager with Caffeine cache, as it's a near-optimal caching lib ([reference](https://github.com/ben-manes/caffeine))
- Bucket4j for rate limiting our API - using Caffeine in-memory cache for buckets, and rate limiting per remote address
- Decided to soft-fail in case of a rate-limit header error, so that we still return a valid response to user