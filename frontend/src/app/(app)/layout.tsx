import { AppShell } from "@/components/app-shell";
import { MonthProvider } from "@/components/month-context";

export default function AppLayout({ children }: LayoutProps<"/">) {
  return (
    <AppShell>
      <MonthProvider>{children}</MonthProvider>
    </AppShell>
  );
}
