using System;
using System.Collections.Generic;

namespace TransformationExample
{
    public class UpdateMethod
    {
        public void Update(List<string> items, string text)
        {
            foreach (var item in items)              
                Console.WriteLine(item ?? text);     
        }
    }
}
