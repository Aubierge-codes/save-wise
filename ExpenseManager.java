import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExpenseManager {

    private ArrayList<Expense> expenses;

    private Map<ExpenseCategory, Double> budgets;

    public ExpenseManager() {
        expenses = new ArrayList<>();
        budgets = new HashMap<>();
    }

    public void addExpense(Expense expense) {
        expenses.add(expense);
    }

    public ArrayList<Expense> getExpenses() {
        return expenses;
    }

    public double calculateTotalExpenses() {

        double total = 0;

        for (Expense expense : expenses) {
            total = total + expense.getAmount();
        }

        return total;
    }

    public double calculateRemainingMoney(double income) {

        double totalExpenses = calculateTotalExpenses();

        return income - totalExpenses;
    }

    public double calculateSavingRate(double income) {

        if (income <= 0) {
            return 0;
        }

        double remainingMoney = calculateRemainingMoney(income);

        return (remainingMoney / income) * 100;
    }

    public Expense findBiggestExpense() {

        if (expenses.isEmpty()) {
            return null;
        }

        Expense biggestExpense = expenses.get(0);

        for (Expense expense : expenses) {

            if (expense.getAmount() > biggestExpense.getAmount()) {
                biggestExpense = expense;
            }
        }

        return biggestExpense;
    }

    public Map<ExpenseCategory, Double> calculateSpendingByCategory() {

        Map<ExpenseCategory, Double> categoryTotals = new HashMap<>();

        for (Expense expense : expenses) {

            ExpenseCategory category = expense.getCategory();

            double currentTotal =
                    categoryTotals.getOrDefault(category, 0.0);

            categoryTotals.put(
                    category,
                    currentTotal + expense.getAmount()
            );
        }

        return categoryTotals;
    }

    public boolean isSpendingTooHigh(double income) {

        if (income <= 0) {
            return false;
        }

        double totalExpenses = calculateTotalExpenses();

        double spendingPercentage =
                (totalExpenses / income) * 100;

        return spendingPercentage >= 80;
    }

    public void setBudget(
            ExpenseCategory category,
            double amount) {

        if (amount > 0) {
            budgets.put(category, amount);
        }
    }

    public Map<ExpenseCategory, Double> getBudgets() {
        return budgets;
    }

    public String getBudgetStatus(ExpenseCategory category) {

        if (!budgets.containsKey(category)) {
            return "NO BUDGET";
        }

        double budget = budgets.get(category);

        Map<ExpenseCategory, Double> spending =
                calculateSpendingByCategory();

        double spent =
                spending.getOrDefault(category, 0.0);

        if (spent > budget) {
            return "OVER BUDGET";
        }

        if (spent >= budget * 0.80) {
            return "NEAR LIMIT";
        }

        return "UNDER BUDGET";
    }

    public String getSavingRecommendation(double income) {

        if (income <= 0) {
            return "Add a valid income before creating a saving plan.";
        }

        if (isSpendingTooHigh(income)) {
            return "Try reducing your expenses. You are spending 80% or more of your income.";
        }

        for (ExpenseCategory category : budgets.keySet()) {

            String status = getBudgetStatus(category);

            if (status.equals("OVER BUDGET")) {
                return "Reduce your spending on "
                        + category
                        + ". You are over your budget.";
            }
        }

        for (ExpenseCategory category : budgets.keySet()) {

            String status = getBudgetStatus(category);

            if (status.equals("NEAR LIMIT")) {
                return "Be careful with "
                        + category
                        + ". You are getting close to your budget.";
            }
        }

        double savingRate = calculateSavingRate(income);

        if (savingRate >= 20) {
            return "Great job! You are saving at least 20% of your income.";
        }

        return "Consider saving more of your remaining money.";
    }
}