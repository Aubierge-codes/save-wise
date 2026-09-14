public class Main {

    public static void main(String[] args) {

        Income income = new Income("Allowance", 50000);

        Expense expense = new Expense(
                "Lunch",
                "Food",
                5000
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

        System.out.println("EXPENSE");
        System.out.println("Description: " + expense.getDescription());
        System.out.println("Category: " + expense.getCategory());
        System.out.println("Amount: " + expense.getAmount() + " RWF");
    }
}