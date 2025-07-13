# API Path Configuration 🛣️

## `@path` Attribute

### Default Behavior
```java
@Entity
@HyperResource // No path specified
public class Product {
    //...
}
```
➡️ Resulting API Path: /Product

> ⚠️ It takes the class name as the endpoint

### Custom Path
```java
@Entity
@HyperResource(path = "/inventory/items")
public class Product {
    //...
}
```
➡️ Resulting API Path: /inventory/items

