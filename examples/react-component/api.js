/**
 * api.js — REST API Client Utility
 *
 * A centralized module for making authenticated HTTP requests to the backend.
 *
 * Key concepts demonstrated:
 *   - Attaching JWT tokens to every request via the Authorization header
 *   - Centralized error handling
 *   - Base URL configuration
 *   - Reusable request helper (no code duplication across components)
 */

const BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

/**
 * Retrieves the JWT token stored after login.
 * In production, consider using httpOnly cookies instead of localStorage
 * to prevent XSS attacks from accessing the token.
 */
function getToken() {
  return localStorage.getItem('jwt_token');
}

/**
 * Core HTTP request function.
 * Automatically attaches the JWT token and handles common error responses.
 *
 * @param {string} path    - API path, e.g. '/api/products'
 * @param {object} options - fetch options (method, body, etc.)
 * @returns {Promise<any>} - Parsed JSON response body
 * @throws {Error}         - On HTTP error responses
 */
async function request(path, options = {}) {
  const token = getToken();

  const headers = {
    'Content-Type': 'application/json',
    ...(token && { Authorization: `****** }),
    ...options.headers,
  };

  const response = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers,
  });

  // Handle HTTP error responses
  if (response.status === 401) {
    // Token expired or invalid — redirect to login
    localStorage.removeItem('jwt_token');
    window.location.href = '/login';
    throw new Error('Session expired. Please log in again.');
  }

  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({}));
    throw new Error(errorBody.message || `HTTP error ${response.status}`);
  }

  // Return null for 204 No Content responses
  if (response.status === 204) {
    return null;
  }

  return response.json();
}

// ─── Authentication ──────────────────────────────────────────────────────────

/**
 * Log in and store the JWT token.
 * POST /api/auth/login
 *
 * @param {string} username
 * @param {string} password
 * @returns {Promise<{ token: string, user: object }>}
 */
export async function login(username, password) {
  const data = await request('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  });
  localStorage.setItem('jwt_token', data.token);
  return data;
}

/**
 * Log out — remove the JWT token from storage.
 */
export function logout() {
  localStorage.removeItem('jwt_token');
}

// ─── Products ────────────────────────────────────────────────────────────────

/**
 * Fetch all products.
 * GET /api/products
 *
 * @returns {Promise<Product[]>}
 */
export function getAllProducts() {
  return request('/api/products');
}

/**
 * Fetch a single product by ID.
 * GET /api/products/:id
 *
 * @param {number} id - Product ID
 * @returns {Promise<Product>}
 */
export function getProductById(id) {
  return request(`/api/products/${id}`);
}

// ─── Cart ─────────────────────────────────────────────────────────────────────

/**
 * Add a product to the cart.
 * POST /api/cart
 *
 * @param {number} productId - ID of the product to add
 * @param {number} [quantity=1] - Quantity to add
 * @returns {Promise<CartItem>}
 */
export function addToCart(productId, quantity = 1) {
  return request('/api/cart', {
    method: 'POST',
    body: JSON.stringify({ productId, quantity }),
  });
}

/**
 * Remove an item from the cart.
 * DELETE /api/cart/:productId
 *
 * @param {number} productId - ID of the product to remove
 * @returns {Promise<null>}
 */
export function removeFromCart(productId) {
  return request(`/api/cart/${productId}`, { method: 'DELETE' });
}

/**
 * Fetch the current user's cart contents.
 * GET /api/cart
 *
 * @returns {Promise<Cart>}
 */
export function getCart() {
  return request('/api/cart');
}
