"use client";

import { useState } from "react";

import { Button } from "@/components/ui";
import { errorMessage } from "@/lib/api";

/** Edit and delete buttons for a list row. Delete asks for confirmation first. */
export function RowActions({
  label,
  onEdit,
  onDelete,
}: {
  label: string;
  onEdit?: () => void;
  onDelete: () => Promise<void>;
}) {
  const [deleting, setDeleting] = useState(false);

  async function remove() {
    if (!window.confirm(`Delete "${label}"? This can't be undone.`)) return;
    setDeleting(true);
    try {
      await onDelete();
    } catch (e) {
      window.alert(errorMessage(e));
      setDeleting(false);
    }
  }

  return (
    <div className="flex shrink-0 gap-0.5">
      {onEdit && (
        <Button variant="ghost" size="sm" onClick={onEdit} aria-label={`Edit ${label}`}>
          Edit
        </Button>
      )}
      <Button variant="danger" size="sm" onClick={remove} disabled={deleting} aria-label={`Delete ${label}`}>
        {deleting ? "…" : "Delete"}
      </Button>
    </div>
  );
}
