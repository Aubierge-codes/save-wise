"use client";

import { useCallback, useEffect, useState } from "react";

import { api, ApiError } from "./api";

interface State<T> {
  key: string;
  data?: T;
  error?: ApiError;
}

/**
 * Loads `path` from the API and reloads when it changes. Previous data stays visible while a new
 * request is in flight so switching months doesn't flash an empty page.
 */
export function useApi<T>(path: string) {
  const [version, setVersion] = useState(0);
  const key = `${path}#${version}`;
  const [state, setState] = useState<State<T>>({ key: "" });

  useEffect(() => {
    let cancelled = false;
    api<T>(path)
      .then((data) => {
        if (!cancelled) setState({ key, data });
      })
      .catch((error: unknown) => {
        if (!cancelled) {
          setState((prev) => ({
            key,
            data: prev.data,
            error: error instanceof ApiError ? error : new ApiError(0, String(error)),
          }));
        }
      });
    return () => {
      cancelled = true;
    };
  }, [path, key]);

  const reload = useCallback(() => setVersion((v) => v + 1), []);

  return {
    data: state.data,
    error: state.key === key ? state.error : undefined,
    loading: state.key !== key,
    reload,
  };
}
