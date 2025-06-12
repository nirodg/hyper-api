# Generate DTO and Configuration ✨

## `@RestService.dto` Attribute

## Key Behaviors
### 1. Automatic Suffixing
```java
@RestService() // Becomes "ProductDTO"
public class Product{
    //...
}
```

### 2. Custom naming
```java
@RestService( dto = "SingleProduct" ) // Becomes "SingleProductDTO"
public class Product{
    //...
}
```

### Default Naming Convention
```java
@RestService // No DTO specified
public class User {
    //...
}
```
➡️ Generated DTO: UserDTO (Entity name + "DTO")

### Custon DTO Name
```java
@RestService(dto = "AccountResponse")
public class User {
    //...
}
```
➡️ Generated DTO: AccountResponseDTO

### Field Inclusion Rule (for JSON)
```java
@Entity
@RestService(dto = "MyProduct")
public class Product {
    String name;          // Included
    BigDecimal price;     // Included
    
    @JsonbTransient
    List<Order> orders;   // Excluded
}
```

### Generated DTO example
```java
// For @RestService( dto = "MyProduct" )
@JsonInclude(Include.NON_NULL)
public class MyProductDTO extends BaseDTO {
    @JsonProperty("name")
    private String name;
    @JsonProperty("price")
    private BigDecimal price;
    @JsonProperty("orders")
    private List<Order> orders = new ArrayList();

    public MyProductDTO() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public List<Order> getOrders() {
        return this.orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public void addOrdersItem(Order item) {
        this.orders.add(item);
    }

    public void clearOrders() {
        this.orders.clear();
    }

    public String toString() {
        return String.format("MyProductDTO [name=%s, price=%s, orders=%s]", this.name, this.price, this.orders);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            MyProductDTO that = (MyProductDTO)o;
            return Objects.equals(this.name, that.name) && Objects.equals(this.price, that.price) && Objects.equals(this.orders, that.orders);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.name, this.price, this.orders});
    }
}
```