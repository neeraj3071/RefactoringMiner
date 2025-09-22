package org.refactoringminer.csharp;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleCSharpToJavaTransformer {
    
    public static CompilationUnit transformCSharpToJava(String csharpContent, String fileName) {
        try {
            System.out.println("SimpleCSharpToJavaTransformer: Transforming C# to Java for " + fileName);
            
            // Simple text-based transformations
            String javaContent = convertCSharpSyntaxToJava(csharpContent);
            
            System.out.println("SimpleCSharpToJavaTransformer: Transformation completed, parsing as Java");
            
            // Parse the transformed Java content using Eclipse JDT
            return parseJavaContent(javaContent, fileName);
            
        } catch (Exception e) {
            System.err.println("SimpleCSharpToJavaTransformer: Error transforming " + fileName + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private static String convertCSharpSyntaxToJava(String csharpContent) {
        String javaContent = csharpContent;
        
        // Convert C# using statements to Java imports
        javaContent = javaContent.replaceAll("using\\s+System;", "import java.lang.*;");
        javaContent = javaContent.replaceAll("using\\s+([^;]+);", "import $1.*;");
        
        // Convert C# namespace to Java package
        javaContent = javaContent.replaceAll("namespace\\s+([^{]+)\\s*\\{", "package $1;");
        
        // Convert C# Console.WriteLine to System.out.println
        javaContent = javaContent.replaceAll("Console\\.WriteLine\\(", "System.out.println(");
        
        // Convert C# string to Java String (if lowercase)
        javaContent = javaContent.replaceAll("\\bstring\\b", "String");
        
        // Convert C# var to appropriate Java types (simplified)
        javaContent = javaContent.replaceAll("\\bvar\\b", "Object");
        
        // Convert C# properties with get/set to simple fields (simplified)
        Pattern propertyPattern = Pattern.compile("public\\s+(\\w+)\\s+(\\w+)\\s*\\{\\s*get;\\s*set;\\s*\\}");
        Matcher propertyMatcher = propertyPattern.matcher(javaContent);
        javaContent = propertyMatcher.replaceAll("public $1 $2;");
        
        // Remove extra closing braces from namespace conversion
        // Count opening and closing braces to balance them
        int openBraces = countOccurrences(javaContent, '{');
        int closeBraces = countOccurrences(javaContent, '}');
        
        // If we have one extra closing brace (from namespace), remove it
        if (closeBraces > openBraces) {
            int lastBraceIndex = javaContent.lastIndexOf('}');
            if (lastBraceIndex != -1) {
                javaContent = javaContent.substring(0, lastBraceIndex) + javaContent.substring(lastBraceIndex + 1);
            }
        }
        
        System.out.println("SimpleCSharpToJavaTransformer: Converted content preview:\n" + 
                          javaContent.substring(0, Math.min(200, javaContent.length())) + "...");
        
        return javaContent;
    }
    
    private static int countOccurrences(String str, char ch) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ch) {
                count++;
            }
        }
        return count;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static CompilationUnit parseJavaContent(String javaContent, String fileName) {
        Map options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
        
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setSource(javaContent.toCharArray());
        parser.setCompilerOptions(options);
        parser.setResolveBindings(false);
        parser.setBindingsRecovery(true);
        parser.setUnitName(fileName.replace(".cs", ".java"));
        
        ASTNode ast = parser.createAST(null);
        
        if (ast instanceof CompilationUnit) {
            CompilationUnit cu = (CompilationUnit) ast;
            System.out.println("SimpleCSharpToJavaTransformer: Successfully parsed Java AST with " + 
                             cu.types().size() + " types");
            return cu;
        } else {
            System.err.println("SimpleCSharpToJavaTransformer: Failed to parse as CompilationUnit");
            return null;
        }
    }
}