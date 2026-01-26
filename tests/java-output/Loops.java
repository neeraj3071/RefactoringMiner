package TestFeatures;

public class Loops {
    public void BasicWhileLoop() {
        int counter = 0;
        while ((counter < 5)) {
            System.out.println("Counter: " + counter);
            counter++;
        }
    }
    public void WhileLoopWithCondition(int max) {
        int i = 0;
        while ((i < max && i < 100)) {
            i = i + 2;
            System.out.println(i);
        }
    }
    public void DoWhileLoop() {
        int counter = 0;
        do {
            System.out.println("Counter: " + counter);
            counter++;
        } while ((counter < 5));
    }
    public void DoWhileOnce() {
        int value = 10;
        do {
            System.out.println("Value: " + value);
            value++;
        } while ((value < 10));
    }
    public void BasicForLoop() {
        for (int i = 0; i < 10; i++) {
            System.out.println("Iteration: " + i);
        }
    }
    public void ForLoopWithStep() {
        for (int i = 0; i < 20; i += 2) {
            System.out.println("Even number: " + i);
        }
    }
    public void NestedLoops() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                System.out.println("i: " + i + ", j: " + j);
            }
        }
    }
    public void WhileWithBreak() {
        int count = 0;
        while ((true)) {
            if ((count >= 5)) {
                break;
            }
            System.out.println(count);
            count++;
        }
    }
    public void WhileWithContinue() {
        int i = 0;
        while ((i < 10)) {
            i++;
            if ((i % 2 == 0)) {
                continue;
            }
            System.out.println("Odd: " + i);
        }
    }
}
