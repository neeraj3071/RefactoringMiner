package TestFeatures;

public class NullableTypes {
    public Integer nullableInt;
    public Boolean nullableBool;
    public Double nullableDouble;
    public Long nullableLong;
    public Float nullableFloat;
    public void ProcessNullableInt(Integer value) {
        if ((value != null)) {
            System.out.println("Value: " + value);
        }
        System.out.println("Value is null");
    }
    public Integer GetNullableValue(boolean returnNull) {
        if ((returnNull)) {
            return null;
        }
        return 42;
    }
    public boolean CheckNullableValue(Integer value) {
        return value != null;
    }
    public int GetValueOrDefault(Integer value) {
        if ((value != null)) {
            return value;
        }
        return 0;
    }
    public int GetValueWithDefault(Integer value, int defaultValue) {
        return (value != null ? value : defaultValue);
    }
}
