"use client";

import Link from "next/link";
import type { ReactNode } from "react";

import { MonthPicker, useMonth } from "@/components/month-context";
import { Alert, Card, CardTitle, CategoryBadge, EmptyState, PageHeader, ProgressBar, Skeleton } from "@/components/ui";
import { STATUS_META } from "@/components/views/budgets-view";
import { goalEstimate } from "@/components/views/goals-view";
import { CATEGORY_META, formatMoney, formatPercent, monthLabel } from "@/lib/format";
import type { Dashboard } from "@/lib/types";
import { useApi } from "@/lib/use-api";

export function DashboardView() {
  const { month } = useMonth();
  const { data, error, loading } = useApi<Dashboard>(`/dashboard?month=${month}`);

  return (
    <>
      <PageHeader title="Dashboard" subtitle={`Your money in ${monthLabel(month)}`} action={<MonthPicker />} />
      {error && (
        <div className="mb-6">
          <Alert>{error.message}</Alert>
        </div>
      )}
      {!data && loading && <DashboardSkeleton />}
      {data && (
        <div className={"space-y-6 " + (loading ? "opacity-60 transition-opacity" : "")}>
          <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
            <Stat label="Income" value={formatMoney(data.totalIncome)} />
            <Stat
              label="Spent"
              value={formatMoney(data.totalExpenses)}
              note={data.spendingTooHigh ? "80%+ of income" : undefined}
              noteTone="danger"
            />
            <Stat
              label="Remaining"
              value={formatMoney(data.remaining)}
              valueClass={data.remaining < 0 ? "text-danger" : undefined}
            />
            <Stat
              label="Saving rate"
              value={data.totalIncome > 0 ? formatPercent(data.savingRate) : "—"}
              valueClass={data.savingRate >= 20 ? "text-good" : data.savingRate < 0 ? "text-danger" : undefined}
            />
          </div>

          <Alert tone={data.recommendation.tone}>
            <span className="font-semibold">Tip: </span>
            {data.recommendation.message}
          </Alert>

          {data.incomeCount === 0 && data.expenseCount === 0 ? (
            <Card>
              <EmptyState title={`Nothing recorded for ${monthLabel(month)} yet`}>
                Start by{" "}
                <Link href="/income" className="font-medium text-brand hover:underline">
                  adding your income
                </Link>{" "}
                and{" "}
                <Link href="/expenses" className="font-medium text-brand hover:underline">
                  logging expenses
                </Link>
                .
              </EmptyState>
            </Card>
          ) : (
            <div className="grid gap-6 lg:grid-cols-2">
              <Card>
                <CardTitle action={<ManageLink href="/expenses" />}>Spending by category</CardTitle>
                {data.spendingByCategory.length === 0 ? (
                  <p className="text-sm text-muted">No spending this month.</p>
                ) : (
                  <ul className="space-y-3">
                    {data.spendingByCategory.map((s) => (
                      <li key={s.category}>
                        <div className="mb-1 flex items-center justify-between text-sm">
                          <CategoryBadge category={s.category} />
                          <span className="tabular">
                            {formatMoney(s.amount)} <span className="text-muted">· {formatPercent(s.share)}</span>
                          </span>
                        </div>
                        <div className="h-2 overflow-hidden rounded-full bg-surface-muted">
                          <div
                            className="h-full rounded-full"
                            style={{ width: `${s.share}%`, backgroundColor: CATEGORY_META[s.category].color }}
                          />
                        </div>
                      </li>
                    ))}
                  </ul>
                )}
                {data.biggestExpense && (
                  <div className="mt-5 rounded-xl bg-surface-muted px-4 py-3 text-sm">
                    <span className="text-muted">Biggest expense: </span>
                    <span className="font-medium">{data.biggestExpense.description}</span>
                    <span className="tabular"> · {formatMoney(data.biggestExpense.amount)}</span>
                  </div>
                )}
              </Card>

              <Card>
                <CardTitle action={<ManageLink href="/budgets" />}>Budgets</CardTitle>
                {data.budgets.length === 0 ? (
                  <p className="text-sm text-muted">
                    No budgets yet.{" "}
                    <Link href="/budgets" className="font-medium text-brand hover:underline">
                      Set monthly limits
                    </Link>{" "}
                    to get warnings before you overspend.
                  </p>
                ) : (
                  <ul className="space-y-4">
                    {data.budgets.map((b) => {
                      const status = STATUS_META[b.status];
                      return (
                        <li key={b.category}>
                          <div className="mb-1 flex items-center justify-between gap-2 text-sm">
                            <CategoryBadge category={b.category} />
                            <span className="tabular text-muted">
                              {formatMoney(b.spent)} / {formatMoney(b.monthlyLimit)}
                            </span>
                          </div>
                          <ProgressBar
                            percent={b.usedPercent}
                            tone={status.tone}
                            label={`${CATEGORY_META[b.category].label} budget: ${status.label}`}
                          />
                        </li>
                      );
                    })}
                  </ul>
                )}
              </Card>
            </div>
          )}

          <Card>
            <CardTitle action={<ManageLink href="/goals" />}>Saving goals</CardTitle>
            {data.goals.length === 0 ? (
              <p className="text-sm text-muted">
                No goals yet.{" "}
                <Link href="/goals" className="font-medium text-brand hover:underline">
                  Create one
                </Link>{" "}
                to start saving towards something.
              </p>
            ) : (
              <ul className="grid gap-5 sm:grid-cols-2">
                {data.goals.map((g) => (
                  <li key={g.id}>
                    <div className="mb-1 flex items-center justify-between gap-2 text-sm">
                      <span className="truncate font-medium">{g.name}</span>
                      <span className="tabular text-muted">{formatPercent(g.progressPercentage)}</span>
                    </div>
                    <ProgressBar percent={g.progressPercentage} label={`${g.name} progress`} />
                    <p className="mt-1.5 text-xs text-muted">{goalEstimate(g)}</p>
                  </li>
                ))}
              </ul>
            )}
          </Card>
        </div>
      )}
    </>
  );
}

function Stat({
  label,
  value,
  note,
  noteTone,
  valueClass,
}: {
  label: string;
  value: ReactNode;
  note?: string;
  noteTone?: "danger";
  valueClass?: string;
}) {
  return (
    <Card className="p-4">
      <p className="text-sm text-muted">{label}</p>
      <p className={"tabular mt-1 text-lg font-semibold sm:text-xl " + (valueClass ?? "")}>{value}</p>
      {note && <p className={"mt-1 text-xs font-medium " + (noteTone === "danger" ? "text-danger" : "text-muted")}>{note}</p>}
    </Card>
  );
}

function ManageLink({ href }: { href: string }) {
  return (
    <Link href={href} className="text-sm font-medium text-brand hover:underline">
      Manage
    </Link>
  );
}

function DashboardSkeleton() {
  return (
    <div className="space-y-6">
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        {[0, 1, 2, 3].map((i) => (
          <Skeleton key={i} className="h-24 rounded-2xl" />
        ))}
      </div>
      <Skeleton className="h-12 rounded-xl" />
      <div className="grid gap-6 lg:grid-cols-2">
        <Skeleton className="h-64 rounded-2xl" />
        <Skeleton className="h-64 rounded-2xl" />
      </div>
    </div>
  );
}
