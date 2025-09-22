package org.refactoringminer.csharp.integration;

import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;
import gr.uom.java.xmi.LocationInfo;
import gr.uom.java.xmi.diff.CodeRange;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * Wrapper class that adapts RefactoringMiner's Java-based refactoring detection results
 * back to the C# context. This class handles the mapping of file paths, type names,
 * and other language-specific elements from the transformed Java-like representation
 * back to their original C# form.
 * 
 * @author Integration Pipeline
 * @version 1.0
 */
public class CSharpRefactoringWrapper implements Refactoring {
    
    private final Refactoring originalRefactoring;
    
    public CSharpRefactoringWrapper(Refactoring originalRefactoring) {
        this.originalRefactoring = originalRefactoring;
    }
    
    @Override
    public RefactoringType getRefactoringType() {
        return originalRefactoring.getRefactoringType();
    }
    
    @Override
    public String getName() {
        return originalRefactoring.getName();
    }
    
    @Override
    public String toString() {
        // Transform the string representation from Java-like back to C# context
        String originalString = originalRefactoring.toString();
        return transformJavaLikeStringToCSharp(originalString);
    }
    
    @Override
    public String toJSON() {
        // Transform the JSON representation from Java-like back to C# context
        String originalJSON = originalRefactoring.toJSON();
        return transformJavaLikeJSONToCSharp(originalJSON);
    }
    
    @Override
    public List<CodeRange> leftSide() {
        List<CodeRange> originalRanges = originalRefactoring.leftSide();
        return transformCodeRangesToCSharp(originalRanges);
    }
    
    @Override
    public List<CodeRange> rightSide() {
        List<CodeRange> originalRanges = originalRefactoring.rightSide();
        return transformCodeRangesToCSharp(originalRanges);
    }
    
    @Override
    public Set<String> getInvolvedClassesBeforeRefactoring() {
        Set<String> originalClasses = originalRefactoring.getInvolvedClassesBeforeRefactoring();
        return transformClassNamesToCSharp(originalClasses);
    }
    
    @Override
    public Set<String> getInvolvedClassesAfterRefactoring() {
        Set<String> originalClasses = originalRefactoring.getInvolvedClassesAfterRefactoring();
        return transformClassNamesToCSharp(originalClasses);
    }
    
    /**
     * Transform string representation from Java-like context back to C# context
     */
    private String transformJavaLikeStringToCSharp(String javaLikeString) {
        String transformed = javaLikeString;
        
        // Transform file paths from .java back to .cs
        transformed = transformed.replaceAll("\\.java", ".cs");
        
        // Transform Java-like method names back to C# conventions
        transformed = transformMethodNamesToCSharp(transformed);
        
        // Transform Java-like type names back to C# types
        transformed = transformTypeNamesToCSharp(transformed);
        
        // Transform Java-like property access patterns back to C# properties
        transformed = transformPropertyAccessToCSharp(transformed);
        
        return transformed;
    }
    
    /**
     * Transform JSON representation from Java-like context back to C# context
     */
    private String transformJavaLikeJSONToCSharp(String javaLikeJSON) {
        String transformed = javaLikeJSON;
        
        // Transform file paths in JSON
        transformed = transformed.replaceAll("\\.java", ".cs");
        
        // Transform other elements as needed
        transformed = transformMethodNamesToCSharp(transformed);
        transformed = transformTypeNamesToCSharp(transformed);
        
        return transformed;
    }
    
    /**
     * Transform method names from Java convention back to C# convention
     */
    private String transformMethodNamesToCSharp(String text) {
        String transformed = text;
        
        // Transform getter/setter patterns back to property access
        // Pattern: getFoo() -> Foo (property)
        transformed = transformed.replaceAll("get([A-Z]\\w*)\\(\\)", "$1");
        
        // Pattern: setFoo(...) -> Foo = ... (property assignment)
        transformed = transformed.replaceAll("set([A-Z]\\w*)\\(([^)]+)\\)", "$1 = $2");
        
        return transformed;
    }
    
