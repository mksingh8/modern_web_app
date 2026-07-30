# Troubleshooting Guide

Common issues when building and running modern web applications, with clear explanations and fixes.

---

## Table of Contents

1. [React Issues](#react-issues)
2. [Spring Boot Issues](#spring-boot-issues)
3. [JWT Authentication Issues](#jwt-authentication-issues)
4. [CORS Issues](#cors-issues)
5. [Docker Issues](#docker-issues)
6. [Kubernetes Issues](#kubernetes-issues)
7. [Database Issues](#database-issues)
8. [Azure Issues](#azure-issues)

---

## React Issues

### ❌ `Cannot read properties of undefined (reading 'map')`

**Cause**: Trying to call `.map()` on a value that is `undefined` or `null` (usually before data loads).

```jsx
// ❌ WRONG — products is undefined on first render
function ProductList() {
  const [products, setProducts] = useState(); // ← defaults to undefined!
  return <ul>{products.map(p => <li>{p.name}</li>)}</ul>;
}

// ✅ FIX — always initialize arrays with []
function ProductList() {
  const [products, setProducts] = useState([]); // ← safe default
  return <ul>{products.map(p => <li key={p.id}>{p.name}</li>)}</ul>;
}

// ✅ FIX — optional chaining as extra safety
{products?.map(p => <li key={p.id}>{p.name}</li>)}
```

---

### ❌ `useEffect` runs infinitely

**Cause**: A dependency in the array changes on every render (usually an object or array created inside the component).

```jsx
// ❌ WRONG — new object on every render triggers infinite loop
function MyComponent() {
  const config = { page: 1 }; // new reference every render
  useEffect(() => { fetchData(config); }, [config]); // infinite!
}

// ✅ FIX — move static values outside the component
const CONFIG = { page: 1 };
function MyComponent() {
  useEffect(() => { fetchData(CONFIG); }, []); // runs once
}

// ✅ FIX — use primitive values as dependencies
function MyComponent({ page }) {
  useEffect(() => { fetchData(page); }, [page]); // only re-runs when page changes
}
```

---

### ❌ `Warning: Each child in a list should have a unique "key" prop`

**Cause**: Missing `key` prop when rendering lists.

```jsx
// ❌ WRONG
products.map(p => <li>{p.name}</li>)

// ✅ FIX — use a unique, stable ID
products.map(p => <li key={p.id}>{p.name}</li>)

// ⚠️ Avoid using index as key if the list can change order
products.map((p, index) => <li key={index}>{p.name}</li>) // may cause bugs
```

---

### ❌ State update on an unmounted component

**Symptom**: `Warning: Can't perform a React state update on an unmounted component`

```jsx
// ✅ FIX — use a cancellation flag
useEffect(() => {
  let cancelled = false;

  fetch('/api/data')
    .then(res => res.json())
    .then(data => {
      if (!cancelled) setData(data); // only update if still mounted
    });

  return () => { cancelled = true; }; // cleanup on unmount
}, []);
```

---

### ❌ React app can't connect to the backend API

**Cause**: Wrong API URL or missing proxy config.

**Development fix** — add a proxy in `package.json`:
```json
{
  "proxy": "http://localhost:8080"
}
```

Then use relative URLs: `fetch('/api/products')` instead of `fetch('http://localhost:8080/api/products')`.

**Production** — set the correct URL via environment variable:
```bash
REACT_APP_API_URL=https://api.myapp.com
```

---

## Spring Boot Issues

### ❌ `Failed to configure a DataSource`

**Cause**: Spring Boot can't connect to the database. Common reasons:
- Missing database dependency in `pom.xml`
- Wrong connection URL in `application.yml`
- Database server not running

```yaml
# application.yml — check these values
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=myapp
    username: sa
    password: YourPassword123!
  jpa:
    hibernate:
      ddl-auto: update  # 'create-drop' for dev, 'validate' for prod
```

```bash
# Check if SQL Server is running (Docker)
docker ps | grep sqlserver

# Start if needed
docker run -e "ACCEPT_EULA=Y" -e "SA_PASSWORD=YourPassword123!" \
  -p 1433:1433 mcr.microsoft.com/mssql/server:2022-latest
```

---

### ❌ `Port 8080 is already in use`

```bash
# Find the process using port 8080
lsof -i :8080        # macOS/Linux
netstat -ano | findstr :8080  # Windows

# Kill it (replace PID with the actual number)
kill -9 <PID>        # macOS/Linux
taskkill /F /PID <PID>  # Windows

# Or change the port in application.yml
server:
  port: 8081
```

---

### ❌ `No qualifying bean of type ... found`

**Cause**: Spring can't find a bean to inject. Common reasons:
- Missing `@Service`, `@Repository`, or `@Component` annotation
- Class is in a package not scanned by Spring

```java
// ❌ WRONG — missing annotation
public class UserService {  // Spring doesn't know about this class
    ...
}

// ✅ FIX
@Service
public class UserService {
    ...
}
```

```java
// Verify your main class scans the right packages
@SpringBootApplication(scanBasePackages = "com.example.myapp")
public class MyAppApplication { ... }
```

---

### ❌ `LazyInitializationException`

**Cause**: Accessing a JPA lazy-loaded relationship outside of an open Hibernate session (typically in the controller after the transaction ended).

```java
// ❌ WRONG — accessing lazy collection outside transaction
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) {
    User user = userRepository.findById(id).get();
    return user; // orders is lazily loaded — triggers exception during serialization
}

// ✅ FIX 1 — use eager loading for specific queries
@Query("SELECT u FROM User u JOIN FETCH u.orders WHERE u.id = :id")
Optional<User> findByIdWithOrders(@Param("id") Long id);

// ✅ FIX 2 — use DTOs (preferred)
// Map the entity to a DTO in the service (inside the transaction)
// Return the DTO from the controller
```

---

### ❌ `HttpMessageNotWritableException: No converter found`

**Cause**: Spring can't serialize your response to JSON. Usually caused by bidirectional JPA relationships creating a circular reference.

```java
// ❌ WRONG — circular reference: User → Orders → User → ...
@Entity class User {
    @OneToMany(mappedBy = "user")
    private List<Order> orders;
}
@Entity class Order {
    @ManyToOne
    private User user; // ← circular!
}

// ✅ FIX — use @JsonIgnore or DTOs
@Entity class Order {
    @ManyToOne
    @JsonIgnore // break the cycle
    private User user;
}

// ✅ BETTER FIX — use DTOs, never serialize entities directly
```

---

## JWT Authentication Issues

### ❌ `401 Unauthorized` on every request

**Checklist:**
1. Is the token being sent in the `Authorization` header?
2. Does the header use the format `****** (with a space)?
3. Is the token expired?
4. Is the JWT secret the same on both sides?

```javascript
// Check the request in browser DevTools → Network tab
// Headers should include:
// Authorization: ******

// Check token expiry at jwt.io
const decoded = JSON.parse(atob(token.split('.')[1]));
console.log('Expires:', new Date(decoded.exp * 1000));
```

---

### ❌ `JWT signature does not match`

**Cause**: The JWT was signed with a different secret than the one used to verify it.

**In development**: Make sure `app.jwt.secret` in `application.yml` matches what the token was created with.

**In production**: Use an environment variable, not a hardcoded string:
```yaml
app:
  jwt:
    secret: ${JWT_SECRET}  # set in environment or Azure Key Vault
```

---

### ❌ `403 Forbidden` (user is authenticated but still blocked)

**Cause**: The user is authenticated but doesn't have the required role/authority.

```java
// Check your security config
.requestMatchers("/api/admin/**").hasRole("ADMIN")
// User must have ROLE_ADMIN authority

// Debug: log the current user's authorities
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
System.out.println("Authorities: " + auth.getAuthorities());
```

---

## CORS Issues

### ❌ `CORS policy: No 'Access-Control-Allow-Origin' header`

**Cause**: The browser is blocking the request because the backend doesn't allow the frontend origin.

```java
// ✅ FIX — configure CORS in Spring Boot
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://localhost:3000",         // React dev server
            "https://myapp.azurewebsites.net" // Production
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

```java
// In SecurityConfig — apply the CORS configuration
http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
```

**Common mistake**: Setting CORS in `@CrossOrigin` on the controller but forgetting to apply it in `SecurityConfig`. Spring Security intercepts requests before they reach the controller, so CORS must also be configured at the security level.

---

## Docker Issues

### ❌ `Cannot connect to the Docker daemon`

```bash
# Start Docker Desktop (macOS/Windows)
# Or start the Docker service (Linux):
sudo systemctl start docker
sudo systemctl enable docker
```

---

### ❌ Container exits immediately

```bash
# Check the logs
docker logs <container-id>

# Run interactively to debug
docker run -it myapp:latest /bin/sh
```

---

### ❌ Container can't reach another container

```yaml
# In docker-compose.yml, use the service name as the hostname
services:
  backend:
    environment:
      SPRING_DATASOURCE_URL: jdbc:sqlserver://db:1433  # 'db' is the service name
  db:
    image: mcr.microsoft.com/mssql/server:2022-latest
```

**Don't use `localhost`** — inside Docker Compose, containers communicate using service names.

---

## Kubernetes Issues

### ❌ `ImagePullBackOff`

**Cause**: Kubernetes can't pull the Docker image.

```bash
# Check details
kubectl describe pod <pod-name> -n <namespace>

# Common fixes:
# 1. Wrong image name or tag
# 2. Image not pushed to registry
# 3. Missing pull secret for private registry

# Create pull secret for Azure Container Registry
kubectl create secret docker-registry acr-secret \
  --docker-server=myregistry.azurecr.io \
  --docker-username=<username> \
  --docker-******
```

---

### ❌ `CrashLoopBackOff`

**Cause**: The container keeps crashing and Kubernetes keeps restarting it.

```bash
# View logs from the crashed container
kubectl logs <pod-name> --previous

# Common causes:
# 1. Application startup error (check env vars/config)
# 2. OOMKilled — out of memory (increase resource limits)
# 3. Failed readiness probe (check the health endpoint)
```

---

### ❌ `Connection refused` between pods

```bash
# Verify the service exists
kubectl get services -n <namespace>

# Test DNS resolution from inside the cluster
kubectl run test --image=busybox --rm -it --restart=Never -- \
  wget -O- http://backend-service/actuator/health

# Service name format: <service-name>.<namespace>.svc.cluster.local
```

---

### ❌ Pod stuck in `Pending`

```bash
kubectl describe pod <pod-name>

# Common causes:
# 1. Insufficient cluster resources (CPU/memory)
# 2. No node matches node selector
# 3. PVC not bound

# Check node capacity
kubectl describe nodes | grep -A 5 "Allocated resources"
```

---

## Database Issues

### ❌ `Cannot open server 'myserver'` (Azure SQL)

1. Check if your IP is in the Azure SQL firewall allowlist
2. For AKS pods: use the AKS managed identity or service endpoint
3. Verify the connection string format:

```yaml
spring:
  datasource:
    url: jdbc:sqlserver://myserver.database.windows.net:1433;database=mydb;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30
```

---

### ❌ `Transaction rolled back because it has been marked as rollback-only`

**Cause**: An exception was thrown inside a `@Transactional` method, which marks the transaction for rollback, but the exception was caught and swallowed.

```java
// ❌ WRONG — swallowing exception inside @Transactional
@Transactional
public void processOrder(Order order) {
    try {
        paymentService.charge(order);
        orderRepository.save(order);
    } catch (Exception e) {
        log.error("Error", e); // ← transaction is already marked for rollback
        // but we continue — causes "rollback-only" error
    }
}

// ✅ FIX — let the exception propagate or use separate transactions
@Transactional
public void processOrder(Order order) {
    paymentService.charge(order); // exception propagates → transaction rolls back cleanly
    orderRepository.save(order);
}
```

---

## Azure Issues

### ❌ AKS pod can't connect to Azure SQL

**Solution**: Use a managed identity instead of username/password.

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:sqlserver://server.database.windows.net:1433;database=mydb;authentication=ActiveDirectoryMSI
```

```yaml
# Deployment.yaml — enable workload identity
spec:
  template:
    metadata:
      labels:
        azure.workload.identity/use: "true"
    spec:
      serviceAccountName: myapp-service-account
```

---

### ❌ Azure Key Vault secrets not loading

```yaml
# application.yml — Spring Cloud Azure Key Vault
spring:
  cloud:
    azure:
      keyvault:
        secret:
          enabled: true
          endpoint: https://my-keyvault.vault.azure.net/

# Reference a secret: use the secret name with -- replacing -
# Secret name "jwt-secret" → property "jwt--secret" OR use @Value
```

```java
@Value("${jwt-secret}")  // Key Vault secret named "jwt-secret"
private String jwtSecret;
```

---

## General Debugging Checklist

When something goes wrong and you don't know where to start:

1. **Check logs first**: `docker logs`, `kubectl logs`, Spring Boot console
2. **Check the HTTP response**: Browser DevTools → Network tab → look at status code and response body
3. **Simplify**: Comment out parts until you find what's broken
4. **Check environment variables**: `printenv | grep APP` or `kubectl exec <pod> -- env`
5. **Verify connectivity**: Can the app reach the database? Can pods reach each other?
6. **Search the exact error message**: Most error messages are well-documented online
7. **Check versions**: Incompatible library versions cause many mysterious issues

---

## Useful Commands Reference

```bash
# Docker
docker build -t myapp:latest .
docker run -p 8080:8080 myapp:latest
docker logs -f <container-id>
docker exec -it <container-id> /bin/bash

# Kubernetes
kubectl get pods -n <namespace>
kubectl describe pod <pod-name> -n <namespace>
kubectl logs <pod-name> -n <namespace> --follow
kubectl exec -it <pod-name> -n <namespace> -- /bin/sh
kubectl apply -f k8s/
kubectl rollout status deployment/<name> -n <namespace>
kubectl rollout undo deployment/<name> -n <namespace>

# Spring Boot (Maven)
./mvnw spring-boot:run
./mvnw test
./mvnw package -DskipTests

# React
npm start
npm test
npm run build
```
