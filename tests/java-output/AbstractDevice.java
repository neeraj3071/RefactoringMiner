package TestFeatures;

public abstract class AbstractDevice {
    protected String deviceId;
    public void Initialize();
    public String GetDeviceInfo();
    public void Start() {
        System.out.println("Starting device");
        Initialize();
    }
    public void Stop() {
        System.out.println("Stopping device");
    }
}
public abstract class AbstractController {
    public int GetBatteryLevel();
    public void CheckBattery() {
        int level = GetBatteryLevel();
        System.out.println("Battery level: " + level);
    }
}
