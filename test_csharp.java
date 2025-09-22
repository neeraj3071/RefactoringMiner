import org.refactoringminer.csharp.CSharpRefactoringMiner;

/**
 * Test class to verify C# RefactoringMiner integration
 */
public class test_csharp {
    public static void main(String[] args) {
        try {
            System.out.println("Testing C# RefactoringMiner integration...");
            
            // Test with a simple args array - just checking if classes load correctly
            String[] testArgs = {"-h"};
            
            // This should load our CSharpRefactoringMiner and show that all dependencies are working
            System.out.println("Calling CSharpRefactoringMiner.main()...");
            CSharpRefactoringMiner.main(testArgs);
            
            System.out.println("C# RefactoringMiner integration test completed successfully!");
        } catch (Exception e) {
            System.err.println("Error testing C# RefactoringMiner: " + e.getMessage());
            e.printStackTrace();
        }
    }
}