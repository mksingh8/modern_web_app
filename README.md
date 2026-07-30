# Modern Web Application Architecture: A Comprehensive Guide

> A practical, developer-friendly guide to understanding how modern web applications are built, deployed, and scaled — from React frontends to Spring Boot microservices running on Azure Kubernetes Service.

---

## Table of Contents

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
    </div>
  );
}

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
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> createProduct(@RequestBody @Valid ProductRequest request) {
        Product saved = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
```

**Advantages of microservices:**
- Each service can be deployed independently
- Different services can use different technologies (polyglot)
- Failure in one service doesn't bring down the whole application
- Teams can own and develop services independently

> **See also:** [`examples/spring-boot-controller/ProductController.java`](examples/spring-boot-controller/ProductController.java)

---

### Dependency Injection

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
```

---

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
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**A Dockerfile for a React application:**

```dockerfile
# Stage 1: Build the React app
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Stage 2: Serve with nginx
FROM nginx:alpine
COPY --from=builder /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

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
```

---

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
```

---

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
```

---

## Glossary

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

---

## Further Reading

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