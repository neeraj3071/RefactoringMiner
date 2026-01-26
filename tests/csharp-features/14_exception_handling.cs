namespace TestFeatures
{
    using System;
    
    /// <summary>
    /// Test case for exception handling: try, catch, finally, and throw.
    /// Tests transformation of C# exception handling to Java exception handling.
    /// </summary>
    public class ExceptionHandling
    {
        // Try-catch block
        public void BasicTryCatch()
        {
            try
            {
                int result = 10 / 0;
            }
            catch (Exception e)
            {
                Console.WriteLine("Error: " + e.Message);
            }
        }
        
        // Try-catch-finally block
        public void TryCatchFinally()
        {
            try
            {
                Console.WriteLine("Attempting operation");
                PerformOperation();
            }
            catch (Exception e)
            {
                Console.WriteLine("Operation failed: " + e.Message);
            }
            finally
            {
                Console.WriteLine("Cleanup completed");
            }
        }
        
        // Multiple catch blocks
        public void MultipleCatchBlocks()
        {
            try
            {
                PerformOperation();
            }
            catch (ArgumentException ae)
            {
                Console.WriteLine("Argument error: " + ae.Message);
            }
            catch (InvalidOperationException ioe)
            {
                Console.WriteLine("Invalid operation: " + ioe.Message);
            }
            catch (Exception e)
            {
                Console.WriteLine("General error: " + e.Message);
            }
        }
        
        // Throw statement
        public void ThrowException()
        {
            throw new Exception("Custom exception message");
        }
        
        // Throw with condition
        public void ValidateAndThrow(string input)
        {
            if (input == null)
            {
                throw new ArgumentException("Input cannot be null");
            }
            Console.WriteLine("Input is valid");
        }
        
        // Method with throws declaration equivalent
        public void PerformOperation()
        {
            throw new InvalidOperationException("Operation not supported");
        }
        
        // Nested try-catch
        public void NestedTryCatch()
        {
            try
            {
                try
                {
                    PerformOperation();
                }
                catch (InvalidOperationException e)
                {
                    Console.WriteLine("Inner catch: " + e.Message);
                    throw;
                }
            }
            catch (Exception e)
            {
                Console.WriteLine("Outer catch: " + e.Message);
            }
        }
    }
}
