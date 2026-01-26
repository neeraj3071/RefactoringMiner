package TestFeatures;

public class Methods {
    public void DoNothing() {
    }
    public void ProcessValue(int value) {
        System.out.println(value);
    }
    public int GetValue() {
        return 42;
    }
    public String Concatenate(String first, String second) {
        return first + second;
    }
    public boolean Compare(int a, int b) {
        return a == b;
    }
    public void InternalProcess() {
        System.out.println("Internal processing");
    }
    public void ProtectedOperation() {
        System.out.println("Protected operation");
    }
    public int Add(int a, int b) {
        return a + b;
    }
    public int Calculate(int input) {
        int result = input * 2;
        result = result + 10;
        return result;
    }
}
