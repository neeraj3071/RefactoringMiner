package org.refactoringminer.csharp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * CPatMinerExecutor - Executes CPatMiner to generate C# AST
 * 
 * This class is responsible for:
 * 1. Loading CPatMiner JAR dynamically
 * 2. Executing CPatMiner's transform_csharp_to_java() method
 * 3. Capturing the generated CompilationUnit AST
 * 4. Managing temporary files for C# content
 */
public class CPatMinerExecutor {
    
    private static final String CPATMINER_JAR_PATH = "CPatMinerV2/AtomicASTChangeMining/target/AtomicASTChangeMining-0.0.1-SNAPSHOT.jar";
    private static URLClassLoader cpatMinerClassLoader;
    private static Class<?> transformationClass;
    private static Method transformMethod;
    
    static {
        initializeCPatMiner();
    }
    
    /**
     * Initialize CPatMiner by loading the JAR and reflection setup
     */
    private static void initializeCPatMiner() {
        try {
            System.out.println("CPatMinerExecutor: Initializing CPatMiner integration...");
            
            // Get the absolute path to CPatMiner JAR
            File jarFile = new File(CPATMINER_JAR_PATH);
            if (!jarFile.exists()) {
                throw new RuntimeException("CPatMiner JAR not found at: " + jarFile.getAbsolutePath());
            }
            
            System.out.println("CPatMinerExecutor: Loading CPatMiner JAR from: " + jarFile.getAbsolutePath());
            
            // Create URLClassLoader for CPatMiner JAR
            URL jarUrl = jarFile.toURI().toURL();
            cpatMinerClassLoader = new URLClassLoader(new URL[]{jarUrl}, CPatMinerExecutor.class.getClassLoader());
            
            // Load the Transformation class
            transformationClass = cpatMinerClassLoader.loadClass("transformation.Transformation");
            
            // Get the transform_csharp_to_java method
            transformMethod = transformationClass.getMethod("transform_csharp_to_java", String.class);
            
            System.out.println("CPatMinerExecutor: Successfully initialized CPatMiner integration");
            
        } catch (Exception e) {
            System.err.println("CPatMinerExecutor: Failed to initialize CPatMiner: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize CPatMiner", e);
        }
    }
    
    /**
     * Transform C# content to Java AST using CPatMiner
     * Falls back to direct srcML if CPatMiner fails due to dependencies
     * 
     * @param csharpContent The C# source code content
     * @param filePath The original file path (for debugging)
     * @return CompilationUnit representing the Java AST equivalent
     */
    public static CompilationUnit transformCSharpToJavaAST(String csharpContent, String filePath) {
        System.out.println("CPatMinerExecutor: Attempting C# to Java AST transformation for: " + filePath);
        
        // First try CPatMiner (preferred method)
        CompilationUnit result = tryCPatMinerTransformation(csharpContent, filePath);
        
        if (result != null) {
            System.out.println("CPatMinerExecutor: Successfully used CPatMiner for " + filePath);
            return result;
        }
        
        // Fallback to direct srcML approach
        System.out.println("CPatMinerExecutor: CPatMiner failed, falling back to direct srcML for " + filePath);
        result = SrcMLBasedCSharpProcessor.transformCSharpToJavaAST(csharpContent, filePath);
        
        if (result != null) {
            System.out.println("CPatMinerExecutor: Successfully used direct srcML fallback for " + filePath);
            return result;
        }
        
        System.err.println("CPatMinerExecutor: Both CPatMiner and srcML fallback failed for " + filePath);
        return null;
    }
    
    /**
     * Try CPatMiner transformation (may fail due to GumTree dependencies)
     */
    private static CompilationUnit tryCPatMinerTransformation(String csharpContent, String filePath) {
        try {
            System.out.println("CPatMinerExecutor: Trying CPatMiner transformation for: " + filePath);
            System.out.println("CPatMinerExecutor: C# content length: " + csharpContent.length());
            
            // Call CPatMiner's transform_csharp_to_java method
            Object result = transformMethod.invoke(null, csharpContent);
            
            if (result instanceof CompilationUnit) {
                CompilationUnit compilationUnit = (CompilationUnit) result;
                System.out.println("CPatMinerExecutor: CPatMiner generated Java AST with " + 
                                 compilationUnit.types().size() + " types for " + filePath);
                return compilationUnit;
            } else {
                System.err.println("CPatMinerExecutor: CPatMiner returned non-CompilationUnit result: " + 
                                 (result != null ? result.getClass().getName() : "null"));
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("CPatMinerExecutor: CPatMiner transformation failed for " + filePath + ": " + e.getMessage());
            // Don't print full stack trace here as we have a fallback
            return null;
        }
    }
    
    /**
     * Process multiple C# files and generate their Java AST equivalents
     * 
     * @param csharpFileContents Map of C# file paths to their content
     * @return Map of processed file paths to their CompilationUnit AST
     */
    public static Map<String, CompilationUnit> processCSharpFiles(Map<String, String> csharpFileContents) {
        System.out.println("CPatMinerExecutor: Processing " + csharpFileContents.size() + " C# files with CPatMiner");
        
        Map<String, CompilationUnit> astResults = new HashMap<>();
        int successCount = 0;
        int failureCount = 0;
        
        for (Map.Entry<String, String> entry : csharpFileContents.entrySet()) {
            String filePath = entry.getKey();
            String content = entry.getValue();
            
            if (isCSharpFile(filePath)) {
                CompilationUnit ast = transformCSharpToJavaAST(content, filePath);
                if (ast != null) {
                    // Convert .cs file path to .java for RefactoringMiner compatibility
                    String javaFilePath = convertCSharpPathToJava(filePath);
                    astResults.put(javaFilePath, ast);
                    successCount++;
                } else {
                    failureCount++;
                    System.err.println("CPatMinerExecutor: Failed to process: " + filePath);
                }
            }
        }
        
        System.out.println("CPatMinerExecutor: Processing completed - Success: " + successCount + 
                         ", Failures: " + failureCount);
        return astResults;
    }
    
    /**
     * Convert CompilationUnit AST back to Java source code string
     * 
     * @param compilationUnit The AST to convert
     * @return Java source code as string
     */
    public static String astToString(CompilationUnit compilationUnit) {
        if (compilationUnit == null) {
            return "";
        }
        return compilationUnit.toString();
    }
    
    /**
     * Check if a file is a C# source file
     */
    private static boolean isCSharpFile(String filePath) {
        return filePath != null && filePath.toLowerCase().endsWith(".cs");
    }
    
    /**
     * Convert C# file path to Java file path
     */
    private static String convertCSharpPathToJava(String csharpPath) {
        return csharpPath.replaceAll("\\.cs$", ".java");
    }
    
    /**
     * Clean up resources
     */
    public static void cleanup() {
        if (cpatMinerClassLoader != null) {
            try {
                cpatMinerClassLoader.close();
                System.out.println("CPatMinerExecutor: Cleaned up CPatMiner class loader");
            } catch (IOException e) {
                System.err.println("CPatMinerExecutor: Error cleaning up class loader: " + e.getMessage());
            }
        }
    }
}