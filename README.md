# PRIME-RIDES

## Frontend

The frontend is a React application powered by Vite.

```powershell
cd frontend
npm install
npm run dev
```

Use `npm run build` to create a production build in `frontend/dist`.

## Deploy to Vercel with Neon PostgreSQL

Vercel runs the Flask API and serves the built Vite app from the same project
domain. The frontend uses relative `/api` URLs, so no cross-origin API URL or
CORS setup is needed in production. Vercel's build script copies the Vite output
into its `public/` static-assets directory.

### Vercel environment variables

Add these under **Vercel → Project → Settings → Environment Variables** for
Production, Preview, and Development:

- `DATABASE_URL`: use the **pooled** connection string from Neon (**Connect** →
  select the `production` branch and `neondb` → enable pooled connection). Keep
  `sslmode=require` in the URL. Never commit or paste this secret into source.
- `SECRET_KEY`: a long random value generated in Vercel.

Do not set `VITE_API_URL`; the app intentionally calls `/api` on its own domain.
The checked-in `database/schema_postgres.sql` is the source of truth. The Neon
database used for this project was verified to contain all 11 expected tables,
the booking columns used by the API, and 8 available vehicles. `/api/health`
checks connectivity and schema readiness and reports the available vehicle count.
If the health endpoint reports missing tables or columns, run
`database/schema_postgres.sql` in the Neon SQL Editor for that same branch and
database. Do not run the MySQL `database/schema.sql` against Neon.

### Deploy and run Selenium

1. Push the project to GitHub without committing `.env` files.
2. In Vercel, choose **Add New → Project**, import the repository, and keep the
   repository root as the Root Directory. Vercel reads `pyproject.toml` and
   `requirements.txt`; its build script builds the frontend and the Flask app
   handles API and SPA routes.
3. Add `DATABASE_URL` and `SECRET_KEY` as above, then deploy.
4. Open `https://YOUR-PROJECT.vercel.app/api/health`. Wait for `"status":"healthy"`
   and `"schema":"ready"` before opening the website.
5. From a third PowerShell terminal, run the suite against the deployed site:

   ```powershell
   cd C:\Users\neeha\Downloads\PRIME-RIDES\testing\prime-rides-tests
   & "C:\Tools\apache-maven-3.10.0\bin\mvn.cmd" clean test "-DbaseUrl=https://YOUR-PROJECT.vercel.app"
   ```

6. Open the graphical result dashboard:

   ```powershell
   ii .\target\prime-rides-test-report.html
   ```

The report contains the actual pass/fail/skip counts from the run. Keep the
Neon database active while testing; the suite's UI catalog cases use its
available vehicle rows.

## UI automation tests

The Maven Selenium WebDriver and TestNG test project is in `testing/prime-rides-tests`.
Start the frontend, then run `mvn clean test` from that directory. See its README for
requirements, test cases, and options for changing the app URL or browser mode.
