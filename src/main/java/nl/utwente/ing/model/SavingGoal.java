package nl.utwente.ing.model;

public class SavingGoal {

    private int id;
    private String name;
    private double goal;
    private double savePerMonth;
    private double minBalanceRequired;
    private double balance;

    public SavingGoal(int id, String name, double goal, double savePerMonth, double minBalanceRequired) {
        this.id = id;
        this.name = name;
        this.goal = goal;
        this.savePerMonth = savePerMonth;
        this.minBalanceRequired = minBalanceRequired;
        this.balance = 0;
    }

    public double getBalance() {
        return balance;
    }

    public double getGoal() {
        return goal;
    }

    public double getMinBalanceRequired() {
        return minBalanceRequired;
    }

    public double getSavePerMonth() {
        return savePerMonth;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setGoal(double goal) {
        this.goal = goal;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setMinBalanceRequired(double minBalanceRequired) {
        this.minBalanceRequired = minBalanceRequired;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSavePerMonth(double savePerMonth) {
        this.savePerMonth = savePerMonth;
    }

    public boolean validSavingGoal() {
        return id > 0 && name != null && goal >0 && savePerMonth > 0 && minBalanceRequired >= 0 && balance >= 0;
    }
}
