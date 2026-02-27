using System;

class TestChainedNullCoalesce
{
    public void TestMethod()
    {
        string x = null;
        string y = null;
        string z = "default";
        
        // Test chained null-coalescing: x ?? y ?? z
        string result = x ?? y ?? z;
        Console.WriteLine(result);
    }
}
