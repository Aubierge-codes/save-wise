"use client";

import { useState, type FormEvent } from "react";

import { MonthPicker, useMonth } from "@/components/month-context";
import { Alert, Button, Card, CategoryBadge, Input, ListSkeleton, PageHeader, ProgressBar } from "@/components/ui";
import { api } from "@/lib/api";
import { formatMoney, formatPercent, monthLabel } from "@/lib/format";
import { CATEGORIES, type BudgetStatus, type BudgetUsage, type Category, type Dashboard, type Tone } from "@/lib/types";
import { useApi } from "@/lib/use-api";
import { useSubmit } from "@/lib/use-submit";

export const STATUS_META: Record<BudgetStatus, { label: string; tone: Tone }> = {
  UNDER_BUDGET: { label: "On track", tone: "GOOD" },
  NEAR_LIMIT: { label: "Near limit", tone: "WARNING" },
  OVER_BUDGET: { label: "Over budget", tone: "DANGER" },
};

const STATUS_TEXT: Record<Tone, string> = {
  GOOD: "text-good",
  INFO: "text-info",
  WARNING: "text-warn",
  DANGER: "text-danger",
};

export function BudgetsView() {
  const { month } = useMonth();
  // The dashboard already pairs each budget with this month's spending.
  const { data, error, loading, reload } = useApi<Dashboard>(`/dashboard?month=${month}`);

  return (
    <>
      <PageHeader
        title="Budgets"
        subtitle="Set a monthly limit per category. You'll be warned at 80% and when you go over."
        action={<MonthPicker />}
      />
      {error && <Alert>{error.message}</Alert>}
      {!data && loading && <ListSkeleton />}
      {data && (
        <div className={"grid gap-4 sm:grid-cols-2 lg:grid-cols-3 " + (loading ? "opacity-60" : "")}>
          {CATEGORIES.map((category) => {
            const usage = data.budgets.find((b) => b.category === category);
            const spent = data.spendingByCategory.find((s) => s.category === category)?.amount ?? 0;
            return (
              <BudgetCard
                key={`${category}-${usage?.monthlyLimit ?? "none"}`}
                category={category}
                usage={usage}
                spent={spent}
                month={month}
                onChanged={reload}
              />
            );
          })}
        </div>
      )}
    </>
  );
}

function BudgetCard({
  category,
  usage,
  spent,
  month,
  onChanged,
}: {
  category: Category;
  usage?: BudgetUsage;
  spent: number;
  month: string;
  onChanged: () => void;
}) {
  const [limit, setLimit] = useState(usage ? String(usage.monthlyLimit) : "");
  const { run, submitting, error, fieldErrors } = useSubmit();
  const status = usage ? STATUS_META[usage.status] : undefined;
  const dirty = limit !== (usage ? String(usage.monthlyLimit) : "");

  async function save(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const ok = await run(() => api(`/budgets/${category}`, { method: "PUT", body: { monthlyLimit: Number(limit) } }));
    if (ok) onChanged();
  }

  async function remove() {
    const ok = await run(() => api(`/budgets/${category}`, { method: "DELETE" }));
    if (ok) onChanged();
  }

  return (
    <Card className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <CategoryBadge category={category} />
        {status && <span className={"text-xs font-semibold " + STATUS_TEXT[status.tone]}>{status.label}</span>}
      </div>

      <div>
        <p className="tabular text-xl font-semibold">{formatMoney(spent)}</p>
        <p className="text-sm text-muted">
          {usage
            ? `of ${formatMoney(usage.monthlyLimit)} · ${usage.left >= 0 ? `${formatMoney(usage.left)} left` : `${formatMoney(-usage.left)} over`}`
            : `spent in ${monthLabel(month)} · no budget set`}
        </p>
      </div>

      {usage && status && (
        <div className="space-y-1">
          <ProgressBar percent={usage.usedPercent} tone={status.tone} label={`${category} budget used`} />
          <p className="text-right text-xs text-muted">{formatPercent(usage.usedPercent)} used</p>
        </div>
      )}

      <form onSubmit={save} className="mt-auto space-y-2" noValidate>
        {(error || fieldErrors.monthlyLimit) && <Alert>{error ?? fieldErrors.monthlyLimit}</Alert>}
        <div className="flex gap-2">
          <Input
            type="number"
            inputMode="decimal"
            min="0"
            step="any"
            placeholder="Monthly limit (RWF)"
            aria-label={`Monthly limit for ${category.toLowerCase()}`}
            value={limit}
            onChange={(e) => setLimit(e.target.value)}
          />
          <Button type="submit" variant={usage ? "secondary" : "primary"} disabled={submitting || !dirty || !limit}>
            {usage ? "Update" : "Set"}
          </Button>
        </div>
        {usage && (
          <Button variant="danger" size="sm" onClick={remove} disabled={submitting}>
            Remove budget
          </Button>
        )}
      </form>
    </Card>
  );
}
