namespace TestFeatures
{
    using System;
    
    /// <summary>
    /// Test case for field declarations with various access modifiers and types.
    /// Tests transformation of C# fields to Java fields.
    /// </summary>
    public class FieldDeclarations
    {
        // Public fields
        public int publicIntField;
        public string publicStringField;
        public bool publicBoolField;
        
        // Private fields
        private int privateIntField;
        private string privateStringField;
        private bool privateBoolField;
        
        // Protected fields
        protected int protectedIntField;
        protected string protectedStringField;
        
        // Static fields
        public static int staticIntField;
        private static string staticStringField;
        
        // Readonly fields
        public readonly int readonlyIntField;
        private readonly string readonlyStringField;
        
        // Const fields
        public const int CONST_INT = 100;
        private const string CONST_STRING = "constant";
        
        // Backing fields with underscore prefix
        private int _backingField;
        private string _name;
        private bool _isEnabled;
    }
}
