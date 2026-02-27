using System;
using System.Collections.Generic;

namespace TestNamespace
{
    class TestClass
    {
        void TestMethod()
        {
            List<string> items = new List<string>();
            string text = "default";
            
            // Test case: foreach with null coalescing operator on single line
            foreach (var item in items)Console.WriteLine(item ?? text);
        }
    }
}
