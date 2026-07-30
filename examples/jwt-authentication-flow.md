# JWT Authentication Flow — Complete Implementation Guide

**JSON Web Tokens (JWT)** are the standard for authenticating REST API requests. This guide covers the complete flow from user login to every subsequent authenticated request.

---

## What is a JWT?

A JWT is a compact, self-contained token that encodes claims about a user. It has three parts separated by dots:

```
******
│──────────────────│ │────────────────────────────────────────────────────────────────────│ │──────────────│
     Header                                    Payload                                        Signature
  (algorithm)                          (claims: who you are)                          (proves it's genuine)
```

**Decoded Payload:**
```json
{
  "sub": "alice@example.com",
  "role": "USER",
  "iat": 1706000000,
  "exp": 1706086400
}
```

The server never stores the token. It only needs the **secret key** to verify the signature.

---

## Complete Authentication Flow

### Phase 1: Login

```
React                    Spring Boot              Database
  │                           │                      │
  │  POST /api/auth/login      │                      │
  │  { email, password }  ──► │                      │
  │                           │  SELECT * FROM users  │
  │                           │  WHERE email = ?  ──► │
  │                           │ ◄────────────────────-│
  │                           │  BCrypt.verify(        │
  │                           │    password,          │
  │                           │    user.passwordHash  │
  │                           │  )                    │
  │                           │                       │
  │                           │  if (valid):          │
  │                           │    token = JWT.sign({ │
  │                           │      sub: email,      │
  │                           │      exp: +24h        │
  │                           │    }, SECRET_KEY)     │
  │ ◄─────────────────────── │                       │
  │  200 OK                   │                       │
  │  { "token": "eyJ..." }    │                       │
  │                           │                       │
  │  localStorage.setItem(    │                       │
  │    'jwt_token', token)    │                       │
```

### Phase 2: Authenticated Request

```
React                   JwtAuthFilter           Controller            Database
  │                          │                      │                    │
  │  GET /api/orders          │                      │                    │
  │  Authorization:           │                      │                    │
  │  ******  ──► │                      │                    │
  │                          │  Extract token        │                    │
  │                          │  Decode JWT           │                    │
  │                          │  Verify signature     │                    │
  │                          │  Check expiry         │                    │
  │                          │  Load user from DB ──────────────────────►│
  │                          │  Set SecurityContext ◄──────────────────── │
  │                          │ ────────────────────►│                    │
  │                          │                      │  Query orders  ───►│
  │                          │                      │ ◄──────────────────│
  │ ◄──────────────────────────────────────────────-│                    │
  │  200 OK                  │                      │                    │
  │  [{ orders... }]         │                      │                    │
```

---

## Spring Boot Implementation

