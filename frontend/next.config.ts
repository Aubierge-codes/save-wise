import type { NextConfig } from "next";

// The browser only ever talks to this Next.js origin. `/api/*` is forwarded to the Spring Boot
// API so the session and CSRF cookies are first-party and no CORS setup is needed.
const apiUrl = process.env.SAVEWISE_API_URL ?? "http://localhost:8080";

const nextConfig: NextConfig = {
  async rewrites() {
    return [{ source: "/api/:path*", destination: `${apiUrl}/api/:path*` }];
  },
};

export default nextConfig;
