public class TestClass {
    public void SimpleMethod() {
        if (true) {
            Console.WriteLine("Hello");
        }
    }
    
    public void MethodWithLoop() {
        foreach (var item in items) {
            DoSomething(item);
        }
    }
}
