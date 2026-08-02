import { useEffect, useState } from 'react';
import DashboardPage from '../pages/DashboardPage';
import LoginPage from '../pages/LoginPage';
import TaskDetailsPage from '../pages/TaskDetailsPage';
import TaskListPage from '../pages/TaskListPage';

const taskDetailsRoutePattern = /^\/tasks\/([^/]+)$/;

function resolveRoute(pathname) {
  if (pathname === '/login') {
    return <LoginPage />;
  }

  if (pathname === '/dashboard') {
    return <DashboardPage />;
  }

  if (pathname === '/tasks') {
    return <TaskListPage />;
  }

  const taskDetailsMatch = pathname.match(taskDetailsRoutePattern);
  if (taskDetailsMatch) {
    return <TaskDetailsPage taskId={decodeURIComponent(taskDetailsMatch[1])} />;
  }

  return (
    <main>
      <h1>Page Not Found</h1>
      <p>No placeholder is available for this route yet.</p>
    </main>
  );
}

export function AppRoutes() {
  const [pathname, setPathname] = useState(window.location.pathname);

  useEffect(() => {
    const handlePopState = () => setPathname(window.location.pathname);

    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  return resolveRoute(pathname);
}
