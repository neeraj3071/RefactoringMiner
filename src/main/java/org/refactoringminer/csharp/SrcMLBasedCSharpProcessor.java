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
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * SrcMLBasedCSharpProcessor - Enhanced Direct srcML integration without GumTree
 * 
 * This class bypasses the GumTree dependency issue by calling srcML directly
 * and parsing the resulting XML AST to create Java CompilationUnit objects.
 * Flow: C# Source → srcML XML → Parse XML → Enhanced Java Conversion → CompilationUnit
 */
public class SrcMLBasedCSharpProcessor {
    
    private static final String SRCML_COMMAND = "/opt/homebrew/bin/srcml";
    
    // Type mapping for C# to Java conversions
    private static final Map<String, String> TYPE_MAPPINGS = new HashMap<String, String>() {{
        put("string", "String");
        put("object", "Object");
        put("bool", "boolean");
        put("byte", "byte");
        put("short", "short");
        put("int", "int");
        put("long", "long");
        put("float", "float");
        put("double", "double");
        put("char", "char");
        put("void", "void");
        put("decimal", "BigDecimal");
        put("DateTime", "LocalDateTime");
        put("TimeSpan", "Duration");
        put("Guid", "UUID");
        put("List", "List");
        put("Dictionary", "HashMap");
        put("HashSet", "HashSet");
        put("Queue", "Queue");
        put("Stack", "Stack");
        put("IEnumerable", "Iterable");
        put("IList", "List");
        put("IDictionary", "Map");
    }};
    
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
     * ENHANCED: Now handles properties, interfaces, structs, generics, lambdas, LINQ, async/await, etc.
     */
    private static void processXMLNode(Node node, StringBuilder javaCode, int depth) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            String tagName = element.getTagName();
            
