package org.refactoringminer.csharp;

import java.nio.file.Files;
import java.nio.file.Paths;

public class CSharpToJavaPrintDemo {
    public static void main(String[] args) {
        try {
            String xmlFilePath = args.length > 0 ? args[0] : "TestClass.xml";
            // Debug: Print file contents before parsing
            System.out.println("=== Debug: Printing TestClass.xml contents ===");
            String xmlContents = new String(Files.readAllBytes(Paths.get(xmlFilePath)));
            System.out.println(xmlContents);
            System.out.println("=== End Debug ===");
            String javaLike = CPatMinerCSharpConverter.convertToJavaFromFile(xmlFilePath);
            System.out.println("=== Converted Output ===");
            System.out.println("=== TestClass.xml srcML to Java conversion output ===");
            System.out.println(javaLike == null ? "<null>" : javaLike.trim().isEmpty() ? "<empty>" : javaLike);
        } catch (Throwable t) {
            System.out.println("=== Exception during conversion ===");
            t.printStackTrace(System.out);
        }
    }
}
