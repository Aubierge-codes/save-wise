public class SavingGoal {

    private String name;
    private double targetAmount;
    private double savedAmount;

    public SavingGoal(String name, double targetAmount) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.savedAmount = 0;
    }

    public String getName() {
        return name;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public double getSavedAmount() {
        return savedAmount;
    }

    public void addSavings(double amount) {

        if (amount > 0) {
            savedAmount = savedAmount + amount;
        }
    }

    public double calculateRemainingAmount() {

        return targetAmount - savedAmount;
    }

    public double calculateProgressPercentage() {

        if (targetAmount <= 0) {
            return 0;
        }

        return (savedAmount / targetAmount) * 100;
    }
}