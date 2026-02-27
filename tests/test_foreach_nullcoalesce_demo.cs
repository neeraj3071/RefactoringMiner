using System;
using System.Collections.Generic;

class ForeachNullCoalesceDemo
{
    void ProcessItems()
    {
        List<string> items = new List<string>();
        string text = "default";
        
        foreach (var item in items)              
            Console.WriteLine(item ?? text);
    }
}
