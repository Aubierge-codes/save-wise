"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";

import { Alert, Button, Card, Field, Input } from "@/components/ui";
import { api, ApiError, errorMessage } from "@/lib/api";
import type { User } from "@/lib/types";

/** Only follow same-site relative paths after login, never `//evil.example` or absolute URLs. */
function safeNext(next: string | undefined): string {
  return next && next.startsWith("/") && !next.startsWith("//") && !next.startsWith("/\\") ? next : "/dashboard";
}

export function AuthForm({ mode, next, expired }: { mode: "login" | "register"; next?: string; expired?: boolean }) {
  const router = useRouter();
  const [values, setValues] = useState({ fullName: "", email: "", password: "" });
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [error, setError] = useState<string>();
  const [submitting, setSubmitting] = useState(false);
  const isRegister = mode === "register";

  function update(field: keyof typeof values, value: string) {
    setValues((v) => ({ ...v, [field]: value }));
    setFieldErrors((e) => ({ ...e, [field]: "" }));
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    setError(undefined);
    setFieldErrors({});
    try {
      const body = isRegister ? values : { email: values.email, password: values.password };
      await api<User>(isRegister ? "/auth/register" : "/auth/login", { method: "POST", body });
      router.replace(safeNext(next));
      router.refresh();
    } catch (e) {
      if (e instanceof ApiError && Object.keys(e.fieldErrors).length > 0) {
        setFieldErrors(e.fieldErrors);
      } else {
        setError(errorMessage(e));
      }
      setSubmitting(false);
    }
  }

  return (
    <Card className="p-6">
      <h1 className="text-xl font-semibold">{isRegister ? "Create your account" : "Welcome back"}</h1>
      <p className="mt-1 mb-6 text-sm text-muted">
        {isRegister ? "Start tracking your money in under a minute." : "Sign in to see your dashboard."}
      </p>

      <form onSubmit={submit} className="space-y-4" noValidate>
        {expired && !error && <Alert tone="INFO">Your session has ended. Please sign in again.</Alert>}
        {error && <Alert>{error}</Alert>}

        {isRegister && (
          <Field label="Full name" error={fieldErrors.fullName}>
            <Input
              name="fullName"
              autoComplete="name"
              required
              maxLength={100}
              value={values.fullName}
              onChange={(e) => update("fullName", e.target.value)}
              aria-invalid={!!fieldErrors.fullName}
            />
          </Field>
        )}
        <Field label="Email" error={fieldErrors.email}>
          <Input
            name="email"
            type="email"
            autoComplete="email"
            required
            value={values.email}
            onChange={(e) => update("email", e.target.value)}
            aria-invalid={!!fieldErrors.email}
          />
        </Field>
        <Field label="Password" error={fieldErrors.password} hint={isRegister ? "At least 8 characters." : undefined}>
          <Input
            name="password"
            type="password"
            autoComplete={isRegister ? "new-password" : "current-password"}
            required
            minLength={isRegister ? 8 : undefined}
            value={values.password}
            onChange={(e) => update("password", e.target.value)}
            aria-invalid={!!fieldErrors.password}
          />
        </Field>

        <Button type="submit" className="w-full" disabled={submitting}>
          {submitting ? "Please wait…" : isRegister ? "Create account" : "Sign in"}
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-muted">
        {isRegister ? "Already have an account? " : "New to SaveWise? "}
        <Link
          href={isRegister ? "/login" : "/register"}
          className="font-medium text-brand hover:text-brand-strong hover:underline"
        >
          {isRegister ? "Sign in" : "Create an account"}
        </Link>
      </p>
    </Card>
  );
}
