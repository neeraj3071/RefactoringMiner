package org.refactoringminer.csharp;

import transformation.Transformation;
import transformation.TransformationUtils;
import transformation.SrcMLTreeVisitor;
import transformation.UnitNode;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.TreeContext;
import com.github.gumtreediff.gen.srcml.SrcmlCsTreeGenerator;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class CPatMinerCSharpConverter {
    /**
     * Converts C# code to Java AST-compatible code using CPatMinerV2 logic.
     * @param csharpCode The C# source code as a String
     * @return Java-like code as a String, or null if conversion fails
     */
    public static String convertToJava(String csharpCode) {
        try {
            System.out.println("=== DEBUG: Input C# code ===");
            System.out.println(csharpCode);

            // Write C# code to a temp file
            java.io.File tempFile = java.io.File.createTempFile("csharp_input", ".cs");
            java.nio.file.Files.write(tempFile.toPath(), csharpCode.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // Run srcML and capture XML output
            ProcessBuilder pb = new ProcessBuilder("srcml", "--position", "--tabs=4", "--language=C#", tempFile.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process proc = pb.start();
            String xml = new String(proc.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            proc.waitFor();
            tempFile.delete();

            System.out.println("=== DEBUG: srcML XML output ===");
            System.out.println("Length: " + xml.length());
            System.out.println("First 200 chars:\n" + xml.substring(0, Math.min(200, xml.length())));
            System.out.println("Last 200 chars:\n" + xml.substring(Math.max(0, xml.length()-200)));
            // Optionally, write XML to a temp file for manual inspection
            java.nio.file.Path xmlDebugPath = java.nio.file.Files.createTempFile("debug_xml_output", ".xml");
            java.nio.file.Files.writeString(xmlDebugPath, xml);
            System.out.println("XML written to: " + xmlDebugPath);

            // Pass XML file path to CPatMiner bridge (instead of string)
            System.out.println("=== DEBUG: GumTreeDiff srcml version ===");
            try {
                System.out.println("GumTreeDiff srcml generator: " + com.github.gumtreediff.gen.srcml.SrcmlCsTreeGenerator.class.getPackage().getImplementationVersion());
            } catch (Throwable t) {
                System.out.println("Could not get GumTreeDiff srcml version: " + t);
            }

            CompilationUnit javaAst = null;
            try {
                javaAst = Transformation.transform_csharp_to_java(xmlDebugPath.toString());
            } catch (Throwable t) {
                System.err.println("[ERROR] Bridge threw: " + t.getMessage());
                t.printStackTrace(System.err);
            }
            if (javaAst == null) {
                System.err.println("[WARN] CPatMinerV2 conversion returned null for input C# code.");
                return null;
            }
            System.out.println("=== DEBUG: Java AST output ===");
            System.out.println(javaAst.toString());
            return javaAst.toString();
        } catch (Exception e) {
            System.err.println("[ERROR] CPatMinerV2 conversion failed: " + e.getMessage());
            e.printStackTrace(System.err);
            return null;
        }
    }

    /**
     * Converts a C# source file to Java code using CPatMinerV2 logic.
     * @param xmlFilePath The file path of the C# source file
     * @return Java-like code as a String, or null if conversion fails
     */
    public static String convertToJavaFromFile(String xmlFilePath) {
        try {
            SrcmlCsTreeGenerator l = new SrcmlCsTreeGenerator();
            TreeContext tc = l.generateFrom().file(xmlFilePath);
            Tree tree_csharp = tc.getRoot();
            Tree transformedTree = TransformationUtils.transformTree(tree_csharp);
            SrcMLTreeVisitor visitor = new SrcMLTreeVisitor();
            if (transformedTree instanceof UnitNode) {
                CompilationUnit m = visitor.visit((UnitNode) transformedTree);
                return m.toString();
            }
        } catch (Exception e) {
            System.err.println("[WARN] CPatMinerV2 conversion failed: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
