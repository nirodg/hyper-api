# DTO Field Mapping Configuration 🗺️

## `@RestService.Mapping` Attribute

### Define fields to be ignored
```java
@RestService(
    mapping = @Mapping(
        ignore = {"internalId"},
        ignoreNested = {"orders.checkout"} // Deep path exclusion
    )
)
public class Product {
    //...
}
```

### Generated output

```java
public abstract class ProductMapper extends AbstractMapper<ProductDTO, Product> {
    @Mapping(
            target = "internalId",
            ignore = true
    )
    @Mapping(
            target = "orders.checkout.orders",
            ignore = true
    )
    public abstract Product toEntity(ProductDTO dto);
    
    @Mapping(
            target = "internalId",
            ignore = true
    )
    @Mapping(
            target = "orders.checkout.orders",
            ignore = true
    )
    public abstract ProductDTO toDto(Product entity);
}
```