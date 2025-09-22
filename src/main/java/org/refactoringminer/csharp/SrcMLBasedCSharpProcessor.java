package org.refactoringminer.csharp;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.*;

/**
 * SrcMLBasedCSharpProcessor - Direct srcML integration without GumTree
 * 
 * This class bypasses the GumTree dependency issue by calling srcML directly
 * and parsing the resulting XML AST to create Java CompilationUnit objects.
 * 
 * Flow: C# Source → srcML XML → Parse XML → Java CompilationUnit
 */
public class SrcMLBasedCSharpProcessor {
    
    private static final String SRCML_COMMAND = "/opt/homebrew/bin/srcml";
    
    /**
     * Transform C# content to Java CompilationUnit using direct srcML approach
     * 
     * @param csharpContent The C# source code
     * @param filePath Original file path for debugging
     * @return CompilationUnit representing Java AST equivalent
     */
    public static CompilationUnit transformCSharpToJavaAST(String csharpContent, String filePath) {
        try {
            System.out.println("SrcMLBasedCSharpProcessor: Processing " + filePath + " with direct srcML");
            
            // Step 1: Call srcML to get XML AST
            String srcmlXml = callSrcML(csharpContent);
            if (srcmlXml == null || srcmlXml.trim().isEmpty()) {
                System.err.println("SrcMLBasedCSharpProcessor: srcML returned empty result for " + filePath);
                return null;
            }
            
            // Step 2: Parse the XML AST
            Document xmlDoc = parseXML(srcmlXml);
            if (xmlDoc == null) {
                System.err.println("SrcMLBasedCSharpProcessor: Failed to parse srcML XML for " + filePath);
                return null;
            }
            
            // Step 3: Convert XML AST to Java syntax
            String javaCode = convertSrcMLXMLToJava(xmlDoc, filePath);
            if (javaCode == null || javaCode.trim().isEmpty()) {
                System.err.println("SrcMLBasedCSharpProcessor: Failed to convert XML to Java for " + filePath);
                return null;
            }
            
            // Step 4: Parse Java syntax to CompilationUnit
            CompilationUnit compilationUnit = parseJavaCode(javaCode, filePath);
            
            if (compilationUnit != null) {
                System.out.println("SrcMLBasedCSharpProcessor: Successfully created CompilationUnit for " + filePath + 
                                 " with " + compilationUnit.types().size() + " types");
                return compilationUnit;
            } else {
                System.err.println("SrcMLBasedCSharpProcessor: Failed to create CompilationUnit for " + filePath);
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("SrcMLBasedCSharpProcessor: Error processing " + filePath + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Call srcML command line tool to convert C# to XML AST
     */
    private static String callSrcML(String csharpContent) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(SRCML_COMMAND, "-l", "C#");
        Process process = pb.start();
        
        // Send C# content to stdin
        try (PrintWriter writer = new PrintWriter(process.getOutputStream())) {
            writer.print(csharpContent);
        }
        
        // Read XML output from stdout
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        // Check for errors
        StringBuilder errorOutput = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            System.err.println("SrcMLBasedCSharpProcessor: srcML error (exit code " + exitCode + "): " + errorOutput.toString());
            return null;
        }
        
        return output.toString();
    }
    
    /**
     * Parse srcML XML output into DOM document
     */
    private static Document parseXML(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new ByteArrayInputStream(xml.getBytes()));
        } catch (Exception e) {
            System.err.println("SrcMLBasedCSharpProcessor: XML parsing error: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Convert srcML XML AST to Java source code
     */
    private static String convertSrcMLXMLToJava(Document doc, String filePath) {
        try {
            StringBuilder javaCode = new StringBuilder();
            Element root = doc.getDocumentElement();
            
            // Process the XML structure to generate Java code
            processXMLNode(root, javaCode, 0);
            
            String result = javaCode.toString();
            System.out.println("SrcMLBasedCSharpProcessor: Generated Java code for " + filePath + 
                             " (" + result.length() + " chars)");
            
            return result;
            
        } catch (Exception e) {
            System.err.println("SrcMLBasedCSharpProcessor: Error converting XML to Java: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Process XML nodes recursively to build Java code
     */
    private static void processXMLNode(Node node, StringBuilder javaCode, int depth) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            String tagName = element.getTagName();
            
            switch (tagName) {
                case "using":
                    // Convert C# using to Java import
                    String usingName = getTextContent(element);
                    if (usingName.contains("System")) {
                        javaCode.append("import java.lang.*;\n");
                    } else {
                        javaCode.append("import ").append(usingName.replace("using", "").trim()).append(".*;\n");
                    }
                    break;
                    
                case "namespace":
                    // Convert C# namespace to Java package
                    String namespaceName = getChildElementText(element, "name");
                    if (namespaceName != null) {
                        javaCode.append("package ").append(namespaceName).append(";\n\n");
                    }
                    // Process children (classes inside namespace)
                    processChildren(element, javaCode, depth);
                    break;
                    
                case "class":
                    // Process class declaration
                    String className = getChildElementText(element, "name");
                    if (className != null) {
                        javaCode.append(getIndent(depth)).append("public class ").append(className).append(" {\n");
                        processChildren(element, javaCode, depth + 1);
                        javaCode.append(getIndent(depth)).append("}\n");
                    }
                    break;
                    
                case "function":
                    // Process method declaration
                    processMethod(element, javaCode, depth);
                    break;
                    
                case "expr_stmt":
                    // Process expression statements
                    processExpressionStatement(element, javaCode, depth);
                    break;
                    
                default:
                    // For other elements, process children
                    processChildren(element, javaCode, depth);
                    break;
            }
        } else if (node.getNodeType() == Node.TEXT_NODE) {
            // Skip text nodes at this level - we handle them specifically
        }
    }
    
    /**
     * Process method declarations
     */
    private static void processMethod(Element methodElement, StringBuilder javaCode, int depth) {
        String methodName = getChildElementText(methodElement, "name");
        String returnType = getMethodReturnType(methodElement);
        String parameters = getMethodParameters(methodElement);
        
        if (methodName != null) {
            javaCode.append(getIndent(depth));
            
            // Add modifiers
            if (hasSpecifier(methodElement, "static")) {
                javaCode.append("public static ");
            } else {
                javaCode.append("public ");
            }
            
            javaCode.append(returnType != null ? returnType : "void")
                   .append(" ")
                   .append(methodName)
                   .append("(")
                   .append(parameters != null ? parameters : "")
                   .append(") {\n");
            
            // Process method body
            Element block = getChildElement(methodElement, "block");
            if (block != null) {
                processChildren(block, javaCode, depth + 1);
            }
            
            javaCode.append(getIndent(depth)).append("}\n");
        }
    }
    
    /**
     * Process expression statements (like Console.WriteLine -> System.out.println)
     */
    private static void processExpressionStatement(Element exprElement, StringBuilder javaCode, int depth) {
        String exprText = getTextContent(exprElement);
        
        // Convert C# specific calls to Java equivalents
        exprText = exprText.replaceAll("Console\\.WriteLine", "System.out.println");
        exprText = exprText.replaceAll("\\bstring\\b", "String");
        
        javaCode.append(getIndent(depth)).append(exprText.trim());
        if (!exprText.trim().endsWith(";")) {
            javaCode.append(";");
        }
        javaCode.append("\n");
    }
    
    // Helper methods
    private static void processChildren(Element element, StringBuilder javaCode, int depth) {
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            processXMLNode(children.item(i), javaCode, depth);
        }
    }
    
    private static String getChildElementText(Element parent, String childTagName) {
        NodeList children = parent.getElementsByTagName(childTagName);
        if (children.getLength() > 0) {
            return children.item(0).getTextContent().trim();
        }
        return null;
    }
    
    private static Element getChildElement(Element parent, String childTagName) {
        NodeList children = parent.getElementsByTagName(childTagName);
        if (children.getLength() > 0 && children.item(0) instanceof Element) {
            return (Element) children.item(0);
        }
        return null;
    }
    
    private static String getTextContent(Element element) {
        return element.getTextContent().replaceAll("\\s+", " ").trim();
    }
    
    private static String getMethodReturnType(Element methodElement) {
        Element typeElement = getChildElement(methodElement, "type");
        if (typeElement != null) {
            String type = getChildElementText(typeElement, "name");
            if (type != null) {
                return type.equals("void") ? "void" : type.replace("string", "String");
            }
        }
        return "void";
    }
    
    private static String getMethodParameters(Element methodElement) {
        Element paramListElement = getChildElement(methodElement, "parameter_list");
        if (paramListElement != null) {
            String params = getTextContent(paramListElement);
            // Convert C# parameter syntax to Java
            params = params.replaceAll("string\\[\\]", "String[]");
            params = params.replaceAll("\\bstring\\b", "String");
            return params.replaceAll("[()]", "");
        }
        return "";
    }
    
    private static boolean hasSpecifier(Element methodElement, String specifier) {
        Element typeElement = getChildElement(methodElement, "type");
        if (typeElement != null) {
            String typeText = getTextContent(typeElement);
            return typeText.contains(specifier);
        }
        return false;
    }
    
    private static String getIndent(int depth) {
        return "    ".repeat(depth);
    }
    
    /**
     * Parse Java source code to CompilationUnit
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static CompilationUnit parseJavaCode(String javaCode, String fileName) {
        try {
            Map options = JavaCore.getOptions();
            options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
            options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
            options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
            
            ASTParser parser = ASTParser.newParser(AST.JLS8);
            parser.setSource(javaCode.toCharArray());
            parser.setCompilerOptions(options);
            parser.setResolveBindings(false);
            parser.setBindingsRecovery(true);
            parser.setUnitName(fileName.replace(".cs", ".java"));
            
            ASTNode ast = parser.createAST(null);
            
            if (ast instanceof CompilationUnit) {
                return (CompilationUnit) ast;
            }
        } catch (Exception e) {
            System.err.println("SrcMLBasedCSharpProcessor: Java parsing error: " + e.getMessage());
        }
        return null;
    }
}