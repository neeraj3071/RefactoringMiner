package org.refactoringminer.csharp.integration;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * CSharpToJavaConverter - Converts C# syntax to Java-like syntax
 * 
 * This converter transforms C# language constructs into equivalent Java constructs
 * that RefactoringMiner's Java AST parser can understand, preserving the semantic
 * structure needed for refactoring detection.
 */
public class CSharpToJavaConverter {
    
    // Patterns for C# to Java syntax conversion
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("namespace\\s+([\\w\\.]+)");
    private static final Pattern USING_PATTERN = Pattern.compile("using\\s+([\\w\\.]+);");
    private static final Pattern CLASS_PATTERN = Pattern.compile("(public|private|protected|internal)?\\s*class\\s+(\\w+)");
    private static final Pattern INTERFACE_PATTERN = Pattern.compile("(public|private|protected|internal)?\\s*interface\\s+(\\w+)");
    private static final Pattern METHOD_PATTERN = Pattern.compile("(public|private|protected|internal)?\\s*(static\\s+)?(\\w+)\\s+(\\w+)\\s*\\(([^)]*)\\)");
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("(public|private|protected|internal)?\\s*(\\w+)\\s+(\\w+)\\s*\\{\\s*get;\\s*set;\\s*\\}");
    private static final Pattern AUTO_PROPERTY_PATTERN = Pattern.compile("(public|private|protected|internal)?\\s*(\\w+)\\s+(\\w+)\\s*\\{\\s*get;\\s*(private\\s+)?set;\\s*\\}");
    
    /**
     * Converts all C# files to Java-like syntax
     */
    public Map<String, String> convertAll(Map<String, String> csharpContents) {
        Map<String, String> javaLikeContents = new HashMap<>();
        
        for (Map.Entry<String, String> entry : csharpContents.entrySet()) {
            String originalPath = entry.getKey();
            String csharpContent = entry.getValue();
            
            // Convert .cs extension to .java for RefactoringMiner compatibility
            String javaPath = convertPathToJava(originalPath);
            String javaContent = convertCSharpToJava(csharpContent, javaPath);
            
            javaLikeContents.put(javaPath, javaContent);
        }
        
        return javaLikeContents;
    }
    
    /**
     * Converts a single C# code string to Java-like syntax (for testing)
     */
    public String convertCSharpToJava(String csharpContent) {
        if (csharpContent == null) {
            return null;
        }
        return convertCSharpToJava(csharpContent, "Test.java");
    }
    
    /**
     * Converts a single C# file to Java-like syntax
     */
    private String convertCSharpToJava(String csharpContent, String javaPath) {
        String converted = csharpContent;
        
        // Convert namespace to package
        converted = NAMESPACE_PATTERN.matcher(converted).replaceAll("package $1;");
        
        // Convert using to import
        converted = USING_PATTERN.matcher(converted).replaceAll("import $1;");
        
        // Convert class declarations (keep visibility modifiers)
        converted = CLASS_PATTERN.matcher(converted).replaceAll("$1 class $2");
        
        // Convert interface declarations
        converted = INTERFACE_PATTERN.matcher(converted).replaceAll("$1 interface $2");
        
        // Convert C# properties to Java getter/setter methods
        converted = convertProperties(converted);
        
        // Convert C# method declarations
        converted = convertMethods(converted);
        
        // Convert C# types to Java equivalents
        converted = convertTypes(converted);
        
        // Convert C# access modifiers
        converted = convertAccessModifiers(converted);
        
        // Convert C# language features
        converted = convertLanguageFeatures(converted);
        
        // Add necessary imports for converted content
        converted = addJavaImports(converted);
        
        return converted;
    }
    
    /**
     * Converts C# properties to Java getter/setter methods
     */
    private String convertProperties(String content) {
        // Convert auto-properties like "public string Name { get; set; }"
        content = AUTO_PROPERTY_PATTERN.matcher(content).replaceAll(match -> {
            String visibility = match.group(1) != null ? match.group(1) : "public";
            String type = match.group(2);
            String name = match.group(3);
            boolean hasPrivateSetter = match.group(4) != null;
            
            String fieldName = Character.toLowerCase(name.charAt(0)) + name.substring(1);
            String getterName = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
            String setterName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
            
            StringBuilder replacement = new StringBuilder();
            replacement.append("private ").append(type).append(" ").append(fieldName).append(";\n");
            replacement.append("    ").append(visibility).append(" ").append(type).append(" ").append(getterName).append("() { return ").append(fieldName).append("; }\n");
            
            String setterVisibility = hasPrivateSetter ? "private" : visibility;
            replacement.append("    ").append(setterVisibility).append(" void ").append(setterName).append("(").append(type).append(" value) { this.").append(fieldName).append(" = value; }");
            
            return replacement.toString();
        });
        
        return content;
    }
    
