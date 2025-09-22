using System;
using System.Collections.Generic;

namespace TestProject
{
    public class Calculator
    {
        private int value;
        
        public Calculator()
        {
            this.value = 0;
        }
        
        public int Add(int a, int b)
        {
            return a + b;
        }
        
        public int Multiply(int a, int b)
        {
            return a * b;
        }
        
        public void Reset()
        {
            this.value = 0;
        }
    }
    
    public class MathUtils
    {
        public static double CalculateAverage(List<int> numbers)
        {
            if (numbers == null || numbers.Count == 0)
                return 0.0;
                
            int sum = 0;
            foreach (int number in numbers)
            {
                sum += number;
            }
            
            return (double)sum / numbers.Count;
        }
    }
}