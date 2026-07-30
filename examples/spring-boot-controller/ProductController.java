package com.example.modernwebapp.controller;

import com.example.modernwebapp.model.Product;
import com.example.modernwebapp.model.ProductRequest;
import com.example.modernwebapp.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ProductController — REST API endpoints for the Product resource.
 *
 * Key concepts demonstrated:
 *   - @RestController: marks this class as a REST controller (returns JSON by default)
 *   - @RequestMapping: sets the base URL path for all endpoints in this class
 *   - Dependency Injection: ProductService is injected via the constructor
 *   - @PreAuthorize: Role-based access control using JWT claims
 *   - ResponseEntity: gives full control over HTTP status codes and headers
 *   - @Valid: triggers validation of the request body
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    /**
     * Constructor injection (preferred over field injection with @Autowired).
     *
     * Spring sees this constructor and automatically injects a ProductService bean.
     * This makes the controller easy to unit-test — just pass a mock ProductService.
     */
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * GET /api/products
     *
     * Returns all available products. Accessible to all authenticated users.
     *
     * @return 200 OK with list of products
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.findAll();
        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/products/{id}
     *
     * Returns a single product by its ID.
     *
     * @param id  the product ID from the URL path
     * @return    200 OK with the product, or 404 Not Found if it doesn't exist
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/products
     *
     * Creates a new product. Restricted to ADMIN role only.
     * The @Valid annotation triggers validation rules defined on ProductRequest fields
     * (e.g., @NotBlank, @Min, @Max). If validation fails, Spring returns 400 Bad Request.
     *
     * @param request  the validated product creation request body
     * @return         201 Created with the saved product
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> createProduct(@RequestBody @Valid ProductRequest request) {
        Product saved = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * PUT /api/products/{id}
     *
     * Updates an existing product. Restricted to ADMIN role only.
     *
     * @param id      the product ID from the URL path
     * @param request the updated product data
     * @return        200 OK with updated product, or 404 if not found
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody @Valid ProductRequest request) {
        return productService.update(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/products/{id}
     *
     * Deletes a product by ID. Restricted to ADMIN role only.
     *
     * @param id  the product ID from the URL path
     * @return    204 No Content on success, or 404 if not found
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (productService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
