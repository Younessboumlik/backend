# SOLID Principles & Design Patterns in MyShop Backend

This document identifies and explains all SOLID principles and design patterns used throughout the MyShop Backend codebase, with exact file locations and code snippets.

---

## Table of Contents

1. [SOLID Principles](#solid-principles)
   - [Single Responsibility Principle (SRP)](#1-single-responsibility-principle-srp)
   - [Open/Closed Principle (OCP)](#2-openclosed-principle-ocp)
   - [Liskov Substitution Principle (LSP)](#3-liskov-substitution-principle-lsp)
   - [Interface Segregation Principle (ISP)](#4-interface-segregation-principle-isp)
   - [Dependency Inversion Principle (DIP)](#5-dependency-inversion-principle-dip)
2. [Design Patterns](#design-patterns)
   - [Repository Pattern](#1-repository-pattern)
   - [Service Layer Pattern](#2-service-layer-pattern)
   - [Data Transfer Object (DTO) Pattern](#3-data-transfer-object-dto-pattern)
   - [Builder Pattern](#4-builder-pattern)
   - [Dependency Injection Pattern](#5-dependency-injection-pattern)
   - [Factory Pattern](#6-factory-pattern)
   - [Strategy Pattern](#7-strategy-pattern)
   - [Singleton Pattern](#8-singleton-pattern)
   - [Template Method Pattern](#9-template-method-pattern)
   - [Mapper Pattern](#10-mapper-pattern)
3. [Summary](#summary)

---

## SOLID Principles

### 1. Single Responsibility Principle (SRP)

> **Definition**: A class should have only one reason to change, meaning it should have only one job or responsibility.

#### Implementation in MyShop

Each class in the project has a single, well-defined responsibility:

#### Example 1: ProductController
**File**: `src/main/java/com/myshop/controller/ProductController.java`

**Responsibility**: Handle HTTP requests related to products

```java
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    @GetMapping
    public ResponseEntity<List<ProductResponse>> searchProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Sort sort = Sort.unsorted();
        if (sortBy != null) {
            Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection)
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
            sort = Sort.by(direction, sortBy);
        }
        
        return ResponseEntity.ok(productService.searchProducts(categoryId, minPrice, maxPrice, search, sort));
    }
    
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }
}
```

**Why SRP?**: The controller only handles HTTP request/response mapping. Business logic is delegated to `ProductService`.

#### Example 2: ProductServiceImpl
**File**: `src/main/java/com/myshop/service/impl/ProductServiceImpl.java`

**Responsibility**: Implement business logic for product operations

```java
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderItemRepository orderItemRepository;
    
    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        validatePriceAndStock(request.getPrice(), request.getStockQuantity());
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        
        Product product = Product.builder()
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .build();
        
        return DtoMapper.toProductResponse(productRepository.save(product));
    }
    
    private void validatePriceAndStock(BigDecimal price, Integer stockQuantity) {
        if (price == null || price.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price must be greater than zero");
        }
        if (stockQuantity == null || stockQuantity < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock quantity must be positive");
        }
    }
}
```

**Why SRP?**: The service class focuses solely on business logic - validation, orchestration, and data transformation.

#### Example 3: DtoMapper
**File**: `src/main/java/com/myshop/mapper/DtoMapper.java`

**Responsibility**: Convert entities to DTOs

```java
public final class DtoMapper {
    
    private DtoMapper() {
    }
    
    public static ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .category(toCategoryResponse(product.getCategory()))
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
```

**Why SRP?**: The mapper class has a single responsibility - data transformation between domain entities and DTOs.

---

### 2. Open/Closed Principle (OCP)

> **Definition**: Software entities should be open for extension but closed for modification.

#### Implementation in MyShop

#### Example 1: Service Interface Extension
**File**: `src/main/java/com/myshop/service/ProductService.java`

```java
public interface ProductService {
    List<ProductResponse> searchProducts(Long categoryId,
                                         BigDecimal minPrice,
                                         BigDecimal maxPrice,
                                         String search,
                                         Sort sort);
    
    ProductResponse getProduct(Long id);
    ProductResponse createProduct(CreateProductRequest request);
    ProductResponse updateProduct(Long id, UpdateProductRequest request);
    void deleteProduct(Long id);
}
```

**Implementation**: `src/main/java/com/myshop/service/impl/ProductServiceImpl.java`

```java
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    // Implementation details...
}
```

**Why OCP?**: New implementations can be added without modifying the interface. You can create `ProductServiceImplV2` or `EnhancedProductServiceImpl` without changing existing code.

#### Example 2: Repository Extension
**File**: `src/main/java/com/myshop/repository/ProductRepository.java`

```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    @Query("""
            SELECT p FROM Product p
            WHERE (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
              AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    List<Product> searchProducts(@Param("categoryId") Long categoryId,
                                 @Param("minPrice") BigDecimal minPrice,
                                 @Param("maxPrice") BigDecimal maxPrice,
                                 @Param("search") String search);
    
    boolean existsByCategoryId(Long categoryId);
}
```

**Why OCP?**: The repository extends `JpaRepository` and adds custom methods without modifying the base interface.

#### Example 3: Enum-Based Strategy
**File**: `src/main/java/com/myshop/domain/enums/OrderPaymentMethod.java`

```java
public enum OrderPaymentMethod {
    CREDIT_CARD,
    CASH_ON_DELIVERY,
    BANK_TRANSFER,
    PAYPAL
}
```

**Usage in**: `src/main/java/com/myshop/service/impl/OrderServiceImpl.java`

```java
if (request.getPaymentMethod() == OrderPaymentMethod.ONLINE_PAYMENT) {
    PaymentGateway gateway = request.getPaymentGateway();
    if (gateway == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment gateway is required for online payments");
    }
    Payment payment = Payment.builder()
            .order(savedOrder)
            .paymentMethod(gateway)
            .paymentStatus(PaymentStatus.PENDING)
            .amount(total)
            .build();
    paymentRepository.save(payment);
}
```

**Why OCP?**: New payment methods can be added to the enum without modifying the order processing logic.

---

### 3. Liskov Substitution Principle (LSP)

> **Definition**: Objects of a superclass should be replaceable with objects of its subclasses without breaking the application.

#### Implementation in MyShop

#### Example 1: Service Implementation Substitution
**Interface**: `src/main/java/com/myshop/service/OrderService.java`

```java
public interface OrderService {
    OrderResponse checkout(CheckoutRequest request);
    OrderResponse getOrder(Long id);
    List<OrderResponse> getOrdersForUser(Long userId);
    List<OrderResponse> getAllOrders();
    OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request);
}
```

**Implementation**: `src/main/java/com/myshop/service/impl/OrderServiceImpl.java`

```java
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    // All methods properly implement the interface contract
    
    @Override
    @Transactional
    public OrderResponse checkout(CheckoutRequest request) {
        // Implementation maintains the contract
    }
}
```

**Usage in Controller**: `src/main/java/com/myshop/controller/OrderController.java`

```java
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService; // Interface reference, not implementation
    
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(orderService.checkout(request));
    }
}
```

**Why LSP?**: The controller depends on the `OrderService` interface, not the implementation. Any implementation of `OrderService` can be substituted without breaking the controller.

#### Example 2: JpaRepository Substitution
**File**: `src/main/java/com/myshop/repository/UserRepository.java`

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

**Why LSP?**: Spring Data JPA can substitute different implementations (e.g., `SimpleJpaRepository`) without affecting the service layer code.

---

### 4. Interface Segregation Principle (ISP)

> **Definition**: Clients should not be forced to depend on interfaces they do not use.

#### Implementation in MyShop

#### Example 1: Focused Service Interfaces
**File**: `src/main/java/com/myshop/service/ProductService.java`

```java
public interface ProductService {
    List<ProductResponse> searchProducts(Long categoryId,
                                         BigDecimal minPrice,
                                         BigDecimal maxPrice,
                                         String search,
                                         Sort sort);
    
    ProductResponse getProduct(Long id);
    ProductResponse createProduct(CreateProductRequest request);
    ProductResponse updateProduct(Long id, UpdateProductRequest request);
    void deleteProduct(Long id);
}
```

**File**: `src/main/java/com/myshop/service/CartService.java`

```java
public interface CartService {
    List<CartItemResponse> getCartItems(Long userId);
    CartItemResponse addToCart(Long userId, CartItemRequest request);
    CartItemResponse updateCartItem(Long userId, Long productId, Integer quantity);
    void removeFromCart(Long userId, Long productId);
    void clearCart(Long userId);
}
```

**Why ISP?**: Each service interface is small and focused. Controllers only depend on the specific service they need, not a monolithic interface with all operations.

#### Example 2: Repository Segregation
**File**: `src/main/java/com/myshop/repository/CategoryRepository.java`

```java
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameIgnoreCase(String name);
}
```

**File**: `src/main/java/com/myshop/repository/ReviewRepository.java`

```java
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductId(Long productId);
}
```

**Why ISP?**: Each repository interface extends only `JpaRepository` and adds only the methods relevant to that entity. Services don't get methods they don't need.

---

### 5. Dependency Inversion Principle (DIP)

> **Definition**: High-level modules should not depend on low-level modules. Both should depend on abstractions.

#### Implementation in MyShop

#### Example 1: Service Layer Depends on Repository Abstraction
**File**: `src/main/java/com/myshop/service/impl/ProductServiceImpl.java`

```java
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    
    private final ProductRepository productRepository;        // Abstraction (interface)
    private final CategoryRepository categoryRepository;      // Abstraction (interface)
    private final OrderItemRepository orderItemRepository;    // Abstraction (interface)
    
    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        if (orderItemRepository.existsByProductId(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete product linked to an order");
        }
        productRepository.delete(product);
    }
}
```

**Why DIP?**: `ProductServiceImpl` depends on repository interfaces, not concrete implementations. The actual implementation is injected by Spring.

#### Example 2: Controller Depends on Service Abstraction
**File**: `src/main/java/com/myshop/controller/ProductController.java`

```java
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;  // Abstraction (interface), not ProductServiceImpl
    
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }
}
```

**Why DIP?**: The controller depends on the `ProductService` interface, not the concrete implementation. This allows for easy testing and swapping implementations.

#### Example 3: Security Configuration with Abstractions
**File**: `src/main/java/com/myshop/config/SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // Returns PasswordEncoder interface
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;  // Returns CorsConfigurationSource interface
    }
}
```

**Usage in Service**: `src/main/java/com/myshop/service/impl/UserServiceImpl.java`

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;  // Depends on abstraction
    
    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }
        
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))  // Uses interface
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .role(request.getRole())
                .build();
        
        return DtoMapper.toUserResponse(userRepository.save(user));
    }
}
```

**Why DIP?**: Services depend on `PasswordEncoder` interface, not `BCryptPasswordEncoder` directly. This allows changing the encoding algorithm without modifying services.

---

## Design Patterns

### 1. Repository Pattern

> **Definition**: Mediates between the domain and data mapping layers, acting like an in-memory collection of domain objects.

#### Implementation in MyShop

**Files**: All interfaces in `src/main/java/com/myshop/repository/`

#### Example: ProductRepository
**File**: `src/main/java/com/myshop/repository/ProductRepository.java`

```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    @Query("""
            SELECT p FROM Product p
            WHERE (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
              AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    List<Product> searchProducts(@Param("categoryId") Long categoryId,
                                 @Param("minPrice") BigDecimal minPrice,
                                 @Param("maxPrice") BigDecimal maxPrice,
                                 @Param("search") String search);
    
    boolean existsByCategoryId(Long categoryId);
}
```

**Usage**:
```java
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    
    @Override
    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        return DtoMapper.toProductResponse(product);
    }
}
```

**Benefits**:
- Abstracts data access logic
- Provides collection-like interface for domain objects
- Enables easy testing with mock repositories
- Decouples business logic from persistence mechanism

---

### 2. Service Layer Pattern

> **Definition**: Defines application's boundary with a layer of services that establishes a set of available operations and coordinates the application's response in each operation.

#### Implementation in MyShop

**Files**: All interfaces in `src/main/java/com/myshop/service/` and implementations in `src/main/java/com/myshop/service/impl/`

#### Example: OrderService
**Interface**: `src/main/java/com/myshop/service/OrderService.java`

```java
public interface OrderService {
    OrderResponse checkout(CheckoutRequest request);
    OrderResponse getOrder(Long id);
    List<OrderResponse> getOrdersForUser(Long userId);
    List<OrderResponse> getAllOrders();
    OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request);
}
```

**Implementation**: `src/main/java/com/myshop/service/impl/OrderServiceImpl.java`

```java
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    
    @Override
    @Transactional
    public OrderResponse checkout(CheckoutRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }
        
        Order order = Order.builder()
                .user(user)
                .orderStatus(OrderStatus.PROCESSING)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .shippingName(request.getShippingName())
                .shippingAddress(request.getShippingAddress())
                .shippingPhone(request.getShippingPhone())
                .shippingEmail(request.getShippingEmail())
                .build();
        
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        
        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found during checkout"));
            int requestedQty = cartItem.getQuantity();
            if (requestedQty > product.getStockQuantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough stock for product: " + product.getName());
            }
            product.setStockQuantity(product.getStockQuantity() - requestedQty);
            productRepository.save(product);
            
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(requestedQty)
                    .unitPrice(product.getPrice())
                    .build();
            orderItems.add(orderItem);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(requestedQty)));
        }
        
        order.setTotalAmount(total);
        order.setOrderItems(orderItems);
        Order savedOrder = orderRepository.save(order);
        
        cartItems.forEach(cartItemRepository::delete);
        
        return DtoMapper.toOrderResponse(savedOrder, orderItems);
    }
}
```

**Benefits**:
- Encapsulates business logic
- Provides transaction boundaries with `@Transactional`
- Coordinates multiple repositories
- Handles complex business operations

---

### 3. Data Transfer Object (DTO) Pattern

> **Definition**: Objects that carry data between processes to reduce the number of method calls.

#### Implementation in MyShop

**Files**: 
- Request DTOs: `src/main/java/com/myshop/dto/request/`
- Response DTOs: `src/main/java/com/myshop/dto/response/`

#### Example 1: Request DTO
**File**: `src/main/java/com/myshop/dto/request/CreateProductRequest.java`

```java
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {
    
    @NotNull(message = "Category ID is required")
    private Long categoryId;
    
    @NotBlank(message = "Product name is required")
    @Size(max = 150, message = "Product name must not exceed 150 characters")
    private String name;
    
    private String description;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private BigDecimal price;
    
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity must be positive")
    private Integer stockQuantity;
    
    private String imageUrl;
}
```

#### Example 2: Response DTO
**File**: `src/main/java/com/myshop/dto/response/ProductResponse.java`

```java
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private CategoryResponse category;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private Instant createdAt;
    private Instant updatedAt;
}
```

**Usage in Controller**:
```java
@PostMapping
public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
}
```

**Benefits**:
- Decouples API from domain model
- Enables validation at API boundary
- Prevents exposing internal entity structure
- Reduces data transferred over network

---

### 4. Builder Pattern

> **Definition**: Separates the construction of a complex object from its representation, allowing the same construction process to create different representations.

#### Implementation in MyShop

**Files**: All entities in `src/main/java/com/myshop/domain/entity/` use Lombok's `@Builder`

#### Example: Product Entity
**File**: `src/main/java/com/myshop/domain/entity/Product.java`

```java
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @Column(name = "product_name", nullable = false, length = 150)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;
    
    @Column(name = "image_url", length = 255)
    private String imageUrl;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();
}
```

**Usage**:
```java
Product product = Product.builder()
        .category(category)
        .name(request.getName())
        .description(request.getDescription())
        .price(request.getPrice())
        .stockQuantity(request.getStockQuantity())
        .imageUrl(request.getImageUrl())
        .build();
```

**Benefits**:
- Provides fluent API for object creation
- Makes code more readable
- Handles complex object construction
- Allows optional parameters

---

### 5. Dependency Injection Pattern

> **Definition**: A technique where an object receives other objects that it depends on, rather than creating them itself.

#### Implementation in MyShop

**Framework**: Spring Framework's IoC container

#### Example: Constructor Injection with Lombok
**File**: `src/main/java/com/myshop/service/impl/ProductServiceImpl.java`

```java
@Service
@RequiredArgsConstructor  // Lombok generates constructor for final fields
public class ProductServiceImpl implements ProductService {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderItemRepository orderItemRepository;
    
    // Spring automatically injects dependencies through generated constructor
}
```

**Generated Constructor (by Lombok)**:
```java
public ProductServiceImpl(ProductRepository productRepository,
                         CategoryRepository categoryRepository,
                         OrderItemRepository orderItemRepository) {
    this.productRepository = productRepository;
    this.categoryRepository = categoryRepository;
    this.orderItemRepository = orderItemRepository;
}
```

#### Example: Bean Configuration
**File**: `src/main/java/com/myshop/config/SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**Benefits**:
- Promotes loose coupling
- Enables easy testing with mocks
- Managed by Spring container
- Supports different injection scopes

---

### 6. Factory Pattern

> **Definition**: Provides an interface for creating objects in a superclass, but allows subclasses to alter the type of objects that will be created.

#### Implementation in MyShop

#### Example 1: Spring Bean Factory
**File**: `src/main/java/com/myshop/config/SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // Factory method
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;  // Factory method
    }
}
```

#### Example 2: Entity Creation via Builder (Factory-like)
**File**: `src/main/java/com/myshop/config/DataInitializer.java`

```java
@Configuration
public class DataInitializer {
    
    @Bean
    public CommandLineRunner seedData(UserRepository users, 
                                     CategoryRepository categories, 
                                     ProductRepository products, 
                                     PasswordEncoder encoder) {
        return args -> {
            if (!users.existsByEmail("client@myshop.test")) {
                User user = User.builder()  // Builder acts as a factory
                        .fullName("Client Test")
                        .email("client@myshop.test")
                        .passwordHash(encoder.encode("password"))
                        .phoneNumber("0600000000")
                        .address("Adresse de test")
                        .role(UserRole.CLIENT)
                        .build();
                users.save(user);
            }
            
            Category cat;
            if (!categories.existsByNameIgnoreCase("Electronique")) {
                cat = Category.builder()  // Builder acts as a factory
                        .name("Electronique")
                        .description("Appareils et accessoires")
                        .build();
                cat = categories.save(cat);
            }
        };
    }
}
```

**Benefits**:
- Centralizes object creation
- Encapsulates complex construction logic
- Allows configuration-based object creation
- Promotes consistency

---

### 7. Strategy Pattern

> **Definition**: Defines a family of algorithms, encapsulates each one, and makes them interchangeable.

#### Implementation in MyShop

#### Example: Payment Method Strategy
**File**: `src/main/java/com/myshop/domain/enums/OrderPaymentMethod.java`

```java
public enum OrderPaymentMethod {
    CREDIT_CARD,
    CASH_ON_DELIVERY,
    BANK_TRANSFER,
    PAYPAL,
    ONLINE_PAYMENT
}
```

**File**: `src/main/java/com/myshop/domain/enums/PaymentGateway.java`

```java
public enum PaymentGateway {
    STRIPE,
    PAYPAL,
    BANK_TRANSFER,
    CASH_ON_DELIVERY
}
```

**Usage**: `src/main/java/com/myshop/service/impl/OrderServiceImpl.java`

```java
@Override
@Transactional
public OrderResponse checkout(CheckoutRequest request) {
    // ... order creation logic ...
    
    // Strategy: Different behavior based on payment method
    if (request.getPaymentMethod() == OrderPaymentMethod.ONLINE_PAYMENT) {
        PaymentGateway gateway = request.getPaymentGateway();
        if (gateway == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Payment gateway is required for online payments");
        }
        Payment payment = Payment.builder()
                .order(savedOrder)
                .paymentMethod(gateway)
                .paymentStatus(PaymentStatus.PENDING)
                .amount(total)
                .build();
        paymentRepository.save(payment);
        savedOrder.setPayment(payment);
    }
    
    return DtoMapper.toOrderResponse(savedOrder, orderItems);
}
```

#### Example 2: Sorting Strategy
**File**: `src/main/java/com/myshop/service/impl/ProductServiceImpl.java`

```java
private Comparator<Product> comparatorFor(String property) {
    if (property == null) {
        return null;
    }
    return switch (property) {
        case "price" -> Comparator.comparing(Product::getPrice);
        case "createdAt" -> Comparator.comparing(Product::getCreatedAt);
        case "name" -> Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER);
        default -> null;
    };
}

@Override
@Transactional(readOnly = true)
public List<ProductResponse> searchProducts(Long categoryId,
                                           BigDecimal minPrice,
                                           BigDecimal maxPrice,
                                           String search,
                                           Sort sort) {
    List<Product> products = productRepository.searchProducts(categoryId, minPrice, maxPrice,
            search != null ? search.trim() : null);
    
    if (sort != null && sort.isSorted()) {
        Comparator<Product> comparator = null;
        for (Sort.Order order : sort) {
            Comparator<Product> propertyComparator = comparatorFor(order.getProperty());
            if (propertyComparator == null) {
                continue;
            }
            if (order.isDescending()) {
                propertyComparator = propertyComparator.reversed();
            }
            comparator = comparator == null
                    ? propertyComparator
                    : comparator.thenComparing(propertyComparator);
        }
        if (comparator != null) {
            products = products.stream().sorted(comparator).toList();
        }
    }
    
    return products.stream()
            .map(DtoMapper::toProductResponse)
            .toList();
}
```

**Benefits**:
- Encapsulates different algorithms
- Makes algorithms interchangeable
- Eliminates conditional statements
- Easy to add new strategies

---

### 8. Singleton Pattern

> **Definition**: Ensures a class has only one instance and provides a global point of access to it.

#### Implementation in MyShop

**Framework**: Spring Framework manages beans as singletons by default

#### Example: Service Beans
**File**: `src/main/java/com/myshop/service/impl/ProductServiceImpl.java`

```java
@Service  // Spring creates a single instance
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    // Single instance managed by Spring
}
```

#### Example: Repository Beans
**File**: `src/main/java/com/myshop/repository/ProductRepository.java`

```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Spring Data JPA creates a single proxy instance
}
```

#### Example: Configuration Beans
**File**: `src/main/java/com/myshop/config/SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean  // Single instance per application context
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**Benefits**:
- Reduces memory overhead
- Ensures consistent state
- Simplifies dependency management
- Thread-safe in Spring context

---

### 9. Template Method Pattern

> **Definition**: Defines the skeleton of an algorithm in a method, deferring some steps to subclasses.

#### Implementation in MyShop

#### Example: Spring Data JPA Repository
**File**: `src/main/java/com/myshop/repository/ProductRepository.java`

```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Template methods from JpaRepository:
    // - save()
    // - findById()
    // - findAll()
    // - delete()
    // - etc.
    
    // Custom query methods override the template
    @Query("""
            SELECT p FROM Product p
            WHERE (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
              AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    List<Product> searchProducts(@Param("categoryId") Long categoryId,
                                 @Param("minPrice") BigDecimal minPrice,
                                 @Param("maxPrice") BigDecimal maxPrice,
                                 @Param("search") String search);
}
```

#### Example: Spring Transaction Management
**File**: Service implementations with `@Transactional`

```java
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    
    @Override
    @Transactional  // Template method for transaction management
    public OrderResponse checkout(CheckoutRequest request) {
        // Spring handles:
        // 1. Begin transaction
        // 2. Execute business logic (this method)
        // 3. Commit transaction
        // 4. Rollback on exception
        
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        // ... business logic ...
        return DtoMapper.toOrderResponse(savedOrder, orderItems);
    }
}
```

**Benefits**:
- Defines algorithm structure
- Allows subclasses to redefine steps
- Promotes code reuse
- Enforces consistent behavior

---

### 10. Mapper Pattern

> **Definition**: Responsible for transferring data between two objects, typically between entities and DTOs.

#### Implementation in MyShop

**File**: `src/main/java/com/myshop/mapper/DtoMapper.java`

```java
public final class DtoMapper {
    
    private DtoMapper() {
        // Private constructor prevents instantiation
    }
    
    public static UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
    
    public static CategoryResponse toCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .build();
    }
    
    public static ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .category(toCategoryResponse(product.getCategory()))
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
    
    public static CartItemResponse toCartItemResponse(CartItem cartItem) {
        Product product = cartItem.getProduct();
        BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        return CartItemResponse.builder()
                .id(cartItem.getId())
                .productId(product.getId())
                .productName(product.getName())
                .imageUrl(product.getImageUrl())
                .unitPrice(product.getPrice())
                .quantity(cartItem.getQuantity())
                .lineTotal(lineTotal)
                .build();
    }
    
    public static OrderResponse toOrderResponse(Order order, List<OrderItem> items) {
        List<OrderItemResponse> itemResponses = items.stream()
                .map(DtoMapper::toOrderItemResponse)
                .toList();
        
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getOrderStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .shippingName(order.getShippingName())
                .shippingAddress(order.getShippingAddress())
                .shippingPhone(order.getShippingPhone())
                .shippingEmail(order.getShippingEmail())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(itemResponses)
                .payment(toPaymentResponse(order.getPayment()))
                .build();
    }
}
```

**Usage**:
```java
@Override
public ProductResponse getProduct(Long id) {
    Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    return DtoMapper.toProductResponse(product);
}
```

**Benefits**:
- Centralizes mapping logic
- Prevents tight coupling between layers
- Makes testing easier
- Simplifies maintenance

---

## Summary

### SOLID Principles Implementation Summary

| Principle | Implementation | Key Files |
|-----------|---------------|-----------|
| **Single Responsibility** | Each class has one clear purpose | All Controllers, Services, Repositories |
| **Open/Closed** | Interfaces allow extension without modification | Service interfaces, Repository interfaces |
| **Liskov Substitution** | Implementations can be swapped seamlessly | All Service implementations, Repository proxies |
| **Interface Segregation** | Focused, client-specific interfaces | ProductService, CartService, OrderService, etc. |
| **Dependency Inversion** | Depend on abstractions, not concretions | All Controllers depend on Service interfaces, Services depend on Repository interfaces |

### Design Patterns Implementation Summary

| Pattern | Purpose | Key Files |
|---------|---------|-----------|
| **Repository** | Abstract data access | All `*Repository.java` interfaces |
| **Service Layer** | Encapsulate business logic | All `*Service.java` and `*ServiceImpl.java` |
| **DTO** | Transfer data between layers | All `*Request.java` and `*Response.java` |
| **Builder** | Construct complex objects | All entity classes with `@Builder` |
| **Dependency Injection** | Manage dependencies | All classes with `@RequiredArgsConstructor` |
| **Factory** | Create objects | `SecurityConfig.java`, `DataInitializer.java` |
| **Strategy** | Interchangeable algorithms | Payment methods, Sorting strategies |
| **Singleton** | Single instance per context | All Spring-managed beans |
| **Template Method** | Define algorithm skeleton | `@Transactional` methods, JpaRepository |
| **Mapper** | Convert between objects | `DtoMapper.java` |

### Architecture Benefits

The combination of SOLID principles and design patterns in MyShop Backend provides:

1. **Maintainability**: Clear separation of concerns makes code easy to understand and modify
2. **Testability**: Dependency injection and abstractions enable easy unit testing
3. **Scalability**: Service layer and repository pattern support horizontal scaling
4. **Flexibility**: Strategy pattern and DIP allow easy swapping of implementations
5. **Reusability**: Well-defined interfaces promote code reuse
6. **Robustness**: Multiple validation layers and transaction management ensure data integrity

---

**Last Updated**: December 18, 2024  
**Project Version**: 0.0.1-SNAPSHOT  
**Framework**: Spring Boot 3.4.1  
**Java Version**: 17
