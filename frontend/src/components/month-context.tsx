"use client";

import { createContext, useContext, useState, useSyncExternalStore, type ReactNode } from "react";

import { Button } from "@/components/ui";
import { currentMonth, monthLabel, shiftMonth } from "@/lib/format";

interface MonthState {
  month: string;
  setMonth: (month: string) => void;
}

const MonthContext = createContext<MonthState | null>(null);

const subscribeNever = () => () => {};

/**
 * The month (yyyy-MM) the dashboard, expenses and income pages are showing, shared across pages.
 *
 * "This month" depends on the viewer's clock and timezone, so it is only resolved in the
 * browser: pages are prerendered, and the server's month could differ. Children render once
 * the month is known, which happens immediately after hydration.
 */
export function MonthProvider({ children }: { children: ReactNode }) {
  const thisMonth = useSyncExternalStore(subscribeNever, currentMonth, () => null);
  const [picked, setMonth] = useState<string | null>(null);
  const month = picked ?? thisMonth;
  if (!month) return null;
  return <MonthContext.Provider value={{ month, setMonth }}>{children}</MonthContext.Provider>;
}

export function useMonth(): MonthState {
  const value = useContext(MonthContext);
  if (!value) throw new Error("useMonth must be used inside <MonthProvider>");
  return value;
}

export function MonthPicker() {
  const { month, setMonth } = useMonth();
  const isCurrent = month === currentMonth();
  return (
    <div className="flex items-center gap-1 rounded-xl border border-line bg-surface p-1">
      <Button variant="ghost" size="sm" onClick={() => setMonth(shiftMonth(month, -1))} aria-label="Previous month">
        <Chevron direction="left" />
      </Button>
      <span className="min-w-36 text-center text-sm font-medium" aria-live="polite">
        {monthLabel(month)}
      </span>
      <Button
        variant="ghost"
        size="sm"
        onClick={() => setMonth(shiftMonth(month, 1))}
        disabled={isCurrent}
        aria-label="Next month"
      >
        <Chevron direction="right" />
      </Button>
      {!isCurrent && (
        <Button variant="ghost" size="sm" onClick={() => setMonth(currentMonth())}>
          Today
        </Button>
      )}
    </div>
  );
}

function Chevron({ direction }: { direction: "left" | "right" }) {
  return (
    <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" strokeWidth="2" className="size-4" aria-hidden>
      <path d={direction === "left" ? "M12.5 15 7.5 10l5-5" : "M7.5 5l5 5-5 5"} strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}
