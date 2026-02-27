using System;
using System.Linq;

class TestLambdas
{
    public void TestMethod()
    {
        int[] numbers = new int[] { 1, 2, 3, 4, 5 };
        
        // Single-expression lambda: x => x * 2
        var doubled = numbers.Select(x => x * 2);
        
        // Multi-statement lambda would look like:
        // x => { int temp = x * 2; return temp; }
        
        foreach (var num in doubled)
        {
            Console.WriteLine(num);
        }
    }
}
