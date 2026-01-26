package TestFeatures;

public class ExceptionHandling {
    public void BasicTryCatch() {
        try {
            int result = 10 / 0;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    public void TryCatchFinally() {
        try {
            System.out.println("Attempting operation");
            PerformOperation();
        } catch (Exception e) {
            System.out.println("Operation failed: " + e.getMessage());
        } finally {
            System.out.println("Cleanup completed");
        }
    }
    public void MultipleCatchBlocks() {
        try {
            PerformOperation();
        } catch (Exception e) {
            System.out.println("Argument error: " + ae.getMessage());
        } catch (Exception e) {
            System.out.println("Invalid operation: " + ioe.getMessage());
        } catch (Exception e) {
            System.out.println("General error: " + e.getMessage());
        }
    }
    public void ThrowException() {
        throw new Exception("Custom exception message");
    }
    public void ValidateAndThrow(String input) {
        if ((input == null)) {
            throw new IllegalArgumentException("Input cannot be null");
        }
        System.out.println("Input is valid");
    }
    public void PerformOperation() {
        throw new UnsupportedOperationException("Operation not supported");
    }
    public void NestedTryCatch() {
        try {
            try {
                PerformOperation();
            } catch (Exception e) {
                System.out.println("Inner catch: " + e.getMessage());
                throw e;
            }
        } catch (Exception e) {
            System.out.println("Inner catch: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("Outer catch: " + e.getMessage());
        }
    }
}
