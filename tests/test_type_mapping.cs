using System;
using System.Collections.Generic;

class TypeMappingTest {
    void TestMethod() {
        // Test various C# type mappings
        int count = 10;
        bool isValid = true;
        string name = "test";
        List<int> numbers = new List<int>();
        Dictionary<string, int> map = new Dictionary<string, int>();
        
        // Test with foreach and var
        foreach (var num in numbers)
            Console.WriteLine(num);
            
        foreach (var pair in map)
            Console.WriteLine(pair);
    }
}
