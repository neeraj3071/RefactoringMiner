package org.refactoringminer.csharp;

/**
 * Simple test class to verify CPatMiner integration
 */
public class CPatMinerTest {
    
    public static void main(String[] args) {
        System.out.println("Testing CPatMiner integration...");
        
        try {
            // Test simple C# code
            String testCSharp = "using System;\n" +
                               "namespace TestApp\n" +
                               "{\n" +
                               "    class Program\n" +
                               "    {\n" +
                               "        static void Main(string[] args)\n" +
                               "        {\n" +
                               "            Console.WriteLine(\"Hello World!\");\n" +
                               "        }\n" +
                               "    }\n" +
                               "}";
            
            System.out.println("Testing CPatMiner executor...");
            
            // Test CPatMiner execution
            org.eclipse.jdt.core.dom.CompilationUnit ast = CPatMinerExecutor.transformCSharpToJavaAST(testCSharp, "test.cs");
            
            if (ast != null) {
                System.out.println("SUCCESS: CPatMiner AST transformation completed!");
                System.out.println("Generated AST types: " + ast.types().size());
                String javaCode = CPatMinerExecutor.astToString(ast);
                System.out.println("Generated Java code length: " + javaCode.length());
                System.out.println("First 200 chars: " + javaCode.substring(0, Math.min(200, javaCode.length())));
            } else {
                System.err.println("ERROR: CPatMiner returned null AST");
            }
            
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Test completed.");
    }
}