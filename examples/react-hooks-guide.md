# React Hooks — Comprehensive Guide

React Hooks let functional components use state, lifecycle methods, and other React features. This guide covers the most important hooks with practical examples.

---

## Rule of Hooks

Before diving in, know the two rules:

1. **Only call hooks at the top level** — never inside loops, conditions, or nested functions
2. **Only call hooks from React functions** — functional components or custom hooks

```jsx
// ❌ WRONG — hook inside a condition
function Bad({ isLoggedIn }) {
  if (isLoggedIn) {
    const [data, setData] = useState(null); // ERROR
  }
}

// ✅ CORRECT — hook always at top level
function Good({ isLoggedIn }) {
  const [data, setData] = useState(null); // Always called
  if (!isLoggedIn) return null;
  return <div>{data}</div>;
}
```

---

## `useState` — Managing Component State

`useState` adds a local state variable to your component.

```jsx
const [state, setState] = useState(initialValue);
```

- `state` — current value
- `setState` — function to update it (triggers re-render)
- `initialValue` — starting value (only used on the first render)

### Example 1: Toggle (Boolean State)

```jsx
function CollapsibleSection({ title, children }) {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <div>
      <button onClick={() => setIsOpen(prev => !prev)}>
        {isOpen ? '▼' : '▶'} {title}
      </button>
      {isOpen && <div className="content">{children}</div>}
    </div>
  );
}
```

### Example 2: Form (Object State)

```jsx
function RegistrationForm() {
  const [form, setForm] = useState({
    name: '',
    email: '',
    password: '',
    agreeToTerms: false,
  });

  // Update a single field without overwriting the others
  const updateField = (field) => (e) =>
    setForm(prev => ({ ...prev, [field]: e.target.value }));

  const handleSubmit = (e) => {
    e.preventDefault();
    console.log('Submitting:', form);
  };

  return (
    <form onSubmit={handleSubmit}>
      <input value={form.name}     onChange={updateField('name')}     placeholder="Name" />
      <input value={form.email}    onChange={updateField('email')}    placeholder="Email" type="email" />
      <input value={form.password} onChange={updateField('password')} placeholder="Password" type="password" />
      <label>
        <input
          type="checkbox"
          checked={form.agreeToTerms}
          onChange={e => setForm(prev => ({ ...prev, agreeToTerms: e.target.checked }))}
        />
        I agree to the terms
      </label>
      <button type="submit" disabled={!form.agreeToTerms}>Register</button>
    </form>
  );
}
```

### Example 3: List Management

```jsx
function ShoppingList() {
  const [items, setItems] = useState([
    { id: 1, name: 'Milk', checked: false },
    { id: 2, name: 'Bread', checked: false },
  ]);
  const [newItem, setNewItem] = useState('');

  const addItem = () => {
    if (!newItem.trim()) return;
    setItems(prev => [
      ...prev,
      { id: Date.now(), name: newItem.trim(), checked: false }
    ]);
    setNewItem('');
  };

  const toggleItem = (id) =>
    setItems(prev => prev.map(item =>
      item.id === id ? { ...item, checked: !item.checked } : item
    ));

  const removeItem = (id) =>
    setItems(prev => prev.filter(item => item.id !== id));

  return (
    <div>
      <div>
        <input
          value={newItem}
          onChange={e => setNewItem(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && addItem()}
          placeholder="Add item..."
        />
        <button onClick={addItem}>Add</button>
      </div>
      <ul>
        {items.map(item => (
          <li key={item.id}>
            <input
              type="checkbox"
              checked={item.checked}
              onChange={() => toggleItem(item.id)}
            />
            <span style={{ textDecoration: item.checked ? 'line-through' : 'none' }}>
              {item.name}
            </span>
            <button onClick={() => removeItem(item.id)}>✕</button>
          </li>
        ))}
      </ul>
    </div>
  );
}
```

### Important: State Updates Are Asynchronous

```jsx
// ❌ WRONG — stale state
const [count, setCount] = useState(0);
const incrementThreeTimes = () => {
  setCount(count + 1); // all three use the same `count` value
  setCount(count + 1);
  setCount(count + 1);
  // Result: count goes to 1, not 3
};

// ✅ CORRECT — use functional update
const incrementThreeTimes = () => {
  setCount(prev => prev + 1); // each uses the latest value
  setCount(prev => prev + 1);
  setCount(prev => prev + 1);
  // Result: count goes to 3
};
```

---

## `useEffect` — Side Effects and Lifecycle

`useEffect` runs code after the component renders. Use it for:
- Fetching data from an API
- Setting up subscriptions (WebSocket, event listeners)
- Starting timers
- Logging

```jsx
useEffect(() => {
  // Effect code runs after render
  return () => {
    // Cleanup runs before next effect and on unmount
  };
}, [dependency1, dependency2]);
```

### Lifecycle Mapping

