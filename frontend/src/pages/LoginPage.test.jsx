import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, waitFor, cleanup } from '@testing-library/react';
import LoginPage from './LoginPage';
import * as authService from '../services/authService';

vi.mock('../services/authService');

beforeEach(() => {
  vi.clearAllMocks();
  localStorage.clear();
  window.history.pushState({}, '', '/login');
});

afterEach(() => {
  cleanup();
});

describe('LoginPage', () => {
  it('renders the login form', () => {
    render(<LoginPage />);

    expect(screen.getByRole('heading', { name: 'Login' })).toBeTruthy();
    expect(screen.getByLabelText('Username')).toBeTruthy();
    expect(screen.getByLabelText('Password')).toBeTruthy();
    expect(screen.getByRole('button', { name: 'Log In' })).toBeTruthy();
  });

  it('shows validation errors when submitting empty fields', async () => {
    render(<LoginPage />);

    fireEvent.click(screen.getByRole('button', { name: 'Log In' }));

    await waitFor(() => {
      expect(screen.getByText('Username is required.')).toBeTruthy();
      expect(screen.getByText('Password is required.')).toBeTruthy();
    });
  });

  it('shows a validation error only for empty username', async () => {
    render(<LoginPage />);

    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'secret' } });
    fireEvent.click(screen.getByRole('button', { name: 'Log In' }));

    await waitFor(() => {
      expect(screen.getByText('Username is required.')).toBeTruthy();
      expect(screen.queryByText('Password is required.')).toBeNull();
    });
  });

  it('shows an API error message on invalid credentials', async () => {
    authService.login.mockRejectedValue(new Error('Invalid credentials'));

    render(<LoginPage />);

    fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'user' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'wrongpass' } });
    fireEvent.click(screen.getByRole('button', { name: 'Log In' }));

    await waitFor(() => {
      expect(
        screen.getByText('Invalid username or password. Please try again.')
      ).toBeTruthy();
    });
  });

  it('navigates to /dashboard on successful login', async () => {
    authService.login.mockResolvedValue({ token: 'fake-jwt' });

    render(<LoginPage />);

    fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'user' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'correct' } });
    fireEvent.click(screen.getByRole('button', { name: 'Log In' }));

    await waitFor(() => {
      expect(window.location.pathname).toBe('/dashboard');
    });
  });
});
