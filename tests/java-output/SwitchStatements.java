package TestFeatures;

public class SwitchStatements {
    public void BasicSwitch(int value) {
        switch ((value)) {
            case 0:
                System.out.println("Zero");
                break;
            case 1:
                System.out.println("One");
                break;
            case 2:
                System.out.println("Two");
                break;
            default:
                System.out.println("Other");
                break;
        }
    }
    public void SwitchWithString(String input) {
        switch ((input)) {
            case "start":
                System.out.println("Starting");
                break;
            case "stop":
                System.out.println("Stopping");
                break;
            case "pause":
                System.out.println("Pausing");
                break;
            default:
                System.out.println("Unknown command");
                break;
        }
    }
    public void SwitchWithFallThrough(int value) {
        switch ((value)) {
            case 1:
            case 2:
            case 3:
                System.out.println("Low value");
                break;
            case 4:
            case 5:
                System.out.println("Medium value");
                break;
            default:
                System.out.println("High value");
                break;
        }
    }
    public String SwitchWithReturn(int code) {
        switch ((code)) {
            case 200:
                return "OK";
            case 404:
                return "Not Found";
            case 500:
                return "Internal Error";
            default:
                return "Unknown";
        }
    }
    public void SwitchWithMultipleStatements(int option) {
        switch ((option)) {
            case 1:
                System.out.println("Option 1 selected");
                ProcessOption1();
                break;
            case 2:
                System.out.println("Option 2 selected");
                ProcessOption2();
                break;
            default:
                System.out.println("Invalid option");
                break;
        }
    }
    public void ProcessOption1() {
        System.out.println("Processing option 1");
    }
    public void ProcessOption2() {
        System.out.println("Processing option 2");
    }
}