            switch (tagName) {
                case "using":
                    // Convert C# using to Java import with proper mappings
                    processUsingDirective(element, javaCode);
                    break;
                    
                case "namespace":
                    // Convert C# namespace to Java package
                    processNamespace(element, javaCode, depth);
                    break;
                    
                case "class":
                    // Process class declaration (including records)
                    processClass(element, javaCode, depth);
                    break;
                    
                case "struct":
                    // Convert C# struct to Java class
                    processStruct(element, javaCode, depth);
                    break;
                    
                case "interface":
                    // Process interface declaration
                    processInterface(element, javaCode, depth);
                    break;
                    
                case "enum":
                    // Process enum declaration
                    processEnum(element, javaCode, depth);
                    break;
                    
                case "property":
                    // Convert C# property to getter/setter methods
                    processProperty(element, javaCode, depth);
                    break;
                    
                case "function":
                    // Process method declaration (including async)
                    processMethod(element, javaCode, depth);
                    break;
                    
                case "function_decl":
                    // Process method declaration without body
                    processMethodDeclaration(element, javaCode, depth);
                    break;
                    
                case "constructor":
                    // Process constructor
                    processConstructor(element, javaCode, depth);
                    break;
                    
                case "event":
                    // Convert C# event to listener pattern
                    processEvent(element, javaCode, depth);
                    break;
                    
                case "delegate":
                    // Convert C# delegate to functional interface
                    processDelegate(element, javaCode, depth);
                    break;
                    
                case "lambda":
                    // Convert C# lambda to Java lambda
                    processLambda(element, javaCode, depth);
                    break;
                    
                case "expr_stmt":
                    // Process expression statements (including LINQ, string interpolation)
                    processExpressionStatement(element, javaCode, depth);
                    break;
                    
                case "decl_stmt":
                    // Process variable declarations (including nullable types)
                    processDeclarationStatement(element, javaCode, depth);
                    break;
                    
                case "if":
                    // Process if statements (including pattern matching)
                    processIfStatement(element, javaCode, depth);
                    break;
                    
                case "for":
                case "foreach":
                    // Process loops
                    processLoop(element, javaCode, depth);
                    break;
                    
                case "attribute":
                    // Convert C# attributes to Java annotations
                    processAttribute(element, javaCode, depth);
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
     * Process method declarations with async/await and extension method support
     */
    private static void processMethod(Element methodElement, StringBuilder javaCode, int depth) {
        String methodName = getChildElementText(methodElement, "name");
        String returnType = getMethodReturnType(methodElement);
        String parameters = getMethodParameters(methodElement);
        boolean isAsync = isAsyncMethod(methodElement);
        boolean isExtension = isExtensionMethod(methodElement);
        
        if (methodName != null) {
            javaCode.append(getIndent(depth));
            
            // Add modifiers
            String modifiers = getMethodModifiers(methodElement, isExtension);
            javaCode.append(modifiers);
            
            // Handle async methods - convert to CompletableFuture
            if (isAsync) {
                returnType = "CompletableFuture<" + (returnType != null && !returnType.equals("void") ? convertType(returnType) : "Void") + ">";
            } else if (returnType != null) {
                returnType = convertType(returnType);
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
                if (isAsync) {
                    javaCode.append(getIndent(depth + 1)).append("return CompletableFuture.supplyAsync(() -> {\n");
                    processChildren(block, javaCode, depth + 2);
                    javaCode.append(getIndent(depth + 1)).append("});\n");
                } else {
                    processChildren(block, javaCode, depth + 1);
                }
            }
            
            javaCode.append(getIndent(depth)).append("}\n");
        }
    }
    
    /**
     * Process expression statements with LINQ and string interpolation support
     */
    private static void processExpressionStatement(Element exprElement, StringBuilder javaCode, int depth) {
        String exprText = getTextContent(exprElement);
        
        // Convert string interpolation $"..." to String.format
        exprText = convertStringInterpolation(exprText);
        
        // Convert LINQ to Java Streams
        exprText = convertLinqToStreams(exprText);
        
        // Convert C# specific calls to Java equivalents
        exprText = exprText.replaceAll("Console\\.WriteLine", "System.out.println");
        exprText = exprText.replaceAll("Console\\.Write\\(", "System.out.print(");
        exprText = exprText.replaceAll("\\bstring\\b", "String");
        exprText = exprText.replaceAll("\\bvar\\b", ""); // Remove var, rely on type inference
        
        // Convert await to CompletableFuture
        exprText = exprText.replaceAll("await\\s+", "").replaceAll("\\.Result", ".get()");
        
        // Convert common C# types
        exprText = convertType(exprText);
        
        javaCode.append(getIndent(depth)).append(exprText.trim());
        if (!exprText.trim().endsWith(";")) {
            javaCode.append(";");
        }
        javaCode.append("\n");
    }
    
    // ========== NEW ENHANCED METHODS ==========
    
    /**
     * Process using directives with proper Java import mapping
     */
    private static void processUsingDirective(Element element, StringBuilder javaCode) {
        String usingName = getTextContent(element);
        if (usingName.contains("System.Collections.Generic")) {
            javaCode.append("import java.util.*;\n");
        } else if (usingName.contains("System.Linq")) {
            javaCode.append("import java.util.stream.*;\n");
        } else if (usingName.contains("System.Threading.Tasks")) {
            javaCode.append("import java.util.concurrent.*;\n");
        } else if (usingName.contains("System.IO")) {
            javaCode.append("import java.io.*;\n");
        } else if (usingName.contains("System.Text")) {
            javaCode.append("import java.lang.*;\n");
        } else if (usingName.contains("System")) {
            javaCode.append("import java.lang.*;\n");
        } else {
            javaCode.append("import ").append(usingName.replace("using", "").trim()).append(".*;\n");
        }
    }
    
    /**
     * Process namespace declarations
     */
    private static void processNamespace(Element element, StringBuilder javaCode, int depth) {
        String namespaceName = getChildElementText(element, "name");
        if (namespaceName != null) {
            javaCode.append("package ").append(namespaceName).append(";\n\n");
        }
        processChildren(element, javaCode, depth);
    }
    
    /**
     * Process class declarations with enhanced features
     */
    private static void processClass(Element element, StringBuilder javaCode, int depth) {
        String className = getChildElementText(element, "name");
        if (className != null) {
            processAttributes(element, javaCode, depth);
            
            javaCode.append(getIndent(depth));
            String specifier = getSpecifier(element);
            if (specifier != null && !specifier.isEmpty()) {
                javaCode.append(specifier).append(" ");
            } else {
                javaCode.append("public ");
            }
            
            javaCode.append("class ").append(className);
            
            String generics = getGenerics(element);
            if (generics != null && !generics.isEmpty()) {
                javaCode.append(generics);
            }
            
            String baseClass = getBaseClass(element);
            if (baseClass != null && !baseClass.isEmpty()) {
                javaCode.append(" extends ").append(convertType(baseClass));
            }
            
            String interfaces = getImplementedInterfaces(element);
            if (interfaces != null && !interfaces.isEmpty()) {
                javaCode.append(" implements ").append(convertType(interfaces));
            }
            
            javaCode.append(" {\n");
            processChildren(element, javaCode, depth + 1);
            javaCode.append(getIndent(depth)).append("}\n");
        }
    }
    
    /**
     * Process struct declarations - convert to Java classes
     */
    private static void processStruct(Element element, StringBuilder javaCode, int depth) {
        String structName = getChildElementText(element, "name");
        if (structName != null) {
            processAttributes(element, javaCode, depth);
            javaCode.append(getIndent(depth)).append("public final class ").append(structName).append(" {\n");
            processChildren(element, javaCode, depth + 1);
            javaCode.append(getIndent(depth)).append("}\n");
        }
    }
    
    /**
     * Process interface declarations
     */
    private static void processInterface(Element element, StringBuilder javaCode, int depth) {
        String interfaceName = getChildElementText(element, "name");
        if (interfaceName != null) {
            processAttributes(element, javaCode, depth);
            javaCode.append(getIndent(depth)).append("public interface ").append(interfaceName);
            
            String generics = getGenerics(element);
            if (generics != null && !generics.isEmpty()) {
                javaCode.append(generics);
            }
            
            javaCode.append(" {\n");
            processChildren(element, javaCode, depth + 1);
            javaCode.append(getIndent(depth)).append("}\n");
        }
    }
    
    /**
     * Process enum declarations
     */
    private static void processEnum(Element element, StringBuilder javaCode, int depth) {
        String enumName = getChildElementText(element, "name");
        if (enumName != null) {
            javaCode.append(getIndent(depth)).append("public enum ").append(enumName).append(" {\n");
            processChildren(element, javaCode, depth + 1);
            javaCode.append(getIndent(depth)).append("}\n");
        }
    }
    
    /**
     * Process C# properties - convert to getter/setter methods
     */
    private static void processProperty(Element element, StringBuilder javaCode, int depth) {
        String propertyName = getChildElementText(element, "name");
        String propertyType = getPropertyType(element);
        
        if (propertyName != null && propertyType != null) {
            String fieldName = "_" + Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
            propertyType = convertType(propertyType);
            
            javaCode.append(getIndent(depth)).append("private ").append(propertyType)
                   .append(" ").append(fieldName).append(";\n");
            
            if (hasGetter(element)) {
                javaCode.append(getIndent(depth)).append("public ").append(propertyType)
                       .append(" get").append(propertyName).append("() {\n");
                javaCode.append(getIndent(depth + 1)).append("return ").append(fieldName).append(";\n");
                javaCode.append(getIndent(depth)).append("}\n");
            }
            
            if (hasSetter(element)) {
                javaCode.append(getIndent(depth)).append("public void set").append(propertyName)
                       .append("(").append(propertyType).append(" value) {\n");
                javaCode.append(getIndent(depth + 1)).append("this.").append(fieldName)
                       .append(" = value;\n");
                javaCode.append(getIndent(depth)).append("}\n");
            }
        }
    }
    
    /**
     * Process constructor declarations
     */
    private static void processConstructor(Element element, StringBuilder javaCode, int depth) {
        String className = getParentClassName(element);
        String parameters = getMethodParameters(element);
        
        if (className != null) {
            javaCode.append(getIndent(depth)).append("public ").append(className)
                   .append("(").append(parameters != null ? parameters : "").append(") {\n");
            
            Element block = getChildElement(element, "block");
            if (block != null) {
                processChildren(block, javaCode, depth + 1);
            }
            
            javaCode.append(getIndent(depth)).append("}\n");
        }
    }
    
    /**
     * Process method declaration (interface methods without body)
     */
    private static void processMethodDeclaration(Element element, StringBuilder javaCode, int depth) {
        String methodName = getChildElementText(element, "name");
        String returnType = getMethodReturnType(element);
        String parameters = getMethodParameters(element);
        
        if (methodName != null) {
            javaCode.append(getIndent(depth))
                   .append(returnType != null ? convertType(returnType) : "void")
                   .append(" ").append(methodName)
                   .append("(").append(parameters != null ? parameters : "")
                   .append(");\n");
        }
    }
    
    /**
     * Process C# events - convert to listener pattern
     */
    private static void processEvent(Element element, StringBuilder javaCode, int depth) {
        String eventName = getChildElementText(element, "name");
        String eventType = getEventType(element);
        
        if (eventName != null) {
            String listenerField = eventName + "Listeners";
            javaCode.append(getIndent(depth)).append("private List<")
                   .append(eventType != null ? eventType : "EventListener")
                   .append("> ").append(listenerField)
                   .append(" = new ArrayList<>();\n");
            
            javaCode.append(getIndent(depth)).append("public void add")
                   .append(eventName).append("Listener(")
                   .append(eventType != null ? eventType : "EventListener")
                   .append(" listener) {\n");
            javaCode.append(getIndent(depth + 1)).append(listenerField).append(".add(listener);\n");
            javaCode.append(getIndent(depth)).append("}\n");
            
            javaCode.append(getIndent(depth)).append("public void remove")
                   .append(eventName).append("Listener(")
                   .append(eventType != null ? eventType : "EventListener")
                   .append(" listener) {\n");
            javaCode.append(getIndent(depth + 1)).append(listenerField).append(".remove(listener);\n");
            javaCode.append(getIndent(depth)).append("}\n");
        }
    }
    
    /**
     * Process C# delegates - convert to functional interfaces
     */
    private static void processDelegate(Element element, StringBuilder javaCode, int depth) {
        String delegateName = getChildElementText(element, "name");
        String returnType = getMethodReturnType(element);
        String parameters = getMethodParameters(element);
        
        if (delegateName != null) {
            javaCode.append(getIndent(depth)).append("@FunctionalInterface\n");
            javaCode.append(getIndent(depth)).append("public interface ").append(delegateName).append(" {\n");
            javaCode.append(getIndent(depth + 1))
                   .append(returnType != null ? convertType(returnType) : "void")
                   .append(" invoke(")
                   .append(parameters != null ? parameters : "")
                   .append(");\n");
            javaCode.append(getIndent(depth)).append("}\n");
        }
    }
    
    /**
     * Process C# lambda expressions - convert to Java lambdas
     */
    private static void processLambda(Element element, StringBuilder javaCode, int depth) {
        String parameters = getLambdaParameters(element);
        String body = getLambdaBody(element);
        
        javaCode.append("(").append(parameters != null ? parameters : "")
               .append(") -> ");
        
        if (body != null && body.contains("\n")) {
            javaCode.append("{\n")
                   .append(getIndent(depth + 1)).append(body)
                   .append(getIndent(depth)).append("}");
        } else {
            javaCode.append(body != null ? body : "");
        }
    }
    
    /**
     * Process C# attributes - convert to Java annotations
     */
    private static void processAttribute(Element element, StringBuilder javaCode, int depth) {
        String attributeName = getChildElementText(element, "name");
        if (attributeName != null) {
            String javaAnnotation = mapAttributeToAnnotation(attributeName);
            javaCode.append(getIndent(depth)).append("@").append(javaAnnotation).append("\n");
        }
    }
    
    /**
     * Process multiple attributes for a declaration
     */
    private static void processAttributes(Element element, StringBuilder javaCode, int depth) {
        NodeList attributes = element.getElementsByTagName("attribute");
        for (int i = 0; i < attributes.getLength(); i++) {
            if (attributes.item(i) instanceof Element) {
                processAttribute((Element) attributes.item(i), javaCode, depth);
            }
        }
    }
    
    /**
     * Process declaration statements with nullable type support
     */
    private static void processDeclarationStatement(Element element, StringBuilder javaCode, int depth) {
        Element typeElement = getChildElement(element, "type");
        Element nameElement = getChildElement(element, "name");
        
        if (typeElement != null && nameElement != null) {
            String type = getTextContent(typeElement);
            String name = getTextContent(nameElement);
            String init = getInitializer(element);
            
            type = convertNullableType(type);
            type = convertType(type);
            
            javaCode.append(getIndent(depth)).append(type).append(" ").append(name);
            if (init != null && !init.isEmpty()) {
                javaCode.append(" = ").append(init);
            }
            javaCode.append(";\n");
        }
    }
    
    /**
     * Process if statements with pattern matching support
     */
    private static void processIfStatement(Element element, StringBuilder javaCode, int depth) {
        Element condition = getChildElement(element, "condition");
        Element thenBlock = getChildElement(element, "then");
        Element elseBlock = getChildElement(element, "else");
        
        javaCode.append(getIndent(depth)).append("if (");
        
        if (condition != null) {
            String conditionText = getTextContent(condition);
            conditionText = convertPatternMatching(conditionText);
            javaCode.append(conditionText);
        }
        
        javaCode.append(") {\n");
        
        if (thenBlock != null) {
            processChildren(thenBlock, javaCode, depth + 1);
        }
        
        javaCode.append(getIndent(depth)).append("}");
        
        if (elseBlock != null) {
            javaCode.append(" else {\n");
            processChildren(elseBlock, javaCode, depth + 1);
            javaCode.append(getIndent(depth)).append("}");
        }
        
        javaCode.append("\n");
    }
    
    /**
     * Process loop statements (for, foreach)
     */
    private static void processLoop(Element element, StringBuilder javaCode, int depth) {
        String tagName = element.getTagName();
        
        if ("foreach".equals(tagName)) {
            String itemVar = getLoopVariable(element);
            String collection = getLoopCollection(element);
            
            javaCode.append(getIndent(depth)).append("for (");
            javaCode.append(itemVar).append(" : ").append(collection);
            javaCode.append(") {\n");
            
            Element block = getChildElement(element, "block");
            if (block != null) {
                processChildren(block, javaCode, depth + 1);
            }
            
            javaCode.append(getIndent(depth)).append("}\n");
        } else {
            Element init = getChildElement(element, "init");
            Element condition = getChildElement(element, "condition");
            Element incr = getChildElement(element, "incr");
            
            javaCode.append(getIndent(depth)).append("for (");
            if (init != null) javaCode.append(getTextContent(init));
            javaCode.append("; ");
            if (condition != null) javaCode.append(getTextContent(condition));
            javaCode.append("; ");
            if (incr != null) javaCode.append(getTextContent(incr));
            javaCode.append(") {\n");
            
            Element block = getChildElement(element, "block");
            if (block != null) {
                processChildren(block, javaCode, depth + 1);
            }
            
            javaCode.append(getIndent(depth)).append("}\n");
        }
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
            params = params.replaceAll("\\bthis\\s+", ""); // Remove 'this' from extension method parameters
            params = convertType(params);
            return params.replaceAll("[()]", "");
        }
        return "";
    }
    
