public class TestClass {
    public void TestLambda() {
        var result = items.FirstOrDefault((item) => item.Id == 1);
    }
}
