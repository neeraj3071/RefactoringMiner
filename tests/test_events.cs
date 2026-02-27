using System;

class TestEvents
{
    // C# event declaration
    public event EventHandler MyEvent;
    
    public void TestMethod()
    {
        // Subscribe to event
        MyEvent += OnMyEvent;
        
        // Raise event
        MyEvent?.Invoke(this, EventArgs.Empty);
    }
    
    private void OnMyEvent(object sender, EventArgs e)
    {
        Console.WriteLine("Event fired");
    }
}
