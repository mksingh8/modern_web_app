# Troubleshooting Guide

<<<<<<< HEAD
Common issues when building and running modern web applications, with clear explanations and fixes.
=======
Common issues and solutions for modern web application development and deployment.
>>>>>>> origin/main

---

## Table of Contents

<<<<<<< HEAD
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
=======
1. [React Frontend Issues](#react-frontend-issues)
2. [REST API / JWT Issues](#rest-api--jwt-issues)
3. [Spring Boot Issues](#spring-boot-issues)
4. [Docker Issues](#docker-issues)
5. [Kubernetes Issues](#kubernetes-issues)
6. [Azure Issues](#azure-issues)
7. [Database Issues](#database-issues)

---

## React Frontend Issues

### Issue: API calls fail with "Network Error" or CORS error

**Symptoms:**
```
Access to fetch at 'http://localhost:8080/api/products' from origin 
'http://localhost:3000' has been blocked by CORS policy
```

**Cause:** The browser's Same-Origin Policy prevents JavaScript from making requests to a different domain/port. Your React app runs on port 3000 but your API is on port 8080.

**Solutions:**

**Option 1 — Add CORS configuration to Spring Boot (development):**
```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:3000")
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }
}
```

**Option 2 — Use React's proxy feature (development only):**
```json
// In package.json
>>>>>>> origin/main
{
  "proxy": "http://localhost:8080"
}
```

<<<<<<< HEAD
Then use relative URLs: `fetch('/api/products')` instead of `fetch('http://localhost:8080/api/products')`.

**Production** — set the correct URL via environment variable:
```bash
REACT_APP_API_URL=https://api.myapp.com
=======
**Option 3 — Use environment variables for the API URL:**
```
# .env
REACT_APP_API_URL=http://localhost:8080
```

---

### Issue: JWT token not being sent with requests

**Symptoms:** API calls return `401 Unauthorized` even after logging in.

**Checklist:**
1. Is the token stored? Check browser DevTools → Application → Local Storage
2. Is the token being attached? Check Network tab → request headers for `Authorization: ******`
3. Has the token expired? Decode at [jwt.io](https://jwt.io) and check the `exp` claim

**Fix:**
```javascript
// Ensure all API calls include the token
const token = localStorage.getItem('jwt_token');
if (!token) {
  // Redirect to login
  window.location.href = '/login';
  return;
}

fetch('/api/products', {
  headers: {
    'Authorization': `****** }
});
```

---

### Issue: React app shows stale data after an update

**Cause:** React's state was not updated after the API call succeeded.

**Fix:** Always update state with the response from the API:
```javascript
// Wrong — doesn't update UI
async function deleteProduct(id) {
  await api.deleteProduct(id);
}

// Correct — updates state so React re-renders
async function deleteProduct(id) {
  await api.deleteProduct(id);
  setProducts(prev => prev.filter(p => p.id !== id));
}
```

---

## REST API / JWT Issues

### Issue: JWT validation fails with "signature does not match"

**Cause:** The JWT was signed with a different key than the one used to validate it. This happens when:
- Multiple services use different signing keys
- The key changed after tokens were issued

**Fix:**
- Store the JWT signing key in **Azure Key Vault** so all services read from the same source
- During key rotation, support both old and new keys during a transition window

---

### Issue: JWT token expires and users get logged out unexpectedly

**Cause:** Short token expiry (`exp` claim) without a refresh token strategy.

**Solution — Implement refresh tokens:**
```
Access token:  short-lived (15 minutes) — used for API calls
Refresh token: long-lived (7 days) — used ONLY to get a new access token

When access token expires:
  1. Call POST /api/auth/refresh with the refresh token
  2. Server validates refresh token, issues new access token
  3. Continue with the new access token
```

**Spring Boot refresh token endpoint:**
```java
@PostMapping("/auth/refresh")
public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshRequest request) {
    return tokenService.validateAndRefresh(request.getRefreshToken())
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
}
```

---

### Issue: API returns 403 Forbidden even with a valid JWT

**Cause:** The user's JWT doesn't contain the required role.

**Debug steps:**
1. Decode the token at [jwt.io](https://jwt.io)
2. Check the `roles` or `authorities` claim in the payload
3. Verify the `@PreAuthorize` annotation on the controller method

```java
// This requires the user to have ROLE_ADMIN
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<Product> createProduct(...) { ... }

// Check the JWT payload:
// { "sub": "user123", "roles": ["ROLE_CUSTOMER"] }  ← Missing ADMIN → 403
// { "sub": "user123", "roles": ["ROLE_ADMIN"] }     ← Has ADMIN → 200
>>>>>>> origin/main
```

---

## Spring Boot Issues

<<<<<<< HEAD
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
=======
### Issue: Application fails to start — "Bean creation exception"

**Symptoms:**
```
APPLICATION FAILED TO START
Description: A component required a bean of type 'ProductRepository' that could not be found.
```

**Common causes and fixes:**

| Cause | Fix |
|-------|-----|
| Missing `@Repository` / `@Service` annotation | Add the correct Spring annotation |
| Class not in the component scan path | Move class under the main application package |
| Circular dependency | Refactor to break the cycle, or use `@Lazy` |
| Missing dependency in `pom.xml` | Add the required Maven dependency |

---

### Issue: `NullPointerException` in a Spring Bean

**Cause:** Usually means a dependency wasn't injected (e.g., using `new MyService()` instead of letting Spring inject it).

**Wrong:**
```java
@RestController
public class ProductController {
    // Created manually — Spring cannot inject into this instance
    private ProductService productService = new ProductService();
}
```

**Correct:**
```java
@RestController
public class ProductController {
    private final ProductService productService;

    // Spring injects this automatically
    public ProductController(ProductService productService) {
        this.productService = productService;
>>>>>>> origin/main
    }
}
```

<<<<<<< HEAD
```java
// In SecurityConfig — apply the CORS configuration
http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
```

**Common mistake**: Setting CORS in `@CrossOrigin` on the controller but forgetting to apply it in `SecurityConfig`. Spring Security intercepts requests before they reach the controller, so CORS must also be configured at the security level.
=======
---

### Issue: Spring Boot app connects to wrong database in production

**Cause:** `application.properties` hardcoded values override environment-specific configuration.

**Fix — Use profiles and environment variables:**
```yaml
# application.yml (default)
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:h2:mem:testdb}
    username: ${SPRING_DATASOURCE_USERNAME:sa}
    password: ${SPRING_DATASOURCE_PASSWORD:}
```

In Kubernetes, inject these as environment variables from Secrets:
```yaml
env:
  - name: SPRING_DATASOURCE_URL
    valueFrom:
      secretKeyRef:
        name: db-credentials
        key: url
```
>>>>>>> origin/main

---

## Docker Issues

<<<<<<< HEAD
### ❌ `Cannot connect to the Docker daemon`

```bash
# Start Docker Desktop (macOS/Windows)
# Or start the Docker service (Linux):
sudo systemctl start docker
sudo systemctl enable docker
=======
### Issue: Docker build fails — "COPY failed: file not found"

**Cause:** The file path in the Dockerfile doesn't match the actual file location.

**Debug:**
```bash
# Check what files exist in the build context
ls -la

# Build with verbose output
docker build --progress=plain -t myapp .
>>>>>>> origin/main
```

---

<<<<<<< HEAD
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
=======
### Issue: Container starts but immediately exits

**Debug steps:**
```bash
# Check exit code and logs
docker ps -a
docker logs <container-id>

# Run interactively to debug
docker run -it --entrypoint sh myapp:1.0
```

**Common causes:**
- Application crashes on startup (check logs for stack traces)
- Missing environment variables (add `-e VAR=value` to `docker run`)
- Port conflict (use `-p 8081:8080` to map to a different host port)

---

### Issue: Docker image is too large

**Solutions:**
1. Use multi-stage builds (build in one stage, copy only artifacts to final stage)
2. Use Alpine or slim base images (`eclipse-temurin:17-jre-alpine` instead of `eclipse-temurin:17`)
3. Add a `.dockerignore` file to exclude `node_modules`, `target/`, `.git/`

```
# .dockerignore
node_modules/
target/
.git/
*.md
.env
```
>>>>>>> origin/main

---

## Kubernetes Issues

<<<<<<< HEAD
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
=======
### Issue: Pod is stuck in `CrashLoopBackOff`

**Diagnosis:**
```bash
# See why the pod is crashing
kubectl describe pod <pod-name>
kubectl logs <pod-name>
kubectl logs <pod-name> --previous  # logs from the previous (crashed) container
```

**Common causes:**
- Application error on startup (check logs)
- Insufficient memory — increase `resources.limits.memory` in the Deployment
- Liveness probe failing — pod is restarted before app finishes starting up (increase `initialDelaySeconds`)
- Missing ConfigMap or Secret that the pod tries to mount

---

### Issue: Pod is stuck in `Pending`

**Diagnosis:**
```bash
kubectl describe pod <pod-name>
# Look for "Events" section at the bottom
```

**Common causes:**

| Message | Cause | Fix |
|---------|-------|-----|
| `Insufficient cpu` | No node has enough CPU | Scale up node pool or reduce resource requests |
| `Insufficient memory` | No node has enough memory | Same as above |
| `node(s) had untolerated taint` | Pod scheduled to wrong node | Add tolerations to the Pod spec |
| `persistentvolumeclaim not found` | Missing PVC | Create the required PersistentVolumeClaim |

---

### Issue: Service is not routing traffic to Pods

**Diagnosis:**
```bash
# Check if the service selector matches pod labels
kubectl describe service <service-name>
kubectl get pods --show-labels

# Test connectivity from inside the cluster
kubectl run test --image=busybox --rm -it -- sh
wget -qO- http://product-service:8080/api/products
```

**Common cause:** The Service's `selector` labels don't match the Pod's labels.

```yaml
# Service selector
selector:
  app: product-service   ← Must match ↓

# Pod labels (in Deployment template)
labels:
  app: product-service   ← Must match ↑
  version: "1.2"
>>>>>>> origin/main
```

---

<<<<<<< HEAD
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
=======
### Issue: Ingress returns 404 for all paths

**Checklist:**
1. Is the Ingress Controller installed? (`kubectl get pods -n ingress-nginx`)
2. Does the `ingressClassName` match the installed controller?
3. Do the `serviceName` and `servicePort` match an existing Service?
4. Are the path rules correct? Test with `curl -H "Host: myapp.com" http://<ingress-ip>/api/products`

---

### Issue: Rolling update causes downtime

**Cause:** Pods are terminated before new pods are ready, or new pods receive traffic before they're healthy.

**Fix — Configure proper health checks and update strategy:**
```yaml
spec:
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1        # Allow 1 extra pod during update
      maxUnavailable: 0  # Never reduce below desired count
  template:
    spec:
      containers:
        - name: app
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 60
>>>>>>> origin/main
```

---

## Azure Issues

<<<<<<< HEAD
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
=======
### Issue: AKS nodes are out of memory / CPU

**Signs:** Pods in `Pending` state, HPA cannot scale, `OOMKilled` events.

**Solutions:**
```bash
# Check node resource usage
kubectl top nodes
kubectl top pods --all-namespaces

# Scale up the node pool via Azure CLI
az aks nodepool scale \
  --resource-group myRG \
  --cluster-name myAKS \
  --name nodepool1 \
  --node-count 5
```

---

### Issue: Pods cannot access Azure Key Vault secrets

**Cause:** The pod's Managed Identity doesn't have permission to read the Key Vault secret.

**Fix:**
```bash
# Grant the AKS Managed Identity access to Key Vault
az keyvault set-policy \
  --name myKeyVault \
  --object-id <aks-managed-identity-object-id> \
  --secret-permissions get list
```

---

### Issue: Azure SQL connection timeout from AKS pods

**Cause:** Network Security Group (NSG) or private endpoint configuration blocking traffic.

**Checklist:**
1. Is Azure SQL on a private endpoint in the same VNet as AKS?
2. Does the NSG allow outbound traffic from AKS subnets to the SQL private endpoint?
3. Is the connection string using the private endpoint DNS name?
4. Are you using Azure AD authentication (Managed Identity) instead of SQL passwords?

---

## Database Issues

### Issue: Spring Boot cannot connect to database on startup

**Symptoms:** `Connection refused` or `Unable to acquire JDBC Connection`

**Checklist:**
```bash
# Test connectivity from the pod
kubectl exec -it <pod-name> -- sh
# Try to reach the database
nc -zv <db-host> 1433   # SQL Server port
nc -zv <db-host> 5432   # PostgreSQL port
```

**Common causes:**
- Wrong hostname in connection string
- Database not accepting connections from the pod's IP
- Firewall/NSG blocking the database port
- Database is still starting up (add retry logic with `spring.datasource.hikari.connection-timeout`)

---

### Issue: Slow database queries causing high API response times

**Diagnosis:**
```sql
-- Find slow queries (Azure SQL)
SELECT TOP 10
    qs.total_elapsed_time / qs.execution_count AS avg_elapsed_time,
    qs.execution_count,
    SUBSTRING(qt.text, 1, 200) AS query_text
FROM sys.dm_exec_query_stats qs
CROSS APPLY sys.dm_exec_sql_text(qs.sql_handle) qt
ORDER BY avg_elapsed_time DESC;
```

**Common fixes:**
1. Add missing database indexes
2. Use pagination (`LIMIT`/`OFFSET` or Spring's `Pageable`) instead of loading all records
3. Use projections (select only needed columns, not `SELECT *`)
4. Enable query caching with Azure Cache for Redis

---

*For additional help, refer to the official documentation linked in the main [README.md](README.md#further-reading).*
>>>>>>> origin/main
