/**
 * Browser-side client for the SaveWise API, reached through the `/api` rewrite in next.config.ts.
 *
 * Auth is an HttpOnly session cookie set by the API. State-changing requests must also echo the
 * readable `XSRF-TOKEN` cookie in the `X-XSRF-TOKEN` header (Spring Security's SPA CSRF scheme).
 */

const CSRF_COOKIE = "XSRF-TOKEN";
const CSRF_HEADER = "X-XSRF-TOKEN";

export class ApiError extends Error {
  constructor(
    readonly status: number,
    message: string,
    /** Field name to message, for validation failures. */
    readonly fieldErrors: Record<string, string> = {},
  ) {
    super(message);
    this.name = "ApiError";
  }
}

type Method = "GET" | "POST" | "PUT" | "DELETE";

interface RequestOptions {
  method?: Method;
  body?: unknown;
}

function readCookie(name: string): string | undefined {
  const match = document.cookie
    .split("; ")
    .find((part) => part.startsWith(`${name}=`));
  return match ? decodeURIComponent(match.slice(name.length + 1)) : undefined;
}

let csrfRequest: Promise<void> | null = null;

/** Makes sure the CSRF cookie exists, fetching it once even if many requests ask at the same time. */
function ensureCsrfCookie(force = false): Promise<void> {
  if (!force && readCookie(CSRF_COOKIE)) {
    return Promise.resolve();
  }
  csrfRequest ??= fetch("/api/auth/csrf", { credentials: "same-origin", cache: "no-store" })
    .then(() => undefined)
    .finally(() => {
      csrfRequest = null;
    });
  return csrfRequest;
}

const FALLBACK_MESSAGES: Record<number, string> = {
  401: "Please sign in to continue.",
  403: "That action isn't allowed. Please refresh the page and try again.",
  404: "We couldn't find that item. It may have been deleted.",
  409: "That conflicts with something that already exists.",
  429: "Too many attempts. Please wait a few minutes and try again.",
};

async function toApiError(response: Response): Promise<ApiError> {
  let problem: { detail?: string; errors?: Record<string, string> } | undefined;
  try {
    problem = await response.json();
  } catch {
    // Not JSON (e.g. a proxy error page); fall back to a generic message.
  }
  const message =
    problem?.detail ??
    FALLBACK_MESSAGES[response.status] ??
    (response.status >= 500
      ? "The SaveWise server is unavailable. Please try again shortly."
      : "Something went wrong. Please try again.");
  return new ApiError(response.status, message, problem?.errors ?? {});
}

async function send(path: string, method: Method, body: unknown): Promise<Response> {
  const headers: Record<string, string> = { Accept: "application/json" };
  if (method !== "GET") {
    await ensureCsrfCookie();
    const token = readCookie(CSRF_COOKIE);
    if (token) headers[CSRF_HEADER] = token;
  }
  if (body !== undefined) headers["Content-Type"] = "application/json";

  return fetch(`/api${path}`, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
    credentials: "same-origin",
    cache: "no-store",
  });
}

export async function api<T>(path: string, { method = "GET", body }: RequestOptions = {}): Promise<T> {
  let response: Response;
  try {
    response = await send(path, method, body);
    // A stale or missing CSRF cookie gives 403; refresh it once and retry.
    if (response.status === 403 && method !== "GET") {
      await ensureCsrfCookie(true);
      response = await send(path, method, body);
    }
  } catch {
    throw new ApiError(0, "Can't reach SaveWise. Check your connection and try again.");
  }

  if (response.status === 401 && !path.startsWith("/auth/")) {
    const next = window.location.pathname + window.location.search;
    // A full navigation on purpose: this runs outside React and should drop all client state.
    // eslint-disable-next-line @next/next/no-location-assign-relative-destination
    window.location.assign(`/login?expired=1&next=${encodeURIComponent(next)}`);
    throw new ApiError(401, "Your session has expired. Please sign in again.");
  }
  if (!response.ok) {
    throw await toApiError(response);
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return (await response.json()) as T;
}

export function errorMessage(error: unknown): string {
  return error instanceof ApiError || error instanceof Error ? error.message : "Something went wrong.";
}
