# Spring Boot Project Structure — Detailed Guide

This document explains the standard layered architecture used in Spring Boot applications, what each package/class is responsible for, and how everything connects.

---

## Directory Layout

```
src/
├── main/
│   ├── java/com/example/myapp/
│   │   ├── MyAppApplication.java        ← Entry point (@SpringBootApplication)
│   │   │
│   │   ├── controller/                  ← HTTP layer: receives requests, returns responses
│   │   │   ├── UserController.java
│   │   │   ├── ProductController.java
│   │   │   └── AuthController.java
│   │   │
│   │   ├── service/                     ← Business logic layer
│   │   │   ├── UserService.java
│   │   │   ├── ProductService.java
│   │   │   └── AuthService.java
│   │   │
│   │   ├── repository/                  ← Data access layer (JPA/database)
│   │   │   ├── UserRepository.java
│   │   │   └── ProductRepository.java
│   │   │
│   │   ├── entity/                      ← Database table mappings (JPA entities)
│   │   │   ├── User.java
│   │   │   └── Product.java
│   │   │
│   │   ├── dto/                         ← Data Transfer Objects (API shapes)
│   │   │   ├── request/
│   │   │   │   ├── CreateUserRequest.java
│   │   │   │   └── LoginRequest.java
│   │   │   └── response/
│   │   │       ├── UserDto.java
│   │   │       └── JwtResponse.java
│   │   │
│   │   ├── config/                      ← App configuration (CORS, beans, etc.)
│   │   │   ├── SecurityConfig.java
│   │   │   └── CorsConfig.java
│   │   │
│   │   ├── security/                    ← JWT filter and utilities
│   │   │   ├── JwtAuthFilter.java
│   │   │   └── JwtUtil.java
│   │   │
│   │   └── exception/                   ← Custom exceptions and error handling
│   │       ├── ResourceNotFoundException.java
│   │       └── GlobalExceptionHandler.java
│   │
│   └── resources/
│       ├── application.yml              ← App configuration (DB URL, server port, etc.)
│       └── application-prod.yml        ← Production overrides
│
└── test/
    └── java/com/example/myapp/
        ├── controller/
        │   └── UserControllerTest.java
        └── service/
            └── UserServiceTest.java
```

---

## Layer 1: Controller (`controller/`)

### Responsibility
- Accept HTTP requests and extract data from them (path variables, query params, request body)
- Call the appropriate service method
- Return an HTTP response with the correct status code

### Rules
- **No business logic** — only routing and translation
- Always validate input using `@Valid`
- Return DTOs, never entities (entities can expose sensitive data)

### Example

```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable Long id) {
        return userService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody @Valid CreateUserRequest req) {
        UserDto created = userService.create(req);
        URI location = URI.create("/api/users/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> update(@PathVariable Long id,
                                          @RequestBody @Valid UpdateUserRequest req) {
        return ResponseEntity.ok(userService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## Layer 2: Service (`service/`)

### Responsibility
- Implement business rules and workflows
- Orchestrate multiple repositories or external calls
- Manage transactions

### Rules
- Always annotate with `@Service`
- Use `@Transactional` on methods that write to the database
- Throw meaningful custom exceptions (not raw `RuntimeException`)
- Convert entities ↔ DTOs here (keep this logic out of controllers and repositories)

### Example

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserDto> findAll() {
        return userRepository.findAll()
            .stream()
            .map(UserMapper::toDto)
            .collect(Collectors.toList());
    }

    public Optional<UserDto> findById(Long id) {
        return userRepository.findById(id).map(UserMapper::toDto);
    }

    @Transactional
    public UserDto create(CreateUserRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ConflictException("Email already registered: " + req.getEmail());
        }

        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole(Role.USER);

        log.info("Creating user with email: {}", req.getEmail());
        return UserMapper.toDto(userRepository.save(user));
    }

    @Transactional
    public UserDto update(Long id, UpdateUserRequest req) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        user.setName(req.getName());
        return UserMapper.toDto(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found: " + id);
        }
        userRepository.deleteById(id);
        log.info("Deleted user with id: {}", id);
    }
}
```

---

