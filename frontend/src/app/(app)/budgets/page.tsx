import type { Metadata } from "next";

import { BudgetsView } from "@/components/views/budgets-view";

export const metadata: Metadata = { title: "Budgets" };

export default function Page() {
  return <BudgetsView />;
}
