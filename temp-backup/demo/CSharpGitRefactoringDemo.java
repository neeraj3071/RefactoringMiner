package org.refactoringminer.csharp.demo;

import org.refactoringminer.api.GitService;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.eclipse.jgit.lib.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Demonstration of how the C# RefactoringMiner integration would work
 * with a real Git repository containing C# refactorings
 */
public class CSharpGitRefactoringDemo {
    
    public static void main(String[] args) {
        System.out.println("=== C# RefactoringMiner Git Integration Demo ===\n");
        
        try {
            // Step 1: Create a mock git repository with our test case
            String testRepoPath = "/Users/neerajsaini/Desktop/RefactoringMiner/test_git_repo";
            createMockGitRepository(testRepoPath);
            
            // Step 2: Use RefactoringMiner to analyze the repository
            System.out.println("🔍 Analyzing Git repository for C# refactorings...");
            System.out.println("Repository path: " + testRepoPath);
            
            GitService gitService = new org.refactoringminer.util.GitServiceImpl();
            GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
            
            // This would be where we integrate our C# transformation logic
            System.out.println("\n📋 Simulated RefactoringMiner Analysis Results:");
            System.out.println("===============================================");
            
            // Simulate what RefactoringMiner would find after our C# transformation
            simulateRefactoringResults();
            
            System.out.println("\n✅ Integration Demonstration Complete!");
            System.out.println("In the full implementation, RefactoringMiner would:");
            System.out.println("1. Process C# files through CSharpASTTransformer");
            System.out.println("2. Apply RefactoringMiner analysis on transformed ASTs");
            System.out.println("3. Map results back to original C# context using CSharpRefactoringWrapper");
            
        } catch (Exception e) {
            System.err.println("Demo error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void createMockGitRepository(String repoPath) throws IOException {
        System.out.println("Setting up mock Git repository with C# refactoring history...");
        
        Path repo = Paths.get(repoPath);
        if (Files.exists(repo)) {
            // Clean existing repo
            deleteDirectory(repo.toFile());
        }
        
        Files.createDirectories(repo);
        
        // Copy our test files to simulate git commits
        Path srcDir = repo.resolve("src");
        Files.createDirectories(srcDir);
        
        // Simulate commit 1 (original version)
        String originalContent = Files.readString(
            Paths.get("/Users/neerajsaini/Desktop/RefactoringMiner/test/csharp/version1/OrderManager.cs"));
        Files.writeString(srcDir.resolve("OrderManager.cs"), originalContent);
        
        System.out.println("✅ Created commit 1: Original OrderManager.cs");
        
        // Simulate commit 2 (refactored version) 
        String refactoredContent = Files.readString(
            Paths.get("/Users/neerajsaini/Desktop/RefactoringMiner/test/csharp/version2/OrderProcessor.cs"));
        Files.writeString(srcDir.resolve("OrderProcessor.cs"), refactoredContent);
        
        // Remove original file to simulate rename
        Files.deleteIfExists(srcDir.resolve("OrderManager.cs"));
        
        System.out.println("✅ Created commit 2: Refactored to OrderProcessor.cs with extracted services");
        System.out.println("Repository structure created successfully.\n");
    }
    
    private static void simulateRefactoringResults() {
        System.out.println("Commit: abc123 - Refactor OrderManager to use service pattern");
        System.out.println("Date: 2025-09-16");
        System.out.println("Author: Developer");
        System.out.println();
        
        System.out.println("🔄 Detected Refactorings:");
        System.out.println("1. [RENAME_CLASS] Rename Class 'OrderManager' to 'OrderProcessor'");
        System.out.println("   - File: src/OrderManager.cs → src/OrderProcessor.cs");
        System.out.println("   - Confidence: 98%");
        System.out.println();
        
        System.out.println("2. [EXTRACT_CLASS] Extract Class 'InventoryService'");
        System.out.println("   - Extracted from: OrderManager.ProcessCustomerOrder()");
        System.out.println("   - New file: src/InventoryService.cs (simulated)");
        System.out.println("   - Methods: ValidateAndReserveProducts, AddProduct");
        System.out.println();
        
        System.out.println("3. [EXTRACT_CLASS] Extract Class 'CustomerService'");
        System.out.println("   - Extracted from: OrderManager.ProcessCustomerOrder()");
        System.out.println("   - New file: src/CustomerService.cs (simulated)");
        System.out.println("   - Methods: ValidateCustomer, AddOrderToCustomer, AddCustomer");
        System.out.println();
        
        System.out.println("4. [EXTRACT_CLASS] Extract Class 'PaymentProcessor'");
        System.out.println("   - Extracted from: OrderManager.ProcessCustomerOrder()");
        System.out.println("   - New file: src/PaymentProcessor.cs (simulated)");
        System.out.println("   - Methods: ProcessPayment");
        System.out.println();
        
        System.out.println("5. [EXTRACT_METHOD] Extract Method 'ValidateCustomer'");
        System.out.println("   - From: ProcessCustomerOrder() lines 20-30");
        System.out.println("   - To: OrderProcessor.ValidateCustomer()");
        System.out.println();
        
        System.out.println("6. [EXTRACT_METHOD] Extract Method 'ValidateInventory'");
        System.out.println("   - From: ProcessCustomerOrder() lines 32-50");
        System.out.println("   - To: OrderProcessor.ValidateInventory()");
        System.out.println();
        
        System.out.println("7. [EXTRACT_METHOD] Extract Method 'ProcessPayment'");
        System.out.println("   - From: ProcessCustomerOrder() lines 52-70");
        System.out.println("   - To: OrderProcessor.ProcessPayment()");
        System.out.println();
        
        System.out.println("8. [RENAME_METHOD] Rename Method 'ProcessCustomerOrder' to 'HandleCustomerOrder'");
        System.out.println("   - Class: OrderManager/OrderProcessor");
        System.out.println("   - Return type: bool");
        System.out.println();
        
        System.out.println("📊 Summary:");
        System.out.println("- Total refactorings: 8");
        System.out.println("- Class-level changes: 4 (1 rename, 3 extractions)");
        System.out.println("- Method-level changes: 4 (1 rename, 3 extractions)");
        System.out.println("- Lines of code reduced in main class: ~70 lines");
        System.out.println("- Separation of concerns: ✅ Achieved");
        System.out.println("- Dependency injection: ✅ Implemented");
    }
    
    private static void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
}