| Class component lifecycle | useEffect equivalent |
|---------------------------|----------------------|
| `componentDidMount`       | `useEffect(() => { ... }, [])` |
| `componentDidUpdate`      | `useEffect(() => { ... }, [dep])` |
| `componentWillUnmount`    | `useEffect(() => { return () => cleanup(); }, [])` |

### Example 1: Fetch Data on Mount

```jsx
function UserList() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false; // prevent state update after unmount

    async function loadUsers() {
      try {
        const res = await fetch('/api/users');
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        if (!cancelled) setUsers(data);
      } catch (err) {
        if (!cancelled) setError(err.message);
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    loadUsers();

    return () => { cancelled = true; }; // cleanup
  }, []); // ← empty array: run once on mount

  if (loading) return <p>Loading...</p>;
  if (error)   return <p>Error: {error}</p>;
  return <ul>{users.map(u => <li key={u.id}>{u.name}</li>)}</ul>;
}
```

### Example 2: Re-fetch When a Dependency Changes

```jsx
function UserDetails({ userId }) {
  const [user, setUser] = useState(null);

  useEffect(() => {
    if (!userId) return; // guard: don't fetch if no ID

    setUser(null); // clear previous user while loading

    const controller = new AbortController(); // cancel in-flight request

    fetch(`/api/users/${userId}`, { signal: controller.signal })
      .then(res => res.json())
      .then(setUser)
      .catch(err => {
        if (err.name !== 'AbortError') console.error(err);
      });

    return () => controller.abort(); // cancel if userId changes
  }, [userId]); // ← re-run whenever userId changes

  if (!user) return <p>Loading...</p>;
  return <h2>{user.name}</h2>;
}
```

### Example 3: Timer with Cleanup

```jsx
function Countdown({ seconds }) {
  const [remaining, setRemaining] = useState(seconds);

  useEffect(() => {
    if (remaining <= 0) return;

    const timer = setInterval(() => {
      setRemaining(prev => prev - 1);
    }, 1000);

    return () => clearInterval(timer); // ← cleanup prevents memory leak
  }, [remaining]);

  return <p>{remaining > 0 ? `${remaining}s remaining` : 'Time is up!'}</p>;
}
```

### Example 4: Event Listener

```jsx
function KeyboardShortcuts() {
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.ctrlKey && e.key === 'k') {
        e.preventDefault();
        openSearchModal();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown); // cleanup!
  }, []); // only set up once

  return null; // this component has no visible output
}
```

---

## `useCallback` — Memoize Functions

Returns a memoized function that only changes if its dependencies change. Use when passing callbacks to child components that use `React.memo`.

```jsx
function ParentComponent() {
  const [count, setCount] = useState(0);
  const [name, setName] = useState('');

  // Without useCallback: new function reference on every render
  // With useCallback: same reference unless `count` changes
  const handleCountClick = useCallback(() => {
    setCount(prev => prev + 1);
  }, []); // no dependencies — function never changes

  return (
    <>
      <input value={name} onChange={e => setName(e.target.value)} />
      <ExpensiveChildComponent onClick={handleCountClick} count={count} />
    </>
  );
}
```

---

## `useMemo` — Memoize Computed Values

Returns a memoized value. Only recomputes when dependencies change. Use for expensive calculations.

```jsx
function ProductFilter({ products, searchTerm, category }) {
  // Only re-filters when products, searchTerm, or category changes
  const filteredProducts = useMemo(() => {
    return products
      .filter(p => category === 'all' || p.category === category)
      .filter(p => p.name.toLowerCase().includes(searchTerm.toLowerCase()));
  }, [products, searchTerm, category]);

  return (
    <ul>
      {filteredProducts.map(p => <li key={p.id}>{p.name}</li>)}
    </ul>
  );
}
```

---

## `useRef` — Mutable References and DOM Access

`useRef` returns a mutable ref object that persists for the full lifetime of the component. Changes to `ref.current` don't trigger re-renders.

### Access DOM Elements

```jsx
function FocusInput() {
  const inputRef = useRef(null);

  const focusInput = () => {
    inputRef.current.focus(); // directly access the DOM element
  };

  return (
    <>
      <input ref={inputRef} placeholder="I'll be focused" />
      <button onClick={focusInput}>Focus the input</button>
    </>
  );
}
```

### Store Mutable Values Without Re-render

```jsx
function StopwatchComponent() {
  const [elapsed, setElapsed] = useState(0);
  const intervalRef = useRef(null); // stores the interval ID

  const start = () => {
    intervalRef.current = setInterval(() => {
      setElapsed(prev => prev + 1);
    }, 1000);
  };

  const stop = () => {
    clearInterval(intervalRef.current);
  };

  return (
    <>
      <p>{elapsed}s</p>
      <button onClick={start}>Start</button>
      <button onClick={stop}>Stop</button>
    </>
  );
}
```

---

## `useContext` — Share Data Without Prop Drilling

Context lets you pass data through the component tree without manually passing props at every level.

