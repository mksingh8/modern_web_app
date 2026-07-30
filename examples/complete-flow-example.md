# Complete Request Flow — Step-by-Step with Code

This document traces the full journey of a single button click in React all the way to the database and back, with real code at every step.

## Scenario

A logged-in user clicks **"Add to Cart"** on a product page. We follow the entire round trip.

---

## The Flow at a Glance

```
┌─────────────────────────────────────────────────────────────────┐
│  BROWSER                                                        │
│                                                                 │
│  User clicks "Add to Cart" button                               │
│         │                                                       │
│         ▼                                                       │
│  handleAddToCart(productId=5, quantity=2)   ← React handler    │
│         │                                                       │
│         ▼                                                       │
│  api.post('/cart/items', { productId:5, quantity:2 })  ← Axios │
│         │                                                       │
└─────────┼───────────────────────────────────────────────────────┘
          │  HTTP POST /api/cart/items
          │  Authorization: ******
          │  Content-Type: application/json
          │  Body: { "productId": 5, "quantity": 2 }
          │
┌─────────▼───────────────────────────────────────────────────────┐
│  SPRING BOOT                                                    │
│                                                                 │
│  JwtAuthFilter              ← validates JWT, sets user context  │
│         │                                                       │
│         ▼                                                       │
│  CartController.addItem()   ← receives request, parses body    │
│         │                                                       │
│         ▼                                                       │
│  CartService.addItem()      ← business logic                   │
│         │                                                       │
│         ├── ProductRepository.findById(5)  → DB SELECT         │
│         ├── CartRepository.findByUserId()  → DB SELECT         │
│         └── CartRepository.save()          → DB INSERT/UPDATE  │
│                                                                 │
│  Returns: CartItemDto { id:101, productId:5, quantity:2, ... } │
└─────────┼───────────────────────────────────────────────────────┘
          │  HTTP 201 Created
          │  Body: { "id": 101, "productId": 5, "quantity": 2, "price": 29.99 }
          │
┌─────────▼───────────────────────────────────────────────────────┐
│  BROWSER (back in React)                                        │
│                                                                 │
│  Axios resolves with response                                   │
│  setCartItems([...cartItems, response.data])  ← state update   │
│  React re-renders — cart icon shows new item count             │
└─────────────────────────────────────────────────────────────────┘
```

---

## Step 1 — React: Button and Click Handler

```jsx
// ProductDetailPage.jsx

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from './apiClient';  // pre-configured axios instance

function ProductDetailPage({ product }) {
  const navigate = useNavigate();
  const [quantity, setQuantity] = useState(1);
  const [adding, setAdding] = useState(false);
  const [feedback, setFeedback] = useState(null);

  // Step 1a: User clicks the button — this handler fires
  const handleAddToCart = async () => {
    setAdding(true);
    setFeedback(null);

    try {
      // Step 1b: Call the API function
      await addToCart(product.id, quantity);
      setFeedback({ type: 'success', message: 'Added to cart!' });
    } catch (err) {
      setFeedback({ type: 'error', message: err.message });
    } finally {
      setAdding(false);
    }
  };

  return (
    <div className="product-detail">
      <h1>{product.name}</h1>
      <p>${product.price.toFixed(2)}</p>

      <div className="quantity-selector">
        <label>Quantity:</label>
        <input
          type="number"
          min="1"
          max={product.stock}
          value={quantity}
          onChange={(e) => setQuantity(Number(e.target.value))}
        />
      </div>

      {/* THE BUTTON */}
      <button
        onClick={handleAddToCart}
        disabled={adding || product.stock === 0}
        className="btn-primary"
      >
        {adding ? 'Adding...' : product.stock === 0 ? 'Out of Stock' : 'Add to Cart'}
      </button>

      {feedback && (
        <p className={`feedback ${feedback.type}`}>{feedback.message}</p>
      )}
    </div>
  );
}
```

---

## Step 2 — React: The API Call Function

```jsx
// cartService.js

import { api } from './apiClient';

/**
 * Sends an "add to cart" request to the backend.
 * @param {number} productId  - ID of the product to add
 * @param {number} quantity   - How many units to add
 * @returns {Promise<CartItem>} - The created or updated cart item
 */
export async function addToCart(productId, quantity) {
  try {
    const response = await api.post('/cart/items', { productId, quantity });
    return response.data;  // { id, productId, quantity, price, ... }
  } catch (error) {
    // Translate server error to user-friendly message
    const serverMessage = error.response?.data?.message;
    throw new Error(serverMessage || 'Failed to add item to cart');
  }
}
```

---

## Step 3 — Axios: HTTP Request with JWT Token

```jsx
// apiClient.js

import axios from 'axios';

export const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
});

// Request interceptor: attach JWT before every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('jwt_token');
  if (token) {
    // This is how the backend knows who you are
    config.headers.Authorization = `******;
  }
  return config;
});

