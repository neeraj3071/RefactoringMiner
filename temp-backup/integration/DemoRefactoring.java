package org.refactoringminer.csharp.integration;

import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;
import gr.uom.java.xmi.diff.CodeRange;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * Demo implementation of a refactoring for demonstration purposes.
 * This simulates the results that would come from the full RefactoringMiner analysis.
 */
public class DemoRefactoring implements Refactoring {
    
    private final String name;
    private final String description;
    private final RefactoringType type;
    
    public DemoRefactoring(String name, String description) {
        this.name = name;
        this.description = description;
        this.type = determineType(name);
    }
    
    private RefactoringType determineType(String name) {
        switch (name.toLowerCase()) {
            case "rename method":
                return RefactoringType.RENAME_METHOD;
            case "extract method":
                return RefactoringType.EXTRACT_OPERATION;
            case "rename class":
                return RefactoringType.RENAME_CLASS;
            case "extract class":
                return RefactoringType.EXTRACT_CLASS;
            case "move/rename file":
                return RefactoringType.MOVE_CLASS;
            default:
                return RefactoringType.RENAME_METHOD; // Default fallback
        }
    }
    
    @Override
    public RefactoringType getRefactoringType() {
        return type;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return name + ": " + description;
    }
    
    @Override
    public String toJSON() {
        return String.format("{\"type\":\"%s\",\"description\":\"%s\"}", name, description);
    }
    
    @Override
    public List<CodeRange> leftSide() {
        return new ArrayList<>(); // Simplified for demo
    }
    
    @Override
    public List<CodeRange> rightSide() {
        return new ArrayList<>(); // Simplified for demo
    }
    
    @Override
    public Set<String> getInvolvedClassesBeforeRefactoring() {
        return new HashSet<>(); // Simplified for demo
    }
    
    @Override
    public Set<String> getInvolvedClassesAfterRefactoring() {
        return new HashSet<>(); // Simplified for demo
    }
}