package TestFeatures;

public class TypeConversions {
    public String stringField;
    public boolean boolField;
    public int intField;
    public long longField;
    public float floatField;
    public double doubleField;
    public char charField;
    public byte byteField;
    public short shortField;
    public Object objField;
    public List<String> stringList;
    public List<int> intList;
    public HashMap<String, int> dictionary;
    public String ProcessString(String input) {
        return input;
    }
    public boolean CheckCondition(boolean condition) {
        return condition;
    }
    public Object GetObject() {
        return new Object();
    }
    public List<String> GetStringList() {
        return new List<String>();
    }
}
