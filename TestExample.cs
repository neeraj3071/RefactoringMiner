using System;

namespace TestExample
{
    public class Calculator
    {
        public int Add(int a, int b)
        {
            return a + b;
        }

        public int MultiplyNumbers(int x, int y)
        {
            return x * y;
        }

        public void DisplayResult(int result)
        {
            Console.WriteLine("The result is: " + result);
        }
    }

    public class Program
    {
        public static void Main(string[] args)
        {
            Calculator calc = new Calculator();
            int sum = calc.Add(5, 3);
            calc.DisplayResult(sum);
        }
    }
}