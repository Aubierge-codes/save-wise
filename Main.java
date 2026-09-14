import java.util.Map;

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

        SavingGoal savingGoal =
                new SavingGoal("Laptop", 300000);

        savingGoal.addSavings(50000);

        double monthlySaving = 50000;

        double totalExpenses =
                expenseManager.calculateTotalExpenses();

        double remainingMoney =
                expenseManager.calculateRemainingMoney(
                        income.getAmount()
                );

        double savingRate =
                expenseManager.calculateSavingRate(
                        income.getAmount()
                );

        Expense biggestExpense =
                expenseManager.findBiggestExpense();

        Map<ExpenseCategory, Double> categoryTotals =
                expenseManager.calculateSpendingByCategory();

        double remainingGoalAmount =
                savingGoal.calculateRemainingAmount();

        double progressPercentage =
                savingGoal.calculateProgressPercentage();

        int monthsToGoal =
                savingGoal.estimateMonthsToGoal(monthlySaving);

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

        System.out.println("Saving rate: "
                + savingRate
                + "%");

        System.out.println();

        if (biggestExpense != null) {

            System.out.println("BIGGEST EXPENSE");

            System.out.println(
                    biggestExpense.getDescription()
                            + " - "
                            + biggestExpense.getAmount()
                            + " RWF"
            );
        }

        System.out.println();

        System.out.println("SPENDING BY CATEGORY");

        for (Map.Entry<ExpenseCategory, Double> entry
                : categoryTotals.entrySet()) {

            System.out.println(
                    entry.getKey()
                            + ": "
                            + entry.getValue()
                            + " RWF"
            );
        }

        System.out.println();

        System.out.println("SAVING GOAL");

        System.out.println("Goal: "
                + savingGoal.getName());

        System.out.println("Target: "
                + savingGoal.getTargetAmount()
                + " RWF");

        System.out.println("Saved: "
                + savingGoal.getSavedAmount()
                + " RWF");

        System.out.println("Still needed: "
                + remainingGoalAmount
                + " RWF");

        System.out.println("Progress: "
                + progressPercentage
                + "%");

        System.out.println("Monthly saving: "
                + monthlySaving
                + " RWF");

        System.out.println("Estimated months: "
                + monthsToGoal);
    }
}