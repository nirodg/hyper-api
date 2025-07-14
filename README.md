# 🚀 HyperAPI  
**A Quarkus extension for instant, zero-boilerplate, secured CRUD APIs**

HyperAPI scans your JPA entities, finds those annotated with `@HyperResource`, and – during **compilation (soon features at runtime)** – wires complete REST endpoints, DTO mapping, Services, Events and role-based access control. No controllers, no services, no MapStruct classes to write by hand.

## ⚠️ Important Notice

> **Development Preview**  
> HyperAPI is currently in **active development** and not yet ready for production use.  
> Core features are functional, but APIs may change until v1.0 release.
>
> ✅ **Recommended for**: Evaluation, prototyping, and feedback gathering  
> ❌ **Not recommended for**: Production workloads without thorough testing

**❗ Important Legal Notice**  
> This software is provided **"as is"** without warranties of any kind.  
> By using HyperAPI, you agree that:
>
> ✗ The maintainers **accept no liability** for any damages  
> ✗ You assume **all risks** associated with its use  
> ✗ No guarantee is provided for **security or reliability**
>
> _Always evaluate thoroughly before production use._

---

## ✨ Features (implemented today)

| Capability                                       | What it does                                                                                              |
|--------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| **Generic CRUD endpoints**                       | Exposes `GET / POST / PUT / DELETE` at `/api/{Entity}` or your custom path.                               |
 | **Generate DTO**                                 | Automatically fetches the JPA's fields and generated a DTO version of if, it's constumizable.             |           |
| **Generate at compile DTO mapping**              | Automatically generated a Mapper and on top of it MapStruct generates the implementation                  |
| **Annotation-driven security** (not implemented) | `@HyperResource.security(requireAuth, rolesAllowed)` → returns `401/403` via a name-bound JAX-RS filter.      |
| **Standard JSON error payload**                  | Uniform `ApiError` body with timestamp, HTTP status, message, path, and `WWW-Authenticate` header on 401. |
| **Quarkus-native extension packaging**           | Published as `quarkus-hyperapi-extension` (runtime) + `quarkus-hyperapi-deployment` (build-time).         |
 | **Event mechanism**                              | Offers customizable Event Pattern for events on create/update/delete                                      |                                     |                                                                        |
---

## 🛠 Quick start

### 1. Add the dependency

```xml
<dependency>
  <groupId>com.eorghe</groupId>
  <artifactId>quarkus-hyperapi</artifactId>
  <version>0.1.0</version>
</dependency>
```

### 2. Configure scanning (if your entities live outside the root package)
```
# application.properties
hyperapi.scan-packages=com.example.domain
quarkus.log.console.json=false   # pretty console logs in dev
```
### 3. Annotate a JPA entity
```java
package com.example.domain;

///  imports

@Getter
@Setter
@Entity
@HyperResource(
  path = "/api/products",                       // customise base path
  mapping = @Mapping(ignore = {"version"}), // hide optimistic-lock column
  security = @Security(
      requireAuth  = true,                  // caller must be authenticated
      rolesAllowed = {"PRODUCT_WRITE"}      // and hold one of these roles
  )
)
public class Product extends HyperEntity{
  
  String name;
  BigDecimal price;

  @Version
  Integer version;
}
```
### 4. Run dev-mode
```./mvnw quarkus:dev```
Console shows:
```
Installed features: …, hyperapi, …
HyperAPI extension started successfully!
Detected API Entity: Product
```
### 5. Call the API

```
# create
curl -X POST http://localhost:8080/api/products \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer <jwt-with-PRODUCT_WRITE>" \
     -d '{"name":"Pen","price":2.50}'

# list
curl -H "Authorization: Bearer <jwt>" \
     http://localhost:8080/api/products
```
## 🧩 @HyperResource — Annotation Reference
> For full reference please see the [@HyperResource](https://github.com/nirodg/hyper-api/blob/dev/quarkus-hyperapi/src/main/java/dev/hyperapi/runtime/core/processor/annotations/HyperResource.java)

```java
@HyperResource(
    /** 1️⃣ Base API path (default: entity name) */
    path = "/orders",
    
    /** 2️⃣ Custom DTO class (default: auto-generated) */
    dto = "com.example.CustomOrderDTO",
    
    /** 3️⃣ CDI scope for generated service */
    scope = Scope.REQUEST,
    
    /** 4️⃣ Disabled HTTP methods */
    disabledFor = {HttpMethod.PATCH, HttpMethod.DELETE},
    
    /** 5️⃣ Field filtering */
    mapping = @Mapping(
        ignore = {"secret"},       // Top-level fields
        ignoreNested = {"user.password"}  // Nested object fields
    ),
    
    /** 6️⃣ Pagination controls */
    pageable = @Pageable(
        limit = 50,      // Default page size
        maxLimit = 200   // Maximum allowed page size
    ),
    
    /** 7️⃣ Event configuration */
    events = @Events(
        onCreate = true,    // Fire on entity creation
        onUpdate = true,    // Fire on entity update  
        onDelete = false,   // Disable delete events
        emitter = CustomEmitter.class  // Custom event emitter
    ),
    
    /** 8️⃣ Caching behavior  | NOT YET IMPLEMENTED */
    cache = @Cache(
        enabled = true,     // Enable response caching
        ttlSeconds = 300    // 5-minute cache duration
    ),
    
    /** 9️⃣ Security controls | NOT YET IMPLEMENTED */
    security = @Security(
        requireAuth = true,            // Authentication required
        rolesAllowed = {"ORDER_ADMIN"}  // Required roles
    )
)
```

## 📚 Documentation

Explore implementation guides for key HyperAPI features:

### Core
- [Rest API definition](docs/api-path.md) - Generatin the CRUD endpoints, automatically!
- [DTO Generation](docs/custom-dto.md) - Generating DTOs from JPAs, automatically! 
- [DTO Mapping](docs/dto-mapping.md) - Ignoring unnecessary fields

### Event System
- [CDI Events Overview](docs/events-cdi.md) - Basic event observation patterns
- [Advanced Event Processing](docs/events-cdi-complex.md) - Multi-stage event pipelines
- [Kafka Integration](docs/events-kafka-producer.md) - Streaming events to Kafka
- [MQTT Integration](docs/events-mqtt-producer.md) - IoT/edge computing scenarios
- [API Gateway Patterns](docs/events-cdi-external-apis.md) - Sync/async external API calls




## 🛣 Roadmap
- ✅ Generate mappers
- ✅ Field-ignore mapping
- ✅ Generate Services
- ✅ Generate Rest Endpoints (CRUD)
- 🔜 Annotation-first security
- ✅ Pagination & sorting
- ✅ PATCH (partial updates)
- ✅ CDI events on create/update/delete
- 🔜 In-memory caching
- 🔜 Auto-generated OpenAPI docs

Contributions and feedback welcome — let’s make HyperAPI even more awesome!

## 📄 License
HyperAPI is licensed under the Apache 2.0 License. See the LICENSE file for details.
