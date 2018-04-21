package nl.utwente.ing.transaction;

public class CategoryRule {

    private int id;
    private String description;
    private String iBan;
    private transactionType type;
    private int categoryId;
    private boolean applyOnHistory;
    public enum transactionType {
        deposit, withdrawal;
    }

    public CategoryRule(int id, String description, String iBAN, transactionType type, int categoryId) {
        this.id = id;
        this.description = description;
        this.iBan = iBAN;
        this.type = type;
        this.categoryId = categoryId;
        this.applyOnHistory = false;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getiBan() {
        return iBan;
    }

    public transactionType getType() {
        return type;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public boolean isApplyOnHistory() {
        return applyOnHistory;
    }

    public void setApplyOnHistory(boolean applyOnHistory) {
        this.applyOnHistory = applyOnHistory;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setiBan(String iBan) {
        this.iBan = iBan;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setType(transactionType type) {
        this.type = type;
    }
}
