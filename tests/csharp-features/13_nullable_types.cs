namespace TestFeatures
{
    using System;
    
    /// <summary>
    /// Test case for nullable value types.
    /// Tests transformation of C# nullable types (int?, bool?, etc.) to Java wrapper types (Integer, Boolean, etc.).
    /// </summary>
    public class NullableTypes
    {
        // Nullable primitive types
        public int? nullableInt;
        public bool? nullableBool;
        public double? nullableDouble;
        public long? nullableLong;
        public float? nullableFloat;
        
        // Method with nullable parameter
        public void ProcessNullableInt(int? value)
        {
            if (value != null)
            {
                Console.WriteLine("Value: " + value);
            }
            else
            {
                Console.WriteLine("Value is null");
            }
        }
        
        // Method with nullable return type
        public int? GetNullableValue(bool returnNull)
        {
            if (returnNull)
            {
                return null;
            }
            return 42;
        }
        
        // Method using HasValue property
        public bool CheckNullableValue(int? value)
        {
            return value.HasValue;
        }
        
        // Method using Value property
        public int GetValueOrDefault(int? value)
        {
            if (value.HasValue)
            {
                return value.Value;
            }
            return 0;
        }
        
        // Method using GetValueOrDefault
        public int GetValueWithDefault(int? value, int defaultValue)
        {
            return value ?? defaultValue;
        }
    }
}
