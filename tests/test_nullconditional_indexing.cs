using System;

class TestNullConditionalIndexing
{
    public void TestMethod()
    {
        int[] array = new int[] { 1, 2, 3 };
        int index = 1;
        
        // Test null-conditional indexing: array?[index]
        int? result = array?[index];
        Console.WriteLine(result);
    }
}
