package org.refactoringminer.csharp.integration;

import gr.uom.java.xmi.UMLModel;
import gr.uom.java.xmi.diff.UMLModelDiff;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringMinerTimedOutException;
import org.refactoringminer.csharp.CSharpUMLModelASTReader;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CSharpASTBridge - Real integration layer between CPatMiner C# AST and RefactoringMiner
 * 
 * This class provides the bridge to convert C# AST information from CPatMiner
 * into RefactoringMiner's UMLModel format, using ACTUAL CPatMiner AST generation
 * (not simple text transformation).
 * 
 * Flow: C# Source → CPatMiner AST → CompilationUnit → UMLModel → Refactorings
 */
public class CSharpASTBridge {
    
    /**
     * Converts C# file contents to RefactoringMiner's UMLModel format
     * by using CPatMiner to generate proper Java AST from C# code.
     * 
     * @param csharpFileContents Map of C# file paths to their content
     * @param repositoryDirectories Set of repository directories
     * @return UMLModel compatible with RefactoringMiner, built from CPatMiner AST
     */
    public static UMLModel createModelFromCSharp(Map<String, String> csharpFileContents, Set<String> repositoryDirectories) {
        try {
            System.out.println("CSharpASTBridge: Creating UMLModel from C# using CPatMiner AST");
            
            // Use the C# aware UMLModel reader that integrates with CPatMiner
            CSharpUMLModelASTReader reader = new CSharpUMLModelASTReader(
                csharpFileContents, 
                repositoryDirectories, 
                false
            );
            
            UMLModel model = reader.getUmlModel();
            System.out.println("CSharpASTBridge: Successfully created UMLModel with " + 
                             (model != null ? "CPatMiner AST integration" : "null model"));
            return model;
            
        } catch (Exception e) {
            System.err.println("CSharpASTBridge: Failed to create UMLModel from C# content: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create UMLModel from C# content using CPatMiner AST", e);
        }
    }
    
    /**
     * Detects refactorings between two C# project versions using CPatMiner AST
     * and RefactoringMiner's algorithms.
     * 
     * @param csharpFileContentsBefore Map of C# file contents (before version)
     * @param csharpFileContentsAfter Map of C# file contents (after version) 
     * @param repositoryDirectoriesBefore Set of directories in before version
     * @param repositoryDirectoriesAfter Set of directories in after version
     * @return List of detected refactorings
     * @throws RefactoringMinerTimedOutException if processing times out
     */
    public static List<Refactoring> detectRefactorings(
            Map<String, String> csharpFileContentsBefore,
            Map<String, String> csharpFileContentsAfter,
            Set<String> repositoryDirectoriesBefore,
            Set<String> repositoryDirectoriesAfter) throws RefactoringMinerTimedOutException {
        
        System.out.println("CSharpASTBridge: Detecting refactorings using CPatMiner AST integration");
        
        // Create UMLModels from C# content using CPatMiner AST
        UMLModel modelBefore = createModelFromCSharp(csharpFileContentsBefore, repositoryDirectoriesBefore);
        UMLModel modelAfter = createModelFromCSharp(csharpFileContentsAfter, repositoryDirectoriesAfter);
        
        if (modelBefore == null || modelAfter == null) {
            System.err.println("CSharpASTBridge: Failed to create UMLModels - cannot detect refactorings");
            throw new RuntimeException("Failed to create UMLModels from CPatMiner AST");
        }
        
        // Use RefactoringMiner's diff algorithm on CPatMiner-generated models
        UMLModelDiff modelDiff = modelBefore.diff(modelAfter);
        
        // Return the detected refactorings
        List<Refactoring> refactorings = modelDiff.getRefactorings();
        System.out.println("CSharpASTBridge: Detected " + refactorings.size() + 
                         " refactorings using CPatMiner AST");
        
        return refactorings;
    }
}