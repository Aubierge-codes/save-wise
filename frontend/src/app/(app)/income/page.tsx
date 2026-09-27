import type { Metadata } from "next";

import { IncomeView } from "@/components/views/income-view";

export const metadata: Metadata = { title: "Income" };

export default function Page() {
  return <IncomeView />;
}