    private static String getIndent(int depth) {
        return "    ".repeat(depth);
    }
    
    // ========== ENHANCED HELPER METHODS ==========
    
    /**
     * Convert C# type to Java type with comprehensive mappings
     */
    private static String convertType(String csharpType) {
        if (csharpType == null) return null;
        
        // Use TYPE_MAPPINGS for basic type conversions
        for (Map.Entry<String, String> entry : TYPE_MAPPINGS.entrySet()) {
            csharpType = csharpType.replaceAll("\\b" + entry.getKey() + "\\b", entry.getValue());
        }
        
        // Handle generic types: Dictionary<K,V> -> HashMap<K,V>
        csharpType = csharpType.replace("Dictionary<", "HashMap<");
        csharpType = csharpType.replace("IList<", "List<");
        csharpType = csharpType.replace("IDictionary<", "Map<");
        csharpType = csharpType.replace("IEnumerable<", "Iterable<");
        
        return csharpType;
    }
    
    /**
     * Convert C# nullable types (int?) to Java boxed types (Integer)
     */
    private static String convertNullableType(String type) {
        if (type == null) return null;
        
        if (type.endsWith("?")) {
            String baseType = type.substring(0, type.length() - 1).trim();
            // Convert primitive types to their boxed equivalents
            switch (baseType) {
                case "int": return "Integer";
                case "long": return "Long";
                case "short": return "Short";
                case "byte": return "Byte";
                case "float": return "Float";
                case "double": return "Double";
                case "bool": return "Boolean";
                case "char": return "Character";
                default: return baseType; // Already reference type
            }
        }
        return type;
    }
    
