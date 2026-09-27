"use client";

import { useState, type FormEvent } from "react";

import { RowActions } from "@/components/row-actions";
import { Alert, Button, Card, CardTitle, EmptyState, Field, Input, ListSkeleton, PageHeader, ProgressBar } from "@/components/ui";
import { api } from "@/lib/api";
import { formatMoney, formatPercent } from "@/lib/format";
import type { Goal } from "@/lib/types";
import { useApi } from "@/lib/use-api";
import { useSubmit } from "@/lib/use-submit";

export function goalEstimate(goal: Goal): string {
  if (goal.completed) return "Goal reached!";
  if (goal.monthsToGoal === null) return "Set a monthly saving to see how long this will take.";
  const months = goal.monthsToGoal === 1 ? "1 month" : `${goal.monthsToGoal} months`;
  return `About ${months} to go at ${formatMoney(goal.monthlySaving)}/month.`;
}

export function GoalsView() {
  const { data: goals, error, loading, reload } = useApi<Goal[]>("/goals");
  const [editing, setEditing] = useState<Goal | null>(null);

  return (
    <>
      <PageHeader title="Saving goals" subtitle="Save towards the things that matter and see when you'll get there." />
      <div className="grid items-start gap-6 lg:grid-cols-[minmax(0,360px)_1fr]">
        <Card>
          <GoalForm
            key={editing ? `edit-${editing.id}` : "new"}
            editing={editing}
            onSaved={() => {
              setEditing(null);
              reload();
            }}
            onCancel={() => setEditing(null)}
          />
        </Card>
        <div className="space-y-4">
          {error && <Alert>{error.message}</Alert>}
          {!goals && loading && <ListSkeleton />}
          {goals && goals.length === 0 && (
            <EmptyState title="No goals yet">Create a goal, like a laptop or school fees, and track your progress.</EmptyState>
          )}
          {goals?.map((goal) => (
            <GoalCard
              key={goal.id}
              goal={goal}
              onChanged={reload}
              onEdit={() => setEditing(goal)}
              onDeleted={() => {
                if (editing?.id === goal.id) setEditing(null);
                reload();
              }}
            />
          ))}
        </div>
      </div>
    </>
  );
}

function GoalCard({
  goal,
  onChanged,
  onEdit,
  onDeleted,
}: {
  goal: Goal;
  onChanged: () => void;
  onEdit: () => void;
  onDeleted: () => void;
}) {
  const [amount, setAmount] = useState("");
  const { run, submitting, error, fieldErrors } = useSubmit();

  async function contribute(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const ok = await run(() =>
      api(`/goals/${goal.id}/contributions`, { method: "POST", body: { amount: Number(amount) } }),
    );
    if (ok) {
      setAmount("");
      onChanged();
    }
  }

  return (
    <Card>
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <h3 className="flex items-center gap-2 truncate font-semibold">
            {goal.name}
            {goal.completed && (
              <span className="rounded-full bg-good-soft px-2 py-0.5 text-xs font-semibold text-good">Done</span>
            )}
          </h3>
          <p className="tabular text-sm text-muted">
            {formatMoney(goal.savedAmount)} of {formatMoney(goal.targetAmount)}
          </p>
        </div>
        <RowActions
          label={goal.name}
          onEdit={onEdit}
          onDelete={async () => {
            await api(`/goals/${goal.id}`, { method: "DELETE" });
            onDeleted();
          }}
        />
      </div>

      <div className="mt-4 space-y-1.5">
        <ProgressBar percent={goal.progressPercentage} tone="GOOD" label={`${goal.name} progress`} />
        <div className="flex justify-between text-xs text-muted">
          <span>{formatPercent(goal.progressPercentage)} saved</span>
          {!goal.completed && <span className="tabular">{formatMoney(goal.remainingAmount)} to go</span>}
        </div>
      </div>
      <p className="mt-3 text-sm">{goalEstimate(goal)}</p>

      {!goal.completed && (
        <form onSubmit={contribute} className="mt-4 space-y-2" noValidate>
          {(error || fieldErrors.amount) && <Alert>{error ?? fieldErrors.amount}</Alert>}
          <div className="flex gap-2">
            <Input
              type="number"
              inputMode="decimal"
              min="0"
              step="any"
              placeholder="Amount to add (RWF)"
              aria-label={`Amount to add to ${goal.name}`}
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
            />
            <Button type="submit" variant="secondary" disabled={submitting || !amount}>
              Add savings
            </Button>
          </div>
        </form>
      )}
    </Card>
  );
}

interface Draft {
  name: string;
  targetAmount: string;
  monthlySaving: string;
}

function GoalForm({ editing, onSaved, onCancel }: { editing: Goal | null; onSaved: () => void; onCancel: () => void }) {
  const blank: Draft = { name: "", targetAmount: "", monthlySaving: "" };
  const [draft, setDraft] = useState<Draft>(
    editing
      ? { name: editing.name, targetAmount: String(editing.targetAmount), monthlySaving: String(editing.monthlySaving) }
      : blank,
  );
  const { run, submitting, error, fieldErrors } = useSubmit();

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const body = {
      name: draft.name.trim(),
      targetAmount: Number(draft.targetAmount),
      monthlySaving: draft.monthlySaving === "" ? 0 : Number(draft.monthlySaving),
    };
    const saved = await run(() =>
      api(editing ? `/goals/${editing.id}` : "/goals", { method: editing ? "PUT" : "POST", body }),
    );
    if (saved) {
      if (!editing) setDraft(blank);
      onSaved();
    }
  }

  return (
    <form onSubmit={submit} className="space-y-4" noValidate>
      <CardTitle>{editing ? "Edit goal" : "New goal"}</CardTitle>
      {error && <Alert>{error}</Alert>}
      <Field label="What are you saving for?" error={fieldErrors.name}>
        <Input
          required
          maxLength={100}
          placeholder="e.g. Laptop"
          value={draft.name}
          onChange={(e) => setDraft({ ...draft, name: e.target.value })}
          aria-invalid={!!fieldErrors.name}
        />
      </Field>
      <Field label="Target amount (RWF)" error={fieldErrors.targetAmount}>
        <Input
          type="number"
          inputMode="decimal"
          min="0"
          step="any"
          required
          placeholder="0"
          value={draft.targetAmount}
          onChange={(e) => setDraft({ ...draft, targetAmount: e.target.value })}
          aria-invalid={!!fieldErrors.targetAmount}
        />
      </Field>
      <Field
        label="Planned monthly saving (RWF)"
        error={fieldErrors.monthlySaving}
        hint="Used to estimate how many months you need."
      >
        <Input
          type="number"
          inputMode="decimal"
          min="0"
          step="any"
          placeholder="0"
          value={draft.monthlySaving}
          onChange={(e) => setDraft({ ...draft, monthlySaving: e.target.value })}
          aria-invalid={!!fieldErrors.monthlySaving}
        />
      </Field>
      <div className="flex gap-2">
        <Button type="submit" disabled={submitting} className="flex-1">
          {submitting ? "Saving…" : editing ? "Save changes" : "Create goal"}
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
