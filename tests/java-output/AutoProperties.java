package TestFeatures;

public class AutoProperties {
    private int id;
    public int getId() {
        return id;
    }
    public void setId(int value) {
        this.id = value;
    }
    private String name;
    public String getName() {
        return name;
    }
    public void setName(String value) {
        this.name = value;
    }
    private int readOnlyId;
    public int getReadOnlyId() {
        return readOnlyId;
    }
    public void setReadOnlyId(int value) {
        this.readOnlyId = value;
    }
    private String protectedName;
    public String getProtectedName() {
        return protectedName;
    }
    public void setProtectedName(String value) {
        this.protectedName = value;
    }
    private LocalDateTime createdAt;
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    private boolean isEnabled;
    public boolean getIsEnabled() {
        return isEnabled;
    }
    public void setIsEnabled(boolean value) {
        this.isEnabled = value;
    }
    private double value;
    public double getValue() {
        return value;
    }
    public void setValue(double value) {
        this.value = value;
    }
    private Object data;
    public Object getData() {
        return data;
    }
    public void setData(Object value) {
        this.data = value;
    }
    public AutoProperties() {
        CreatedAt = LocalDateTime.Now;
        ReadOnlyId = 1;
    }
}
