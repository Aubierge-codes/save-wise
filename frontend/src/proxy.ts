import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

/** Must match `savewise.cookie.name` in the API. */
const SESSION_COOKIE = "sw_session";

/**
 * Sends visitors without a session cookie to the login page. The cookie is HttpOnly and verified
 * by the API on every call; this check only avoids rendering app pages for signed-out users.
 */
export function proxy(request: NextRequest) {
  if (request.cookies.get(SESSION_COOKIE)?.value) {
    return NextResponse.next();
  }
  const login = new URL("/login", request.url);
  const { pathname, search } = request.nextUrl;
  if (pathname !== "/") {
    login.searchParams.set("next", pathname + search);
  }
  return NextResponse.redirect(login);
}

export const config = {
  matcher: [
    "/",
    "/dashboard/:path*",
    "/expenses/:path*",
    "/income/:path*",
    "/budgets/:path*",
    "/goals/:path*",
  ],
};