## Layer 3: Repository (`repository/`)

### Responsibility
- All database communication (CRUD operations)
- Defines queries — either via method naming conventions or `@Query`

### Rules
- Extend `JpaRepository<Entity, ID>` for automatic CRUD
- Only work with entities (not DTOs)
- Keep queries here — never write SQL in services or controllers

### Method Naming Convention

Spring Data JPA reads method names and generates the query automatically:

| Method Name                                  | Generated SQL equivalent                          |
|----------------------------------------------|---------------------------------------------------|
| `findByEmail(String email)`                  | `WHERE email = ?`                                 |
| `findByRoleAndActive(Role role, boolean active)` | `WHERE role = ? AND active = ?`              |
| `findByNameContainingIgnoreCase(String name)` | `WHERE LOWER(name) LIKE LOWER('%?%')`            |
| `findByCreatedAtAfter(LocalDateTime date)`   | `WHERE created_at > ?`                            |
| `existsByEmail(String email)`                | `SELECT COUNT(*) > 0 WHERE email = ?`            |
| `countByRole(Role role)`                     | `SELECT COUNT(*) WHERE role = ?`                  |
| `deleteByEmail(String email)`                | `DELETE WHERE email = ?`                          |

### Example

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);

    @Query("SELECT u FROM User u WHERE u.name LIKE %:q% OR u.email LIKE %:q%")
    List<User> search(@Param("q") String query);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLoginAt = :now WHERE u.id = :id")
    void updateLastLogin(@Param("id") Long id, @Param("now") LocalDateTime now);
}
```

---

## Layer 4: Entity (`entity/`)

### Responsibility
- Represents a database table as a Java class
- Each field maps to a column
- JPA handles the SQL generation for inserts, updates, selects

### Rules
- Annotate with `@Entity` and `@Table`
- Always have an `@Id` field
- Never expose entity directly from controller — use DTOs
- Use `@JsonIgnore` on sensitive fields like passwords

### Example

```java
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email")
})
@Getter @Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false)
    @JsonIgnore
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
}
```

---

## Layer 5: Config (`config/`)

### Responsibility
- Application-wide configuration beans
- Security configuration (which URLs are protected)
- CORS configuration (which frontends can call the API)

### Example — SecurityConfig

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())       // REST APIs don't need CSRF
            .sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS)) // JWT = no sessions
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()    // login/register: open
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()  // browsing: open
                .requestMatchers("/api/admin/**").hasRole("ADMIN")  // admin: restricted
                .anyRequest().authenticated()                   // everything else: needs JWT
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
```

---

## Layer 6: Security (`security/`)

### Responsibility
- JWT token generation (on login)
- JWT token validation (on every protected request)
- Extract user identity from token

### Example — JwtUtil

```java
@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms:86400000}")  // default 24 hours
    private long expirationMs;

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .claim("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
```

---

## DTO Layer (`dto/`)

### Why DTOs?

**Never return raw entities from your API.** Entities may contain:
- Sensitive fields (password hash)
- Lazy-loaded relationships that cause `LazyInitializationException`
- Internal IDs or audit fields you don't want to expose

DTOs are plain Java objects that represent exactly the shape of your API.

### Request DTO (input validation)

```java
public record CreateUserRequest(
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
    String name,

    @NotBlank @Email(message = "Valid email required")
    String email,

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password
) {}
```

### Response DTO (output shaping)

```java
public record UserDto(
    Long id,
    String name,
    String email,
    Role role,
    LocalDateTime createdAt
) {}
```

---

## Exception Handling (`exception/`)

### Global Exception Handler

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse("CONFLICT", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}
```

---

## application.yml

```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=myapp;encrypt=true;trustServerCertificate=true
    username: ${DB_USERNAME:sa}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate   # Don't auto-create tables in production
    show-sql: false        # Don't log every SQL query in production
  jackson:
    default-property-inclusion: non_null

server:
  port: 8080

app:
  jwt:
    secret: ${JWT_SECRET}  # Load from environment / Azure Key Vault
    expiration-ms: 86400000

logging:
  level:
    com.example.myapp: INFO
    org.springframework.security: WARN
```
