package org.refactoringminer.csharp.debug;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.refactoringminer.csharp.CPatMinerExecutor;
import org.refactoringminer.csharp.CSharpUMLModelASTReader;

import gr.uom.java.xmi.UMLClass;
import gr.uom.java.xmi.UMLModel;

/**
 * Debug runner for tracing the complete flow from C# source to UMLModel
 * 
 * Traces the flow:
 * C# Source → CPatMiner/SrcML → CompilationUnit → String → UMLModelASTReader → UMLModel
 * 
 * Usage: Set breakpoints at key transformation points:
 * 1. CPatMinerExecutor.transformCSharpToJavaAST() - Initial AST generation
 * 2. CPatMinerExecutor.astToString() - AST to string conversion
 * 3. CSharpUMLModelASTReader.processFilesWithCPatMiner() - Processing files
 * 4. UMLModelASTReader.processJavaFileContents() - Parsing Java string
 * 5. UMLModelASTReader.processCompilationUnit() - Creating UMLModel
 */
public class ASTFlowDebugger {
    
    public static void main(String[] args) {
        System.out.println("=== C# to UMLModel Flow Debugger ===\n");
        
        // Allow passing file path as argument, default to UpdateMethod.cs
        String testFilePath = (args.length > 0) ? args[0] : "CS_TO_JAVA_TRANSFORMATION_EG/UpdateMethod.cs";
        
        try {
            // Step 1: Read C# file
            Path path = Paths.get(testFilePath);
            if (!Files.exists(path)) {
                System.err.println("Test file not found: " + testFilePath);
                return;
            }
            
            String csharpContent = Files.readString(path);
            System.out.println("STEP 1: Read C# Source");
            System.out.println("File: " + testFilePath);
            System.out.println("Size: " + csharpContent.length() + " chars");
            System.out.println("--- C# Source Content (first 500 chars) ---");
            System.out.println(csharpContent.substring(0, Math.min(500, csharpContent.length())));
            if (csharpContent.length() > 500) {
                System.out.println("... (truncated)");
            }
            System.out.println("-------------------------------------------\n");
            
            // Step 2: Transform to CompilationUnit AST
            System.out.println("STEP 2: Transform C# to Java CompilationUnit AST");
            System.out.println(">>> Calling CPatMinerExecutor.transformCSharpToJavaAST() <<<");
            CompilationUnit ast = CPatMinerExecutor.transformCSharpToJavaAST(csharpContent, testFilePath);
            
            if (ast == null) {
                System.err.println("ERROR: Failed to create CompilationUnit AST");
                return;
            }
            System.out.println("✓ CompilationUnit created successfully!");
            System.out.println("  - Number of types: " + ast.types().size());
            System.out.println("  - AST root: " + ast.getClass().getSimpleName());
            System.out.println("  - Has package declaration: " + (ast.getPackage() != null));
            System.out.println("  - Number of imports: " + ast.imports().size());
            System.out.println();
            
            // Step 3: Convert AST to String
            System.out.println("STEP 3: Convert CompilationUnit to Java String");
            System.out.println(">>> Calling CPatMinerExecutor.astToString() <<<");
            String javaCode = CPatMinerExecutor.astToString(ast);
            
            if (javaCode == null || javaCode.isEmpty()) {
                System.err.println("ERROR: Failed to convert AST to string");
                return;
            }
            System.out.println("✓ Java code generated successfully!");
            System.out.println("  - Generated code length: " + javaCode.length() + " chars");
            System.out.println("  - Number of lines: " + javaCode.split("\n").length);
            System.out.println("\n=== Generated Java Code (Full Output) ===");
            System.out.println(javaCode);
            System.out.println("========================================\n");
            
            // Step 4: Create file contents map for UMLModel
            System.out.println("STEP 4: Preparing file contents map for UMLModel");
            Map<String, String> fileContents = new HashMap<>();
            String javaFilePath = testFilePath.replace(".cs", ".java");
            fileContents.put(javaFilePath, javaCode);
            System.out.println("  - Mapped file: " + javaFilePath);
            System.out.println("  - File contents size: " + javaCode.length() + " chars");
            
            Set<String> repositoryDirectories = new HashSet<>();
            repositoryDirectories.add("CS_TO_JAVA_TRANSFORMATION_EG/");
            System.out.println("  - Repository directory: CS_TO_JAVA_TRANSFORMATION_EG/");
            System.out.println();
            
            // Step 5: Create UMLModel through CSharpUMLModelASTReader
            System.out.println("STEP 5: Create UMLModel via CSharpUMLModelASTReader");
            System.out.println(">>> Calling CSharpUMLModelASTReader constructor <<<");
            CSharpUMLModelASTReader reader = new CSharpUMLModelASTReader(
                fileContents, 
                repositoryDirectories, 
                false
            );
            
            System.out.println(">>> Retrieving UMLModel from reader <<<");
            UMLModel umlModel = reader.getUmlModel();
            
            if (umlModel == null) {
                System.err.println("ERROR: Failed to create UMLModel");
                return;
            }
            
            System.out.println("✓ UMLModel created successfully!");
            System.out.println();
            
            // Step 6: Display UMLModel contents
            System.out.println("STEP 6: Analyzing UMLModel Output");
            System.out.println("===========================================");
            System.out.println("Total Classes/Interfaces/Enums: " + umlModel.getClassList().size());
            
            // Count different types
            long classes = umlModel.getClassList().stream().filter(c -> !c.isInterface() && !c.isEnum()).count();
            long interfaces = umlModel.getClassList().stream().filter(UMLClass::isInterface).count();
            long enums = umlModel.getClassList().stream().filter(UMLClass::isEnum).count();
            System.out.println("  - Classes: " + classes);
            System.out.println("  - Interfaces: " + interfaces);
            System.out.println("  - Enums: " + enums);
            System.out.println();
            
            System.out.println("Detailed Class Information:");
            System.out.println("---------------------------");
            umlModel.getClassList().forEach(umlClass -> {
                System.out.println("\nClass: " + umlClass.getName());
                System.out.println("  Type: " + (umlClass.isInterface() ? "Interface" : umlClass.isEnum() ? "Enum" : "Class"));
                System.out.println("  Package: " + umlClass.getPackageName());
                System.out.println("  Source file: " + umlClass.getSourceFile());
                System.out.println("  Operations: " + umlClass.getOperations().size());
                System.out.println("  Attributes: " + umlClass.getAttributes().size());
                
                if (!umlClass.getAttributes().isEmpty()) {
                    System.out.println("  Fields:");
                    umlClass.getAttributes().forEach(attr -> {
                        System.out.println("    • " + attr.getType() + " " + attr.getName());
                    });
                }
                
                if (!umlClass.getOperations().isEmpty()) {
                    System.out.println("  Methods:");
                    umlClass.getOperations().forEach(op -> {
                        String params = op.getParameters().isEmpty() ? "" : 
                            " - " + op.getParameters().size() + " param(s)";
                        System.out.println("    • " + op.getName() + "()" + params);
                    });
                }
            });
            
            System.out.println("\n===========================================");
            System.out.println("=== Flow Trace Complete ===");
            System.out.println("All transformation steps executed successfully!");
            System.out.println("Review the output above to trace the complete flow:");
            System.out.println("  1. C# Source → Read from file");
            System.out.println("  2. CompilationUnit AST → CPatMiner transformation");
            System.out.println("  3. Java String → AST serialization");
            System.out.println("  4. File Map → Preparation for UML parsing");
            System.out.println("  5. UMLModel → CSharpUMLModelASTReader parsing");
            System.out.println("  6. Analysis → Complete UML structure");
            System.out.println("===========================================");
            
        } catch (Exception e) {
            System.err.println("\nERROR during flow execution:");
            System.err.println("Message: " + e.getMessage());
            System.err.println("\nStack trace:");
            e.printStackTrace();
        }
    }
}