    /**
     * Convert C# string interpolation to Java String.format
     * Example: $"Hello {name}" -> String.format("Hello %s", name)
     */
    private static String convertStringInterpolation(String code) {
        if (!code.contains("$\"")) return code;
        
        // Pattern to match $"text {var} more {var2}" format
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\$\"([^\"]+)\"");
        java.util.regex.Matcher matcher = pattern.matcher(code);
        
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String interpolatedString = matcher.group(1);
            List<String> variables = new ArrayList<>();
            
            // Extract variables between {}
            java.util.regex.Pattern varPattern = java.util.regex.Pattern.compile("\\{([^}]+)\\}");
            java.util.regex.Matcher varMatcher = varPattern.matcher(interpolatedString);
            
            String formatString = interpolatedString;
            while (varMatcher.find()) {
                variables.add(varMatcher.group(1));
                formatString = formatString.replace("{" + varMatcher.group(1) + "}", "%s");
            }
            
            // Build String.format call
            StringBuilder replacement = new StringBuilder("String.format(\"" + formatString + "\"");
            for (String var : variables) {
                replacement.append(", ").append(var);
            }
            replacement.append(")");
            
            matcher.appendReplacement(result, replacement.toString());
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Convert LINQ queries to Java Streams API
     * Example: items.Where(x => x > 5).Select(x => x * 2) -> items.stream().filter(x -> x > 5).map(x -> x * 2)
     */
    private static String convertLinqToStreams(String code) {
        // Convert LINQ method chaining to Stream API
        code = code.replaceAll("\\.Where\\(", ".stream().filter(");
        code = code.replaceAll("\\.Select\\(", ".map(");
        code = code.replaceAll("\\.First\\(\\)", ".findFirst().orElse(null)");
        code = code.replaceAll("\\.FirstOrDefault\\(\\)", ".findFirst().orElse(null)");
        code = code.replaceAll("\\.Any\\(", ".anyMatch(");
        code = code.replaceAll("\\.All\\(", ".allMatch(");
        code = code.replaceAll("\\.Count\\(\\)", ".count()");
        code = code.replaceAll("\\.Sum\\(\\)", ".sum()");
        code = code.replaceAll("\\.ToList\\(\\)", ".collect(Collectors.toList())");
        code = code.replaceAll("\\.ToArray\\(\\)", ".toArray()");
        code = code.replaceAll("\\.OrderBy\\(", ".sorted(Comparator.comparing(");
        code = code.replaceAll("\\.OrderByDescending\\(", ".sorted(Comparator.comparing(").replace("))", ").reversed())");
        
        return code;
    }
    
