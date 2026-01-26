namespace TestFeatures
{
    using System;
    
    /// <summary>
    /// Test case for C# attributes (annotations in Java).
    /// Tests transformation of C# attributes to Java annotations.
    /// </summary>
    [Serializable]
    [Obsolete("This class is deprecated")]
    public class AttributeDeclarations
    {
        // Field with attribute
        [NonSerialized]
        private int temporaryData;
        
        // Property with attribute
        [Obsolete]
        public string OldProperty { get; set; }
        
        // Method with multiple attributes
        [Obsolete("Use NewMethod instead")]
        [Serializable]
        public void OldMethod()
        {
            Console.WriteLine("Old method");
        }
        
        // Method with attribute and parameters
        [Obsolete("Deprecated", true)]
        public void DeprecatedMethod()
        {
            Console.WriteLine("Deprecated");
        }
        
        // Custom attribute usage
        [CustomValidation("Required")]
        public string Name { get; set; }
        
        // Parameter with attribute
        public void ProcessData([NonSerialized] int data)
        {
            Console.WriteLine("Processing: " + data);
        }
    }
    
    /// <summary>
    /// Custom attribute definition for testing.
    /// </summary>
    [AttributeUsage(AttributeTargets.Property | AttributeTargets.Field)]
    public class CustomValidationAttribute : Attribute
    {
        public string ValidationRule { get; set; }
        
        public CustomValidationAttribute(string rule)
        {
            ValidationRule = rule;
        }
    }
}
