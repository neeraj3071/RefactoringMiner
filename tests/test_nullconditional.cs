using System;

class TestNullConditional
{
    public string GetProperty()
    {
        return "property";
    }
    
    public void TestMethod()
    {
        TestNullConditional obj = null;
        
        // Test null-conditional: obj?.GetProperty()
        string result = obj?.GetProperty();
        Console.WriteLine(result);
    }
}
