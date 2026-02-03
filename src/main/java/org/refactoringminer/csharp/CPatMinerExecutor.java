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
 * 
 * NOTE: CPatMiner is REQUIRED for C# support. If initialization fails, C# files cannot be processed.
 */
public class CPatMinerExecutor {
    
    private static final String CPATMINER_JAR_PATH = "CPatMinerV2/AtomicASTChangeMining/target/AtomicASTChangeMining-0.0.1-SNAPSHOT-jar-with-dependencies.jar";
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
                System.err.println("CPatMinerExecutor: ERROR - CPatMiner JAR not found at: " + jarFile.getAbsolutePath());
                System.err.println("CPatMinerExecutor: C# file support requires CPatMiner. Build it with: mvn clean package -DskipTests in CPatMinerV2/AtomicASTChangeMining");
                transformationClass = null;
                transformMethod = null;
                return;
            }
            
            System.out.println("CPatMinerExecutor: Loading CPatMiner JAR from: " + jarFile.getAbsolutePath());
            
            // Create URLClassLoader with CPatMiner JAR
            // Use the current classloader as parent, which includes all RefactoringMiner dependencies
            URL jarUrl = jarFile.toURI().toURL();
            ClassLoader parentLoader = CPatMinerExecutor.class.getClassLoader();
            cpatMinerClassLoader = new URLClassLoader(new URL[]{jarUrl}, parentLoader);
            
            System.out.println("CPatMinerExecutor: Created classloader with parent: " + parentLoader.getClass().getName());
            
            // Load the Transformation class
            transformationClass = cpatMinerClassLoader.loadClass("transformation.Transformation");
            System.out.println("CPatMinerExecutor: Loaded Transformation class");
            
            // Get the transform_csharp_to_java method
            transformMethod = transformationClass.getMethod("transform_csharp_to_java", String.class);
            System.out.println("CPatMinerExecutor: Found transform_csharp_to_java method");
            
            System.out.println("CPatMinerExecutor: Successfully initialized CPatMiner integration");
            
        } catch (ClassNotFoundException e) {
            System.err.println("CPatMinerExecutor: ERROR - Failed to load transformation.Transformation class: " + e.getMessage());
            System.err.println("CPatMinerExecutor: This usually means GumTree or other dependencies are not properly packaged in the CPatMiner JAR");
            System.err.println("CPatMinerExecutor: C# support is disabled. Rebuild CPatMiner: mvn clean package -DskipTests");
            transformationClass = null;
            transformMethod = null;
        } catch (NoSuchMethodException e) {
            System.err.println("CPatMinerExecutor: ERROR - Failed to find transform_csharp_to_java method: " + e.getMessage());
            System.err.println("CPatMinerExecutor: The CPatMiner JAR appears to be corrupted or incomplete");
            transformationClass = null;
            transformMethod = null;
        } catch (Exception e) {
            System.err.println("CPatMinerExecutor: ERROR - Failed to initialize CPatMiner: " + e.getMessage());
            System.err.println("CPatMinerExecutor: C# support is disabled");
            transformationClass = null;
            transformMethod = null;
        }
    }
    
    /**
     * Transform C# content to Java AST using CPatMiner (REQUIRED for C# support)
     * 
     * @param csharpContent The C# source code content
     * @param filePath The original file path (for debugging)
     * @return CompilationUnit representing the Java AST equivalent
     */
    public static CompilationUnit transformCSharpToJavaAST(String csharpContent, String filePath) {
        if (transformationClass == null || transformMethod == null) {
            System.err.println("CPatMinerExecutor: ERROR - CPatMiner not initialized. C# file processing unavailable: " + filePath);
            return null;
        }
        
        System.out.println("CPatMinerExecutor: Transforming C# file to Java AST: " + filePath);
        return tryCPatMinerTransformation(csharpContent, filePath);
    }
    
    private static CompilationUnit tryCPatMinerTransformation(String csharpContent, String filePath) {
        // CPatMiner must be initialized at this point
        if (transformationClass == null || transformMethod == null) {
            System.err.println("CPatMinerExecutor: ERROR - CPatMiner not initialized when attempting transformation");
            return null;
        }
        
        try {
            System.out.println("CPatMinerExecutor: Executing CPatMiner transformation (" + csharpContent.length() + " chars)...");
            
            // Call CPatMiner's transform_csharp_to_java method
            Object result = transformMethod.invoke(null, csharpContent);
            
            if (result instanceof CompilationUnit) {
                CompilationUnit compilationUnit = (CompilationUnit) result;
                System.out.println("CPatMinerExecutor: SUCCESS - Generated Java AST with " + 
                                 compilationUnit.types().size() + " types");
                return compilationUnit;
            } else if (result == null) {
                System.err.println("CPatMinerExecutor: ERROR - CPatMiner returned null");
                return null;
            } else {
                System.err.println("CPatMinerExecutor: ERROR - CPatMiner returned unexpected type: " + 
                                 result.getClass().getName());
                return null;
            }
            
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            String errorMsg = cause != null ? cause.getClass().getSimpleName() : "unknown";
            String detail = cause != null ? cause.getMessage() : "";
            System.err.println("CPatMinerExecutor: ERROR - CPatMiner execution failed: " + errorMsg);
            if (detail != null && !detail.isEmpty()) {
                System.err.println("CPatMinerExecutor: Details: " + detail);
            }
            if (System.getProperty("debug") != null) {
                e.getCause().printStackTrace();
            }
            return null;
        } catch (Throwable e) {
            System.err.println("CPatMinerExecutor: ERROR - CPatMiner transformation failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            if (System.getProperty("debug") != null) {
                e.printStackTrace();
            }
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