# Modern Web Application Architecture: A Comprehensive Guide

> A deep-dive article explaining how modern web applications are built, from the React UI all the way to Azure-hosted microservices — with real code examples, diagrams, and best practices.

---

## Repository Structure

```
modern_web_app/
├── frontend/   # React single-page application
├── backend/    # Spring Boot REST API
├── infra/      # Infrastructure-as-code (Kubernetes, Terraform, Docker)
└── docs/       # Project documentation and architecture diagrams
```

---

## Getting Started

### Prerequisites

| Tool | Version |
|------|---------|
| Node.js | 18 LTS or later |
| npm | 9+ |
| Java | 17+ |
| Maven | 3.8+ (or use the included `mvnw` wrapper) |
| Docker | 24+ (optional, for containerised runs) |

### Setup

**1. Clone the repository**

```bash
git clone https://github.com/mksingh8/modern_web_app.git
cd modern_web_app
```

**2. Install frontend dependencies**

```bash
cd frontend
npm install
cd ..
```

**3. Install backend dependencies**

```bash
cd backend
./mvnw install -DskipTests
cd ..
```

### Run

**Frontend** (React dev server on `http://localhost:3000`)

```bash
cd frontend
npm start
```

**Backend** (Spring Boot API on `http://localhost:8080`)

```bash
cd backend
./mvnw spring-boot:run
```

### Run with Docker Compose (optional)

```bash
docker compose up --build
```

---

## Table of Contents

