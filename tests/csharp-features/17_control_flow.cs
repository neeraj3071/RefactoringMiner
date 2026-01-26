namespace TestFeatures
{
    using System;
    
    /// <summary>
    /// Test case for control flow statements: if, else, break, continue, return.
    /// Tests transformation of C# control flow to Java control flow.
    /// </summary>
    public class ControlFlow
    {
        // Basic if statement
        public void BasicIf(int value)
        {
            if (value > 0)
            {
                Console.WriteLine("Positive");
            }
        }
        
        // If-else statement
        public void IfElse(int value)
        {
            if (value > 0)
            {
                Console.WriteLine("Positive");
            }
            else
            {
                Console.WriteLine("Non-positive");
            }
        }
        
        // If-else if-else chain
        public void IfElseIfChain(int value)
        {
            if (value > 0)
            {
                Console.WriteLine("Positive");
            }
            else if (value < 0)
            {
                Console.WriteLine("Negative");
            }
            else
            {
                Console.WriteLine("Zero");
            }
        }
        
        // Nested if statements
        public void NestedIf(int x, int y)
        {
            if (x > 0)
            {
                if (y > 0)
                {
                    Console.WriteLine("Both positive");
                }
                else
                {
                    Console.WriteLine("X positive, Y non-positive");
                }
            }
        }
        
        // If with complex condition
        public void ComplexCondition(int a, int b, bool flag)
        {
            if (a > 0 && b > 0 && flag)
            {
                Console.WriteLine("All conditions met");
            }
            else if (a > 0 || b > 0)
            {
                Console.WriteLine("At least one positive");
            }
        }
        
        // Break statement in loop
        public void BreakInLoop()
        {
            for (int i = 0; i < 10; i++)
            {
                if (i == 5)
                {
                    break;
                }
                Console.WriteLine(i);
            }
        }
        
        // Continue statement in loop
        public void ContinueInLoop()
        {
            for (int i = 0; i < 10; i++)
            {
                if (i % 2 == 0)
                {
                    continue;
                }
                Console.WriteLine(i);
            }
        }
        
        // Early return
        public int EarlyReturn(int value)
        {
            if (value < 0)
            {
                return -1;
            }
            if (value == 0)
            {
                return 0;
            }
            return value * 2;
        }
        
        // Ternary operator
        public string TernaryOperator(bool condition)
        {
            return condition ? "True" : "False";
        }
        
        // Null coalescing operator
        public string NullCoalescing(string input)
        {
            return input ?? "Default";
        }
    }
}
