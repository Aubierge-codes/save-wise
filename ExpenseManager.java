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
}