package org.refactoringminer.csharp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import static org.junit.jupiter.api.Assertions.*;

public class CPatMinerCSharpConverterTest {

    @Test
    void convertsSimpleCSharpClassToJavaLikeCode() {
        boolean cpatMinerPresent;
        try {
            Class.forName("transformation.Transformation");
            cpatMinerPresent = true;
        } catch (Throwable t) {
            cpatMinerPresent = false;
        }
        Assumptions.assumeTrue(cpatMinerPresent, "CPatMiner Transformation class not on classpath; skipping conversion test");

        String csharp = "namespace Demo { public class Greeter { public string SayHello(string name) { return \"Hi \" + name; } } }";

        String javaLike;
        try {
            javaLike = CPatMinerCSharpConverter.convertToJava(csharp);
        } catch (NoClassDefFoundError err) {
            Assumptions.assumeTrue(false, "CPatMiner dependencies not fully available: " + err.getMessage());
            return;
        }

        System.out.println("=== C# to Java conversion output ===");
        System.out.println(javaLike);

        assertNotNull(javaLike, "Converter should return non-null output");
        assertFalse(javaLike.trim().isEmpty(), "Converter output should not be empty");
    }

    @Test
    void convertsTestClassFileToJavaLikeCode() {
        boolean cpatMinerPresent;
        try {
            Class.forName("transformation.Transformation");
            cpatMinerPresent = true;
        } catch (Throwable t) {
            cpatMinerPresent = false;
        }
        Assumptions.assumeTrue(cpatMinerPresent, "CPatMiner Transformation class not on classpath; skipping conversion test");

        String csharp = "";
        try {
            java.nio.file.Path path = java.nio.file.Paths.get("src/test/java/org/refactoringminer/csharp/TestClass.cs");
            csharp = java.nio.file.Files.readString(path);
        } catch (java.io.IOException e) {
            fail("Failed to read TestClass.cs: " + e.getMessage());
        }

        String javaLike;
        try {
            javaLike = CPatMinerCSharpConverter.convertToJava(csharp);
        } catch (NoClassDefFoundError err) {
            Assumptions.assumeTrue(false, "CPatMiner dependencies not fully available: " + err.getMessage());
            return;
        }

        System.out.println("=== TestClass.cs C# to Java conversion output ===");
        System.out.println(javaLike);

        assertNotNull(javaLike, "Converter should return non-null output");
        assertFalse(javaLike.trim().isEmpty(), "Converter output should not be empty");
    }
}
