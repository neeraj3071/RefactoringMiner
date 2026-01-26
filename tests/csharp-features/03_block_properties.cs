namespace TestFeatures
{
    using System;
    
    /// <summary>
    /// Test case for block-style properties with explicit getter and setter bodies.
    /// Tests transformation of C# block properties to Java getter/setter methods.
    /// </summary>
    public class BlockProperties
    {
        private int _count;
        private string _description;
        private bool _isValid;
        
        // Block property with simple getter and setter
        public int Count
        {
            get { return _count; }
            set { _count = value; }
        }
        
        // Block property with validation in setter
        public string Description
        {
            get { return _description; }
            set
            {
                if (value != null)
                {
                    _description = value;
                }
            }
        }
        
        // Block property with computed getter
        public int DoubleCount
        {
            get
            {
                return _count * 2;
            }
        }
        
        // Block property with side effects in setter
        public bool IsValid
        {
            get { return _isValid; }
            set
            {
                _isValid = value;
                OnValidationChanged();
            }
        }
        
        private void OnValidationChanged()
        {
            Console.WriteLine("Validation state changed");
        }
    }
}
