package TestFeatures;

public interface IDevice {
    public void PowerOn();
    public void PowerOff();
    public String GetStatus();
}
public interface IConnectable {
    public void Connect();
    public void Disconnect();
}
public class NetworkDevice implements IDevice, IConnectable {
    private boolean isPoweredOn;
    private boolean isConnected;
    public void PowerOn() {
        isPoweredOn = true;
        System.out.println("Device powered on");
    }
    public void PowerOff() {
        isPoweredOn = false;
        System.out.println("Device powered off");
    }
    public String GetStatus() {
        return isPoweredOn ? "On" : "Off";
    }
    public void Connect() {
        isConnected = true;
        System.out.println("Device connected");
    }
    public void Disconnect() {
        isConnected = false;
        System.out.println("Device disconnected");
    }
}
public class SimpleDevice implements IDevice {
    public void PowerOn() {
        System.out.println("Simple device on");
    }
    public void PowerOff() {
        System.out.println("Simple device off");
    }
    public String GetStatus() {
        return "Ready";
    }
}
