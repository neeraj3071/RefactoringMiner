using System;

namespace TestAPI
{
    class ApiTestAfter
    {
        void TestMethod()
        {
            string message = "Hello";
            Logger.Log(message);  // Changed from Console.WriteLine
            Logger.Log("World");  // Changed from Console.WriteLine
        }
        
        void RenamedMethod()  // Renamed from AnotherMethod
        {
            Console.WriteLine("Test");
        }
    }
}