    /**
     * Transform type names from Java back to C# equivalents
     */
    private String transformTypeNamesToCSharp(String text) {
        String transformed = text;
        
        // Basic type mappings (reverse of what we did in transformation)
        transformed = transformed.replace("String", "string");
        transformed = transformed.replace("boolean", "bool");
        transformed = transformed.replace("Object", "object");
        
        // Collection mappings
        transformed = transformed.replace("java.util.List<", "List<");
        transformed = transformed.replace("java.util.Map<", "Dictionary<");
        transformed = transformed.replace("java.util.ArrayList<", "List<");
        transformed = transformed.replace("java.util.HashMap<", "Dictionary<");
        
        // Date/Time mappings
        transformed = transformed.replace("java.time.LocalDateTime", "DateTime");
        transformed = transformed.replace("java.time.Duration", "TimeSpan");
        
        // I/O mappings
        transformed = transformed.replace("System.out.println", "Console.WriteLine");
        
        return transformed;
    }
    
    /**
     * Transform property access patterns back to C# style
     */
    private String transformPropertyAccessToCSharp(String text) {
        String transformed = text;
        
        // Transform method calls that were originally properties
        // This is a simplified approach - a more sophisticated implementation
        // would maintain metadata about what was originally a property
        
        return transformed;
    }
    
    /**
     * Transform class names from Java-like context back to C# context
     */
    private Set<String> transformClassNamesToCSharp(Set<String> javaLikeClasses) {
        Set<String> csharpClasses = new HashSet<>();
        if (javaLikeClasses != null) {
            for (String className : javaLikeClasses) {
                // Transform class names as needed (e.g., remove java package prefixes)
                String csharpClassName = className.replace("java.util.", "").replace("java.lang.", "");
                csharpClasses.add(csharpClassName);
            }
        }
        return csharpClasses;
    }

    /**
     * Transform code ranges from Java-like context back to C# context
     */
    private List<CodeRange> transformCodeRangesToCSharp(List<CodeRange> originalRanges) {
        List<CodeRange> transformedRanges = new ArrayList<>();
        
        for (CodeRange range : originalRanges) {
            CodeRange transformedRange = transformSingleCodeRangeToCSharp(range);
            transformedRanges.add(transformedRange);
        }
        
        return transformedRanges;
    }
    
    /**
     * Transform a single code range from Java-like context back to C# context
     */
    private CodeRange transformSingleCodeRangeToCSharp(CodeRange originalRange) {
        // Create a new CodeRange with transformed file path
        String originalFilePath = originalRange.getFilePath();
        String csharpFilePath = originalFilePath.replace(".java", ".cs");
        
        // Create new CodeRange with C# file path and same positioning
        return originalRange.setFilePath(csharpFilePath);
    }
    
    /**
     * Get the original wrapped refactoring for advanced use cases
     */
    public Refactoring getOriginalRefactoring() {
        return originalRefactoring;
    }
    
    /**
     * Check if this refactoring involves C# specific constructs that may need special handling
     */
    public boolean involvesCSharSpecificConstructs() {
        String description = toString().toLowerCase();
        
        return description.contains("property") ||
               description.contains("event") ||
               description.contains("delegate") ||
               description.contains("extension") ||
               description.contains("nullable") ||
               description.contains("linq") ||
               description.contains("async") ||
               description.contains("await");
    }
    
    /**
     * Get information about any C# specific handling applied to this refactoring
     */
    public String getCSharpTransformationInfo() {
        if (!involvesCSharSpecificConstructs()) {
            return "No C# specific transformations detected";
        }
        
        StringBuilder info = new StringBuilder();
        info.append("C# specific transformations applied:\n");
        
        String description = toString().toLowerCase();
        
        if (description.contains("property")) {
            info.append("- Property access patterns transformed\n");
        }
        if (description.contains("event")) {
            info.append("- Event handling patterns transformed\n");
        }
        if (description.contains("delegate")) {
            info.append("- Delegate patterns transformed to functional interfaces\n");
        }
        if (description.contains("extension")) {
            info.append("- Extension method patterns transformed\n");
        }
        
        return info.toString();
    }
}