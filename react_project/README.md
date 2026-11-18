# SeatSync frontend

SeatSync is a React 18 and Vite bus booking client. Styling uses Tailwind CSS, routing uses React Router, and API calls share one Axios client.

## Local development

1. Install Node.js 22 or newer.
2. From this directory, copy `.env.example` to `.env.local`.
3. Set `VITE_API_URL` to the Spring Boot API origin (default: `http://localhost:8080`).
4. Run:

```powershell
npm ci
npm run dev
```

Vite prints the local URL, normally `http://localhost:5173`.

## Checks

```powershell
npm run lint
npm run build
```

## Docker

Build the static site with the API origin embedded in the frontend bundle:

```powershell
docker build --build-arg VITE_API_URL=http://localhost:8080 -t seatsync-frontend .
docker run --rm -p 3000:80 seatsync-frontend
```

Open `http://localhost:3000`. Nginx serves the Vite build and falls back to `index.html` for client-side routes.

## API paths used

The client uses documented API paths under `/auth`, `/trips`, `/bookings`, `/admin`, and `/chat`. User endpoints require the bearer token stored under `seatsync_token`; the shared client adds it automatically. Never place secrets in `VITE_*` variables because Vite embeds them in browser code.
