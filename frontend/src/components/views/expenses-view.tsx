"use client";

import { useState, type FormEvent } from "react";

import { MonthPicker, useMonth } from "@/components/month-context";
import { RowActions } from "@/components/row-actions";
import {
  Alert,
  Button,
  Card,
  CardTitle,
  CategoryBadge,
  EmptyState,
  Field,
  Input,
  ListSkeleton,
  PageHeader,
  Select,
} from "@/components/ui";
import { api } from "@/lib/api";
import { CATEGORY_META, defaultDateFor, formatDate, formatMoney, monthLabel } from "@/lib/format";
import { CATEGORIES, type Category, type Expense } from "@/lib/types";
import { useApi } from "@/lib/use-api";
import { useSubmit } from "@/lib/use-submit";

export function ExpensesView() {
  const { month } = useMonth();
  const { data: expenses, error, loading, reload } = useApi<Expense[]>(`/expenses?month=${month}`);
  const [editing, setEditing] = useState<Expense | null>(null);
  const total = expenses?.reduce((sum, e) => sum + e.amount, 0) ?? 0;

  return (
    <>
      <PageHeader
        title="Expenses"
        subtitle={expenses ? `${expenses.length} in ${monthLabel(month)} · ${formatMoney(total)} spent` : monthLabel(month)}
        action={<MonthPicker />}
      />
      <div className="grid items-start gap-6 lg:grid-cols-[minmax(0,360px)_1fr]">
        <Card>
          <ExpenseForm
            key={editing ? `edit-${editing.id}` : `new-${month}`}
            month={month}
            editing={editing}
            onSaved={() => {
              setEditing(null);
              reload();
            }}
            onCancel={() => setEditing(null)}
          />
        </Card>
        <Card>
          <CardTitle>{monthLabel(month)}</CardTitle>
          {error && <Alert>{error.message}</Alert>}
          {!expenses && loading && <ListSkeleton />}
          {expenses && expenses.length === 0 && (
            <EmptyState title="No expenses yet">Add what you spend to see where your money goes.</EmptyState>
          )}
          {expenses && expenses.length > 0 && (
            <ul className={"divide-y divide-line " + (loading ? "opacity-60" : "")}>
              {expenses.map((expense) => (
                <li key={expense.id} className="flex items-center gap-3 py-3">
                  <span className="w-14 shrink-0 text-sm text-muted">{formatDate(expense.spentOn)}</span>
                  <div className="min-w-0 flex-1">
                    <p className="truncate font-medium">{expense.description}</p>
                    <CategoryBadge category={expense.category} />
                  </div>
                  <span className="tabular shrink-0 font-medium">{formatMoney(expense.amount)}</span>
                  <RowActions
                    label={expense.description}
                    onEdit={() => setEditing(expense)}
                    onDelete={async () => {
                      await api(`/expenses/${expense.id}`, { method: "DELETE" });
                      if (editing?.id === expense.id) setEditing(null);
                      reload();
                    }}
                  />
                </li>
              ))}
            </ul>
          )}
        </Card>
      </div>
    </>
  );
}

interface Draft {
  description: string;
  category: Category;
  amount: string;
  spentOn: string;
}

function ExpenseForm({
  month,
  editing,
  onSaved,
  onCancel,
}: {
  month: string;
  editing: Expense | null;
  onSaved: () => void;
  onCancel: () => void;
}) {
  const blank: Draft = { description: "", category: "FOOD", amount: "", spentOn: defaultDateFor(month) };
  const [draft, setDraft] = useState<Draft>(
    editing
      ? { description: editing.description, category: editing.category, amount: String(editing.amount), spentOn: editing.spentOn }
      : blank,
  );
  const { run, submitting, error, fieldErrors } = useSubmit();

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const body = { ...draft, description: draft.description.trim(), amount: Number(draft.amount) };
    const saved = await run(() =>
      api(editing ? `/expenses/${editing.id}` : "/expenses", { method: editing ? "PUT" : "POST", body }),
    );
    if (saved) {
      if (!editing) setDraft({ ...blank, category: draft.category, spentOn: draft.spentOn });
      onSaved();
    }
  }

  return (
    <form onSubmit={submit} className="space-y-4" noValidate>
      <CardTitle>{editing ? "Edit expense" : "Add an expense"}</CardTitle>
      {error && <Alert>{error}</Alert>}
      <Field label="What did you pay for?" error={fieldErrors.description}>
        <Input
          required
          maxLength={120}
          placeholder="e.g. Lunch"
          value={draft.description}
          onChange={(e) => setDraft({ ...draft, description: e.target.value })}
          aria-invalid={!!fieldErrors.description}
        />
      </Field>
      <div className="grid grid-cols-2 gap-3">
        <Field label="Category" error={fieldErrors.category}>
          <Select value={draft.category} onChange={(e) => setDraft({ ...draft, category: e.target.value as Category })}>
            {CATEGORIES.map((c) => (
              <option key={c} value={c}>
                {CATEGORY_META[c].label}
              </option>
            ))}
          </Select>
        </Field>
        <Field label="Amount (RWF)" error={fieldErrors.amount}>
          <Input
            type="number"
            inputMode="decimal"
            min="0"
            step="any"
            required
            placeholder="0"
            value={draft.amount}
            onChange={(e) => setDraft({ ...draft, amount: e.target.value })}
            aria-invalid={!!fieldErrors.amount}
          />
        </Field>
      </div>
      <Field label="Date" error={fieldErrors.spentOn}>
        <Input
          type="date"
          required
          value={draft.spentOn}
          onChange={(e) => setDraft({ ...draft, spentOn: e.target.value })}
          aria-invalid={!!fieldErrors.spentOn}
        />
      </Field>
      <div className="flex gap-2">
        <Button type="submit" disabled={submitting} className="flex-1">
          {submitting ? "Saving…" : editing ? "Save changes" : "Add expense"}
        </Button>
        {editing && (
          <Button variant="secondary" onClick={onCancel}>
            Cancel
          </Button>
        )}
      </div>
    </form>
  );
}
