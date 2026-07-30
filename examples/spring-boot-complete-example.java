/**
 * spring-boot-complete-example.java
 *
 * A single-file reference showing all layers of a Spring Boot application
 * working together: Entity, Repository, DTO, Service, Controller, Security.
 *
 * In a real project each class lives in its own file inside the appropriate
 * package (entity/, repository/, service/, controller/, security/).
 *
 * Package: com.example.myapp
 * Dependencies: Spring Boot 3.x, Spring Data JPA, Spring Security,
 *               Lombok, JJWT 0.11+, Jakarta Validation
 */

package com.example.myapp;

// ═══════════════════════════════════════════════════════════════
// ENTITY LAYER  —  com.example.myapp.entity
// Maps directly to a database table.
// ═══════════════════════════════════════════════════════════════

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor
class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal price;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    @JsonIgnore  // Never serialize the password hash
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

enum Role { USER, ADMIN }

// ═══════════════════════════════════════════════════════════════
// DTO LAYER  —  com.example.myapp.dto
// API input/output shapes — never expose entities directly.
// ═══════════════════════════════════════════════════════════════

import jakarta.validation.constraints.*;

// Input: creating a product
record CreateProductRequest(
    @NotBlank(message = "Name is required")
    @Size(max = 200)
    String name,

    String description,

    @NotNull @DecimalMin("0.01")
    java.math.BigDecimal price,

    @NotBlank
    String category,

    @Min(0)
    Integer stock
) {}

// Input: updating a product
record UpdateProductRequest(
    @NotBlank String name,
    String description,
    @NotNull @DecimalMin("0.01") java.math.BigDecimal price,
    @NotBlank String category,
    @Min(0) Integer stock
) {}

// Output: product response
record ProductDto(
    Long id,
    String name,
    String description,
    java.math.BigDecimal price,
    String category,
    Integer stock,
    LocalDateTime createdAt
) {}

// Input: login
record LoginRequest(
    @NotBlank @Email String email,
    @NotBlank String password
) {}

// Output: JWT token
record JwtResponse(String token, String type, Long expiresIn) {
    JwtResponse(String token) {
        this(token, "Bearer", 86400L);
    }
}

// Error response shape
record ErrorResponse(String code, String message) {}

// ═══════════════════════════════════════════════════════════════
// REPOSITORY LAYER  —  com.example.myapp.repository
// Spring Data JPA generates SQL from method names.
// ═══════════════════════════════════════════════════════════════

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
interface ProductRepository extends JpaRepository<Product, Long> {

    // SELECT * FROM products WHERE category = ? AND active = true
    List<Product> findByCategoryAndActiveTrue(String category);

    // SELECT * FROM products WHERE price BETWEEN ? AND ?
    List<Product> findByPriceBetween(
        java.math.BigDecimal minPrice,
        java.math.BigDecimal maxPrice
    );

    // Full-text search across name and description
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Product> search(@Param("q") String query);

    // Low-stock alert
    @Query("SELECT p FROM Product p WHERE p.stock < :threshold AND p.active = true")
    List<Product> findLowStock(@Param("threshold") int threshold);
}

@Repository
interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}

// ═══════════════════════════════════════════════════════════════
// SERVICE LAYER  —  com.example.myapp.service
// All business logic lives here.
// ═══════════════════════════════════════════════════════════════

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
class ProductService {

    private final ProductRepository productRepository;

    public List<ProductDto> findAll() {
        return productRepository.findAll()
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public Optional<ProductDto> findById(Long id) {
        return productRepository.findById(id).map(this::toDto);
    }

    public List<ProductDto> search(String query) {
        return productRepository.search(query)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public ProductDto create(CreateProductRequest req) {
        Product product = new Product();
        product.setName(req.name());
        product.setDescription(req.description());
        product.setPrice(req.price());
        product.setCategory(req.category());
        product.setStock(req.stock() != null ? req.stock() : 0);

        Product saved = productRepository.save(product);
        log.info("Created product id={} name={}", saved.getId(), saved.getName());
        return toDto(saved);
    }

    @Transactional
    public ProductDto update(Long id, UpdateProductRequest req) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        product.setName(req.name());
        product.setDescription(req.description());
        product.setPrice(req.price());
        product.setCategory(req.category());
        product.setStock(req.stock());

        return toDto(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found: " + id);
        }
        productRepository.deleteById(id);
        log.info("Deleted product id={}", id);
    }

    // Private mapper — keep entity-to-DTO conversion in the service
    private ProductDto toDto(Product p) {
        return new ProductDto(
            p.getId(), p.getName(), p.getDescription(),
            p.getPrice(), p.getCategory(), p.getStock(), p.getCreatedAt()
        );
    }
}

// Auth service — handles login and token generation
@Service
@RequiredArgsConstructor
@Slf4j
class AuthService {

    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    public JwtResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
            .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException(
                "Invalid credentials"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new org.springframework.security.authentication.BadCredentialsException(
                "Invalid credentials");
        }

