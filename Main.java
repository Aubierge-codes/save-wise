public class Main {

    public static void main(String[] args) {

        Income income = new Income("Allowance", 50000);

        ExpenseManager expenseManager = new ExpenseManager();

        expenseManager.addExpense(
                new Expense("Lunch", ExpenseCategory.FOOD, 5000)
        );

        expenseManager.addExpense(
                new Expense("Bus", ExpenseCategory.TRANSPORT, 2000)
        );

        expenseManager.addExpense(
                new Expense("Notebook", ExpenseCategory.EDUCATION, 3000)
        );

        expenseManager.addExpense(
                new Expense("Movie", ExpenseCategory.ENTERTAINMENT, 4000)
        );

        double totalExpenses =
                expenseManager.calculateTotalExpenses();

        double remainingMoney =
                expenseManager.calculateRemainingMoney(
                        income.getAmount()
                );

        System.out.println("=================================");
        System.out.println("            SAVEWISE");
        System.out.println("     Personal Money Assistant");
        System.out.println("=================================");

        System.out.println();

        System.out.println("Income: "
                + income.getAmount()
                + " RWF");

        System.out.println("Total spent: "
                + totalExpenses
                + " RWF");

        System.out.println("Remaining: "
                + remainingMoney
                + " RWF");
    }
}