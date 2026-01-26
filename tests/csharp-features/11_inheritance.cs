namespace TestFeatures
{
    using System;
    
    /// <summary>
    /// Base class for inheritance testing.
    /// </summary>
    public class BaseDevice
    {
        protected string deviceId;
        
        public BaseDevice()
        {
            deviceId = "BASE-001";
        }
        
        public virtual void Initialize()
        {
            Console.WriteLine("Base device initialization");
        }
        
        public void CommonMethod()
        {
            Console.WriteLine("Common method");
        }
    }
    
    /// <summary>
    /// Test case for class inheritance.
    /// Tests transformation of C# class inheritance to Java extends keyword.
    /// </summary>
    public class DerivedDevice : BaseDevice
    {
        private bool isActive;
        
        public DerivedDevice()
        {
            isActive = false;
        }
        
        public override void Initialize()
        {
            Console.WriteLine("Derived device initialization");
            isActive = true;
        }
        
        public void DerivedMethod()
        {
            Console.WriteLine("Derived method");
        }
    }
    
    /// <summary>
    /// Multiple level inheritance.
    /// </summary>
    public class AdvancedDevice : DerivedDevice
    {
        public void AdvancedMethod()
        {
            Console.WriteLine("Advanced method");
        }
    }
}
