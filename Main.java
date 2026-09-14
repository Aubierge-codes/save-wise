public class Main {

    public static void main(String[] args) {

        Income income = new Income("Allowance", 50000);

        Expense foodExpense = new Expense(
                "Lunch",
                ExpenseCategory.FOOD,
                5000
        );

        Expense transportExpense = new Expense(
                "Bus",
                ExpenseCategory.TRANSPORT,
                2000
        );

        System.out.println("=================================");
        System.out.println("            SAVEWISE");
        System.out.println("     Personal Money Assistant");
        System.out.println("=================================");

        System.out.println();

        System.out.println("INCOME");
        System.out.println("Source: " + income.getSource());
        System.out.println("Amount: " + income.getAmount() + " RWF");

        System.out.println();

        System.out.println("EXPENSES");

        System.out.println(
                foodExpense.getDescription()
                        + " | "
                        + foodExpense.getCategory()
                        + " | "
                        + foodExpense.getAmount()
                        + " RWF"
        );

        System.out.println(
                transportExpense.getDescription()
                        + " | "
                        + transportExpense.getCategory()
                        + " | "
                        + transportExpense.getAmount()
                        + " RWF"
        );
    }
}