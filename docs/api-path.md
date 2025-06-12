# API Path Configuration 🛣️

## `@RestService.path` Attribute

### Default Behavior
```java
@Entity
@RestService // No path specified
public class Product {
    //...
}
```
➡️ Resulting API Path: /Product

> ⚠️ It takes the class name as the endpoint

### Custom Path
```java
@Entity
@RestService(path = "/inventory/items")
public class Product {
    //...
}
```
➡️ Resulting API Path: /inventory/items

