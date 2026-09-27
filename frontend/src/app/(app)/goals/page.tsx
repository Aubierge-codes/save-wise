import type { Metadata } from "next";

import { GoalsView } from "@/components/views/goals-view";

export const metadata: Metadata = { title: "Saving goals" };

export default function Page() {
  return <GoalsView />;
}
