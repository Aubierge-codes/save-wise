import type { Category } from "./types";

const amountFormat = new Intl.NumberFormat("en-US", { maximumFractionDigits: 2 });

/** Formats an amount the way the original CLI printed it, e.g. `50,000 RWF`. */
export function formatMoney(amount: number): string {
  return `${amountFormat.format(amount)} RWF`;
}

export function formatPercent(value: number): string {
  return `${Number.isInteger(value) ? value : value.toFixed(1)}%`;
}

export const CATEGORY_META: Record<Category, { label: string; color: string }> = {
  FOOD: { label: "Food", color: "#f59e0b" },
  TRANSPORT: { label: "Transport", color: "#3b82f6" },
  EDUCATION: { label: "Education", color: "#8b5cf6" },
  ENTERTAINMENT: { label: "Entertainment", color: "#ec4899" },
  HEALTH: { label: "Health", color: "#10b981" },
  OTHER: { label: "Other", color: "#64748b" },
};

function pad(n: number): string {
  return String(n).padStart(2, "0");
}

/** `yyyy-MM` for the current local month. */
export function currentMonth(): string {
  const now = new Date();
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}`;
}

export function todayIso(): string {
  const now = new Date();
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`;
}

export function shiftMonth(month: string, delta: number): string {
  const [year, m] = month.split("-").map(Number);
  const date = new Date(year, m - 1 + delta, 1);
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}`;
}

export function monthLabel(month: string): string {
  const [year, m] = month.split("-").map(Number);
  return new Date(year, m - 1, 1).toLocaleDateString("en-US", { month: "long", year: "numeric" });
}

/** A sensible default date for a new entry in `month`: today if it's this month, else the 1st. */
export function defaultDateFor(month: string): string {
  const today = todayIso();
  return today.startsWith(month) ? today : `${month}-01`;
}

export function formatDate(iso: string): string {
  const [year, m, d] = iso.split("-").map(Number);
  return new Date(year, m - 1, d).toLocaleDateString("en-US", { day: "numeric", month: "short" });
}