    /**
     * Convert C# pattern matching to Java instanceof checks
     * Example: if (obj is string s) -> if (obj instanceof String) { String s = (String)obj; ...}
     */
    private static String convertPatternMatching(String condition) {
        // Convert "x is Type y" to "x instanceof Type"
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\w+)\\s+is\\s+(\\w+)\\s+(\\w+)");
        java.util.regex.Matcher matcher = pattern.matcher(condition);
        
        if (matcher.find()) {
            String variable = matcher.group(1);
            String type = matcher.group(2);
            // In Java, we'd need to add the cast in the block, but for condition we just use instanceof
            return variable + " instanceof " + convertType(type);
        }
        
        // Simple "x is Type" without variable declaration
        condition = condition.replaceAll("(\\w+)\\s+is\\s+(\\w+)", "$1 instanceof $2");
        
        return condition;
    }
    
    /**
     * Map C# attributes to Java annotations
     */
    private static String mapAttributeToAnnotation(String csharpAttribute) {
        // Remove "Attribute" suffix if present
        csharpAttribute = csharpAttribute.replace("Attribute", "");
        
        Map<String, String> attributeMap = new HashMap<>();
        attributeMap.put("Serializable", "Serializable");
        attributeMap.put("Obsolete", "Deprecated");
        attributeMap.put("Test", "Test");
        attributeMap.put("Override", "Override");
        attributeMap.put("SerializeField", "Transient"); // Unity specific
        attributeMap.put("NonSerialized", "Transient");
        
        return attributeMap.getOrDefault(csharpAttribute, csharpAttribute);
    }
    
    /**
     * Get generics declaration from class/interface
     */
    private static String getGenerics(Element element) {
        Element genericsElement = getChildElement(element, "parameter_list");
        if (genericsElement != null) {
            String generics = getTextContent(genericsElement);
            if (!generics.isEmpty()) {
                return "<" + generics + ">";
            }
        }
        return null;
    }
    
    /**
     * Get base class from class declaration
     */
    private static String getBaseClass(Element element) {
        Element superElement = getChildElement(element, "super");
        if (superElement != null) {
            NodeList extendsList = superElement.getElementsByTagName("extends");
            if (extendsList.getLength() > 0) {
                Element extendsElement = (Element) extendsList.item(0);
                return getChildElementText(extendsElement, "name");
            }
        }
        return null;
    }
    
    /**
     * Get implemented interfaces from class declaration
     */
    private static String getImplementedInterfaces(Element element) {
        Element superElement = getChildElement(element, "super");
        if (superElement != null) {
            NodeList implementsList = superElement.getElementsByTagName("implements");
            if (implementsList.getLength() > 0) {
                Element implementsElement = (Element) implementsList.item(0);
                return getTextContent(implementsElement);
            }
        }
        return null;
    }
    
    /**
     * Get specifier (public, private, protected, static, etc.)
     */
    private static String getSpecifier(Element element) {
        Element specifierElement = getChildElement(element, "specifier");
        if (specifierElement != null) {
            return getTextContent(specifierElement);
        }
        return null;
    }
    
    /**
     * Get method modifiers including handling for extension methods
     */
    private static String getMethodModifiers(Element methodElement, boolean isExtension) {
        StringBuilder modifiers = new StringBuilder();
        
        String specifier = getSpecifier(methodElement);
        if (specifier != null && !specifier.isEmpty()) {
            modifiers.append(specifier);
        } else {
            modifiers.append("public");
        }
        
        // Extension methods become static in Java
        if (isExtension && !modifiers.toString().contains("static")) {
            modifiers.append(" static");
        }
        
        modifiers.append(" ");
        return modifiers.toString();
    }
    
    /**
     * Check if method is async
     */
    private static boolean isAsyncMethod(Element methodElement) {
        String specifier = getSpecifier(methodElement);
        return specifier != null && specifier.contains("async");
    }
    
    /**
     * Check if method is extension method (this modifier on first parameter)
     */
    private static boolean isExtensionMethod(Element methodElement) {
        Element paramList = getChildElement(methodElement, "parameter_list");
        if (paramList != null) {
            NodeList params = paramList.getElementsByTagName("parameter");
            if (params.getLength() > 0) {
                Element firstParam = (Element) params.item(0);
                String paramText = getTextContent(firstParam);
                return paramText.startsWith("this ");
            }
        }
        return false;
    }
    
    /**
     * Get property type
     */
    private static String getPropertyType(Element propertyElement) {
        Element typeElement = getChildElement(propertyElement, "type");
        if (typeElement != null) {
            Element nameElement = getChildElement(typeElement, "name");
            if (nameElement != null) {
                return getTextContent(nameElement);
            }
        }
        return null;
    }
    
    /**
     * Check if property has getter
     */
    private static boolean hasGetter(Element propertyElement) {
        NodeList accessors = propertyElement.getElementsByTagName("get");
        return accessors.getLength() > 0;
    }
    
    /**
     * Check if property has setter
     */
    private static boolean hasSetter(Element propertyElement) {
        NodeList accessors = propertyElement.getElementsByTagName("set");
        return accessors.getLength() > 0;
    }
    
    /**
     * Get event type from event declaration
     */
    private static String getEventType(Element eventElement) {
        Element typeElement = getChildElement(eventElement, "type");
        if (typeElement != null) {
            return getTextContent(typeElement);
        }
        return null;
    }
    
    /**
     * Get lambda parameters
     */
    private static String getLambdaParameters(Element lambdaElement) {
        Element paramList = getChildElement(lambdaElement, "parameter_list");
        if (paramList != null) {
            return getTextContent(paramList).replaceAll("[()]", "");
        }
        return "";
    }
    
    /**
     * Get lambda body
     */
    private static String getLambdaBody(Element lambdaElement) {
        Element block = getChildElement(lambdaElement, "block");
        if (block != null) {
            return getTextContent(block);
        }
        return "";
    }
    
    /**
     * Get loop variable for foreach
     */
    private static String getLoopVariable(Element loopElement) {
        Element declElement = getChildElement(loopElement, "decl");
        if (declElement != null) {
            String decl = getTextContent(declElement);
            return decl.trim();
        }
        return "item";
    }
    
    /**
     * Get loop collection for foreach
     */
    private static String getLoopCollection(Element loopElement) {
        Element exprElement = getChildElement(loopElement, "expr");
        if (exprElement != null) {
            return getTextContent(exprElement);
        }
        return "";
    }
    
    /**
     * Get initializer from declaration
     */
    private static String getInitializer(Element declElement) {
        Element initElement = getChildElement(declElement, "init");
        if (initElement != null) {
            return getTextContent(initElement).replaceAll("^=\\s*", "");
        }
        return null;
    }
    
    /**
     * Get parent class name (for constructors)
     */
    private static String getParentClassName(Element element) {
        Node parent = element.getParentNode();
        while (parent != null) {
            if (parent instanceof Element) {
                Element parentElement = (Element) parent;
                if ("class".equals(parentElement.getTagName())) {
                    return getChildElementText(parentElement, "name");
                }
            }
            parent = parent.getParentNode();
        }
        return null;
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