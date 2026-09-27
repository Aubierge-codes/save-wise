"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useState, type ReactNode } from "react";

import { Button } from "@/components/ui";
import { api } from "@/lib/api";
import type { User } from "@/lib/types";
import { useApi } from "@/lib/use-api";

const NAV = [
  { href: "/dashboard", label: "Dashboard" },
  { href: "/expenses", label: "Expenses" },
  { href: "/income", label: "Income" },
  { href: "/budgets", label: "Budgets" },
  { href: "/goals", label: "Goals" },
];

export function Logo() {
  return (
    <span className="flex items-center gap-2 text-lg font-semibold tracking-tight">
      <span className="grid size-8 place-items-center rounded-lg bg-brand text-white" aria-hidden>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" className="size-4.5">
          <path d="M4 17l5-5 4 4 7-8" strokeLinecap="round" strokeLinejoin="round" />
          <path d="M15 8h5v5" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      </span>
      SaveWise
    </span>
  );
}

export function AppShell({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const { data: user } = useApi<User>("/auth/me");
  const [signingOut, setSigningOut] = useState(false);

  async function signOut() {
    setSigningOut(true);
    try {
      await api("/auth/logout", { method: "POST" });
    } catch {
      // Leave anyway (e.g. offline); an unrevoked session cookie still expires on its own.
    }
    router.replace("/login");
    router.refresh();
  }

  return (
    <div className="flex min-h-full flex-col">
      <header className="sticky top-0 z-10 border-b border-line bg-surface/90 backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-3">
          <Link href="/dashboard" className="rounded-lg focus-visible:outline-2 focus-visible:outline-brand">
            <Logo />
          </Link>
          <nav className="hidden items-center gap-1 md:flex" aria-label="Main">
            {NAV.map((item) => (
              <NavLink key={item.href} {...item} active={pathname.startsWith(item.href)} />
            ))}
          </nav>
          <div className="flex items-center gap-2">
            {user && <span className="hidden text-sm text-muted sm:inline">{user.fullName}</span>}
            <Button variant="secondary" size="sm" onClick={signOut} disabled={signingOut}>
              {signingOut ? "Signing out…" : "Sign out"}
            </Button>
          </div>
        </div>
        <nav className="flex gap-1 overflow-x-auto px-3 pb-2 md:hidden" aria-label="Main">
          {NAV.map((item) => (
            <NavLink key={item.href} {...item} active={pathname.startsWith(item.href)} />
          ))}
        </nav>
      </header>
      <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-6 sm:py-8">{children}</main>
    </div>
  );
}

function NavLink({ href, label, active }: { href: string; label: string; active: boolean }) {
  return (
    <Link
      href={href}
      aria-current={active ? "page" : undefined}
      className={
        "whitespace-nowrap rounded-lg px-3 py-2 text-sm font-medium transition-colors " +
        (active ? "bg-brand-soft text-brand-strong" : "text-muted hover:bg-surface-muted hover:text-ink")
      }
    >
      {label}
    </Link>
  );
}