        var userDetails = userDetailsService.loadUserByUsername(req.email());
        String token = jwtUtil.generateToken(userDetails);
        log.info("User logged in: {}", req.email());
        return new JwtResponse(token);
    }
}

// ═══════════════════════════════════════════════════════════════
// CONTROLLER LAYER  —  com.example.myapp.controller
// Thin HTTP layer — receives requests, delegates to service.
// ═══════════════════════════════════════════════════════════════

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
class ProductController {

    private final ProductService productService;

    // GET /api/products
    // GET /api/products?search=laptop
    @GetMapping
    public List<ProductDto> getProducts(
        @RequestParam(required = false) String search
    ) {
        if (search != null && !search.isBlank()) {
            return productService.search(search);
        }
        return productService.findAll();
    }

    // GET /api/products/42
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long id) {
        return productService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/products
    // Body: { "name": "Laptop", "price": 999.99, "category": "electronics", "stock": 50 }
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(
        @RequestBody @Valid CreateProductRequest req
    ) {
        ProductDto created = productService.create(req);
        URI location = URI.create("/api/products/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    // PUT /api/products/42
    @PutMapping("/{id}")
    public ProductDto updateProduct(
        @PathVariable Long id,
        @RequestBody @Valid UpdateProductRequest req
    ) {
        return productService.update(id, req);
    }

    // DELETE /api/products/42
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        productService.delete(id);
    }
}

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
class AuthController {

    private final AuthService authService;

    // POST /api/auth/login
    // Body: { "email": "alice@example.com", "password": "secret123" }
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody @Valid LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }
}

// ═══════════════════════════════════════════════════════════════
// SECURITY LAYER  —  com.example.myapp.security
// JWT filter validates token on every protected request.
// ═══════════════════════════════════════════════════════════════

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    JwtAuthFilter(JwtUtil jwtUtil,
                  org.springframework.security.core.userdetails.UserDetailsService uds) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = uds;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws java.io.IOException, ServletException {

        String authHeader = req.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String username = jwtUtil.extractUsername(token);
                if (username != null
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (jwtUtil.isTokenValid(token, userDetails)) {
                        var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(req));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (JwtException ex) {
                // Invalid token — don't set authentication, request will be rejected by Spring Security
                logger.debug("JWT validation failed: " + ex.getMessage());
            }
        }

        chain.doFilter(req, res);
    }
}

@Component
class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long expirationMs;

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername())
            && !isExpired(token);
    }

    private boolean isExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    private Claims parseClaims(String token) {
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

// ═══════════════════════════════════════════════════════════════
// EXCEPTION HANDLING  —  com.example.myapp.exception
// ═══════════════════════════════════════════════════════════════

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import lombok.extern.slf4j.Slf4j;

@ResponseStatus(HttpStatus.NOT_FOUND)
class ResourceNotFoundException extends RuntimeException {
    ResourceNotFoundException(String message) { super(message); }
}

@ResponseStatus(HttpStatus.CONFLICT)
class ConflictException extends RuntimeException {
    ConflictException(String message) { super(message); }
}

@RestControllerAdvice
@Slf4j
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse("INVALID_CREDENTIALS", "Invalid email or password"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}

// ═══════════════════════════════════════════════════════════════
// APPLICATION ENTRY POINT
// ═══════════════════════════════════════════════════════════════

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MyAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyAppApplication.class, args);
    }
}
