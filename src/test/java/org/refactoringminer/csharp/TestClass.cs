namespace Demo {
    public class TestClass {
        private int counter = 0;

        public void SayHello() {
            System.Console.WriteLine("Hello from C#");
        }

        public int Increment() {
            counter++;
            return counter;
        }
    }
}
