namespace TestFeatures
{
    using System;
    
    /// <summary>
    /// Test case for null-coalescing and null-conditional operators.
    /// Tests transformation of C# null operators to Java equivalents.
    /// </summary>
    public class NullOperators
    {
        private string _name;
        private int? _value;
        
        // Null-coalescing operator (??)
        public string GetNameOrDefault()
        {
            return _name ?? "Unknown";
        }
        
        // Null-coalescing with method call
        public string GetNameOrComputed()
        {
            return _name ?? ComputeDefaultName();
        }
        
        // Null-coalescing assignment (??=)
        public void EnsureNameExists()
        {
            _name ??= "Default Name";
        }
        
        // Chained null-coalescing
        public string GetFirstNonNull(string first, string second, string third)
        {
            return first ?? second ?? third ?? "None";
        }
        
        // Null-coalescing with nullable types
        public int GetValueOrDefault()
        {
            return _value ?? 0;
        }
        
        // Null-conditional operator (?.)
        public int? GetNameLength()
        {
            return _name?.Length;
        }
        
        // Null-conditional with method call
        public string GetUppercaseName()
        {
            return _name?.ToUpper();
        }
        
        // Null-conditional with indexer
        public char? GetFirstChar()
        {
            return _name?[0];
        }
        
        // Combined null-conditional and null-coalescing
        public string GetNameOrDefaultLength()
        {
            return (_name?.Length.ToString()) ?? "0";
        }
        
        // Null-conditional in condition
        public bool IsNameLong()
        {
            return _name?.Length > 10;
        }
        
        private string ComputeDefaultName()
        {
            return "Computed_" + DateTime.Now.Ticks;
        }
    }
}