// Response interceptor: handle auth errors globally
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid — send user back to login
      localStorage.removeItem('jwt_token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

/*
 * The outgoing HTTP request looks like this:
 *
 * POST /api/cart/items HTTP/1.1
 * Host: localhost:8080
 * Authorization: ******
 * Content-Type: application/json
 *
 * {
 *   "productId": 5,
 *   "quantity": 2
 * }
 */
```

---

## Step 4 — Spring Boot: JWT Filter

Before the request reaches the controller, it passes through a security filter:

```java
// JwtAuthFilter.java

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {

        // 1. Extract Authorization header
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Remove "Bearer " prefix

            try {
                // 2. Decode the JWT and get the username (email)
                String username = jwtUtil.extractUsername(token);  // "alice@example.com"

                if (username != null
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // 3. Load full user details from database
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // 4. Validate the token
                    if (jwtUtil.isTokenValid(token, userDetails)) {

                        // 5. Set authentication in Spring Security's context
                        //    From this point on, Spring knows who made the request
                        var auth = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (JwtException ex) {
                // Invalid token — Spring Security will return 401
            }
        }

        // 6. Continue to the next filter (eventually reaches the controller)
        chain.doFilter(request, response);
    }
}
```

---

## Step 5 — Spring Boot: Controller Receives the Request

```java
// CartController.java

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // Mapped to: POST /api/cart/items
    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartItemDto addItem(
        @RequestBody @Valid AddToCartRequest request,
        @AuthenticationPrincipal UserDetails currentUser  // Populated by JwtAuthFilter
    ) {
        // currentUser.getUsername() == "alice@example.com"
        // request == AddToCartRequest { productId: 5, quantity: 2 }

        return cartService.addItem(request, currentUser.getUsername());
    }
}

// AddToCartRequest.java — maps the JSON body
public record AddToCartRequest(
    @NotNull(message = "Product ID is required")
    Long productId,

    @NotNull @Min(1) @Max(100)
    Integer quantity
) {}
```

---

## Step 6 — Spring Boot: Service Applies Business Logic

```java
// CartService.java

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartItemDto addItem(AddToCartRequest request, String userEmail) {

        // 1. Look up the user
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2. Look up the product
        Product product = productRepository.findById(request.productId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Product not found: " + request.productId()));

        // 3. Business rule: check stock
        if (product.getStock() < request.quantity()) {
            throw new InsufficientStockException(
                String.format("Only %d units in stock", product.getStock()));
        }

        // 4. Get or create cart for user
        Cart cart = cartRepository.findByUserId(user.getId())
            .orElseGet(() -> {
                Cart newCart = new Cart();
                newCart.setUser(user);
                return cartRepository.save(newCart);
            });

        // 5. Add or update cart item
        CartItem cartItem = cartItemRepository
            .findByCartAndProduct(cart, product)
            .orElse(new CartItem(cart, product, 0));

        cartItem.setQuantity(cartItem.getQuantity() + request.quantity());
        CartItem saved = cartItemRepository.save(cartItem);

        // 6. Return DTO (never return entity directly)
        return new CartItemDto(
            saved.getId(),
            product.getId(),
            product.getName(),
            saved.getQuantity(),
            product.getPrice()
        );
    }
}
```

---

## Step 7 — Spring Boot: Repository Accesses Database

```java
// CartItemRepository.java

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Finds an existing cart item (for updating quantity)
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    // Gets all items in a cart (for displaying the cart page)
    List<CartItem> findByCartId(Long cartId);
}

// ProductRepository.java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // JpaRepository provides findById() out of the box
    // No additional code needed for this flow
}

/*
 * SQL generated by JPA for this flow:
 *
 * SELECT * FROM products WHERE id = 5
 * SELECT * FROM carts WHERE user_id = 42
 * SELECT * FROM cart_items WHERE cart_id = 7 AND product_id = 5
 * INSERT INTO cart_items (cart_id, product_id, quantity) VALUES (7, 5, 2)
 *   -- OR --
 * UPDATE cart_items SET quantity = 4 WHERE id = 101
 */
```

---

## Step 8 — Response Flows Back to React

```
Spring Boot sends:
─────────────────
HTTP/1.1 201 Created
Content-Type: application/json

{
  "id": 101,
  "productId": 5,
  "productName": "Wireless Headphones",
  "quantity": 2,
  "price": 79.99
}

React receives it:
──────────────────
// Inside handleAddToCart():
const cartItem = await addToCart(product.id, quantity);
// cartItem = { id: 101, productId: 5, productName: "...", quantity: 2, price: 79.99 }

// Update React state — triggers re-render
setCartItems(prev => [...prev, cartItem]);
setCartCount(prev => prev + cartItem.quantity);

// User sees:
// ✓ "Added to cart!" success message
// ✓ Cart icon badge shows updated count
```

---

## Error Scenarios

### What happens if the product is out of stock?

```
CartService throws InsufficientStockException
       ↓
GlobalExceptionHandler catches it
       ↓
Returns HTTP 400 Bad Request:
  { "code": "INSUFFICIENT_STOCK", "message": "Only 0 units in stock" }
       ↓
Axios rejects the promise
       ↓
catch (err) in React:
  setFeedback({ type: 'error', message: 'Only 0 units in stock' })
       ↓
User sees red error message under the button
```

### What happens if the JWT is expired?

```
JwtAuthFilter fails to validate token
       ↓
Spring Security returns HTTP 401 Unauthorized
       ↓
Axios response interceptor catches 401
       ↓
Clears localStorage token
       ↓
Redirects user to /login page
```

---

## Summary: Who Does What

| Layer             | Responsibility                                     |
|-------------------|----------------------------------------------------|
| React button      | Captures the user interaction                      |
| React handler     | Coordinates loading state, calls service function  |
| Axios interceptor | Attaches JWT token to the request                  |
| JwtAuthFilter     | Validates token, sets user identity                |
| Controller        | Routes request to service, parses input            |
| Service           | Business logic, transaction management             |
| Repository        | Database queries (insert/select/update)            |
| Controller        | Wraps result in HTTP response (status code)        |
| Axios             | Resolves promise with response data                |
| React handler     | Updates state, triggers re-render                  |
| React render      | User sees updated UI                               |
