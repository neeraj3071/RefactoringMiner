namespace TestFeatures
{
    using System;
    
    /// <summary>
    /// Test case for C# events.
    /// Tests transformation of C# events to Java listener pattern with add/remove/fire methods.
    /// </summary>
    public class EventDeclarations
    {
        // Simple event without arguments
        public event Action OnStarted;
        
        // Event with single argument
        public event Action<string> OnMessageReceived;
        
        // Event with multiple arguments
        public event Action<int, string> OnDataChanged;
        
        // Event with EventHandler pattern
        public event EventHandler OnCompleted;
        
        // Private event
        private event Action OnInternalEvent;
        
        public void TriggerStarted()
        {
            OnStarted?.Invoke();
        }
        
        public void TriggerMessage(string message)
        {
            OnMessageReceived?.Invoke(message);
        }
        
        public void TriggerDataChanged(int id, string data)
        {
            OnDataChanged?.Invoke(id, data);
        }
        
        public void TriggerCompleted()
        {
            OnCompleted?.Invoke(this, EventArgs.Empty);
        }
    }
}
