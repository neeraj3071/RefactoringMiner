namespace TestFeatures
{
    using System;
    
    /// <summary>
    /// Test case for constructor declarations with various parameter patterns.
    /// Tests transformation of C# constructors to Java constructors.
    /// </summary>
    public class Constructors
    {
        private int _id;
        private string _name;
        private bool _isActive;
        
        // Default parameterless constructor
        public Constructors()
        {
            _id = 0;
            _name = "Default";
            _isActive = false;
        }
        
        // Constructor with single parameter
        public Constructors(int id)
        {
            _id = id;
            _name = "Default";
            _isActive = false;
        }
        
        // Constructor with multiple parameters
        public Constructors(int id, string name)
        {
            _id = id;
            _name = name;
            _isActive = false;
        }
        
        // Constructor with all parameters
        public Constructors(int id, string name, bool isActive)
        {
            _id = id;
            _name = name;
            _isActive = isActive;
        }
        
        // Private constructor for singleton pattern
        private Constructors(string name)
        {
            _name = name;
        }
        
        // Static method using private constructor
        public static Constructors CreateNamed(string name)
        {
            return new Constructors(name);
        }
    }
}
