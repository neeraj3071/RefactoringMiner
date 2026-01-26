namespace TestFeatures
{
    using System;
    using System.Collections.Generic;
    
    /// <summary>
    /// Test case for C# to Java type conversions.
    /// Tests transformation of C# primitive and common types to Java equivalents.
    /// </summary>
    public class TypeConversions
    {
        // Primitive type conversions
        public string stringField;      // string -> String
        public bool boolField;           // bool -> boolean
        public int intField;             // int -> int
        public long longField;           // long -> long
        public float floatField;         // float -> float
        public double doubleField;       // double -> double
        public char charField;           // char -> char
        public byte byteField;           // byte -> byte
        public short shortField;         // short -> short
        
        // Object type
        public object objField;          // object -> Object
        
        // Generic collections
        public List<string> stringList;              // List<string> -> List<String>
        public List<int> intList;                    // List<int> -> List<Integer>
        public Dictionary<string, int> dictionary;   // Dictionary -> Map
        
        // Method with type conversions
        public string ProcessString(string input)
        {
            return input;
        }
        
        public bool CheckCondition(bool condition)
        {
            return condition;
        }
        
        public object GetObject()
        {
            return new object();
        }
        
        public List<string> GetStringList()
        {
            return new List<string>();
        }
    }
}
