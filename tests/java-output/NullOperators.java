package TestFeatures;

public class NullOperators {
    private String _name;
    private Integer _value;
    public String GetNameOrDefault() {
        return (_name != null ? _name : "Unknown");
    }
    public String GetNameOrComputed() {
        return (_name != null ? _name : ComputeDefaultName());
    }
    public void EnsureNameExists() {
        _name = (_name != null ? _name : "Default Name");
    }
    public String GetFirstNonNull(String first, String second, String third) {
        return (((first != null ? first : second) != null ? (first != null ? first : second) : third) != null ? ((first != null ? first : second) != null ? (first != null ? first : second) : third) : "None");
    }
    public int GetValueOrDefault() {
        return (_value != null ? _value : 0);
    }
    public Integer GetNameLength() {
        return (_name != null ? _name.length() : null);
    }
    public String GetUppercaseName() {
        return (_name != null ? _name.toUpperCase() : null);
    }
    public Character GetFirstChar() {
        return (_name != null && _name.length() > 0 ? _name.charAt(0) : null);
    }
    public String GetNameOrDefaultLength() {
        return (((_name != null ? _name.length() : null).toString()) != null ? ((_name != null ? _name.length() : null).toString()) : "0");
    }
    public boolean IsNameLong() {
        return (_name != null ? _name.length() : null) > 10;
    }
    public String ComputeDefaultName() {
        return "Computed_" + System.currentTimeMillis();
    }
}
