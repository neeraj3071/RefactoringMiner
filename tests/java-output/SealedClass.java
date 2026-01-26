package TestFeatures;

public final class SealedClass {
    private int _value;
    private int value;
    public int getValue() {
        return _value;
    }
    public void setValue(int value) {
        _value = value;
    }
    public SealedClass(int value) {
        _value = value;
    }
    public void ProcessValue() {
        System.out.println("Processing value: " + _value);
    }
}
public final class AnotherSealedClass {
    private String name;
    public String getName() {
        return name;
    }
    public void setName(String value) {
        this.name = value;
    }
    public void Display() {
        System.out.println(Name);
    }
}
