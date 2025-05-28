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