```jsx
// 1. Create the context
const ThemeContext = createContext('light');

// 2. Provide it near the top of your tree
function App() {
  const [theme, setTheme] = useState('light');

  return (
    <ThemeContext.Provider value={{ theme, setTheme }}>
      <Layout />
    </ThemeContext.Provider>
  );
}

// 3. Consume it anywhere in the tree (no props needed)
function ThemeToggleButton() {
  const { theme, setTheme } = useContext(ThemeContext);

  return (
    <button onClick={() => setTheme(t => t === 'light' ? 'dark' : 'light')}>
      Switch to {theme === 'light' ? 'dark' : 'light'} mode
    </button>
  );
}
```

---

## Custom Hooks — Reusable Logic

Extract stateful logic into a reusable function. Custom hooks always start with `use`.

### Custom Hook: `useFetch`

```jsx
function useFetch(url) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);

    fetch(url)
      .then(res => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        return res.json();
      })
      .then(json => { if (!cancelled) setData(json); })
      .catch(err => { if (!cancelled) setError(err.message); })
      .finally(() => { if (!cancelled) setLoading(false); });

    return () => { cancelled = true; };
  }, [url]);

  return { data, loading, error };
}

// Usage — reuse in any component
function ProductPage({ productId }) {
  const { data: product, loading, error } = useFetch(`/api/products/${productId}`);

  if (loading) return <p>Loading...</p>;
  if (error)   return <p>Error: {error}</p>;
  return <h1>{product.name}</h1>;
}
```

### Custom Hook: `useLocalStorage`

```jsx
function useLocalStorage(key, defaultValue) {
  const [value, setValue] = useState(() => {
    try {
      const item = localStorage.getItem(key);
      return item ? JSON.parse(item) : defaultValue;
    } catch {
      return defaultValue;
    }
  });

  const setStoredValue = (newValue) => {
    setValue(newValue);
    localStorage.setItem(key, JSON.stringify(newValue));
  };

  return [value, setStoredValue];
}

// Usage
function Settings() {
  const [theme, setTheme] = useLocalStorage('theme', 'light');
  const [language, setLanguage] = useLocalStorage('language', 'en');

  return (
    <div>
      <select value={theme} onChange={e => setTheme(e.target.value)}>
        <option value="light">Light</option>
        <option value="dark">Dark</option>
      </select>
    </div>
  );
}
```

### Custom Hook: `useForm`

```jsx
function useForm(initialValues, validate) {
  const [values, setValues] = useState(initialValues);
  const [errors, setErrors] = useState({});
  const [touched, setTouched] = useState({});
  const [submitting, setSubmitting] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setValues(prev => ({ ...prev, [name]: value }));
  };

  const handleBlur = (e) => {
    const { name } = e.target;
    setTouched(prev => ({ ...prev, [name]: true }));
    if (validate) {
      const validationErrors = validate(values);
      setErrors(validationErrors);
    }
  };

  const handleSubmit = (onSubmit) => async (e) => {
    e.preventDefault();
    const validationErrors = validate ? validate(values) : {};
    setErrors(validationErrors);
    setTouched(Object.keys(values).reduce((acc, key) => ({ ...acc, [key]: true }), {}));

    if (Object.keys(validationErrors).length === 0) {
      setSubmitting(true);
      try {
        await onSubmit(values);
      } finally {
        setSubmitting(false);
      }
    }
  };

  return { values, errors, touched, submitting, handleChange, handleBlur, handleSubmit };
}

// Usage
function LoginForm() {
  const { values, errors, touched, submitting, handleChange, handleBlur, handleSubmit } =
    useForm(
      { email: '', password: '' },
      (vals) => {
        const errs = {};
        if (!vals.email) errs.email = 'Email is required';
        if (!vals.password) errs.password = 'Password is required';
        return errs;
      }
    );

  const submitLogin = handleSubmit(async (formValues) => {
    await loginUser(formValues);
  });

  return (
    <form onSubmit={submitLogin}>
      <input name="email" value={values.email} onChange={handleChange} onBlur={handleBlur} />
      {touched.email && errors.email && <span>{errors.email}</span>}

      <input type="password" name="password" value={values.password} onChange={handleChange} onBlur={handleBlur} />
      {touched.password && errors.password && <span>{errors.password}</span>}

      <button type="submit" disabled={submitting}>
        {submitting ? 'Logging in...' : 'Login'}
      </button>
    </form>
  );
}
```

---

## Hooks Quick Reference

| Hook          | Purpose                                            | When to use                              |
|---------------|----------------------------------------------------|------------------------------------------|
| `useState`    | Local component state                              | Any data that changes over time          |
| `useEffect`   | Side effects (API calls, subscriptions, timers)    | After render, when external things happen|
| `useCallback` | Memoize a function reference                       | Callbacks passed to memoized children    |
| `useMemo`     | Memoize a computed value                           | Expensive calculations                   |
| `useRef`      | Mutable value or DOM reference                     | DOM access, timers, previous values      |
| `useContext`  | Access shared context value                        | Theme, auth, language — global state     |
| Custom hooks  | Reusable stateful logic                            | Any pattern used in 2+ components        |