### 1. AuthController — Login Endpoint

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody @Valid LoginRequest request) {
        JwtResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto register(@RequestBody @Valid RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(
        @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.substring(7);
        return ResponseEntity.ok(authService.refresh(token));
    }
}
```

### 2. AuthService — Business Logic

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        // Verify password (BCrypt compares hashes — never plain text)
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Failed login attempt for email: {}", request.email());
            throw new BadCredentialsException("Invalid credentials");
        }

        // Generate JWT
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        String token = jwtUtil.generateToken(userDetails);

        log.info("Successful login for: {}", request.email());
        return new JwtResponse(token, "Bearer", jwtUtil.getExpirationMs() / 1000);
    }

    @Transactional
    public UserDto register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);

        return toDto(userRepository.save(user));
    }
}
```

### 3. JwtUtil — Token Generation and Validation

```java
@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long expirationMs;

    /**
     * Generate a JWT for the given user.
     * Claims:
     *   sub  = email (the username)
     *   role = user's role
     *   iat  = issued at (now)
     *   exp  = expires at (now + expirationMs)
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()));

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    /** Extract the username (email) from a token. */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /** Check if token is valid and not expired. */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public long getExpirationMs() { return expirationMs; }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

### 4. JwtAuthFilter — Validate Token on Every Request

```java
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Skip filter if no Authorization header or wrong format
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            final String userEmail = jwtUtil.extractUsername(jwt);

            // Only authenticate if not already authenticated this request
            if (userEmail != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                if (jwtUtil.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                        );
                    authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (ExpiredJwtException e) {
            // Token expired — Spring Security will return 401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":\"TOKEN_EXPIRED\",\"message\":\"JWT token has expired\"}");
            return;
        } catch (JwtException e) {
            // Malformed or tampered token
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":\"INVALID_TOKEN\",\"message\":\"Invalid JWT token\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
```

### 5. SecurityConfig — Wire It All Together

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints — no JWT required
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

                // Admin-only endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // Everything else requires a valid JWT
                .anyRequest().authenticated()
            )
            // Add our JWT filter BEFORE Spring's default username/password filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt with cost factor 12 — strong but not too slow
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

---

## React Implementation

### Login Page

```jsx
// LoginPage.jsx
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

function LoginPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const response = await axios.post(
        `${process.env.REACT_APP_API_URL}/api/auth/login`,
        form
      );

      const { token } = response.data;

      // Store the JWT — accessible on every page reload
      localStorage.setItem('jwt_token', token);

      // Redirect to the main app
      navigate('/dashboard');
    } catch (err) {
      if (err.response?.status === 401) {
        setError('Invalid email or password');
      } else {
        setError('Login failed. Please try again later.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <form onSubmit={handleSubmit}>
        <h1>Sign In</h1>

        <input
          type="email"
          placeholder="Email"
          value={form.email}
          onChange={(e) => setForm({ ...form, email: e.target.value })}
          required
        />

        <input
          type="password"
          placeholder="Password"
          value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })}
          required
        />

        {error && <p className="error">{error}</p>}

        <button type="submit" disabled={loading}>
          {loading ? 'Signing in...' : 'Sign In'}
        </button>
      </form>
    </div>
  );
}
```

### Auth Context (Share Login State Across the App)

```jsx
// AuthContext.jsx
import { createContext, useContext, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    // Restore user from stored token on page load
    const token = localStorage.getItem('jwt_token');
    if (!token) return null;
    try {
      const decoded = jwtDecode(token);
      if (decoded.exp * 1000 < Date.now()) {
        localStorage.removeItem('jwt_token');
        return null;
      }
      return { email: decoded.sub, role: decoded.role };
    } catch {
      return null;
    }
  });

  const login = useCallback((token) => {
    localStorage.setItem('jwt_token', token);
    const decoded = jwtDecode(token);
    setUser({ email: decoded.sub, role: decoded.role });
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('jwt_token');
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, login, logout, isLoggedIn: !!user }}>
      {children}
    </AuthContext.Provider>
  );
}

// Custom hook for easy access
export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider');
  return ctx;
}

// Protected route component
export function RequireAuth({ children }) {
  const { isLoggedIn } = useAuth();
  const navigate = useNavigate();

  if (!isLoggedIn) {
    navigate('/login', { replace: true });
    return null;
  }

  return children;
}
```

---

## JWT Security Best Practices

| Practice | Why |
|----------|-----|
| Use a long, random secret key (256-bit minimum) | Short secrets can be brute-forced |
| Set token expiry (24h for regular, 15min for sensitive) | Limits damage if token is stolen |
| Use HTTPS in production | Prevents token interception |
| Don't store sensitive data in JWT payload | Payload is only base64-encoded, not encrypted |
| Implement token refresh | Allows short-lived tokens without frequent logouts |
| Validate on every request | Never trust the client |
| Log failed authentication attempts | Detect brute force attacks |

---

## application.yml — JWT Configuration

```yaml
app:
  jwt:
    # IMPORTANT: Use a real random 256-bit key in production
    # Generate with: openssl rand -base64 32
    secret: ${JWT_SECRET}          # Load from environment variable / Azure Key Vault
    expiration-ms: 86400000        # 24 hours
    refresh-expiration-ms: 604800000  # 7 days (for refresh tokens)
```

**In Azure, store the secret in Azure Key Vault:**
```bash
# Set secret in Azure Key Vault
az keyvault secret set --vault-name myapp-vault --name JWT-SECRET --value "your-secret"

# Reference it in application.yml (with Spring Cloud Azure)
app:
  jwt:
    secret: ${JWT_SECRET}  # Azure Key Vault injects this at startup
```
