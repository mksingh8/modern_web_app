# Troubleshooting Guide

Common issues and solutions for modern web application development and deployment.

---

## Table of Contents

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
{
  "proxy": "http://localhost:8080"
}
```

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
```

---

## Spring Boot Issues

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
    }
}
```

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

---

## Docker Issues

### Issue: Docker build fails — "COPY failed: file not found"

**Cause:** The file path in the Dockerfile doesn't match the actual file location.

**Debug:**
```bash
# Check what files exist in the build context
ls -la

# Build with verbose output
docker build --progress=plain -t myapp .
```

---

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

---

## Kubernetes Issues

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
```

---

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
```

---

## Azure Issues

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
