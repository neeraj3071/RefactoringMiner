using System;

class TestStringInterpolation
{
    public void TestMethod()
    {
        int x = 42;
        string name = "Test";
        
        // Test string interpolation: $"Value: {x}"
        string result = $"Value: {x}, Name: {name}";
        Console.WriteLine(result);
    }
}
