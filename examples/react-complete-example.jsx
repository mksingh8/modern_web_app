/**
 * react-complete-example.jsx
 *
 * A fully featured React component demonstrating:
 *  - useState and useEffect hooks
 *  - Axios with JWT authentication
 *  - Loading and error states
 *  - CRUD operations (Create, Read, Update, Delete)
 *  - Form handling with validation
 *  - React Router navigation
 */

import React, { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

// ─────────────────────────────────────────────────────────────
// 1. Axios instance — pre-configured with base URL and JWT
// ─────────────────────────────────────────────────────────────
const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
});

// Automatically attach JWT to every outgoing request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('jwt_token');
  if (token) {
    config.headers.Authorization = `******;
  }
  return config;
});

// Handle 401 Unauthorized globally — redirect to login
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('jwt_token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// ─────────────────────────────────────────────────────────────
// 2. Custom hook: useProducts
//    Encapsulates all product data fetching and mutation logic
// ─────────────────────────────────────────────────────────────
function useProducts() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchProducts = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.get('/products');
      setProducts(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load products');
    } finally {
      setLoading(false);
    }
  }, []);

  // Fetch once on mount
  useEffect(() => {
    fetchProducts();
  }, [fetchProducts]);

  const createProduct = async (productData) => {
    const res = await api.post('/products', productData);
    setProducts((prev) => [...prev, res.data]);
    return res.data;
  };

  const updateProduct = async (id, productData) => {
    const res = await api.put(`/products/${id}`, productData);
    setProducts((prev) => prev.map((p) => (p.id === id ? res.data : p)));
    return res.data;
  };

  const deleteProduct = async (id) => {
    await api.delete(`/products/${id}`);
    setProducts((prev) => prev.filter((p) => p.id !== id));
  };

  return { products, loading, error, refetch: fetchProducts, createProduct, updateProduct, deleteProduct };
}

// ─────────────────────────────────────────────────────────────
// 3. ProductForm component — handles both Create and Edit
// ─────────────────────────────────────────────────────────────
function ProductForm({ initialValues, onSubmit, onCancel }) {
  const [form, setForm] = useState(
    initialValues || { name: '', description: '', price: '', category: '' }
  );
  const [submitting, setSubmitting] = useState(false);
  const [validationErrors, setValidationErrors] = useState({});

  const validate = () => {
    const errors = {};
    if (!form.name.trim()) errors.name = 'Name is required';
    if (!form.price || isNaN(form.price) || Number(form.price) <= 0)
      errors.price = 'Price must be a positive number';
    if (!form.category.trim()) errors.category = 'Category is required';
    return errors;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
    // Clear validation error when user starts typing
    if (validationErrors[name]) {
      setValidationErrors((prev) => ({ ...prev, [name]: undefined }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errors = validate();
    if (Object.keys(errors).length > 0) {
      setValidationErrors(errors);
      return;
    }

    setSubmitting(true);
    try {
      await onSubmit({ ...form, price: parseFloat(form.price) });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="product-form">
      <div className="form-group">
        <label htmlFor="name">Product Name *</label>
        <input
          id="name"
          name="name"
          type="text"
          value={form.name}
          onChange={handleChange}
          placeholder="Enter product name"
        />
        {validationErrors.name && <span className="error">{validationErrors.name}</span>}
      </div>

      <div className="form-group">
        <label htmlFor="description">Description</label>
        <textarea
          id="description"
          name="description"
          value={form.description}
          onChange={handleChange}
          rows={3}
          placeholder="Optional description"
        />
      </div>

      <div className="form-group">
        <label htmlFor="price">Price *</label>
        <input
          id="price"
          name="price"
          type="number"
          step="0.01"
          min="0"
          value={form.price}
          onChange={handleChange}
          placeholder="0.00"
        />
        {validationErrors.price && <span className="error">{validationErrors.price}</span>}
      </div>

      <div className="form-group">
        <label htmlFor="category">Category *</label>
        <select id="category" name="category" value={form.category} onChange={handleChange}>
          <option value="">Select a category</option>
          <option value="electronics">Electronics</option>
          <option value="clothing">Clothing</option>
          <option value="books">Books</option>
          <option value="home">Home & Garden</option>
        </select>
        {validationErrors.category && (
          <span className="error">{validationErrors.category}</span>
        )}
      </div>

      <div className="form-actions">
        <button type="submit" disabled={submitting}>
          {submitting ? 'Saving...' : initialValues ? 'Update Product' : 'Create Product'}
        </button>
        <button type="button" onClick={onCancel}>
          Cancel
        </button>
      </div>
    </form>
  );
}

// ─────────────────────────────────────────────────────────────
// 4. ProductCard component — displays a single product
// ─────────────────────────────────────────────────────────────
function ProductCard({ product, onEdit, onDelete }) {
  const [deleting, setDeleting] = useState(false);

  const handleDelete = async () => {
    if (!window.confirm(`Delete "${product.name}"?`)) return;
    setDeleting(true);
    try {
      await onDelete(product.id);
    } catch (err) {
      alert('Failed to delete product');
      setDeleting(false);
    }
  };

  return (
    <div className="product-card">
      <div className="product-info">
        <h3>{product.name}</h3>
        <p className="category">{product.category}</p>
        {product.description && <p className="description">{product.description}</p>}
        <p className="price">${product.price.toFixed(2)}</p>
      </div>
      <div className="product-actions">
        <button onClick={() => onEdit(product)} className="btn-edit">
          Edit
        </button>
        <button onClick={handleDelete} disabled={deleting} className="btn-delete">
          {deleting ? 'Deleting...' : 'Delete'}
        </button>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// 5. Main ProductsPage component — ties everything together
// ─────────────────────────────────────────────────────────────
function ProductsPage() {
  const navigate = useNavigate();
  const { products, loading, error, createProduct, updateProduct, deleteProduct } =
    useProducts();

  // Controls whether the create/edit form is visible
  const [showForm, setShowForm] = useState(false);
  // If set, we're editing this product; if null, we're creating
  const [editingProduct, setEditingProduct] = useState(null);
  // Feedback message after save/delete
  const [successMessage, setSuccessMessage] = useState(null);

  // Auto-clear success message after 3 seconds
  useEffect(() => {
    if (!successMessage) return;
    const timer = setTimeout(() => setSuccessMessage(null), 3000);
    return () => clearTimeout(timer); // cleanup
  }, [successMessage]);

  const handleCreateClick = () => {
    setEditingProduct(null);
    setShowForm(true);
  };

  const handleEditClick = (product) => {
    setEditingProduct(product);
    setShowForm(true);
  };

  const handleFormSubmit = async (formData) => {
    try {
      if (editingProduct) {
        await updateProduct(editingProduct.id, formData);
        setSuccessMessage(`"${formData.name}" updated successfully`);
      } else {
        await createProduct(formData);
        setSuccessMessage(`"${formData.name}" created successfully`);
      }
      setShowForm(false);
      setEditingProduct(null);
    } catch (err) {
      alert(err.response?.data?.message || 'Save failed. Please try again.');
    }
  };

  const handleFormCancel = () => {
    setShowForm(false);
    setEditingProduct(null);
  };

  // ── Render ────────────────────────────────────────────────
  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner" />
        <p>Loading products...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="error-container">
        <p className="error-message">⚠ {error}</p>
        <button onClick={() => navigate('/login')}>Back to Login</button>
      </div>
    );
  }

  return (
    <div className="products-page">
      <header className="page-header">
        <h1>Products</h1>
        <button onClick={handleCreateClick} className="btn-primary">
          + Add Product
        </button>
      </header>

      {successMessage && (
        <div className="success-banner" role="alert">
          ✓ {successMessage}
        </div>
      )}

      {showForm && (
        <div className="modal-overlay">
          <div className="modal">
            <h2>{editingProduct ? 'Edit Product' : 'New Product'}</h2>
            <ProductForm
              initialValues={editingProduct}
              onSubmit={handleFormSubmit}
              onCancel={handleFormCancel}
            />
          </div>
        </div>
      )}

      {products.length === 0 ? (
        <p className="empty-state">No products yet. Click "+ Add Product" to get started.</p>
      ) : (
        <div className="products-grid">
          {products.map((product) => (
            <ProductCard
              key={product.id}
              product={product}
              onEdit={handleEditClick}
              onDelete={deleteProduct}
            />
          ))}
        </div>
      )}
    </div>
  );
}

export default ProductsPage;
