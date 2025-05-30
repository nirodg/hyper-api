# 🚀 HyperAPI  
**A Quarkus extension for instant, zero-boilerplate, secured CRUD APIs**

HyperAPI scans your JPA entities, finds those annotated with `@ExposeAPI`, and – at **runtime** – wires complete REST endpoints, DTO mapping, and role-based access control. No controllers, no services, no MapStruct classes to write by hand.

---

## ✨ Features (implemented today)

| Capability | What it does |
|------------|--------------|
| **Automatic entity discovery** | Scans packages set in `hyperapi.scan-packages` and detects every `@Entity @ExposeAPI` class. |
| **Generic CRUD endpoints** | Exposes `GET / POST / PUT / DELETE` at `/api/{Entity}` or your custom path. |
| **Runtime DTO mapping** | Converts entity ⇄ `Map<String,Object>`; unwraps Hibernate/CDI proxies; hides fields flagged in `@ExposeAPI(mapping.ignore)`. |
| **Annotation-driven security** | `@ExposeAPI.security(requireAuth, rolesAllowed)` → returns `401/403` via a name-bound JAX-RS filter. |
| **Standard JSON error payload** | Uniform `ApiError` body with timestamp, HTTP status, message, path, and `WWW-Authenticate` header on 401. |
| **Quarkus-native extension packaging** | Published as `quarkus-hyperapi-extension` (runtime) + `quarkus-hyperapi-deployment` (build-time). |

---

## 🛠 Quick start

### 1. Add the dependency

```xml
<dependency>
  <groupId>dev.hyperapi</groupId>
  <artifactId>quarkus-hyperapi-extension</artifactId>
  <version>0.1.0-SNAPSHOT</version>
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

import dev.hyperapi.runtime.annotations.ExposeAPI;
import static dev.hyperapi.runtime.annotations.ExposeAPI.*;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@ExposeAPI(
  path = "/products",                       // customise base path
  mapping = @Mapping(ignore = {"version"}), // hide optimistic-lock column
  security = @Security(
      requireAuth  = true,                  // caller must be authenticated
      rolesAllowed = {"PRODUCT_WRITE"}      // and hold one of these roles
  )
)
public class Product {

  @Id @GeneratedValue
  Long id;

  String name;
  BigDecimal price;

  @Version
  Integer version;

  Instant createdAt = Instant.now();
  // getters/setters …
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
## 🧩 @ExposeAPI — full reference
```
@ExposeAPI(
    /** 1️⃣  URL base path (default: entity simple name) */
    path = "/orders",

    /** 2️⃣  Field filtering */
    mapping = @Mapping(ignore = {"secret", "version"}),

    /** 3️⃣  Pagination defaults (🔜 feature) */
    pageable = @Pageable(limit = 50, maxLimit = 200),

    /** 4️⃣  Security */
    security = @Security(
        requireAuth  = true,                 // false ⇒ anonymous allowed
        rolesAllowed = { "ADMIN", "MANAGER" }
    ),

    /** 5️⃣  CDI lifecycle events (🔜) */
    events = @Events(onCreate = true, onUpdate = true, onDelete = false),

    /** 6️⃣  Response-level cache (🔜) */
    cache = @Cache(enabled = true, ttlSeconds = 300)
)
class Order { … }
```
Section	Purpose	Implemented
path	Override REST base path	✅
mapping.ignore	Hide sensitive or technical fields	✅
pageable	Default / max page sizes	🔜
security.requireAuth	Force authentication (401)	✅
security.rolesAllowed	Allowed roles (403)	✅
events	Fire CDI events on CRUD	🔜
cache	Per-entity GET cache	🔜

## 🛣 Roadmap
- ✅ Field-ignore mapping
- ✅ Annotation-first security
- 🔜 Pagination & sorting
- 🔜 PATCH (partial updates)
- 🔜 CDI events on create/update/delete
- 🔜 In-memory caching
- 🔜 Auto-generated OpenAPI docs

Contributions and feedback welcome—let’s make HyperAPI even more awesome!

## 📄 License
HyperAPI is licensed under the Apache 2.0 License. See the LICENSE file for details.
