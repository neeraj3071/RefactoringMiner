namespace TestFeatures
{
    using System;
    
    /// <summary>
    /// Test case for abstract classes with abstract and concrete members.
    /// Tests transformation of C# abstract classes to Java abstract classes.
    /// </summary>
    public abstract class AbstractDevice
    {
        protected string deviceId;
        
        // Abstract method
        public abstract void Initialize();
        
        // Abstract method with return value
        public abstract string GetDeviceInfo();
        
        // Concrete method
        public void Start()
        {
            Console.WriteLine("Starting device");
            Initialize();
        }
        
        // Concrete method with implementation
        public void Stop()
        {
            Console.WriteLine("Stopping device");
        }
    }
    
    /// <summary>
    /// Another abstract class with properties.
    /// </summary>
    public abstract class AbstractController
    {
        public abstract int GetBatteryLevel();
        
        public void CheckBattery()
        {
            int level = GetBatteryLevel();
            Console.WriteLine("Battery level: " + level);
        }
    }
}
