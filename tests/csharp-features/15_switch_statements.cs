namespace TestFeatures
{
    using System;
    
    /// <summary>
    /// Test case for switch statements with various case patterns.
    /// Tests transformation of C# switch statements to Java switch statements.
    /// </summary>
    public class SwitchStatements
    {
        // Basic switch with int
        public void BasicSwitch(int value)
        {
            switch (value)
            {
                case 0:
                    Console.WriteLine("Zero");
                    break;
                case 1:
                    Console.WriteLine("One");
                    break;
                case 2:
                    Console.WriteLine("Two");
                    break;
                default:
                    Console.WriteLine("Other");
                    break;
            }
        }
        
        // Switch with string
        public void SwitchWithString(string input)
        {
            switch (input)
            {
                case "start":
                    Console.WriteLine("Starting");
                    break;
                case "stop":
                    Console.WriteLine("Stopping");
                    break;
                case "pause":
                    Console.WriteLine("Pausing");
                    break;
                default:
                    Console.WriteLine("Unknown command");
                    break;
            }
        }
        
        // Switch with fall-through (no break)
        public void SwitchWithFallThrough(int value)
        {
            switch (value)
            {
                case 1:
                case 2:
                case 3:
                    Console.WriteLine("Low value");
                    break;
                case 4:
                case 5:
                    Console.WriteLine("Medium value");
                    break;
                default:
                    Console.WriteLine("High value");
                    break;
            }
        }
        
        // Switch with return
        public string SwitchWithReturn(int code)
        {
            switch (code)
            {
                case 200:
                    return "OK";
                case 404:
                    return "Not Found";
                case 500:
                    return "Internal Error";
                default:
                    return "Unknown";
            }
        }
        
        // Switch with multiple statements per case
        public void SwitchWithMultipleStatements(int option)
        {
            switch (option)
            {
                case 1:
                    Console.WriteLine("Option 1 selected");
                    ProcessOption1();
                    break;
                case 2:
                    Console.WriteLine("Option 2 selected");
                    ProcessOption2();
                    break;
                default:
                    Console.WriteLine("Invalid option");
                    break;
            }
        }
        
        private void ProcessOption1()
        {
            Console.WriteLine("Processing option 1");
        }
        
        private void ProcessOption2()
        {
            Console.WriteLine("Processing option 2");
        }
    }
}
