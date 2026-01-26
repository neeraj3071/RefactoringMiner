namespace TestFeatures
{
    using System;
    
    /// <summary>
    /// Test case for sealed classes.
    /// Tests transformation of C# sealed classes to Java final classes.
    /// </summary>
    public sealed class SealedClass
    {
        private int _value;
        
        public int Value
        {
            get { return _value; }
            set { _value = value; }
        }
        
        public SealedClass(int value)
        {
            _value = value;
        }
        
        public void ProcessValue()
        {
            Console.WriteLine("Processing value: " + _value);
        }
    }
    
    /// <summary>
    /// Another sealed class to test multiple sealed classes in same file.
    /// </summary>
    public sealed class AnotherSealedClass
    {
        public string Name { get; set; }
        
        public void Display()
        {
            Console.WriteLine(Name);
        }
    }
}
