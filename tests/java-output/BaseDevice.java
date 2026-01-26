package TestFeatures;

public class BaseDevice {
    protected String deviceId;
    public BaseDevice() {
        deviceId = "BASE-001";
    }
    public void Initialize() {
        System.out.println("Base device initialization");
    }
    public void CommonMethod() {
        System.out.println("Common method");
    }
}
public class DerivedDevice extends BaseDevice {
    private boolean isActive;
    public DerivedDevice() {
        isActive = false;
    }
    public void Initialize() {
        System.out.println("Derived device initialization");
        isActive = true;
    }
    public void DerivedMethod() {
        System.out.println("Derived method");
    }
}
public class AdvancedDevice extends DerivedDevice {
    public void AdvancedMethod() {
        System.out.println("Advanced method");
    }
}
