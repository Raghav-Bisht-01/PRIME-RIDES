// In development, use Vite's same-origin proxy to avoid browser CORS preflights.
// Set VITE_API_URL when the frontend and backend are hosted separately.
const configuredApiUrl = import.meta.env.VITE_API_URL || "";

export const API_URL = configuredApiUrl.replace(/\/+$/, "");
