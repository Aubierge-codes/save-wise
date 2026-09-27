// Mirrors the JSON returned by the SaveWise API.

export const CATEGORIES = [
  "FOOD",
  "TRANSPORT",
  "EDUCATION",
  "ENTERTAINMENT",
  "HEALTH",
  "OTHER",
] as const;

export type Category = (typeof CATEGORIES)[number];

export interface User {
  id: number;
  email: string;
  fullName: string;
}

export interface Expense {
  id: number;
  description: string;
  category: Category;
  amount: number;
  spentOn: string;
}

export interface Income {
  id: number;
  source: string;
  amount: number;
  receivedOn: string;
}

export interface Goal {
  id: number;
  name: string;
  targetAmount: number;
  savedAmount: number;
  monthlySaving: number;
  remainingAmount: number;
  progressPercentage: number;
  /** null when no monthly saving is planned, so no estimate is possible */
  monthsToGoal: number | null;
  completed: boolean;
}

export type BudgetStatus = "UNDER_BUDGET" | "NEAR_LIMIT" | "OVER_BUDGET";

export interface BudgetUsage {
  category: Category;
  monthlyLimit: number;
  spent: number;
  left: number;
  usedPercent: number;
  status: BudgetStatus;
}

export type Tone = "GOOD" | "INFO" | "WARNING" | "DANGER";

export interface Dashboard {
  month: string;
  totalIncome: number;
  totalExpenses: number;
  remaining: number;
  savingRate: number;
  spendingTooHigh: boolean;
  biggestExpense: { description: string; category: Category; amount: number } | null;
  spendingByCategory: { category: Category; amount: number; share: number }[];
  budgets: BudgetUsage[];
  goals: Goal[];
  recommendation: { tone: Tone; message: string };
  incomeCount: number;
  expenseCount: number;
}
