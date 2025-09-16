package org.refactoringminer.csharp;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CPatMinerCSharpConverterBasicTest {
    @Test
    void convertsTestClassFileToJavaLikeCode() {
        String csharp = "";
        try {
            java.nio.file.Path path = java.nio.file.Paths.get("src/test/java/org/refactoringminer/csharp/TestClass.cs");
            csharp = java.nio.file.Files.readString(path);
        } catch (java.io.IOException e) {
            fail("Failed to read TestClass.cs: " + e.getMessage());
        }

        String javaLike = CPatMinerCSharpConverter.convertToJava(csharp);
        System.out.println("=== TestClass.cs C# to Java conversion output ===");
        System.out.println(javaLike);
        assertNotNull(javaLike, "Converter should return non-null output");
        assertFalse(javaLike.trim().isEmpty(), "Converter output should not be empty");
    }

    @Test
    void convertsSimpleCSharpSnippet() {
        String csharp = "public class Greeter { public string Hello(string name){ return \"Hi \" + name; } }";
        String javaLike = CPatMinerCSharpConverter.convertToJava(csharp);
        assertNotNull(javaLike, "Conversion should not return null");
        assertTrue(javaLike.contains("class") || javaLike.contains("Hello"),
                "Converted code should include class name or method");
    }
}
