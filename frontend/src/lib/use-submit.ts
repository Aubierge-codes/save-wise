"use client";

import { useState } from "react";

import { ApiError, errorMessage } from "./api";

/**
 * Runs a form action and tracks its pending state and errors. Validation errors from the API
 * land in `fieldErrors`; anything else in `error`.
 */
export function useSubmit() {
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string>();
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  async function run(action: () => Promise<void>): Promise<boolean> {
    setSubmitting(true);
    setError(undefined);
    setFieldErrors({});
    try {
      await action();
      return true;
    } catch (e) {
      if (e instanceof ApiError && Object.keys(e.fieldErrors).length > 0) {
        setFieldErrors(e.fieldErrors);
      } else {
        setError(errorMessage(e));
      }
      return false;
    } finally {
      setSubmitting(false);
    }
  }

  function clear() {
    setError(undefined);
    setFieldErrors({});
  }

  return { run, submitting, error, fieldErrors, clear };
}
