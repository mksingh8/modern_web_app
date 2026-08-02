import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import App from './App';

const renderAtRoute = (route) => {
  window.history.pushState({}, '', route);
  return render(<App />);
};

describe('App routes', () => {
  it('renders the login page placeholder', () => {
    renderAtRoute('/login');

    expect(screen.getByRole('heading', { name: 'Login' })).toBeTruthy();
    expect(screen.getByText('Login page placeholder')).toBeTruthy();
  });

  it('renders the dashboard page placeholder', () => {
    renderAtRoute('/dashboard');

    expect(screen.getByRole('heading', { name: 'Dashboard' })).toBeTruthy();
    expect(screen.getByText('Dashboard page placeholder')).toBeTruthy();
  });

  it('renders the task list page placeholder', () => {
    renderAtRoute('/tasks');

    expect(screen.getByRole('heading', { name: 'Tasks' })).toBeTruthy();
    expect(screen.getByText('Task list page placeholder')).toBeTruthy();
  });

  it('renders the task details page placeholder for a task id', () => {
    renderAtRoute('/tasks/42');

    expect(screen.getByRole('heading', { name: 'Task Details' })).toBeTruthy();
    expect(screen.getByText('Task details page placeholder')).toBeTruthy();
    expect(screen.getByText('Task ID: 42')).toBeTruthy();
  });
});
