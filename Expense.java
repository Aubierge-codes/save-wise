public class Expense {

    private String description;
    private ExpenseCategory category;
    private double amount;

    public Expense(String description, ExpenseCategory category, double amount) {
        this.description = description;
        this.category = category;
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }
}