namespace TestFeatures
{
    using System;
    
    /// <summary>
    /// Test case for method declarations with various signatures and return types.
    /// Tests transformation of C# methods to Java methods.
    /// </summary>
    public class Methods
    {
        // Void method without parameters
        public void DoNothing()
        {
        }
        
        // Void method with single parameter
        public void ProcessValue(int value)
        {
            Console.WriteLine(value);
        }
        
        // Method with return value
        public int GetValue()
        {
            return 42;
        }
        
        // Method with multiple parameters
        public string Concatenate(string first, string second)
        {
            return first + second;
        }
        
        // Method with complex return type
        public bool Compare(int a, int b)
        {
            return a == b;
        }
        
        // Private method
        private void InternalProcess()
        {
            Console.WriteLine("Internal processing");
        }
        
        // Protected method
        protected void ProtectedOperation()
        {
            Console.WriteLine("Protected operation");
        }
        
        // Static method
        public static int Add(int a, int b)
        {
            return a + b;
        }
        
        // Method with multiple statements
        public int Calculate(int input)
        {
            int result = input * 2;
            result = result + 10;
            return result;
        }
    }
}
