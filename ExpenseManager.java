import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExpenseManager {

    private ArrayList<Expense> expenses;

    public ExpenseManager() {
        expenses = new ArrayList<>();
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
}