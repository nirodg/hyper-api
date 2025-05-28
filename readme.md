# 🚀 HyperAPI — Quarkus Extension for Zero-Boilerplate CRUD

HyperAPI is a plug-and-play Quarkus extension that turns any JPA entity into a fully-featured, secure, type-safe REST API **at runtime**. Define your domain as plain JPA classes—annotate with `@ExposeAPI`—and HyperAPI does the rest.

---

## 🔥 Current Features

- **Runtime classpath scanning**  
  Automatically discovers all `@Entity @ExposeAPI` classes in your app (configurable via `hyperapi.scan-packages`).

- **Generic CRUD endpoints**  
  One controller + one service handles **GET**, **POST**, **PUT**, **DELETE** for every exposed entity under `/api/{entity}`.

- **Reflection-based DTO mapping**  
  Converts entity ↔ `Map<String,Object>` on the fly, unwrapping Hibernate/CDI proxies and hiding internal fields.

- **Fine-grained field filtering**  
  `@ExposeAPI(mapping = @Mapping(ignore = { "version", "secret" }))` hides unwanted properties from your API.

- **Built-in security filter**  
  `@ExposeAPI(security = @Security(requireAuth = true, rolesAllowed = { "ADMIN" }))`  
  – 401 for unauthenticated requests  
  – 403 for unauthorized roles  
  – anonymous by default if neither flag nor roles are set

- **Standardized error responses**  
  All auth failures return a JSON `ApiError` payload with timestamp, status, message and `WWW-Authenticate` header on 401.

- **Quarkus extension packaging**  
  Delivered as two Maven modules:  
  - **`quarkus-hyperapi-extension`** (runtime beans, annotations, mapping)  
  - **`quarkus-hyperapi-deployment`** (build-time processor, bean registration, feature declaration)

---

## 🛠️ Getting Started

1. **Add the dependency** to your `pom.xml`:

    ```xml
    <dependency>
      <groupId>dev.hyperapi</groupId>
      <artifactId>quarkus-hyperapi-extension</artifactId>
      <version>0.1.0-SNAPSHOT</version>
    </dependency>
    ```

2. **Annotate your JPA entity**:

    ```java
    @Entity
    @ExposeAPI(
      mapping = @Mapping(ignore = { "version" }),
      security = @Security(requireAuth = true, rolesAllowed = { "USER","ADMIN" })
    )
    public class Product {
        @Id @GeneratedValue
        private Long id;
        private String name;
        private BigDecimal price;
        @Version
        private Integer version;
        // …
    }
    ```

3. **Configure scanning** (if your entities live outside the root package):

    ```properties
    # application.properties
    hyperapi.scan-packages=com.example.domain
    quarkus.log.console.json=false
    ```

4. **Run in dev mode**:

    ```bash
    ./mvnw quarkus:dev
    ```

5. **Try the endpoints**:

    ```bash
    curl -X POST http://localhost:8080/api/Product \
         -H 'Content-Type: application/json' \
         -d '{"name":"Pen","price":2.50}'
    curl http://localhost:8080/api/Product
    ```

---

## 🔧 Configuration  

| Property                  | Default          | Description                                     |
|---------------------------|------------------|-------------------------------------------------|
| `hyperapi.scan-packages`  | _blank_          | Comma-separated package roots to scan           |
| `%dev.quarkus.log.console.json` | `false`   | Disable JSON logs in dev mode                   |

All other behaviors are driven by your `@ExposeAPI` settings.

---

## 📈 What’s Next

- **Pagination & Sorting** via `@ExposeAPI(pageable = @Pageable(limit=50,maxLimit=200))`  
- **Partial Updates** (PATCH support)  
- **CDI Events** on create/update/delete for audit or workflows  
- **In-memory Caching** per-entity with TTL  
- **OpenAPI / Swagger** auto-generation

Contributions and ideas are welcome!

---

## 📄 License

Apache 2.0 &mdash; see [LICENSE](LICENSE) for details.

---

*Built with ❤️ for super-mega-fast, type-safe, secure APIs.*  
