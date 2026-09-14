public class Main {

    public static void main(String[] args) {

        Income income = new Income("Allowance", 50000);

        System.out.println("=================================");
        System.out.println("            SAVEWISE");
        System.out.println("     Personal Money Assistant");
        System.out.println("=================================");

        System.out.println("Income source: " + income.getSource());
        System.out.println("Income amount: " + income.getAmount() + " RWF");
    }
}