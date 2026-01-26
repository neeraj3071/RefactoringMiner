package TestFeatures;

@Serializable
@Deprecated
@Transient
@Deprecated
@Deprecated
@Serializable
@Deprecated
@CustomValidation
@Transient
public class Serializable {
    @Serializable
    @Deprecated
    private int temporaryData;
    private String oldProperty;
    public String getOldProperty() {
        return oldProperty;
    }
    public void setOldProperty(String value) {
        this.oldProperty = value;
    }
    public void OldMethod() {
        System.out.println("Old method");
    }
    public void DeprecatedMethod() {
        System.out.println("Deprecated");
    }
    private String name;
    public String getName() {
        return name;
    }
    public void setName(String value) {
        this.name = value;
    }
    public void ProcessData(NonSerialized data) {
        System.out.println("Processing: " + data);
    }
}
@Usage
public class AttributeUsage extends Attribute {
    @Usage
    private String validationRule;
    public String getValidationRule() {
        return validationRule;
    }
    public void setValidationRule(String value) {
        this.validationRule = value;
    }
    public AttributeUsage(String rule) {
        ValidationRule = rule;
    }
}
