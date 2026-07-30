# Modern Web Application Architecture: A Comprehensive Guide

<<<<<<< HEAD
> A deep-dive article explaining how modern web applications are built, from the React UI all the way to Azure-hosted microservices — with real code examples, diagrams, and best practices.
=======
> A practical, developer-friendly guide to understanding how modern web applications are built, deployed, and scaled — from React frontends to Spring Boot microservices running on Azure Kubernetes Service.
>>>>>>> origin/main

---

## Table of Contents

<<<<<<< HEAD
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
=======
1. [Introduction](#introduction)
2. [The Big Picture: Basic Architecture](#the-big-picture-basic-architecture)
3. [Layer-by-Layer Breakdown](#layer-by-layer-breakdown)
   - [React (Frontend UI)](#react-frontend-ui)
   - [REST API](#rest-api)
   - [JWT Authentication](#jwt-authentication)
   - [Spring Boot Microservices](#spring-boot-microservices)
   - [Dependency Injection](#dependency-injection)
   - [Business Logic Layer](#business-logic-layer)
   - [Database Layer](#database-layer)
4. [Containerization with Docker](#containerization-with-docker)
5. [Orchestration with Kubernetes](#orchestration-with-kubernetes)
   - [Pod](#pod)
   - [Deployment](#deployment)
   - [Service](#service)
   - [Ingress](#ingress)
6. [Azure Cloud Implementation](#azure-cloud-implementation)
7. [Putting It All Together](#putting-it-all-together)
8. [Glossary](#glossary)
9. [Further Reading](#further-reading)

---

## Introduction

Building a modern web application is like constructing a skyscraper: the visible part (the UI) is just the surface. Beneath it lies a sophisticated infrastructure of services, data stores, security layers, and cloud resources that keep everything running at scale.

This article walks you through each component of a modern web application — starting from the user's browser all the way to the database — and explains how everything connects. By the end, you'll be able to confidently talk about:

- How a React frontend communicates with a backend API
- How Spring Boot handles business logic in microservices
- How JWT secures communication between services
- How Docker packages applications for consistent deployment
- How Kubernetes orchestrates containers in production
- How Azure cloud services tie everything together at enterprise scale

**Real-world analogy:** Think of a restaurant. The customer (user) sees the dining room (React UI). The waiter takes orders and brings food (REST API). The kitchen (Spring Boot) prepares the food using recipes (business logic). The pantry and cold storage (databases) store all the ingredients. Docker is the standardized food container that makes sure the kitchen can operate anywhere. Kubernetes is the restaurant manager who decides how many chefs are needed at any moment. Azure is the entire building, utilities, and infrastructure.

---

## The Big Picture: Basic Architecture

Here is a high-level view of how the layers of a modern web application stack together:

```
┌─────────────────────────────────────────────────────────────────┐
│                        USER'S BROWSER                           │
│                                                                  │
│   ┌──────────────────────────────────────────────────────────┐  │
│   │                   React UI (Frontend)                     │  │
│   │   Components · State · Routing · HTTP Client             │  │
│   └─────────────────────────┬────────────────────────────────┘  │
└─────────────────────────────┼───────────────────────────────────┘
                              │  HTTPS Requests (JWT in header)
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        REST API LAYER                            │
│              (HTTP endpoints: GET, POST, PUT, DELETE)            │
│                     + JWT Authentication                         │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                  Spring Boot Microservices                        │
│                                                                  │
│   ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐  │
│   │ User Service │  │ Order Service│  │  Product Service      │  │
│   └──────┬───────┘  └──────┬───────┘  └──────────┬───────────┘  │
│          └─────────────────┴──────────────────────┘             │
│                             │                                    │
│              ┌──────────────▼──────────────┐                    │
│              │       Business Logic         │                    │
│              │  Validation · Rules · Calc   │                    │
│              └──────────────┬──────────────┘                    │
└─────────────────────────────┼───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        DATABASE LAYER                            │
│                                                                  │
│   ┌───────────────┐  ┌────────────────┐  ┌──────────────────┐  │
│   │  Relational   │  │   Document DB  │  │   Object/Blob    │  │
│   │  (SQL/MySQL)  │  │  (MongoDB etc) │  │   Storage        │  │
│   └───────────────┘  └────────────────┘  └──────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

Each layer has a clear responsibility, communicates with the adjacent layer through well-defined interfaces, and can be developed, tested, and scaled independently.

---

## Layer-by-Layer Breakdown

### React (Frontend UI)

**What it is:** React is a JavaScript library for building user interfaces. Developed by Meta (Facebook), it lets you build UI components that automatically update when your application's data changes.

**Key concepts:**
- **Components:** Reusable building blocks of your UI (buttons, forms, tables, entire pages)
- **State:** Data that, when changed, causes the component to re-render
- **Props:** Data passed from a parent component to a child component
- **Virtual DOM:** React's optimized way of updating only the parts of the webpage that actually changed

**How it fits in the architecture:**
React is the face of your application. It runs entirely in the user's browser, displays data, captures user input, and communicates with the backend through HTTP requests.

```
Browser
  │
  └── React App
        ├── App.js (root component)
        ├── pages/
        │     ├── LoginPage.jsx
        │     ├── Dashboard.jsx
        │     └── ProductList.jsx
        ├── components/
        │     ├── Navbar.jsx
        │     ├── ProductCard.jsx
        │     └── CartButton.jsx
        └── services/
              └── api.js  ← HTTP calls to REST API
```

**Simple React component example:**

```jsx
// ProductCard.jsx
import React, { useState } from 'react';

function ProductCard({ product }) {
  const [inCart, setInCart] = useState(false);

  const handleAddToCart = () => {
    setInCart(true);
    // Calls the REST API to add product to cart
    fetch('/api/cart', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `******'jwt_token')}`
      },
      body: JSON.stringify({ productId: product.id })
    });
  };

  return (
    <div className="product-card">
      <h3>{product.name}</h3>
      <p>${product.price}</p>
      <button onClick={handleAddToCart} disabled={inCart}>
        {inCart ? 'Added!' : 'Add to Cart'}
      </button>
>>>>>>> origin/main
    </div>
  );
}

<<<<<<< HEAD
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
=======
export default ProductCard;
```

> **See also:** [`examples/react-component/ProductCard.jsx`](examples/react-component/ProductCard.jsx)

---

### REST API

**What it is:** REST (Representational State Transfer) is an architectural style for designing networked applications. A REST API is a set of HTTP endpoints that your frontend calls to perform operations on data.

**The four main HTTP methods (CRUD operations):**

| HTTP Method | Operation | Example URL           | What it does                  |
|-------------|-----------|----------------------|-------------------------------|
| `GET`       | Read      | `GET /api/products`  | Fetch all products            |
| `POST`      | Create    | `POST /api/products` | Create a new product          |
| `PUT`       | Update    | `PUT /api/products/1`| Update product with ID 1      |
| `DELETE`    | Delete    | `DELETE /api/products/1` | Delete product with ID 1  |

**How a REST call flows:**

```
React Component
      │
      │  HTTP Request: GET /api/products
      │  Headers: { Authorization: "******" }
      │
      ▼
Spring Boot Controller (@RestController)
      │
      │  Validates JWT token
      │  Calls ProductService
      │
      ▼
ProductService (Business Logic)
      │
      │  Queries database
      │  Applies business rules
      │
      ▼
Database (returns rows)
      │
      ▼  HTTP Response: 200 OK
         Body: [{"id":1,"name":"Laptop","price":999.99}, ...]
      │
      ▼
React Component (renders data on screen)
```

**REST API design principles:**
- **Stateless:** Each request contains all the information needed; the server holds no session state
- **Uniform interface:** Consistent URL patterns and HTTP methods across all resources
- **Resource-based URLs:** `/api/products` not `/api/getProducts`
- **Standard status codes:** `200 OK`, `201 Created`, `400 Bad Request`, `401 Unauthorized`, `404 Not Found`, `500 Internal Server Error`

---

### JWT Authentication

**What it is:** JWT (JSON Web Token) is a compact, self-contained way to securely transmit information between parties as a digitally signed token. It enables stateless authentication — the server does not need to store sessions.

**Structure of a JWT:**

```
******    ← Header (Base64)
.
eyJzdWIiOiJ1c2VyMTIzIiwibmFtZSI6IkFsaWNlIiwicm9sZSI6IkFETUlOIiwiZXhwIjoxNzAwMDAwMDAwfQ==  ← Payload (Base64)
.
SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c    ← Signature (HMAC-SHA256)
```

**Decoded payload example:**

```json
{
  "sub": "user123",
  "name": "Alice",
  "role": "ADMIN",
  "exp": 1700000000
}
```

**JWT Authentication flow:**

```
1. User logs in (POST /api/auth/login)
   Body: { "username": "alice", "password": "secret" }

2. Server validates credentials → generates JWT
   Response: { "token": "eyJhbG..." }

3. React stores token (localStorage or httpOnly cookie)

4. Every subsequent request includes the token:
   GET /api/products
   Authorization: ******

5. Spring Boot filter validates the token:
   ✓ Signature valid?
   ✓ Token not expired?
   ✓ User has required role?

6. If valid → process request → return data
   If invalid → return 401 Unauthorized
```

**Why JWT?**
- **Stateless:** No server-side session storage needed → scales horizontally
- **Self-contained:** User identity and roles embedded in the token
- **Cross-service:** A single token works across multiple microservices
- **Expiry:** Tokens expire, limiting damage from stolen tokens

---

### Spring Boot Microservices

**What it is:** Spring Boot is a Java framework that makes it easy to create stand-alone, production-ready applications. In a microservices architecture, you run multiple small Spring Boot applications, each responsible for one business domain.

**Monolith vs Microservices:**

```
MONOLITH (one big application)        MICROSERVICES (many small services)
┌───────────────────────────┐         ┌──────────┐ ┌──────────┐ ┌──────────┐
│                           │         │   User   │ │  Order   │ │ Product  │
│  Users + Orders +         │   vs    │ Service  │ │ Service  │ │ Service  │
│  Products + Payments      │         │  :8081   │ │  :8082   │ │  :8083   │
│  (all in one app)         │         └──────────┘ └──────────┘ └──────────┘
└───────────────────────────┘
```

**A Spring Boot REST Controller:**

```java
// ProductController.java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    // Constructor injection (Dependency Injection)
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.findAll();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return productService.findById(id)
>>>>>>> origin/main
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

<<<<<<< HEAD
    // POST /api/orders
    // Request body: { "productId": 5, "quantity": 2 }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto createOrder(
        @RequestBody @Valid CreateOrderRequest request,
        @AuthenticationPrincipal UserDetails currentUser  // current logged-in user
    ) {
        return orderService.createOrder(request, currentUser.getUsername());
=======
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> createProduct(@RequestBody @Valid ProductRequest request) {
        Product saved = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
>>>>>>> origin/main
    }
}
```

<<<<<<< HEAD
=======
**Advantages of microservices:**
- Each service can be deployed independently
- Different services can use different technologies (polyglot)
- Failure in one service doesn't bring down the whole application
- Teams can own and develop services independently

> **See also:** [`examples/spring-boot-controller/ProductController.java`](examples/spring-boot-controller/ProductController.java)

>>>>>>> origin/main
---

### Dependency Injection

<<<<<<< HEAD
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
=======
**What it is:** Dependency Injection (DI) is a design pattern where an object receives its dependencies from an external source rather than creating them itself. Spring Boot's IoC (Inversion of Control) container manages this automatically.

**Without DI (tightly coupled — bad):**

```java
public class ProductController {
    // Creates its own dependency — hard to test, hard to swap
    private ProductService productService = new ProductService(new ProductRepository());
}
```

**With DI (loosely coupled — good):**

```java
@RestController
public class ProductController {

    private final ProductService productService;

    // Spring injects the dependency automatically
    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
}
```

**The DI chain in Spring Boot:**

```
@Repository          @Service           @RestController
ProductRepository ──▶ ProductService ──▶ ProductController
      ↑                    ↑                    ↑
      └────────────────────┴────────────────────┘
              Spring IoC Container manages all of this
```

**Spring annotations for DI:**

| Annotation       | Purpose                                                   |
|-----------------|-----------------------------------------------------------|
| `@Component`     | Generic Spring-managed bean                               |
| `@Service`       | Business logic layer                                      |
| `@Repository`    | Data access layer (also handles exceptions)               |
| `@RestController`| HTTP REST endpoint                                        |
| `@Autowired`     | Tell Spring to inject a dependency                        |
| `@Bean`          | Manually declare a bean in a configuration class          |

**Why DI matters:**
- Makes code easier to test (inject mock dependencies in unit tests)
- Decouples components — swap implementations without changing consumers
- Manages object lifecycle (singleton, prototype, request-scoped, etc.)

---

### Business Logic Layer

The business logic layer is where the real work happens. It sits between the API layer and the database, implementing the rules and calculations that make your application valuable.

```
┌─────────────────────────────────────────┐
│           Business Logic Layer           │
│                                          │
│  ┌──────────────────────────────────┐   │
│  │ OrderService                      │   │
│  │                                   │   │
│  │  + calculateTotal(items)          │   │
│  │  + applyDiscounts(order, user)    │   │
│  │  + validateInventory(items)       │   │
│  │  + processPayment(order)          │   │
│  │  + sendConfirmationEmail(order)   │   │
│  └──────────────────────────────────┘   │
│                                          │
│  Rules:                                  │
│  • Orders over $100 get 10% discount    │
│  • Out-of-stock items cannot be ordered │
│  • Users can only place 5 orders/day    │
└─────────────────────────────────────────┘
>>>>>>> origin/main
```

---

<<<<<<< HEAD
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
=======
### Database Layer

Modern applications often use multiple databases, each optimized for a specific type of data:

```
┌──────────────────────────────────────────────────────────┐
│                     Database Layer                        │
│                                                           │
│  ┌─────────────────┐  ┌──────────────────┐  ┌─────────┐  │
│  │  Relational DB  │  │   Document DB    │  │  Blob   │  │
│  │                 │  │                  │  │ Storage │  │
│  │  Azure SQL /    │  │  Azure Cosmos DB │  │  Azure  │  │
│  │  PostgreSQL     │  │  / MongoDB       │  │  Blobs  │  │
│  │                 │  │                  │  │         │  │
│  │ • Users         │  │ • Product catalog│  │ • Images│  │
│  │ • Orders        │  │ • Session data   │  │ • PDFs  │  │
│  │ • Payments      │  │ • Activity logs  │  │ • Videos│  │
│  └─────────────────┘  └──────────────────┘  └─────────┘  │
└──────────────────────────────────────────────────────────┘
```

| Database Type   | Use Case                        | Azure Service        |
|----------------|--------------------------------|----------------------|
| Relational (SQL)| Structured data with relations  | Azure SQL Database   |
| Document (NoSQL)| Flexible schema, JSON documents | Azure Cosmos DB      |
| Key-Value       | Caching, sessions               | Azure Cache for Redis|
| Blob Storage    | Files, images, videos           | Azure Blob Storage   |

---

## Containerization with Docker

**What is Docker?**

Docker is a platform for packaging your application and all its dependencies (Java runtime, configuration files, environment variables) into a single, portable unit called a **container**.

**The problem Docker solves — "it works on my machine":**

```
WITHOUT DOCKER                        WITH DOCKER
                                      
Developer's laptop:                   Container (same everywhere):
  Java 17, MySQL 8.0 ✓               ┌─────────────────────────┐
                                      │  Your Spring Boot App    │
Test server:                          │  + Java 17               │
  Java 11, MySQL 5.7 ✗ ← Breaks!    │  + MySQL client 8.0      │
                                      │  + Config files          │
Production:                           │  (identical everywhere)  │
  Java 21, MySQL 8.1 ✗ ← Breaks!    └─────────────────────────┘
```

**Key Docker concepts:**

```
Dockerfile          Docker Image        Docker Container
(recipe/blueprint) → (packaged app)   → (running instance)
     │                    │                    │
  Instructions        Read-only             Running
  to build the        snapshot of       application
  image               your app
```

**A Dockerfile for a Spring Boot application:**

```dockerfile
# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:17-jre-alpine
>>>>>>> origin/main
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

<<<<<<< HEAD
### React Dockerfile

```dockerfile
# Stage 1: Build React app
=======
**A Dockerfile for a React application:**

```dockerfile
# Stage 1: Build the React app
>>>>>>> origin/main
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

<<<<<<< HEAD
# Stage 2: Serve with Nginx
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
=======
# Stage 2: Serve with nginx
FROM nginx:alpine
COPY --from=builder /app/build /usr/share/nginx/html
>>>>>>> origin/main
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

<<<<<<< HEAD
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
=======
**Common Docker commands:**

```bash
# Build an image
docker build -t my-app:1.0 .

# Run a container
docker run -p 8080:8080 my-app:1.0

# List running containers
docker ps

# View logs
docker logs <container-id>

# Push to container registry
docker push myregistry.azurecr.io/my-app:1.0
>>>>>>> origin/main
```

---

<<<<<<< HEAD
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
=======
## Orchestration with Kubernetes

**What is Kubernetes?**

Kubernetes (often abbreviated as **K8s**) is an open-source container orchestration platform. If Docker is like shipping containers, Kubernetes is like the entire shipping port — it manages where containers go, how many run, and what happens when one breaks down.

**The Kubernetes cluster structure:**

```
┌─────────────────────────────────────────────────────────────┐
│                    Kubernetes Cluster                         │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐  │
│  │                    Control Plane                        │  │
│  │  API Server · Scheduler · Controller Manager · etcd   │  │
│  └─────────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌──────────────────────┐  ┌──────────────────────────────┐  │
│  │      Worker Node 1   │  │        Worker Node 2          │  │
│  │                      │  │                               │  │
│  │  ┌──────┐ ┌───────┐  │  │  ┌──────────┐ ┌──────────┐   │  │
│  │  │ Pod  │ │  Pod  │  │  │  │   Pod    │ │   Pod    │   │  │
│  │  │[App] │ │[App]  │  │  │  │  [App]   │ │  [App]   │   │  │
│  │  └──────┘ └───────┘  │  │  └──────────┘ └──────────┘   │  │
│  └──────────────────────┘  └──────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

### Pod

**What it is:** A Pod is the smallest deployable unit in Kubernetes. It wraps one or more containers that share the same network and storage.

**Analogy:** If a container is a shipping container, a Pod is a pallet that groups related containers together on the same truck.

```
┌─────────────────────────────────────────┐
│                   Pod                    │
│                                          │
│  ┌─────────────────────┐                │
│  │  Main Container     │                │
│  │  (Spring Boot App)  │  ← Port 8080   │
│  │  image: my-app:1.0  │                │
│  └─────────────────────┘                │
│                                          │
│  ┌─────────────────────┐                │
│  │  Sidecar Container  │                │
│  │  (Log shipper)      │  ← Optional    │
│  │  image: fluentd     │                │
│  └─────────────────────┘                │
│                                          │
│  Shared: IP address, localhost, volumes  │
│  Pod IP: 10.0.0.15                       │
└─────────────────────────────────────────┘
```

**Important facts about Pods:**
- Pods are **ephemeral** — they can be killed and restarted at any time
- Never rely on a Pod's IP address (it changes when a Pod restarts)
- Use **Services** (see below) to expose Pods reliably

---

### Deployment

**What it is:** A Deployment manages a set of identical Pods. It ensures the right number of Pods are running, handles rolling updates, and enables rollbacks.

**Analogy:** If Pods are individual chefs, a Deployment is the kitchen manager who ensures there are always enough chefs on duty, hires replacements when one calls in sick, and coordinates when chefs need to adopt a new recipe.

```
Deployment: product-service
│
├── Desired state: 3 replicas of product-service:1.2
│
├── Pod 1: product-service-7d8f9b-abc  ← Running ✓
├── Pod 2: product-service-7d8f9b-def  ← Running ✓
└── Pod 3: product-service-7d8f9b-ghi  ← Running ✓

If Pod 2 crashes:
└── Kubernetes detects: actual=2, desired=3
└── Kubernetes starts a new Pod automatically ✓
```

**Rolling Update (zero-downtime deployment):**

```
Before update:  Pod[v1] Pod[v1] Pod[v1]
                  ↓
Step 1:         Pod[v1] Pod[v1] Pod[v2]  ← Start new
Step 2:         Pod[v1] Pod[v2] Pod[v2]  ← Replace one old
Step 3:         Pod[v2] Pod[v2] Pod[v2]  ← All updated ✓
```

---

### Service

**What it is:** A Kubernetes Service is a stable network endpoint that provides a consistent way to access a group of Pods. Services solve the problem that Pod IPs are temporary and change frequently.

**Analogy:** A Service is like the restaurant's phone number — it never changes, even if the chefs (Pods) behind the scenes are constantly changing.

```
External request
      │
      ▼
┌─────────────────────────────────────────┐
│         Service: product-service         │
│         ClusterIP: 10.96.0.50           │
│         Port: 80 → 8080                 │
└──────────────┬──────────────────────────┘
               │  Load balances between
    ┌──────────┼──────────┐
    ▼          ▼          ▼
 Pod[v2]    Pod[v2]    Pod[v2]
 10.0.0.1   10.0.0.2   10.0.0.3
```

**Service types:**

| Type            | Visibility        | Use Case                                      |
|----------------|-------------------|-----------------------------------------------|
| `ClusterIP`     | Internal only     | Service-to-service communication              |
| `NodePort`      | External via port | Development/testing                           |
| `LoadBalancer`  | External via LB   | Production (creates Azure Load Balancer)      |
| `ExternalName`  | DNS alias         | Pointing to external services                 |

---

### Ingress

**What it is:** An Ingress is a Kubernetes resource that manages external HTTP/HTTPS access to services inside the cluster. It acts as a smart router — directing traffic to the right service based on the URL path or hostname.

**Analogy:** An Ingress is like a hotel front desk — it decides which department (service) to send each guest (request) to based on their request.

```
Internet
    │
    │  https://myapp.com
    ▼
┌────────────────────────────────────────────────────────┐
│                    Ingress Controller                    │
│              (e.g., nginx-ingress or AGIC)               │
│                                                          │
│  Rules:                                                  │
│  myapp.com/        → frontend-service:80                │
│  myapp.com/api/*   → backend-service:8080               │
│  myapp.com/admin/* → admin-service:8090                 │
└──────────┬─────────────────┬───────────────────────────┘
           │                 │                    │
           ▼                 ▼                    ▼
    frontend-service   backend-service      admin-service
           │                 │
     ┌─────┴────┐      ┌─────┴────┐
     Pod[React] │      Pod[Boot]  │
                │               
         Pod[React]     Pod[Boot]
```

**Key Ingress features:**
- **Path-based routing:** `/api/*` goes to the API service, `/` goes to the frontend
- **Host-based routing:** `api.myapp.com` vs `admin.myapp.com`
- **TLS/SSL termination:** Handles HTTPS certificates, backend sees plain HTTP
- **Load balancing:** Distributes traffic across multiple Pod instances

---

## Azure Cloud Implementation

Now let's see how everything above maps to actual Azure services in a production deployment.

### Full Azure Architecture

```
                              INTERNET
                                  │
                            HTTPS Requests
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    Azure Front Door / App Gateway                    │
│                                                                      │
│  • Global load balancing (Front Door)                               │
│  • WAF (Web Application Firewall) protection                        │
│  • SSL/TLS termination                                               │
│  • DDoS protection                                                   │
│  • CDN for static assets (React build files)                        │
└─────────────────────────────────────┬───────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    Azure Kubernetes Service (AKS)                    │
│                                                                      │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │                         Ingress Layer                        │    │
│  │  Application Gateway Ingress Controller (AGIC) / nginx     │    │
│  └──────────────────┬──────────────────┬───────────────────────┘    │
│                     │                  │                             │
│          ┌──────────▼──────┐  ┌────────▼────────────┐              │
│          │  frontend-svc   │  │    api-gateway-svc   │              │
│          └──────────┬──────┘  └────────┬────────────┘              │
│                     │                  │                             │
│          ┌──────────▼──────┐           │                             │
│          │  React Pods     │  ┌────────▼──────────────────────────┐ │
│          │  (nginx static) │  │       Spring Boot Microservices    │ │
│          └─────────────────┘  │                                    │ │
│                               │  ┌────────────┐ ┌────────────┐   │ │
│                               │  │ user-svc   │ │ order-svc  │   │ │
│                               │  │ Pods (x3)  │ │ Pods (x3)  │   │ │
│                               │  └────────────┘ └────────────┘   │ │
│                               │  ┌────────────┐ ┌────────────┐   │ │
│                               │  │product-svc │ │payment-svc │   │ │
│                               │  │ Pods (x2)  │ │ Pods (x2)  │   │ │
│                               │  └────────────┘ └────────────┘   │ │
│                               └───────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
                                      │
              ┌───────────────────────┼───────────────────────┐
              ▼                       ▼                        ▼
┌─────────────────────┐  ┌─────────────────────┐  ┌───────────────────┐
│   Azure SQL Database │  │  Azure Cosmos DB    │  │ Azure Blob Storage│
│                      │  │                     │  │                   │
│ • Users table        │  │ • Product catalog   │  │ • Product images  │
│ • Orders table       │  │ • Activity logs     │  │ • Order documents │
│ • Payments table     │  │ • Sessions          │  │ • Backups         │
│ • Geo-redundant      │  │ • Global replication│  │ • CDN origin      │
└─────────────────────┘  └─────────────────────┘  └───────────────────┘
```

### Azure Services Reference

| Azure Service              | Role in Architecture                                           |
|---------------------------|---------------------------------------------------------------|
| **Azure Front Door**       | Global HTTP load balancer, WAF, CDN, SSL termination          |
| **Azure App Gateway**      | Regional load balancer, WAF, Ingress controller for AKS       |
| **AKS (Azure Kubernetes)** | Managed Kubernetes cluster — runs all your containers         |
| **Azure Container Registry**| Private Docker image registry                                |
| **Azure SQL Database**     | Managed relational database (SQL Server)                      |
| **Azure Cosmos DB**        | Globally distributed NoSQL database (multi-model)             |
| **Azure Blob Storage**     | Object storage for files, images, backups                     |
| **Azure Key Vault**        | Stores secrets, certificates, JWT signing keys                |
| **Azure Monitor / Log Analytics** | Centralized logging, metrics, and alerting            |
| **Azure Active Directory** | Identity and access management (can issue JWTs via OAuth2)    |

### Request Flow on Azure (Step-by-Step)

```
1. User opens browser → https://myapp.com/products

2. DNS resolves → Azure Front Door

3. Azure Front Door:
   a. Terminates SSL
   b. Checks WAF rules (block attacks)
   c. Routes to nearest AKS cluster (global traffic distribution)

4. Traffic enters AKS via Application Gateway Ingress Controller (AGIC)

5. Ingress rules route:
   /          → React frontend pods (served by nginx)
   /api/*     → Backend API pods

6. React app loads in browser
   → Makes API call: GET https://myapp.com/api/products
   → With JWT in Authorization header

7. Ingress routes /api/products → product-service (ClusterIP)

8. Service load-balances → one of 3 product-service Pods

9. Spring Boot Pod:
   a. JWT filter validates token (key from Azure Key Vault)
   b. Controller method called
   c. Service fetches data from Azure Cosmos DB
   d. Returns JSON response

10. Response travels back through Service → Ingress → Front Door → User
>>>>>>> origin/main
```

---

<<<<<<< HEAD
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
=======
## Putting It All Together

Here is the complete journey of a single user request through the entire system:

```
USER ACTION: Alice clicks "View Products" on the website

Step 1: Browser
  React component detects click → calls productService.getAll()

Step 2: HTTP Request leaves browser
  GET https://myapp.com/api/products
  Headers: { Authorization: "******" }

Step 3: Azure Front Door
  • Validates request (WAF rules)
  • Routes to AKS cluster in West Europe (Alice is in London)

Step 4: AKS Ingress (AGIC)
  • Matches rule: /api/* → api-service
  • Forwards to Kubernetes Service

Step 5: Kubernetes Service (api-service)
  • Load balances across 3 healthy product-service Pods
  • Chooses Pod 2 (round-robin)

Step 6: Spring Boot Pod (product-service)
  • JWT filter runs: validates token signature, checks expiry
  • Extracts user role: CUSTOMER
  • ProductController.getAllProducts() called
  • ProductService.findAll() called

Step 7: Business Logic
  • Apply pricing rules (Alice has loyalty discount)
  • Filter out products not available in UK
  • Sort by relevance

Step 8: Database
  • Query Azure Cosmos DB: SELECT * FROM products WHERE region='UK'
  • Returns 150 product documents in 12ms

Step 9: Response
  HTTP 200 OK
  Body: [{"id":1,"name":"Laptop",...}, ...]

Step 10: React receives response
  • Updates component state
  • React re-renders ProductList component
  • 150 product cards appear on screen

Total time: ~180ms
>>>>>>> origin/main
```

---

## Glossary

<<<<<<< HEAD
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
=======
| Term | Definition |
|------|------------|
| **AKS** | Azure Kubernetes Service — Microsoft Azure's managed Kubernetes offering |
| **API** | Application Programming Interface — a contract defining how software components communicate |
| **Business Logic** | The code that implements real-world rules and calculations for your domain |
| **ClusterIP** | A Kubernetes Service type that exposes the service on a cluster-internal IP |
| **Container** | A lightweight, portable unit that packages code and all its dependencies |
| **Controller** | In Spring Boot, a class that handles HTTP requests and returns responses |
| **CRUD** | Create, Read, Update, Delete — the four basic database operations |
| **Dependency Injection** | A pattern where dependencies are provided externally rather than created internally |
| **Deployment** | A Kubernetes resource that manages a set of identical Pods and their lifecycle |
| **Docker** | A platform for building, shipping, and running containers |
| **Dockerfile** | A text file with instructions for building a Docker image |
| **DI** | Dependency Injection — see above |
| **Front Door** | Azure's global HTTP load balancer and CDN service |
| **Horizontal Scaling** | Adding more instances of a service (more Pods) instead of bigger hardware |
| **HTTP** | HyperText Transfer Protocol — the foundation of data communication on the web |
| **Ingress** | A Kubernetes resource that manages external HTTP access to cluster services |
| **IoC** | Inversion of Control — the principle underlying Dependency Injection in Spring |
| **JWT** | JSON Web Token — a compact, signed token used for stateless authentication |
| **K8s** | Common abbreviation for Kubernetes (8 letters between K and s) |
| **Kubernetes** | An open-source system for automating deployment, scaling, and management of containers |
| **Load Balancer** | Distributes incoming network traffic across multiple servers/pods |
| **Microservice** | A small, independently deployable service focused on a single business capability |
| **Node** | A worker machine in a Kubernetes cluster (a VM in Azure terms) |
| **NoSQL** | Non-relational databases designed for flexible schemas or high scale |
| **Pod** | The smallest deployable unit in Kubernetes — wraps one or more containers |
| **React** | A JavaScript library for building interactive user interfaces |
| **Replica** | A copy of a Pod, enabling horizontal scaling and high availability |
| **Repository** | In Spring, a class that handles database operations |
| **REST** | Representational State Transfer — an architectural style for HTTP APIs |
| **Rolling Update** | Updating Pods one-by-one to avoid downtime |
| **Service** | A Kubernetes resource providing a stable network endpoint to access Pods |
| **Service (Spring)** | A Spring class containing business logic, annotated with `@Service` |
| **Spring Boot** | A Java framework for creating standalone, production-grade applications quickly |
| **Stateless** | A service that holds no session data between requests |
| **TLS/SSL** | Transport Layer Security — encrypts HTTPS traffic |
| **WAF** | Web Application Firewall — protects against common web attacks |
>>>>>>> origin/main

---

## Further Reading

<<<<<<< HEAD
- [`examples/spring-boot-project-structure.md`](examples/spring-boot-project-structure.md) — Complete layer-by-layer project breakdown
- [`examples/react-complete-example.jsx`](examples/react-complete-example.jsx) — Full React component with API integration
- [`examples/spring-boot-complete-example.java`](examples/spring-boot-complete-example.java) — Complete Spring Boot example
- [`examples/complete-flow-example.md`](examples/complete-flow-example.md) — Annotated step-by-step request flow
- [`examples/jwt-authentication-flow.md`](examples/jwt-authentication-flow.md) — JWT authentication implementation guide
- [`examples/react-hooks-guide.md`](examples/react-hooks-guide.md) — Comprehensive React Hooks reference
- [`troubleshooting-guide.md`](troubleshooting-guide.md) — Common issues and how to fix them
=======
- [React Official Documentation](https://react.dev/)
- [Spring Boot Reference Guide](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Kubernetes Official Documentation](https://kubernetes.io/docs/)
- [Azure Kubernetes Service Documentation](https://docs.microsoft.com/en-us/azure/aks/)
- [JWT.io — JWT Debugger and Documentation](https://jwt.io/)
- [Docker Documentation](https://docs.docker.com/)
- [Azure Cosmos DB Documentation](https://docs.microsoft.com/en-us/azure/cosmos-db/)

---

**Repository Structure:**

```
modern_web_app/
├── README.md                          ← This article
├── troubleshooting-guide.md           ← Common issues and solutions
├── kubernetes-manifest-examples.yaml  ← K8s Deployment, Service, Ingress
├── examples/
│   ├── react-component/
│   │   ├── ProductCard.jsx            ← React component with API call
│   │   └── api.js                     ← REST API client utility
│   └── spring-boot-controller/
│       └── ProductController.java     ← Spring Boot REST controller
└── architecture-diagrams/
    ├── basic-architecture.txt         ← Detailed basic architecture diagram
    └── azure-architecture.txt         ← Detailed Azure architecture diagram
```
>>>>>>> origin/main
