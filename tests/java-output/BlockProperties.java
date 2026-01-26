package TestFeatures;

public class BlockProperties {
    private int _count;
    private String _description;
    private boolean _isValid;
    private int count;
    public int getCount() {
        return _count;
    }
    public void setCount(int value) {
        _count = value;
    }
    private String description;
    public String getDescription() {
        return _description;
    }
    public void setDescription(String value) {
        _description = value;
    }
    private int doubleCount;
    public int getDoubleCount() {
        return _count * 2;
    }
    private boolean isValid;
    public boolean getIsValid() {
        return _isValid;
    }
    public void setIsValid(boolean value) {
        _isValid = value;
    }
    public void OnValidationChanged() {
        System.out.println("Validation state changed");
    }
}