1. [The Big Picture](#1-the-big-picture)
2. [React – The User Interface Layer](#2-react--the-user-interface-layer)
   - [Components](#components)
   - [Props](#props)
   - [State](#state)
   - [Hooks: useState & useEffect](#hooks-usestate--useeffect)
   - [API Calls with Axios / Fetch](#api-calls-with-axios--fetch)
   - [React Routing](#react-routing)
3. [REST API – The Communication Contract](#3-rest-api--the-communication-contract)
4. [Spring Boot – The Backend Powerhouse](#4-spring-boot--the-backend-powerhouse)
   - [Project Structure](#project-structure)
   - [Annotations Deep Dive](#annotations-deep-dive)
   - [Dependency Injection](#dependency-injection)
   - [Each Layer Explained](#each-layer-explained)
5. [The Complete Request Flow](#5-the-complete-request-flow)
6. [JWT Authentication](#6-jwt-authentication)
7. [Docker – Packaging the Application](#7-docker--packaging-the-application)
8. [Kubernetes – Running at Scale](#8-kubernetes--running-at-scale)
9. [Azure – Cloud Hosting & Services](#9-azure--cloud-hosting--services)
10. [Putting It All Together](#10-putting-it-all-together)
11. [Glossary](#glossary)

---

## 1. The Big Picture

A modern web application is a layered system. Each layer has a clear responsibility, communicates through well-defined interfaces, and can be scaled independently.

```
┌─────────────────────────────────────────────────────┐
│                    Users (Browser)                   │
└───────────────────────┬─────────────────────────────┘
                        │ HTTPS
┌───────────────────────▼─────────────────────────────┐
│           React Single-Page Application              │
│   (Components · State · Hooks · Routing)             │
└───────────────────────┬─────────────────────────────┘
                        │ REST / JSON  (JWT in header)
┌───────────────────────▼─────────────────────────────┐
│                Spring Boot REST API                  │
│   (Controllers · Services · Repositories)            │
└───────────────────────┬─────────────────────────────┘
                        │ JDBC / JPA / ORM
┌───────────────────────▼─────────────────────────────┐
│          Database  (SQL Server / MySQL / Cosmos DB)  │
└─────────────────────────────────────────────────────┘
```

### Azure adds infrastructure around this stack

```
Users
  │
  ▼
Azure Front Door / App Gateway   ← Global load balancing, WAF, SSL termination
  │
  ▼
AKS Cluster (Azure Kubernetes Service)
  │
  ├── Namespace: frontend
  │     └── Pod: React (Nginx)
  │
  └── Namespace: backend
        ├── Pod: Spring Boot Service A
        ├── Pod: Spring Boot Service B
        └── Pod: Spring Boot Service C
              │
              ▼
        Azure SQL / Cosmos DB / Blob Storage
```

---

## 2. React – The User Interface Layer

**React** is a JavaScript library (made by Meta/Facebook) that lets you build interactive user interfaces from small, reusable building blocks called **components**.

> 🏠 **Real-world analogy**: Think of React like LEGO. You build small, independent pieces (components) and assemble them into a complete structure (the page).

### Components

A component is a JavaScript function that returns HTML-like code (called JSX).

```jsx
// A simple functional component
function WelcomeBanner({ userName }) {
  return (
    <div className="banner">
      <h1>Welcome, {userName}!</h1>
      <p>You are now logged in.</p>
    </div>
  );
}

// Usage
<WelcomeBanner userName="Alice" />
```

**Functional vs Class Components**

Modern React uses **functional components** (with Hooks). Class components are the older style.

```jsx
// ✅ Modern: Functional Component
function Counter() {
  const [count, setCount] = useState(0);
  return <button onClick={() => setCount(count + 1)}>Count: {count}</button>;
}

// ⚠️ Older: Class Component
class Counter extends React.Component {
  state = { count: 0 };
  render() {
    return (
      <button onClick={() => this.setState({ count: this.state.count + 1 })}>
        Count: {this.state.count}
      </button>
    );
  }
}
```

---

### Props

**Props** (short for properties) are how you pass data **from a parent component to a child component**. They are read-only — a child cannot modify its own props.

```jsx
// Parent component passes data via props
function App() {
  return (
    <UserCard
      name="Alice Johnson"
      role="Software Engineer"
      avatarUrl="/images/alice.jpg"
    />
  );
}

// Child component receives props
function UserCard({ name, role, avatarUrl }) {
  return (
    <div className="card">
      <img src={avatarUrl} alt={name} />
      <h2>{name}</h2>
      <p>{role}</p>
    </div>
  );
}
```

> 🏷️ **Analogy**: Props are like arguments to a function. You call the function (render the component) with specific values, and it uses those values to produce output.

---

### State

**State** is data that lives inside a component and can change over time. When state changes, React re-renders the component automatically.

```jsx
function LoginForm() {
  // Declare state variables
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null); // clear previous errors

    try {
      await loginUser(email, password);
    } catch (err) {
      setError('Invalid credentials. Please try again.');
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        placeholder="Email"
      />
      <input
        type="password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        placeholder="Password"
      />
      {error && <p className="error">{error}</p>}
      <button type="submit">Login</button>
    </form>
  );
}
```

---

### Hooks: useState & useEffect

Hooks are special React functions (they start with `use`) that let functional components use React features like state and lifecycle.

#### `useState(initialValue)`

Returns `[currentValue, setterFunction]`. Calling the setter triggers a re-render.

```jsx
// Example 1: Simple counter
function Counter() {
  const [count, setCount] = useState(0);
  return (
    <div>
      <p>You clicked {count} times</p>
      <button onClick={() => setCount(count + 1)}>Click me</button>
      <button onClick={() => setCount(0)}>Reset</button>
    </div>
  );
}

// Example 2: Managing a list
function TodoList() {
  const [todos, setTodos] = useState([]);
  const [inputText, setInputText] = useState('');

  const addTodo = () => {
    if (inputText.trim()) {
      setTodos([...todos, { id: Date.now(), text: inputText, done: false }]);
      setInputText('');
    }
  };

  const toggleTodo = (id) => {
    setTodos(todos.map(todo =>
      todo.id === id ? { ...todo, done: !todo.done } : todo
    ));
  };

  return (
    <div>
      <input
        value={inputText}
        onChange={(e) => setInputText(e.target.value)}
        placeholder="Add a task..."
      />
      <button onClick={addTodo}>Add</button>
      <ul>
        {todos.map(todo => (
          <li
            key={todo.id}
            onClick={() => toggleTodo(todo.id)}
            style={{ textDecoration: todo.done ? 'line-through' : 'none' }}
          >
            {todo.text}
          </li>
        ))}
      </ul>
    </div>
  );
}
```

#### `useEffect(callback, [dependencies])`

Runs side effects (data fetching, subscriptions, timers) after the component renders.

```jsx
function UserProfile({ userId }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Runs after EVERY render (no dependency array — use rarely)
  useEffect(() => {
    console.log('Component rendered');
  });

  // Runs ONCE on mount (empty dependency array)
  useEffect(() => {
    console.log('Component mounted');
    return () => console.log('Component unmounted'); // cleanup
  }, []);

  // Runs when `userId` changes
  useEffect(() => {
    setLoading(true);
    setError(null);

    fetch(`/api/users/${userId}`)
      .then(res => {
        if (!res.ok) throw new Error('User not found');
        return res.json();
      })
      .then(data => {
        setUser(data);
        setLoading(false);
      })
      .catch(err => {
        setError(err.message);
        setLoading(false);
      });

    // Cleanup: cancel request if userId changes before fetch completes
    return () => {
      // In production, use AbortController here
    };
  }, [userId]); // ← re-runs only when userId changes

  if (loading) return <p>Loading...</p>;
  if (error)   return <p>Error: {error}</p>;
  if (!user)   return null;

  return (
    <div>
      <h2>{user.name}</h2>
      <p>{user.email}</p>
    </div>
  );
}
```

> See [`examples/react-hooks-guide.md`](examples/react-hooks-guide.md) for the full Hooks reference.

---

### API Calls with Axios / Fetch

React components talk to the Spring Boot backend via HTTP requests.

#### Using Axios (recommended — cleaner syntax)

```jsx
import axios from 'axios';

const API_BASE = 'http://localhost:8080/api';

// Create a configured axios instance
const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
});

// Automatically attach JWT to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('jwt_token');
  if (token) config.headers.Authorization = `******;
  return config;
});

// Example component using the API
function ProductList() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoading(true);
    api.get('/products')
      .then(res => setProducts(res.data))
      .catch(err => setError(err.response?.data?.message || 'Failed to load products'))
      .finally(() => setLoading(false));
  }, []);

  const createProduct = async (productData) => {
    try {
      const res = await api.post('/products', productData);
      setProducts(prev => [...prev, res.data]);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create product');
    }
  };

  if (loading) return <p>Loading products...</p>;
  if (error)   return <p className="error">{error}</p>;

  return (
    <ul>
      {products.map(p => <li key={p.id}>{p.name} — ${p.price}</li>)}
    </ul>
  );
}
```

#### Using Fetch (built-in browser API)

```jsx
async function fetchWithAuth(url, options = {}) {
  const token = localStorage.getItem('jwt_token');
  const response = await fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      Authorization: `******
      ...options.headers,
    },
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || `HTTP ${response.status}`);
  }

  return response.json();
}

// Usage
const products = await fetchWithAuth('/api/products');
```

> See [`examples/react-complete-example.jsx`](examples/react-complete-example.jsx) for a complete working component.

---

### React Routing

React Router lets you build multi-page experiences in a single-page application.

```jsx
import { BrowserRouter, Routes, Route, Link, useNavigate, useParams } from 'react-router-dom';

function App() {
  return (
    <BrowserRouter>
      {/* Navigation */}
      <nav>
        <Link to="/">Home</Link>
        <Link to="/products">Products</Link>
        <Link to="/profile">My Profile</Link>
      </nav>

      <Routes>
        <Route path="/"               element={<HomePage />} />
        <Route path="/products"       element={<ProductListPage />} />
        <Route path="/products/:id"   element={<ProductDetailPage />} />  {/* route param */}
        <Route path="/profile"        element={<ProtectedRoute><ProfilePage /></ProtectedRoute>} />
        <Route path="*"               element={<NotFoundPage />} />
      </Routes>
    </BrowserRouter>
  );
}

// Reading route parameters
function ProductDetailPage() {
  const { id } = useParams();  // reads ":id" from the URL
  const [product, setProduct] = useState(null);

  useEffect(() => {
    api.get(`/products/${id}`).then(res => setProduct(res.data));
  }, [id]);

  return product ? <h1>{product.name}</h1> : <p>Loading...</p>;
}

// Programmatic navigation
function LoginPage() {
  const navigate = useNavigate();

  const handleLogin = async (credentials) => {
    await loginUser(credentials);
    navigate('/dashboard');  // redirect after login
  };
  // ...
}

// Protected routes (redirect if not authenticated)
function ProtectedRoute({ children }) {
  const token = localStorage.getItem('jwt_token');
  return token ? children : <Navigate to="/login" replace />;
}
```

---

## 3. REST API – The Communication Contract

A **REST API** (Representational State Transfer) is a set of rules for how the frontend (React) and backend (Spring Boot) communicate over HTTP.

### Core HTTP Methods

| Method   | Action          | Example URL             | Request Body | Response        |
|----------|-----------------|-------------------------|--------------|-----------------|
| `GET`    | Read data       | `GET /api/users/42`     | None         | User object     |
| `POST`   | Create data     | `POST /api/users`       | New user     | Created user    |
| `PUT`    | Update (full)   | `PUT /api/users/42`     | Updated user | Updated user    |
| `PATCH`  | Update (partial)| `PATCH /api/users/42`   | Partial data | Updated user    |
| `DELETE` | Delete data     | `DELETE /api/users/42`  | None         | 204 No Content  |

### HTTP Status Codes

| Code | Meaning                  | When to use                              |
|------|--------------------------|------------------------------------------|
| 200  | OK                       | Successful GET, PUT, PATCH               |
| 201  | Created                  | Successful POST                          |
| 204  | No Content               | Successful DELETE                        |
| 400  | Bad Request              | Validation error, malformed input        |
| 401  | Unauthorized             | Missing or invalid JWT token             |
| 403  | Forbidden                | Valid token but insufficient permissions |
| 404  | Not Found                | Resource doesn't exist                   |
| 500  | Internal Server Error    | Unexpected server-side error             |

### JSON — The Language of REST APIs

```json
// Response from GET /api/users/42
{
  "id": 42,
  "name": "Alice Johnson",
  "email": "alice@example.com",
  "role": "ADMIN",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

---

## 4. Spring Boot – The Backend Powerhouse

**Spring Boot** is a Java framework that makes it easy to build production-ready REST APIs. It handles configuration, dependency injection, security, database access, and much more — with very little boilerplate.

> 🏭 **Analogy**: If your application were a restaurant, Spring Boot is the entire kitchen staff, including the head chef (business logic), sous chefs (services), and the pantry manager (repositories).

---

### Project Structure

A well-organized Spring Boot project follows a layered package structure:

```
src/main/java/com/example/myapp/
│
├── controller/          ← HTTP endpoints (what URLs does the app expose?)
│   ├── UserController.java
│   └── ProductController.java
│
├── service/             ← Business logic (what does the app DO?)
│   ├── UserService.java
│   └── ProductService.java
│
├── repository/          ← Database access (how does the app STORE data?)
│   ├── UserRepository.java
│   └── ProductRepository.java
│
├── entity/              ← Data models (what does the data LOOK like?)
│   ├── User.java
│   └── Product.java
│
├── config/              ← Configuration (how is the app SET UP?)
│   ├── SecurityConfig.java
│   └── CorsConfig.java
│
└── security/            ← Authentication & authorization
    ├── JwtFilter.java
    └── JwtUtil.java
```

> See [`examples/spring-boot-project-structure.md`](examples/spring-boot-project-structure.md) for the complete breakdown.

---

### Each Layer Explained

#### 🎮 Controller Layer (`controller/`)

**What it does**: Receives HTTP requests from the outside world (from React) and returns HTTP responses. Controllers are the entry point to your application.

**Analogy**: The **waiter** in a restaurant. They take orders from customers (HTTP requests), pass them to the kitchen (service layer), and bring back the food (HTTP response).

```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET /api/users — get all users
    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    // GET /api/users/42 — get one user by ID
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return userService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/users?role=ADMIN — search with query param
    @GetMapping("/search")
    public List<UserDto> searchUsers(@RequestParam String role) {
        return userService.findByRole(role);
    }

    // POST /api/users — create a new user
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody @Valid CreateUserRequest request) {
        return userService.createUser(request);
    }

    // DELETE /api/users/42
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
```

---

#### ⚙️ Service Layer (`service/`)

**What it does**: Contains the **business logic** — the rules and workflows of your application. Services orchestrate repositories and other services to fulfill a request.

**Analogy**: The **kitchen chef**. They receive an order (from the controller), decide how to prepare it using various ingredients (repositories, external APIs), and produce the final dish (result).

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public Optional<UserDto> findById(Long id) {
        return userRepository.findById(id).map(this::toDto);
    }

    public List<UserDto> findByRole(String role) {
        return userRepository.findByRole(Role.valueOf(role.toUpperCase()))
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Transactional  // Ensures DB operations succeed together or all roll back
    public UserDto createUser(CreateUserRequest request) {
        // Business rule: email must be unique
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already in use: " + request.getEmail());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // ← never store plain text!
        user.setRole(Role.USER);

        User saved = userRepository.save(user);

        // Send welcome email (another service)
        emailService.sendWelcomeEmail(saved.getEmail(), saved.getName());

        return toDto(saved);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }

    // Private helper: converts entity to DTO (never expose entity directly!)
    private UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
```

---

#### 🗄️ Repository Layer (`repository/`)

**What it does**: Handles all **database communication**. Spring Data JPA generates the SQL for you based on method names — you rarely write raw SQL.

**Analogy**: The **pantry and storage room**. It knows where everything is stored, retrieves ingredients (data) when needed, and puts new items away (saves data).

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA generates: SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // SELECT * FROM users WHERE role = ?
    List<User> findByRole(Role role);

    // SELECT COUNT(*) > 0 FROM users WHERE email = ?
    boolean existsByEmail(String email);

    // Custom JPQL query for complex lookups
    @Query("SELECT u FROM User u WHERE u.name LIKE %:keyword% OR u.email LIKE %:keyword%")
    List<User> searchByKeyword(@Param("keyword") String keyword);

    // Native SQL when needed
    @Query(value = "SELECT * FROM users WHERE created_at > :date", nativeQuery = true)
    List<User> findUsersCreatedAfter(@Param("date") LocalDateTime date);
}
```

---

#### 📦 Entity Layer (`entity/`)

**What it does**: Represents the **data model** — the shape of data as stored in the database. Each entity class maps to a database table.

**Analogy**: A **blueprint or form**. It describes every field (column) that a record (row) in the database can have.

```java
@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore  // Never serialize the password hash to JSON
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

---

#### ⚙️ Config Layer (`config/`)

**What it does**: Centralizes all application configuration — CORS rules, security settings, bean definitions, external service clients.

```java
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "https://myapp.azurewebsites.net"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

---

#### 🔒 Security Layer (`security/`)

**What it does**: Handles authentication (who are you?) and authorization (what are you allowed to do?). Typically implements JWT token validation.

```java
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

---

### Annotations Deep Dive

Spring Boot's "magic" comes from annotations — they tell the framework what to do with a class.

#### `@RestController`

Marks a class as an HTTP endpoint handler. It is a combination of `@Controller` and `@ResponseBody`, meaning every method's return value is automatically serialized to JSON.

```java
@RestController                     // ← This class handles HTTP requests
@RequestMapping("/api/products")    // ← All methods are under /api/products
public class ProductController {

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        return productService.findById(id); // ← Automatically converted to JSON
    }
}
```

---

#### `@Service`

Marks a class as a **business logic component**. Spring will automatically create and manage an instance of this class (a "bean").

```java
@Service  // ← Spring creates exactly one instance of this class
public class OrderService {

    // Business rule: an order can't be cancelled after it ships
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order " + orderId));

        if (order.getStatus() == OrderStatus.SHIPPED) {
            throw new InvalidOperationException("Cannot cancel a shipped order");
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}
```

---

#### `@Repository`

Marks an interface or class as a **data access component**. Spring Data JPA uses this to generate SQL automatically from method names.

```java
@Repository  // ← Enables Spring Data JPA magic and translates DB exceptions
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategoryAndPriceLessThan(String category, BigDecimal maxPrice);
    // Spring generates: SELECT * FROM products WHERE category = ? AND price < ?

    @Query("SELECT p FROM Product p WHERE p.stock < :threshold")
    List<Product> findLowStockProducts(@Param("threshold") int threshold);
}
```

---

#### `@Autowired` — Dependency Injection

`@Autowired` tells Spring to **inject a dependency** (another Spring-managed bean) into your class. You don't create the object yourself — Spring does it for you.

```java
// ❌ Without dependency injection (tightly coupled, hard to test)
public class OrderController {
    private OrderService service = new OrderService(); // ← You control the object
}

// ✅ With @Autowired field injection
@RestController
public class OrderController {
    @Autowired
    private OrderService service; // ← Spring injects the object
}

// ✅✅ Best practice: Constructor injection (easier to test, explicit dependencies)
@RestController
@RequiredArgsConstructor  // Lombok generates the constructor
public class OrderController {
    private final OrderService service; // ← Spring injects via constructor
}
```

**Why constructor injection is preferred:**
- Dependencies are explicit (visible in the constructor)
- The class can't be instantiated without its dependencies (fail-fast)
- Easier to write unit tests (just pass a mock)

---

#### `@GetMapping` and `@PostMapping`

Map HTTP methods to Java methods.

```java
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // GET /api/orders                        — all orders
    // GET /api/orders?status=PENDING         — filter by query param
    // GET /api/orders?status=PENDING&page=2  — paginated
    @GetMapping
    public Page<OrderDto> getOrders(
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return orderService.getOrders(status, PageRequest.of(page, size));
    }

    // GET /api/orders/99
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable Long id) {
        return orderService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/orders
    // Request body: { "productId": 5, "quantity": 2 }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto createOrder(
        @RequestBody @Valid CreateOrderRequest request,
        @AuthenticationPrincipal UserDetails currentUser  // current logged-in user
    ) {
        return orderService.createOrder(request, currentUser.getUsername());
    }
}
```

---

### Dependency Injection

**Dependency Injection (DI)** is a design pattern where an object receives its dependencies from the outside rather than creating them itself.

> 🔧 **Analogy**: Imagine you're a mechanic (`OrderService`). You need a wrench (`UserRepository`) to do your job. Instead of forging the wrench yourself, someone (the Spring container) hands it to you when you show up to work. You don't need to know how the wrench was made — you just use it.

**Benefits:**
- **Loose coupling** — `OrderService` doesn't need to know HOW `UserRepository` works, just what it can do
- **Testability** — you can inject a mock repository in tests, no real database needed
- **Flexibility** — swap implementations without changing the service

```java
// The interface (contract)
public interface PaymentGateway {
    PaymentResult charge(String cardToken, BigDecimal amount);
}

// Real implementation (used in production)
@Component
@Profile("!test")
public class StripePaymentGateway implements PaymentGateway {
    public PaymentResult charge(String cardToken, BigDecimal amount) {
        // calls Stripe API
    }
}

// Mock implementation (used in tests)
@Component
@Profile("test")
public class MockPaymentGateway implements PaymentGateway {
    public PaymentResult charge(String cardToken, BigDecimal amount) {
        return new PaymentResult("mock-tx-123", Status.SUCCESS);
    }
}

// Service only depends on the interface — Spring injects the right implementation
@Service
@RequiredArgsConstructor
public class CheckoutService {
    private final PaymentGateway paymentGateway; // ← injected by Spring
}
```

---

## 5. The Complete Request Flow

Here is what happens, step by step, when a user clicks a button in React that triggers an API call.

```
Browser
  │  User clicks "Add to Cart"
  ▼
React Component
  │  onClick handler fires
  │  Calls addToCart(productId, quantity)
  ▼
Axios HTTP Request
  │  POST /api/cart/items
  │  Headers: { Authorization: "******" }
  │  Body: { "productId": 5, "quantity": 2 }
  ▼
Spring Boot (JwtAuthFilter)
  │  Validates JWT token
  │  Sets SecurityContext with user identity
  ▼
CartController.addItem()
  │  @PostMapping("/api/cart/items")
  │  Deserializes JSON body → AddToCartRequest object
  │  Calls cartService.addItem(request, userId)
  ▼
CartService.addItem()
  │  Business logic: check stock, calculate price
  │  Calls productRepository.findById(productId)
  │  Calls cartRepository.save(cartItem)
  ▼
Database
  │  SQL: INSERT INTO cart_items ...
  │  Returns saved row
  ▼
CartService  (returns CartItemDto)
  ▼
CartController  (wraps in ResponseEntity, HTTP 201)
  ▼
Axios response
  │  res.data = { id: 101, productId: 5, quantity: 2, price: 29.99 }
  ▼
React Component
  │  Updates state: setCartItems([...cartItems, res.data])
  │  React re-renders the cart icon with new count
  ▼
User sees the cart updated instantly
```

> See [`examples/complete-flow-example.md`](examples/complete-flow-example.md) for the complete annotated code for every step.

---

## 6. JWT Authentication

**JWT (JSON Web Token)** is the standard way to authenticate REST API calls. After login, the server issues a signed token. The client includes this token in every subsequent request.

```
Login Flow:
────────────────────────────────────────────────────

React         Spring Boot        Database
  │                 │                │
  │  POST /login    │                │
  │  {email,pass} ──►                │
  │                 │  SELECT user   │
  │                 │ ──────────────►│
  │                 │◄──────────────-│
  │                 │  Verify password hash
  │                 │  Generate JWT (signed with secret)
  │◄─────────────── │
  │  { token: "eyJ..." }
  │
  │  Store token in localStorage
  │

Subsequent Request Flow:
────────────────────────────────────────────────────

React         JwtFilter          Controller
  │                 │                │
  │  GET /api/me    │                │
  │  Authorization: ****** ──►│
  │                 │  Decode JWT    │
  │                 │  Verify signature
  │                 │  Extract username
  │                 │  Load user from DB
  │                 │  Set SecurityContext
  │                 │ ──────────────►│
  │                 │                │  Process request
  │◄───────────────────────────────── │
  │  { id: 42, name: "Alice" }
```

> See [`examples/jwt-authentication-flow.md`](examples/jwt-authentication-flow.md) for the complete JWT implementation.

---

## 7. Docker – Packaging the Application

**Docker** packages your application and all its dependencies into a portable **container** — an isolated unit that runs identically on any machine.

> 📦 **Analogy**: A Docker container is like a shipping container. It contains everything needed (code, runtime, libraries, config), and it runs the same way whether it's on your laptop, a test server, or production in Azure.

### Key Concepts

| Concept        | Description                                                            |
|----------------|------------------------------------------------------------------------|
| **Image**      | A read-only blueprint (like a class in OOP)                            |
| **Container**  | A running instance of an image (like an object)                        |
| **Dockerfile** | Instructions to build an image                                         |
| **Registry**   | A storage location for images (Docker Hub, Azure Container Registry)   |

### Spring Boot Dockerfile

```dockerfile
# Stage 1: Build the JAR
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN ./mvnw package -DskipTests

# Stage 2: Run the JAR (smaller image)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### React Dockerfile

```dockerfile
# Stage 1: Build React app
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Stage 2: Serve with Nginx
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

### Docker Compose (local development)

```yaml
version: '3.8'
services:
  frontend:
    build: ./frontend
    ports:
      - "3000:80"
    depends_on:
      - backend

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:sqlserver://db:1433;databaseName=myapp
      SPRING_DATASOURCE_USERNAME: sa
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
    depends_on:
      - db

  db:
    image: mcr.microsoft.com/mssql/server:2022-latest
    environment:
      SA_PASSWORD: ${DB_PASSWORD}
      ACCEPT_EULA: Y
    ports:
      - "1433:1433"
    volumes:
      - db-data:/var/opt/mssql

volumes:
  db-data:
```

---

## 8. Kubernetes – Running at Scale

**Kubernetes (K8s)** is a system for automating the deployment, scaling, and management of containerized applications.

> 🚢 **Analogy**: If Docker is a shipping container, Kubernetes is the port authority — it decides how many containers to run, where to put them, restarts them if they break, and routes traffic to them.

### Core Concepts

| Concept        | Description                                                                       |
|----------------|-----------------------------------------------------------------------------------|
| **Pod**        | The smallest deployable unit. Usually one container. Has its own IP.              |
| **Deployment** | Declares the desired state (how many pod replicas, which image version).          |
| **Service**    | A stable network endpoint (DNS name + IP) for a group of pods.                   |
| **Ingress**    | Routes external HTTP traffic into the cluster based on host/path rules.           |
| **ConfigMap**  | Stores non-secret configuration data (environment variables, config files).       |
| **Secret**     | Stores sensitive data (passwords, API keys) encoded in base64.                    |
| **Namespace**  | Virtual cluster to isolate resources by team/environment.                         |

### Pod

```yaml
# pods are usually created by Deployments, not directly
apiVersion: v1
kind: Pod
metadata:
  name: backend-pod
  labels:
    app: backend
spec:
  containers:
    - name: backend
      image: myregistry.azurecr.io/backend:1.0.0
      ports:
        - containerPort: 8080
      resources:
        requests:
          memory: "256Mi"
          cpu: "250m"
        limits:
          memory: "512Mi"
          cpu: "500m"
```

### Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: backend-deployment
  namespace: production
spec:
  replicas: 3                    # Run 3 identical pods
  selector:
    matchLabels:
      app: backend
  template:
    metadata:
      labels:
        app: backend
    spec:
      containers:
        - name: backend
          image: myregistry.azurecr.io/backend:1.0.0
          ports:
            - containerPort: 8080
          env:
            - name: DB_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: db-secret
                  key: password
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 30
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            periodSeconds: 30
```

### Service

```yaml
apiVersion: v1
kind: Service
metadata:
  name: backend-service
  namespace: production
spec:
  type: ClusterIP          # Internal only (Ingress routes external traffic)
  selector:
    app: backend           # Routes to pods with label app=backend
  ports:
    - protocol: TCP
      port: 80             # The port the service exposes
      targetPort: 8080     # The port the pod listens on
```

### Ingress

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: app-ingress
  namespace: production
  annotations:
    kubernetes.io/ingress.class: azure/application-gateway
    appgw.ingress.kubernetes.io/ssl-redirect: "true"
spec:
  rules:
    - host: api.myapp.com
      http:
        paths:
          - path: /api/
            pathType: Prefix
            backend:
              service:
                name: backend-service
                port:
                  number: 80
    - host: myapp.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: frontend-service
                port:
                  number: 80
```

---

## 9. Azure – Cloud Hosting & Services

Azure is Microsoft's cloud platform. It provides managed infrastructure so you don't have to own physical servers.

### Architecture on Azure

```
Internet Users
      │
      ▼
Azure Front Door
  ─ Global CDN and load balancing
  ─ Web Application Firewall (WAF)
  ─ SSL/TLS termination
  ─ Routes to nearest Azure region
      │
      ▼
AKS (Azure Kubernetes Service)
  ─ Managed Kubernetes — Azure handles the control plane
  ─ Your pods run in VM-backed node pools
  ─ Integrates with Azure AD, Azure Monitor, ACR
      │
      ├────────────────────────────────────────
      │                                        │
      ▼                                        ▼
  Frontend Pods                         Backend Pods
  (React served by Nginx)            (Spring Boot)
                                             │
                      ┌──────────────────────┼──────────────────────┐
                      │                      │                      │
                      ▼                      ▼                      ▼
                Azure SQL DB           Cosmos DB              Azure Blob
               (relational data)   (document/NoSQL data)   (files, images)
```

### Key Azure Services

| Service                        | What it does                                              |
|-------------------------------|-----------------------------------------------------------|
| **Azure Kubernetes Service**   | Managed Kubernetes cluster                                |
| **Azure Container Registry**   | Private Docker image registry                             |
| **Azure Front Door**           | Global CDN, load balancer, WAF                            |
| **Azure Application Gateway**  | Regional load balancer, Ingress controller for AKS        |
| **Azure SQL Database**         | Managed SQL Server                                        |
| **Azure Cosmos DB**            | Globally distributed NoSQL database                       |
| **Azure Blob Storage**         | Object storage (images, files, backups)                   |
| **Azure Key Vault**            | Secrets management (connection strings, API keys)         |
| **Azure Active Directory**     | Identity and access management                            |
| **Azure Monitor**              | Logs, metrics, alerts, Application Insights               |

### CI/CD Pipeline on Azure

```
Developer pushes code
        │
        ▼
GitHub Actions / Azure DevOps
        │
        ├── Run tests (JUnit, Jest)
        ├── Build Docker images
        ├── Push images to Azure Container Registry
        └── Deploy to AKS
              │
              ▼
        kubectl apply -f k8s/
        (Rolling update — zero downtime)
```

---

## 10. Putting It All Together

Here is the full lifecycle of a request in a production system:

```
1. User opens https://myapp.com in browser
   → Azure Front Door serves React app from CDN

2. React app loads, user logs in
   → POST /api/auth/login to Spring Boot
   → Spring Boot validates password, returns JWT
   → React stores JWT in localStorage

3. User clicks "View Orders"
   → React calls GET /api/orders with JWT in Authorization header
   → Request hits Azure Front Door → AKS Ingress → backend Service → one of 3 backend Pods

4. Spring Boot processes the request:
   → JwtAuthFilter validates JWT, sets user context
   → OrderController.getOrders() called
   → OrderService applies business logic, filters, pagination
   → OrderRepository fetches data from Azure SQL

5. Response returns:
   → JSON data flows back through the same chain
   → React updates state, re-renders the orders table

6. If pod crashes:
   → Kubernetes detects it via liveness probe
   → Automatically restarts pod
   → Service continues routing to healthy pods
   → User experiences no downtime
```

---

## Glossary

| Term                    | Definition                                                                                                  |
|------------------------|-------------------------------------------------------------------------------------------------------------|
| **React**              | A JavaScript library for building user interfaces from reusable components.                                 |
| **Component**          | A self-contained piece of UI with its own logic and rendering.                                              |
| **Props**              | Read-only data passed from a parent component to a child component.                                         |
| **State**              | Mutable data that lives inside a component; changes trigger re-renders.                                     |
| **Hooks**              | React functions (prefixed with `use`) that add state and lifecycle features to functional components.        |
| **REST API**           | An architectural style for web services using HTTP methods and JSON.                                        |
| **JWT**                | A compact, signed token used to authenticate API requests without server-side sessions.                     |
| **Spring Boot**        | A Java framework for building production-ready backend applications with minimal configuration.              |
| **Dependency Injection**| A design pattern where objects receive their dependencies from an external source (Spring container).       |
| **@RestController**    | Spring annotation marking a class as an HTTP request handler that returns JSON.                             |
| **@Service**           | Spring annotation marking a class as a business logic component.                                            |
| **@Repository**        | Spring annotation marking an interface as a database access component.                                      |
| **@Autowired**         | Spring annotation to inject a dependency (bean) automatically.                                              |
| **Docker**             | A platform for packaging applications into portable containers.                                             |
| **Container**          | A lightweight, isolated process that includes application code and all its dependencies.                    |
| **Kubernetes (K8s)**   | An orchestration system for deploying, scaling, and managing containers.                                    |
| **Pod**                | The smallest Kubernetes unit; typically one container with its own IP address.                              |
| **Deployment**         | A Kubernetes resource declaring how many pod replicas to run and with which image.                          |
| **Service (K8s)**      | A stable DNS/IP endpoint that load-balances traffic across matching pods.                                   |
| **Ingress**            | A Kubernetes resource routing external HTTP/HTTPS traffic into the cluster.                                 |
| **AKS**                | Azure Kubernetes Service — Microsoft's managed Kubernetes offering.                                         |
| **Azure Front Door**   | Azure's global CDN and load balancer with WAF capabilities.                                                 |

---

## Further Reading

- [`examples/spring-boot-project-structure.md`](examples/spring-boot-project-structure.md) — Complete layer-by-layer project breakdown
- [`examples/react-complete-example.jsx`](examples/react-complete-example.jsx) — Full React component with API integration
- [`examples/spring-boot-complete-example.java`](examples/spring-boot-complete-example.java) — Complete Spring Boot example
- [`examples/complete-flow-example.md`](examples/complete-flow-example.md) — Annotated step-by-step request flow
- [`examples/jwt-authentication-flow.md`](examples/jwt-authentication-flow.md) — JWT authentication implementation guide
- [`examples/react-hooks-guide.md`](examples/react-hooks-guide.md) — Comprehensive React Hooks reference
- [`troubleshooting-guide.md`](troubleshooting-guide.md) — Common issues and how to fix them
