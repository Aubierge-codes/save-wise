"use client";

import { useState, type FormEvent } from "react";

import { MonthPicker, useMonth } from "@/components/month-context";
import { RowActions } from "@/components/row-actions";
import { Alert, Button, Card, CardTitle, EmptyState, Field, Input, ListSkeleton, PageHeader } from "@/components/ui";
import { api } from "@/lib/api";
import { defaultDateFor, formatDate, formatMoney, monthLabel } from "@/lib/format";
import type { Income } from "@/lib/types";
import { useApi } from "@/lib/use-api";
import { useSubmit } from "@/lib/use-submit";

export function IncomeView() {
  const { month } = useMonth();
  const { data: incomes, error, loading, reload } = useApi<Income[]>(`/incomes?month=${month}`);
  const [editing, setEditing] = useState<Income | null>(null);
  const total = incomes?.reduce((sum, i) => sum + i.amount, 0) ?? 0;

  return (
    <>
      <PageHeader
        title="Income"
        subtitle={incomes ? `${formatMoney(total)} received in ${monthLabel(month)}` : monthLabel(month)}
        action={<MonthPicker />}
      />
      <div className="grid items-start gap-6 lg:grid-cols-[minmax(0,360px)_1fr]">
        <Card>
          <IncomeForm
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
          {!incomes && loading && <ListSkeleton />}
          {incomes && incomes.length === 0 && (
            <EmptyState title="No income recorded">
              Add your allowance, salary or other earnings so SaveWise can work out your saving rate.
            </EmptyState>
          )}
          {incomes && incomes.length > 0 && (
            <ul className={"divide-y divide-line " + (loading ? "opacity-60" : "")}>
              {incomes.map((income) => (
                <li key={income.id} className="flex items-center gap-3 py-3">
                  <span className="w-14 shrink-0 text-sm text-muted">{formatDate(income.receivedOn)}</span>
                  <p className="min-w-0 flex-1 truncate font-medium">{income.source}</p>
                  <span className="tabular shrink-0 font-medium text-good">+{formatMoney(income.amount)}</span>
                  <RowActions
                    label={income.source}
                    onEdit={() => setEditing(income)}
                    onDelete={async () => {
                      await api(`/incomes/${income.id}`, { method: "DELETE" });
                      if (editing?.id === income.id) setEditing(null);
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
  source: string;
  amount: string;
  receivedOn: string;
}

function IncomeForm({
  month,
  editing,
  onSaved,
  onCancel,
}: {
  month: string;
  editing: Income | null;
  onSaved: () => void;
  onCancel: () => void;
}) {
  const blank: Draft = { source: "", amount: "", receivedOn: defaultDateFor(month) };
  const [draft, setDraft] = useState<Draft>(
    editing ? { source: editing.source, amount: String(editing.amount), receivedOn: editing.receivedOn } : blank,
  );
  const { run, submitting, error, fieldErrors } = useSubmit();

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const body = { ...draft, source: draft.source.trim(), amount: Number(draft.amount) };
    const saved = await run(() =>
      api(editing ? `/incomes/${editing.id}` : "/incomes", { method: editing ? "PUT" : "POST", body }),
    );
    if (saved) {
      if (!editing) setDraft(blank);
      onSaved();
    }
  }

  return (
    <form onSubmit={submit} className="space-y-4" noValidate>
      <CardTitle>{editing ? "Edit income" : "Add income"}</CardTitle>
      {error && <Alert>{error}</Alert>}
      <Field label="Source" error={fieldErrors.source}>
        <Input
          required
          maxLength={120}
          placeholder="e.g. Allowance, Salary"
          value={draft.source}
          onChange={(e) => setDraft({ ...draft, source: e.target.value })}
          aria-invalid={!!fieldErrors.source}
        />
      </Field>
      <div className="grid grid-cols-2 gap-3">
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
        <Field label="Date received" error={fieldErrors.receivedOn}>
          <Input
            type="date"
            required
            value={draft.receivedOn}
            onChange={(e) => setDraft({ ...draft, receivedOn: e.target.value })}
            aria-invalid={!!fieldErrors.receivedOn}
          />
        </Field>
      </div>
      <div className="flex gap-2">
        <Button type="submit" disabled={submitting} className="flex-1">
          {submitting ? "Saving…" : editing ? "Save changes" : "Add income"}
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
