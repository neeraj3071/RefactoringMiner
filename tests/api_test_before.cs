using System;

namespace TestAPI
{
    class ApiTestBefore
    {
        void TestMethod()
        {
            string message = "Hello";
            Console.WriteLine(message);
            Console.WriteLine("World");
        }
        
        void AnotherMethod()
        {
            Console.WriteLine("Test");
        }
    }
}
