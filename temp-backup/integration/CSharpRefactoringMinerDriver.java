package org.refactoringminer.csharp.integration;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import transformation.Transformation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main integration driver that orchestrates the pipeline:
 * C# source code -> CPatMinerV2 transformation -> RefactoringMiner analysis
 * 
 * This class provides the main entry point for analyzing C# codebases using
 * RefactoringMiner's refactoring detection capabilities by leveraging 
 * CPatMinerV2's C# to Java AST transformation.
 * 
 * @author Integration Pipeline
 * @version 1.0
 */
public class CSharpRefactoringMinerDriver {
    
    private final GitHistoryRefactoringMiner refactoringMiner;
    private final CSharpASTTransformer astTransformer;
    
    /**
     * Constructor initializes the refactoring detection components
     */
    public CSharpRefactoringMinerDriver() {
        this.refactoringMiner = new GitHistoryRefactoringMinerImpl();
        this.astTransformer = new CSharpASTTransformer();
    }
    
    /**
     * Detect refactorings between two C# project directories
     * 
     * @param previousPath Path to the previous version of the C# project
     * @param nextPath Path to the next version of the C# project
     * @param handler Handler to process detected refactorings
     */
    public void detectRefactoringsAtDirectories(Path previousPath, Path nextPath, RefactoringHandler handler) {
        try {
            // Step 1: Extract C# files from both directories
            Map<String, String> previousFiles = extractCSharpFiles(previousPath);
            Map<String, String> nextFiles = extractCSharpFiles(nextPath);
            
            // Step 2: Transform C# files to Java-like AST structure
            Map<String, String> transformedPreviousFiles = transformCSharpToJavaLike(previousFiles);
            Map<String, String> transformedNextFiles = transformCSharpToJavaLike(nextFiles);
            
            // Step 3: Create temporary directories with transformed files
            Path tempPreviousDir = createTemporaryJavaProject(transformedPreviousFiles);
            Path tempNextDir = createTemporaryJavaProject(transformedNextFiles);
            
            // Step 4: Use RefactoringMiner to detect refactorings
            refactoringMiner.detectAtDirectories(tempPreviousDir, tempNextDir, new RefactoringHandler() {
                @Override
                public void handle(String commitId, List<Refactoring> refactorings) {
                    // Transform detected refactorings back to C# context
                    List<Refactoring> transformedRefactorings = transformRefactoringsToCSContext(refactorings);
                    handler.handle(commitId, transformedRefactorings);
                }
                
                @Override
                public void handleException(String commit, Exception e) {
                    handler.handleException(commit, e);
                }
            });
            
            // Step 5: Cleanup temporary directories
            cleanupTemporaryDirectories(tempPreviousDir, tempNextDir);
            
        } catch (Exception e) {
            handler.handleException("", e);
        }
    }
    
    /**
     * Extract all C# files from a directory recursively
     */
    private Map<String, String> extractCSharpFiles(Path directoryPath) throws Exception {
        Map<String, String> csharpFiles = new HashMap<>();
        
        Files.walk(directoryPath)
            .filter(path -> path.toString().toLowerCase().endsWith(".cs"))
            .forEach(path -> {
                try {
                    String relativePath = directoryPath.relativize(path).toString();
                    String content = Files.readString(path);
                    csharpFiles.put(relativePath, content);
                } catch (Exception e) {
                    System.err.println("Error reading C# file: " + path + " - " + e.getMessage());
                }
            });
            
        return csharpFiles;
    }
    
    /**
     * Transform C# files to Java-like AST using CPatMinerV2's transformation
     */
    private Map<String, String> transformCSharpToJavaLike(Map<String, String> csharpFiles) {
        Map<String, String> transformedFiles = new HashMap<>();
        
        for (Map.Entry<String, String> entry : csharpFiles.entrySet()) {
            try {
                String csharpFilePath = entry.getKey();
                String csharpContent = entry.getValue();
                
                // Use CPatMinerV2's transformation logic
                CompilationUnit transformedAST = Transformation.transform_csharp_to_java(csharpContent);
                
                if (transformedAST != null) {
                    String javaLikePath = csharpFilePath.replace(".cs", ".java");
                    String javaLikeContent = transformedAST.toString();
                    transformedFiles.put(javaLikePath, javaLikeContent);
                }
            } catch (Exception e) {
                System.err.println("Error transforming C# file: " + entry.getKey() + " - " + e.getMessage());
                // Continue with other files even if one fails
            }
        }
        
        return transformedFiles;
    }
    
    /**
     * Create a temporary directory structure with transformed Java-like files
     */
    private Path createTemporaryJavaProject(Map<String, String> transformedFiles) throws Exception {
        Path tempDir = Files.createTempDirectory("csharp_refactoring_analysis_");
        
        for (Map.Entry<String, String> entry : transformedFiles.entrySet()) {
            Path filePath = tempDir.resolve(entry.getKey());
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, entry.getValue().getBytes());
        }
        
        return tempDir;
    }
    
    /**
     * Transform detected refactorings back to C# context
     * Maps Java-like file paths and construct names back to their C# equivalents
     */
    private List<Refactoring> transformRefactoringsToCSContext(List<Refactoring> refactorings) {
        return refactorings.stream()
            .map(this::transformSingleRefactoringToCSContext)
            .collect(Collectors.toList());
    }
    
    /**
     * Transform a single refactoring to C# context
     */
    private Refactoring transformSingleRefactoringToCSContext(Refactoring refactoring) {
        // Create a wrapper that transforms file paths from .java back to .cs
        // and adjusts method/class names to C# conventions
        return new CSharpRefactoringWrapper(refactoring);
    }
    
    /**
     * Clean up temporary directories created during analysis
     */
    private void cleanupTemporaryDirectories(Path... tempDirs) {
        for (Path tempDir : tempDirs) {
            try {
                Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (Exception e) {
                            System.err.println("Warning: Could not delete temp file: " + path);
                        }
                    });
            } catch (Exception e) {
                System.err.println("Warning: Could not cleanup temporary directory: " + tempDir);
            }
        }
    }
    
    /**
     * Main method for command-line usage
     * Usage: java CSharpRefactoringMinerDriver <previous_path> <next_path>
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java CSharpRefactoringMinerDriver <previous_path> <next_path>");
            System.exit(1);
        }
        
        Path previousPath = Paths.get(args[0]);
        Path nextPath = Paths.get(args[1]);
        
        if (!Files.exists(previousPath) || !Files.exists(nextPath)) {
            System.err.println("Error: One or both specified paths do not exist");
            System.exit(1);
        }
        
        CSharpRefactoringMinerDriver driver = new CSharpRefactoringMinerDriver();
        
        driver.detectRefactoringsAtDirectories(previousPath, nextPath, new RefactoringHandler() {
            @Override
            public void handle(String commitId, List<Refactoring> refactorings) {
                System.out.println("=== Detected Refactorings ===");
                for (Refactoring refactoring : refactorings) {
                    System.out.println(refactoring.toString());
                }
            }
            
            @Override
            public void handleException(String commit, Exception e) {
                System.err.println("Error processing: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}