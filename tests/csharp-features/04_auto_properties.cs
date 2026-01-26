namespace TestFeatures
{
    using System;
    
    /// <summary>
    /// Test case for auto-implemented properties.
    /// Tests transformation of C# auto-properties to Java backing field with getter/setter.
    /// </summary>
    public class AutoProperties
    {
        // Simple auto-property with get and set
        public int Id { get; set; }
        
        // Auto-property with initial value
        public string Name { get; set; } = "Default";
        
        // Auto-property with private setter
        public int ReadOnlyId { get; private set; }
        
        // Auto-property with protected setter
        public string ProtectedName { get; protected set; }
        
        // Auto-property with only getter (truly read-only)
        public DateTime CreatedAt { get; }
        
        // Multiple auto-properties
        public bool IsEnabled { get; set; }
        public double Value { get; set; }
        public object Data { get; set; }
        
        public AutoProperties()
        {
            CreatedAt = DateTime.Now;
            ReadOnlyId = 1;
        }
    }
}
