namespace TestFeatures
{
    using System;
    
    /// <summary>
    /// Test interface definition.
    /// </summary>
    public interface IDevice
    {
        void PowerOn();
        void PowerOff();
        string GetStatus();
    }
    
    /// <summary>
    /// Second test interface.
    /// </summary>
    public interface IConnectable
    {
        void Connect();
        void Disconnect();
    }
    
    /// <summary>
    /// Test case for interface implementation.
    /// Tests transformation of C# interface implementation to Java implements keyword.
    /// </summary>
    public class NetworkDevice : IDevice, IConnectable
    {
        private bool isPoweredOn;
        private bool isConnected;
        
        public void PowerOn()
        {
            isPoweredOn = true;
            Console.WriteLine("Device powered on");
        }
        
        public void PowerOff()
        {
            isPoweredOn = false;
            Console.WriteLine("Device powered off");
        }
        
        public string GetStatus()
        {
            return isPoweredOn ? "On" : "Off";
        }
        
        public void Connect()
        {
            isConnected = true;
            Console.WriteLine("Device connected");
        }
        
        public void Disconnect()
        {
            isConnected = false;
            Console.WriteLine("Device disconnected");
        }
    }
    
    /// <summary>
    /// Class implementing single interface.
    /// </summary>
    public class SimpleDevice : IDevice
    {
        public void PowerOn()
        {
            Console.WriteLine("Simple device on");
        }
        
        public void PowerOff()
        {
            Console.WriteLine("Simple device off");
        }
        
        public string GetStatus()
        {
            return "Ready";
        }
    }
}
