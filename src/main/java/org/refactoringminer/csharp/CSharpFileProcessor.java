package org.refactoringminer.csharp;
import org.eclipse.jdt.core.dom.CompilationUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * CSharpFileProcessor - Handles C# files by using CPatMiner to generate proper Java AST
 * 
 * This class is responsible for:
 * 1. Identifying C# files (.cs extension)
 * 2. Using CPatMiner's real AST generation to convert C# to Java AST
 * 3. Providing transformed content that RefactoringMiner can process
 * 
 * This is the REAL CPatMiner integration (not simple text transformation)
 */
public class CSharpFileProcessor {
    
    /**
     * Process a map of file contents, transforming C# files using CPatMiner AST
     * @param fileContents Map of file paths to file content
     * @return Map with C# files transformed via CPatMiner AST and Java files passed through
     */
    public static Map<String, String> processFiles(Map<String, String> fileContents) {
        System.out.println("CSharpFileProcessor: Processing " + fileContents.size() + " files with CPatMiner AST");
        
        Map<String, String> processedContents = new HashMap<>();
        int csharpCount = 0;
        int transformedCount = 0;
        
        // Use CPatMiner to process C# files
        Map<String, CompilationUnit> csharpAsts = CPatMinerExecutor.processCSharpFiles(fileContents);
        
        // Convert CompilationUnit ASTs back to string format
        for (Map.Entry<String, CompilationUnit> entry : csharpAsts.entrySet()) {
            String javaFilePath = entry.getKey();
            CompilationUnit ast = entry.getValue();
            
            String javaCode = CPatMinerExecutor.astToString(ast);
            if (javaCode != null && !javaCode.trim().isEmpty()) {
                processedContents.put(javaFilePath, javaCode);
                transformedCount++;
                System.out.println("CSharpFileProcessor: Successfully transformed C# to Java AST: " + javaFilePath + 
                                 " (" + javaCode.length() + " chars)");
            } else {
                System.err.println("CSharpFileProcessor: Failed to convert AST to string for: " + javaFilePath);
            }
        }
        
        // Count original C# files
        for (String filePath : fileContents.keySet()) {
            if (isCSharpFile(filePath)) {
                csharpCount++;
            } else if (isJavaFile(filePath)) {
                // Include existing Java files
                processedContents.put(filePath, fileContents.get(filePath));
            }
        }
        
        System.out.println("CSharpFileProcessor: Found " + csharpCount + " C# files, successfully transformed " + transformedCount + " using CPatMiner AST");
        System.out.println("CSharpFileProcessor: Final processed contents has " + processedContents.size() + " files");
        
        return processedContents;
    }

    /**
     * Check if a file is a C# source file
     */
    private static boolean isCSharpFile(String filePath) {
        return filePath != null && filePath.toLowerCase().endsWith(".cs");
    }

    /**
     * Check if a file is a Java source file
     */
    private static boolean isJavaFile(String filePath) {
        return filePath != null && filePath.toLowerCase().endsWith(".java");
    }
    /**
     * Process repository directories to ensure C# relevant paths are included
     * 
     * @param originalDirectories Set of original repository directories
     * @return Set of processed directories (may include C# specific paths)
     */
    public static Set<String> processRepositoryDirectories(Set<String> originalDirectories) {
        // For now, pass through all directories
        // Could be enhanced to filter C# specific directories if needed
        return originalDirectories;
    }
}