import { Logo } from "@/components/app-shell";

export default function AuthLayout({ children }: LayoutProps<"/">) {
  return (
    <main className="flex flex-1 items-center justify-center px-4 py-12">
      <div className="w-full max-w-sm">
        <div className="mb-8 flex flex-col items-center gap-2 text-center">
          <Logo />
          <p className="text-sm text-muted">Your personal money assistant</p>
        </div>
        {children}
      </div>
    </main>
  );
}
