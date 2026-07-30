import React, { useState } from 'react';
import { addToCart } from '../api';

/**
 * ProductCard Component
 *
 * Displays a single product with its details and an "Add to Cart" button.
 * Demonstrates:
 *   - Functional React components
 *   - useState hook for local state management
 *   - Calling a REST API from a React component
 *   - JWT authentication via the Authorization header (handled in api.js)
 */
function ProductCard({ product }) {
  const [inCart, setInCart] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  /**
   * Handles the "Add to Cart" button click.
   * Calls the REST API and updates local state based on the result.
   */
  const handleAddToCart = async () => {
    setLoading(true);
    setError(null);

    try {
      await addToCart(product.id);
      setInCart(true);
    } catch (err) {
      setError('Failed to add to cart. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="product-card">
      <img src={product.imageUrl} alt={product.name} className="product-card__image" />

      <div className="product-card__body">
        <h3 className="product-card__name">{product.name}</h3>
        <p className="product-card__description">{product.description}</p>
        <p className="product-card__price">${product.price.toFixed(2)}</p>

        {error && <p className="product-card__error">{error}</p>}

        <button
          className="product-card__button"
          onClick={handleAddToCart}
          disabled={inCart || loading}
        >
          {loading ? 'Adding...' : inCart ? 'Added to Cart ✓' : 'Add to Cart'}
        </button>
      </div>
    </div>
  );
}

export default ProductCard;
