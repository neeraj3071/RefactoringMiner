namespace TestFeatures
{
    using System;
    
    /// <summary>
    /// Test case for loop statements: while, do-while, for, and foreach.
    /// Tests transformation of C# loops to Java loops.
    /// </summary>
    public class Loops
    {
        // Basic while loop
        public void BasicWhileLoop()
        {
            int counter = 0;
            while (counter < 5)
            {
                Console.WriteLine("Counter: " + counter);
                counter++;
            }
        }
        
        // While loop with complex condition
        public void WhileLoopWithCondition(int max)
        {
            int i = 0;
            while (i < max && i < 100)
            {
                i = i + 2;
                Console.WriteLine(i);
            }
        }
        
        // Do-while loop
        public void DoWhileLoop()
        {
            int counter = 0;
            do
            {
                Console.WriteLine("Counter: " + counter);
                counter++;
            } while (counter < 5);
        }
        
        // Do-while with single execution
        public void DoWhileOnce()
        {
            int value = 10;
            do
            {
                Console.WriteLine("Value: " + value);
                value++;
            } while (value < 10);
        }
        
        // For loop
        public void BasicForLoop()
        {
            for (int i = 0; i < 10; i++)
            {
                Console.WriteLine("Iteration: " + i);
            }
        }
        
        // For loop with step
        public void ForLoopWithStep()
        {
            for (int i = 0; i < 20; i += 2)
            {
                Console.WriteLine("Even number: " + i);
            }
        }
        
        // Nested loops
        public void NestedLoops()
        {
            for (int i = 0; i < 3; i++)
            {
                for (int j = 0; j < 3; j++)
                {
                    Console.WriteLine("i: " + i + ", j: " + j);
                }
            }
        }
        
        // While loop with break
        public void WhileWithBreak()
        {
            int count = 0;
            while (true)
            {
                if (count >= 5)
                {
                    break;
                }
                Console.WriteLine(count);
                count++;
            }
        }
        
        // While loop with continue
        public void WhileWithContinue()
        {
            int i = 0;
            while (i < 10)
            {
                i++;
                if (i % 2 == 0)
                {
                    continue;
                }
                Console.WriteLine("Odd: " + i);
            }
        }
    }
}
