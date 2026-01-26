package TestFeatures;

public class ControlFlow {
    public void BasicIf(int value) {
        if ((value > 0)) {
            System.out.println("Positive");
        }
    }
    public void IfElse(int value) {
        if ((value > 0)) {
            System.out.println("Positive");
        }
        System.out.println("Non-positive");
    }
    public void IfElseIfChain(int value) {
        if ((value > 0)) {
            System.out.println("Positive");
        }
        if ((value < 0)) {
            System.out.println("Negative");
        }
        System.out.println("Zero");
    }
    public void NestedIf(int x, int y) {
        if ((x > 0)) {
            if ((y > 0)) {
                System.out.println("Both positive");
            }
            System.out.println("X positive, Y non-positive");
        } else {
            System.out.println("X positive, Y non-positive");
        }
    }
    public void ComplexCondition(int a, int b, boolean flag) {
        if ((a > 0 && b > 0 && flag)) {
            System.out.println("All conditions met");
        }
        if ((a > 0 || b > 0)) {
            System.out.println("At least one positive");
        }
    }
    public void BreakInLoop() {
        for (int i = 0; i < 10; i++) {
            if ((i == 5)) {
                break;
            }
            System.out.println(i);
        }
    }
    public void ContinueInLoop() {
        for (int i = 0; i < 10; i++) {
            if ((i % 2 == 0)) {
                continue;
            }
            System.out.println(i);
        }
    }
    public int EarlyReturn(int value) {
        if ((value < 0)) {
            return -1;
        }
        if ((value == 0)) {
            return 0;
        }
        return value * 2;
    }
    public String TernaryOperator(boolean condition) {
        return condition ? "True" : "False";
    }
    public String NullCoalescing(String input) {
        return (input != null ? input : "Default");
    }
}
