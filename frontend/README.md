# SaveWise web app

Next.js 16 frontend for SaveWise. See the [project README](../README.md) for the full setup.

```bash
npm install
npm run dev        # http://localhost:3000, expects the API on http://localhost:8080
```

Set `SAVEWISE_API_URL` (see `.env.example`) to point `/api/*` at a different API address.

## Layout

- `src/app/(auth)/` – sign-in and sign-up pages
- `src/app/(app)/` – signed-in pages: dashboard, expenses, income, budgets, goals
- `src/components/views/` – the client-side page bodies
- `src/lib/api.ts` – fetch wrapper that handles the session cookie, CSRF header and API errors
- `src/proxy.ts` – redirects signed-out visitors to `/login`