    /**
     * Converts C# method declarations to Java format
     */
    private String convertMethods(String content) {
        // Convert void methods and handle return types
        content = content.replaceAll("(public|private|protected|internal)\\s+(static\\s+)?void\\s+(\\w+)\\s*\\(([^)]*)\\)", "$1 $2void $3($4)");
        
        // Convert method parameters
        content = content.replaceAll("\\b(string|int|bool|double|float)\\b\\s+(\\w+)", "$1 $2");
        
        return content;
    }
    
    /**
     * Converts C# types to Java equivalents
     */
    private String convertTypes(String content) {
        // Basic type conversions
        content = content.replaceAll("\\bstring\\b", "String");
        content = content.replaceAll("\\bbool\\b", "boolean");
        content = content.replaceAll("\\bobject\\b", "Object");
        content = content.replaceAll("\\bint\\b", "int");
        content = content.replaceAll("\\bdouble\\b", "double");
        content = content.replaceAll("\\bfloat\\b", "float");
        content = content.replaceAll("\\blong\\b", "long");
        content = content.replaceAll("\\bbyte\\b", "byte");
        content = content.replaceAll("\\bchar\\b", "char");
        
        // Collection types
        content = content.replaceAll("\\bList<([^>]+)>", "java.util.List<$1>");
        content = content.replaceAll("\\bDictionary<([^,]+),\\s*([^>]+)>", "java.util.Map<$1, $2>");
        content = content.replaceAll("\\bIEnumerable<([^>]+)>", "java.lang.Iterable<$1>");
        
        return content;
    }
    
    /**
     * Converts C# access modifiers to Java equivalents
     */
    private String convertAccessModifiers(String content) {
        // Convert C# 'internal' to package-private (remove modifier)
        content = content.replaceAll("\\binternal\\b\\s+", "");
        
        // Convert C# 'readonly' to Java 'final'
        content = content.replaceAll("\\breadonly\\b", "final");
        
        // Convert C# 'const' to Java 'static final'
        content = content.replaceAll("\\bconst\\b", "static final");
        
        return content;
    }
    
    /**
     * Converts C# language features to Java equivalents
     */
    private String convertLanguageFeatures(String content) {
        // Convert C# 'var' to appropriate Java type (simplified - use Object)
        content = content.replaceAll("\\bvar\\b\\s+(\\w+)\\s*=", "Object $1 =");
        
        // Convert C# null-conditional operator ?. to regular . (simplified)
        content = content.replaceAll("\\?\\.(?!\\s*=)", ".");
        
        // Convert C# null-coalescing operator ?? to ternary (simplified)
        content = content.replaceAll("(\\w+)\\s*\\?\\?\\s*(\\w+)", "($1 != null ? $1 : $2)");
        
        // Convert C# foreach to Java enhanced for loop
        content = content.replaceAll("foreach\\s*\\(([^\\s]+)\\s+(\\w+)\\s+in\\s+([^)]+)\\)", "for ($1 $2 : $3)");
        
        // Convert C# string interpolation (simplified)
        content = content.replaceAll("\\$\"([^\"]+)\"", "String.format(\"$1\")");
        
        return content;
    }
    
    /**
     * Adds necessary Java imports for converted content
     */
    private String addJavaImports(String content) {
        StringBuilder imports = new StringBuilder();
        
        // Add common Java imports based on detected patterns
        if (content.contains("java.util.List")) {
            imports.append("import java.util.List;\n");
        }
        if (content.contains("java.util.Map")) {
            imports.append("import java.util.Map;\n");
        }
        if (content.contains("java.lang.Iterable")) {
            imports.append("import java.lang.Iterable;\n");
        }
        if (content.contains("String.format")) {
            imports.append("import java.lang.String;\n");
        }
        
        // Insert imports after package declaration
        if (content.contains("package ") && imports.length() > 0) {
            int packageEnd = content.indexOf(';', content.indexOf("package ")) + 1;
            content = content.substring(0, packageEnd) + "\n\n" + imports.toString() + "\n" + content.substring(packageEnd);
        } else if (imports.length() > 0) {
            content = imports.toString() + "\n" + content;
        }
        
        return content;
    }
    
    /**
     * Converts C# file path to Java file path
     */
    private String convertPathToJava(String csharpPath) {
        return csharpPath.replaceAll("\\.cs$", ".java");
    }
    
    /**
     * Extracts class name from Java file path for package inference
     */
    private String extractClassName(String javaPath) {
        String fileName = javaPath.substring(javaPath.lastIndexOf('/') + 1);
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }
}