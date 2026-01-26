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
import java.nio.file.Files;
import java.util.*;

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
            
            // DEBUG: Print generated Java code
            System.out.println("SrcMLBasedCSharpProcessor: Generated Java code:");
            System.out.println("=== START JAVA CODE ===");
            System.out.println(javaCode);
            System.out.println("=== END JAVA CODE ===");
            
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
            StringBuilder imports = new StringBuilder();
            StringBuilder packageDecl = new StringBuilder();
            StringBuilder classes = new StringBuilder();
            
            Element root = doc.getDocumentElement();
            
            // First pass: collect package and imports
            NodeList children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child instanceof Element) {
                    Element el = (Element) child;
                    if ("using".equals(el.getTagName()) && !hasChildElement(el, "block")) {
                        processUsingDirective(el, imports);
                    } else if ("namespace".equals(el.getTagName())) {
                        String namespaceName = getChildElementText(el, "name");
                        if (namespaceName != null) {
                            packageDecl.append("package ").append(namespaceName).append(";\\n\\n");
                        }
                    }
                }
            }
            
            // Second pass: process everything else
            processXMLNode(root, classes, 0);
            
            // Combine in correct order: package, imports, classes
            StringBuilder javaCode = new StringBuilder();
            if (packageDecl.length() > 0) {
                javaCode.append(packageDecl.toString().replace("\\n", "\n"));
            }
            if (imports.length() > 0) {
                javaCode.append(imports.toString().replace("\\n", "\n")).append("\n");
            }
            // Remove duplicate package and import declarations from classes
            String classContent = classes.toString();
            classContent = classContent.replaceAll("package [^;]+;\\s*", "");
            classContent = classContent.replaceAll("import [^;]+;\\s*", "");
            classContent = classContent.replace("\\n", "\n");
            javaCode.append(classContent);
            
            String result = javaCode.toString();
            // DEBUG: System.out.println("SrcMLBasedCSharpProcessor: Generated Java code for " + filePath + 
            //                       " (" + result.length() + " chars)");
            
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
                    // Check if it's using directive or using statement
                    if (hasChildElement(element, "block")) {
                        // using statement (IDisposable) -> try-with-resources
                        processUsingStatement(element, javaCode, depth);
                    } else {
                        // using directive -> import
                        processUsingDirective(element, javaCode);
                    }
                    break;
                    
                case "using_stmt":
                    // using statement (IDisposable) -> try-with-resources
                    processUsingStatement(element, javaCode, depth);
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
                    
                case "while":
                case "do":
                    // Process while/do-while loops
                    processWhileLoop(element, javaCode, depth);
                    break;
                    
                case "try":
                    // Process try-catch-finally
                    processTryCatch(element, javaCode, depth);
                    break;
                    
                case "throw":
                    // Process throw statement
                    processThrow(element, javaCode, depth);
                    break;
                    
                case "switch":
                    // Process switch statement
                    processSwitch(element, javaCode, depth);
                    break;
                    
                case "break":
                    // Process break statement
                    javaCode.append(getIndent(depth)).append("break;\n");
                    break;
                    
                case "continue":
                    // Process continue statement
                    javaCode.append(getIndent(depth)).append("continue;\n");
                    break;
                    
                case "attribute":
                    // Convert C# attributes to Java annotations
                    processAttribute(element, javaCode, depth);
                    break;
                    
                case "return":
                    // Process return statement
                    processReturnStatement(element, javaCode, depth);
                    break;
                    
                case "block_content":
                    // Transparent wrapper - just process children
                    processChildren(element, javaCode, depth);
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
     * Process method declaration without body (abstract methods)
     */
    private static void processMethodDeclaration(Element methodElement, StringBuilder javaCode, int depth) {
        String methodName = getDirectChildElementText(methodElement, "name");
        String returnType = getMethodReturnType(methodElement);
        String parameters = getMethodParameters(methodElement);
        
        if (methodName != null) {
            javaCode.append(getIndent(depth));
            
            // Add modifiers
            String modifiers = getMethodModifiers(methodElement, false);
            javaCode.append(modifiers);
            
            // Return type
            javaCode.append(returnType != null ? convertType(returnType) : "void")
                   .append(" ")
                   .append(methodName)
                   .append("(")
                   .append(parameters != null ? parameters : "")
                   .append(");\n");
        }
    }
    
    /**
     * Process method declarations with async/await and extension method support
     */
    private static void processMethod(Element methodElement, StringBuilder javaCode, int depth) {
        String methodName = getDirectChildElementText(methodElement, "name");
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
     * FIXED: Now detects and processes C# events
     */
    private static void processExpressionStatement(Element exprElement, StringBuilder javaCode, int depth) {
        // FIRST: Check if this is a lambda property (public Type PropertyName => expression;)
        Element exprChild = getChildElement(exprElement, "expr");
        if (exprChild != null) {
            Element lambdaElement = getChildElement(exprChild, "lambda");
            if (lambdaElement != null) {
                // This is a lambda property: public Type PropertyName => expression;
                Element paramList = getChildElement(lambdaElement, "parameter_list");
                Element block = getChildElement(lambdaElement, "block");
                
                if (paramList != null && block != null) {
                    // Extract modifiers and type from preceding siblings
                    String modifiers = extractModifiersFromExpr(exprChild);
                    String returnType = extractTypeFromExpr(exprChild);
                    String propertyName = getTextContent(paramList).trim();
                    String expression = getTextContent(block).trim();
                    
                    if (propertyName != null && !propertyName.isEmpty()) {
                        // Generate getter method: public Type getPropertyName() { return expression; }
                        String getterName = "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
                        javaCode.append(getIndent(depth)).append(modifiers).append(" ")
                               .append(convertType(returnType)).append(" ")
                               .append(getterName).append("() {\n");
                        javaCode.append(getIndent(depth + 1)).append("return ").append(expression).append(";\n");
                        javaCode.append(getIndent(depth)).append("}\n");
                        return; // Lambda property processed, done
                    }
                }
            }
        }
        
        String exprText = getTextContent(exprElement);
        
        // Check if this is a C# event declaration (public event Action EventName)
        if (exprText.contains("event")) {
            if (exprChild != null) {
                String eventDecl = getTextContent(exprChild);
                if (eventDecl.contains("event")) {
                    // Extract event type and name
                    String[] parts = eventDecl.trim().split("\\s+");
                    String eventType = null;
                    String eventName = null;
                    
                    for (int i = 0; i < parts.length; i++) {
                        if (parts[i].equals("event") && i + 2 < parts.length) {
                            eventType = parts[i + 1];
                            eventName = parts[i + 2].replace(";", "");
                            break;
                        }
                    }
                    
                    if (eventName != null) {
                        // Generate listener pattern for the event
                        generateEventListenerPattern(eventName, eventType, javaCode, depth);
                        return; // Event processed, don't continue with normal expression
                    }
                }
            }
        }
        
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
        
        // Convert C# expressions (null operators, APIs, etc.)
        exprText = convertExpression(exprText);
        
        // Convert common C# types
        exprText = convertType(exprText);
        
        javaCode.append(getIndent(depth)).append(exprText.trim());
        if (!exprText.trim().endsWith(";")) {
            javaCode.append(";");
        }
        javaCode.append("\n");
    }
    
    
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
     * FIXED: Now handles lambda-style accessors (get => expr, set => expr)
     */
    private static void processProperty(Element element, StringBuilder javaCode, int depth) {
        String propertyName = getDirectChildElementText(element, "name");
        String propertyType = getPropertyType(element);
        
        if (propertyName != null && propertyType != null) {
            // Convert property name to field name (lowercase first char)
            String fieldName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
            propertyType = convertType(propertyType);
            
            // Check if there's already a backing field by looking for get/set lambda expressions
            boolean hasBackingField = hasExistingBackingField(element);
            String backingFieldName = hasBackingField ? getBackingFieldName(element) : fieldName;
            
            // Only create field if no backing field exists
            if (!hasBackingField) {
                javaCode.append(getIndent(depth)).append("private ").append(propertyType)
                       .append(" ").append(fieldName).append(";\n");
            }
            
            // Generate getter if property has get accessor
            if (hasGetter(element)) {
                javaCode.append(getIndent(depth)).append("public ").append(propertyType)
                       .append(" get").append(propertyName).append("() {\n");
                
                // Get the getter body (from lambda or block)
                String getterBody = getGetterBody(element);
                if (getterBody != null && !getterBody.trim().isEmpty()) {
                    javaCode.append(getIndent(depth + 1)).append("return ").append(getterBody).append(";\n");
                } else {
                    javaCode.append(getIndent(depth + 1)).append("return ").append(backingFieldName).append(";\n");
                }
                
                javaCode.append(getIndent(depth)).append("}\n");
            }
            
            // Generate setter if property has set accessor
            if (hasSetter(element)) {
                javaCode.append(getIndent(depth)).append("public void set").append(propertyName)
                       .append("(").append(propertyType).append(" value) {\n");
                
                // Get the setter body (from lambda or block)
                String setterBody = getSetterBody(element);
                if (setterBody != null && !setterBody.trim().isEmpty()) {
                    javaCode.append(getIndent(depth + 1)).append(setterBody).append(";\n");
                } else {
                    javaCode.append(getIndent(depth + 1)).append("this.").append(backingFieldName)
                           .append(" = value;\n");
                }
                
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
     * FIXED: Handle nested <decl> elements and extract type/name correctly using direct children
     */
    private static void processDeclarationStatement(Element element, StringBuilder javaCode, int depth) {
        // Try to get decl child first (field declarations have this structure)
        Element declElement = getChildElement(element, "decl");
        if (declElement != null) {
            element = declElement;
        }
        
        // Use getDirectChildElement to get only direct children, not nested descendants
        Element typeElement = getDirectChildElement(element, "type");
        Element nameElement = getDirectChildElement(element, "name");
        
        if (typeElement != null && nameElement != null) {
            // Get type name (not including specifiers like "private")
            String type = getChildElementText(typeElement, "name");
            if (type == null) {
                type = getTextContent(typeElement);
            }
            
            // Check for nullable modifier (int?, bool?, etc.)
            Element modifier = getChildElement(typeElement, "modifier");
            boolean isNullable = false;
            if (modifier != null) {
                String modText = getTextContent(modifier);
                if ("?".equals(modText)) {
                    isNullable = true;
                }
            }
            
            // Get variable name from direct child
            String name = getTextContent(nameElement);
            String init = getInitializer(element);
            
            // Get visibility from specifier
            String visibility = getChildElementText(typeElement, "specifier");
            if (visibility != null) {
                visibility = visibility.trim() + " ";
            } else {
                visibility = "";
            }
            
            if (isNullable) {
                type = convertNullableType(type + "?");
            } else {
                type = convertNullableType(type);
                type = convertType(type);
            }
            
            javaCode.append(getIndent(depth)).append(visibility).append(type).append(" ").append(name);
            if (init != null && !init.isEmpty()) {
                javaCode.append(" = ").append(init);
            }
            javaCode.append(";\n");
        }
    }
    
    /**
     * Process return statement
     */
    private static void processReturnStatement(Element element, StringBuilder javaCode, int depth) {
        javaCode.append(getIndent(depth)).append("return");
        
        Element expr = getChildElement(element, "expr");
        if (expr != null) {
            String exprText = getTextContent(expr);
            javaCode.append(" ").append(convertExpression(exprText));
        }
        
        javaCode.append(";\n");
    }
    
    /**
     * Process while and do-while loops
     */
    private static void processWhileLoop(Element element, StringBuilder javaCode, int depth) {
        String tagName = element.getTagName();
        
        if ("do".equals(tagName)) {
            javaCode.append(getIndent(depth)).append("do {\n");
            Element block = getChildElement(element, "block");
            if (block != null) {
                processChildren(block, javaCode, depth + 1);
            }
            javaCode.append(getIndent(depth)).append("} while (");
            Element condition = getChildElement(element, "condition");
            if (condition != null) {
                javaCode.append(convertExpression(getTextContent(condition)));
            }
            javaCode.append(");\n");
        } else {
            Element condition = getChildElement(element, "condition");
            javaCode.append(getIndent(depth)).append("while (");
            if (condition != null) {
                javaCode.append(convertExpression(getTextContent(condition)));
            }
            javaCode.append(") {\n");
            Element block = getChildElement(element, "block");
            if (block != null) {
                processChildren(block, javaCode, depth + 1);
            }
            javaCode.append(getIndent(depth)).append("}\n");
        }
    }
    
    /**
     * Process try-catch-finally blocks
     */
    private static void processTryCatch(Element element, StringBuilder javaCode, int depth) {
        javaCode.append(getIndent(depth)).append("try {\n");
        
        Element block = getChildElement(element, "block");
        if (block != null) {
            processChildren(block, javaCode, depth + 1);
        }
        
        javaCode.append(getIndent(depth)).append("}");
        
        // Process catch blocks
        NodeList catches = element.getElementsByTagName("catch");
        for (int i = 0; i < catches.getLength(); i++) {
            Element catchBlock = (Element) catches.item(i);
            javaCode.append(" catch (");
            
            Element param = getChildElement(catchBlock, "param");
            if (param != null) {
                Element decl = getChildElement(param, "decl");
                if (decl != null) {
                    String type = getChildElementText(decl, "type");
                    String name = getDirectChildElementText(decl, "name");
                    javaCode.append(convertType(type)).append(" ").append(name);
                }
            } else {
                javaCode.append("Exception e");
            }
            
            javaCode.append(") {\n");
            Element catchBlockContent = getChildElement(catchBlock, "block");
            if (catchBlockContent != null) {
                processChildren(catchBlockContent, javaCode, depth + 1);
            }
            javaCode.append(getIndent(depth)).append("}");
        }
        
        // Process finally block
        NodeList finallys = element.getElementsByTagName("finally");
        if (finallys.getLength() > 0) {
            javaCode.append(" finally {\n");
            Element finallyBlock = (Element) finallys.item(0);
            Element finallyBlockContent = getChildElement(finallyBlock, "block");
            if (finallyBlockContent != null) {
                processChildren(finallyBlockContent, javaCode, depth + 1);
            }
            javaCode.append(getIndent(depth)).append("}");
        }
        
        javaCode.append("\n");
    }
    
    /**
     * Process throw statement
     */
    private static void processThrow(Element element, StringBuilder javaCode, int depth) {
        javaCode.append(getIndent(depth)).append("throw ");
        
        Element expr = getChildElement(element, "expr");
        if (expr != null) {
            String exprText = getTextContent(expr).trim();
            if (!exprText.isEmpty()) {
                // Handle 'throw new Exception(msg)'
                exprText = convertExpression(exprText);
                // Convert C# exception types to Java
                exprText = exprText.replace("ArgumentException", "IllegalArgumentException");
                exprText = exprText.replace("InvalidOperationException", "UnsupportedOperationException");
                javaCode.append(exprText);
            } else {
                // C# empty throw (rethrow) - use 'e' as default exception variable
                javaCode.append("e");
            }
        } else {
            // No expression means rethrow - use 'e' as default exception variable
            javaCode.append("e");
        }
        
        javaCode.append(";\n");
    }
    
    /**
     * Process switch statement
     */
    private static void processSwitch(Element element, StringBuilder javaCode, int depth) {
        Element condition = getChildElement(element, "condition");
        javaCode.append(getIndent(depth)).append("switch (");
        if (condition != null) {
            javaCode.append(convertExpression(getTextContent(condition)));
        }
        javaCode.append(") {\n");
        
        Element block = getChildElement(element, "block");
        if (block != null) {
            Element blockContent = getChildElement(block, "block_content");
            if (blockContent != null) {
                // Process children sequentially - case labels come before their statements
                NodeList children = blockContent.getChildNodes();
                boolean inCase = false;
                
                for (int i = 0; i < children.getLength(); i++) {
                    Node child = children.item(i);
                    if (child instanceof Element) {
                        Element childEl = (Element) child;
                        String tagName = childEl.getTagName();
                        
                        if ("case".equals(tagName)) {
                            Element expr = getChildElement(childEl, "expr");
                            javaCode.append(getIndent(depth + 1)).append("case ");
                            if (expr != null) {
                                javaCode.append(convertExpression(getTextContent(expr)));
                            }
                            javaCode.append(":\n");
                            inCase = true;
                        } else if ("default".equals(tagName)) {
                            javaCode.append(getIndent(depth + 1)).append("default:\n");
                            inCase = true;
                        } else if (inCase) {
                            // Process statements under the case
                            processXMLNode(child, javaCode, depth + 2);
                        }
                    }
                }
            }
        }
        
        javaCode.append(getIndent(depth)).append("}\n");
    }
    
    /**
     * Process using statement (IDisposable -> try-with-resources)
     */
    private static void processUsingStatement(Element element, StringBuilder javaCode, int depth) {
        javaCode.append(getIndent(depth)).append("try (");
        
        // Look for declaration in condition or init
        Element condition = getChildElement(element, "condition");
        Element init = getChildElement(element, "init");
        Element target = condition != null ? condition : init;
        
        if (target != null) {
            Element declStmt = getChildElement(target, "decl_stmt");
            if (declStmt != null) {
                Element decl = getChildElement(declStmt, "decl");
                if (decl != null) {
                    String type = getChildElementText(decl, "type");
                    String name = getDirectChildElementText(decl, "name");
                    Element initExpr = getChildElement(decl, "init");
                    javaCode.append(convertType(type)).append(" ").append(name);
                    if (initExpr != null) {
                        Element expr = getChildElement(initExpr, "expr");
                        if (expr != null) {
                            javaCode.append(" = ").append(convertExpression(getTextContent(expr)));
                        }
                    }
                }
            } else {
                // Direct decl without decl_stmt wrapper
                Element decl = getChildElement(target, "decl");
                if (decl != null) {
                    String type = getChildElementText(decl, "type");
                    String name = getDirectChildElementText(decl, "name");
                    Element initExpr = getChildElement(decl, "init");
                    javaCode.append(convertType(type)).append(" ").append(name);
                    if (initExpr != null) {
                        Element expr = getChildElement(initExpr, "expr");
                        if (expr != null) {
                            javaCode.append(" = ").append(convertExpression(getTextContent(expr)));
                        }
                    }
                }
            }
        }
        
        javaCode.append(") {\n");
        
        Element block = getChildElement(element, "block");
        if (block != null) {
            processChildren(block, javaCode, depth + 1);
        }
        
        javaCode.append(getIndent(depth)).append("}\n");
    }
    
    /**
     * Convert C# expressions to Java (handles ??, ??=, ?., ?[], and C# API calls)
     */
    private static String convertExpression(String expr) {
        if (expr == null) return null;
        
        // Null-coalescing assignment: a ?? = b -> a = (a != null ? a : b) 
        // Handle the space that srcML puts between ?? and = and remove trailing semicolon
        expr = expr.replaceAll("(\\w+)\\s*\\?\\?\\s*=\\s*([^;]+);?", "$1 = ($1 != null ? $1 : $2)");
        
        // Convert C# API method calls to Java equivalents
        // Handle DateTime.Now.Ticks first (before general .Now and .Ticks conversion)
        expr = expr.replaceAll("DateTime\\.Now\\.Ticks\\b", "System.currentTimeMillis()");
        expr = expr.replaceAll("LocalDateTime\\.Now\\.Ticks\\b", "System.currentTimeMillis()");
        
        expr = expr.replaceAll("\\.Length\\b", ".length()");
        expr = expr.replaceAll("\\.ToUpper\\(\\)", ".toUpperCase()");
        expr = expr.replaceAll("\\.ToLower\\(\\)", ".toLowerCase()");
        expr = expr.replaceAll("\\.ToString\\(\\)", ".toString()");
        expr = expr.replaceAll("\\.Message\\b", ".getMessage()");
        expr = expr.replaceAll("LocalDateTime\\.Now\\b", "LocalDateTime.now()");
        expr = expr.replaceAll("\\.Ticks\\b", "");
        
        // Null-conditional element access: a?[i] -> (a != null && a.length() > i ? a.charAt(i) : null)
        expr = expr.replaceAll("(\\w+)\\?\\[(\\d+)\\]", "($1 != null && $1.length() > $2 ? $1.charAt($2) : null)");
        
        // Null-conditional operator: a?.b -> (a != null ? a.b : null)
        expr = expr.replaceAll("(\\w+)\\?\\.([\\w()]+)", "($1 != null ? $1.$2 : null)");
        
        // Null-coalescing operator: a ?? b ?? c -> nested ternary operators
        // Strategy: Process from left to right, repeatedly replacing the FIRST ?? operator
        // This handles chained ?? correctly: a ?? b ?? c becomes (a != null ? a : b) ?? c then ((a != null ? a : b) != null ? (a != null ? a : b) : c)
        expr = convertNullCoalescing(expr);
        
        // C# property references to field references
        // Match the full pattern including the dot: value.HasValue -> value != null
        expr = expr.replaceAll("(\\w+)\\.HasValue\\b", "$1 != null");
        expr = expr.replaceAll("\\.Value\\b", "");
        
        // Convert C# types in expressions
        expr = convertType(expr);
        
        return expr;
    }
    
    /**
     * Check if element has a child with given tag name
     */
    private static boolean hasChildElement(Element parent, String tagName) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element && ((Element) child).getTagName().equals(tagName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Convert null-coalescing operators (??) to Java ternary operators.
     * Handles complex nested expressions including method calls, property access, and chained ??.
     * Uses recursive strategy to handle ?? inside method arguments.
     */
    private static String convertNullCoalescing(String expr) {
        if (!expr.contains("??")) {
            return expr;
        }
        
        // First, recursively process any method arguments that contain ??
        expr = processMethodArgumentsRecursively(expr);
        
        // Now handle ?? at the top level
        java.util.List<String> parts = splitByNullCoalescingTopLevel(expr);
        
        if (parts.size() <= 1) {
            return expr; // No top-level ?? found
        }
        
        // Build the ternary expression from left to right
        String result = parts.get(0).trim();
        for (int i = 1; i < parts.size(); i++) {
            String rightOperand = parts.get(i).trim();
            result = "(" + result + " != null ? " + result + " : " + rightOperand + ")";
        }
        
        return result;
    }
    
    /**
     * Recursively process method arguments to convert ?? operators inside them
     */
    private static String processMethodArgumentsRecursively(String expr) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        
        while (i < expr.length()) {
            // Find method call opening parenthesis
            if (expr.charAt(i) == '(' && i > 0 && Character.isLetterOrDigit(expr.charAt(i - 1))) {
                result.append('(');
                i++;
                
                // Extract the argument list
                StringBuilder args = new StringBuilder();
                int depth = 1;
                boolean inString = false;
                
                while (i < expr.length() && depth > 0) {
                    char c = expr.charAt(i);
                    
                    if (c == '"' && (i == 0 || expr.charAt(i - 1) != '\\')) {
                        inString = !inString;
                    }
                    
                    if (!inString) {
                        if (c == '(') depth++;
                        else if (c == ')') depth--;
                    }
                    
                    if (depth > 0) {
                        args.append(c);
                    }
                    i++;
                }
                
                // Recursively convert ?? in the arguments
                String convertedArgs = args.toString();
                if (convertedArgs.contains("??")) {
                    // Split by commas at depth 0 to handle multiple arguments
                    java.util.List<String> argList = splitArguments(convertedArgs);
                    StringBuilder convertedArgList = new StringBuilder();
                    for (int j = 0; j < argList.size(); j++) {
                        if (j > 0) convertedArgList.append(", ");
                        String arg = argList.get(j);
                        if (arg.contains("??")) {
                            // Recursively convert this argument
                            convertedArgList.append(convertNullCoalescing(arg));
                        } else {
                            convertedArgList.append(arg);
                        }
                    }
                    convertedArgs = convertedArgList.toString();
                }
                
                result.append(convertedArgs);
                result.append(')');
            } else {
                result.append(expr.charAt(i));
                i++;
            }
        }
        
        return result.toString();
    }
    
    /**
     * Split arguments by comma at depth 0 (not inside nested parentheses)
     */
    private static java.util.List<String> splitArguments(String args) {
        java.util.List<String> result = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;
        boolean inString = false;
        
        for (int i = 0; i < args.length(); i++) {
            char c = args.charAt(i);
            
            if (c == '"' && (i == 0 || args.charAt(i - 1) != '\\')) {
                inString = !inString;
            }
            
            if (!inString) {
                if (c == '(') depth++;
                else if (c == ')') depth--;
                else if (c == ',' && depth == 0) {
                    result.add(current.toString());
                    current = new StringBuilder();
                    continue;
                }
            }
            
            current.append(c);
        }
        
        if (current.length() > 0) {
            result.add(current.toString());
        }
        
        return result;
    }
    
    /**
     * Split by ?? at top level only (depth 0)
     */
    private static java.util.List<String> splitByNullCoalescingTopLevel(String expr) {
        java.util.List<String> parts = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inString = false;
        int parenDepth = 0;
        
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            
            if (c == '"' && (i == 0 || expr.charAt(i - 1) != '\\')) {
                inString = !inString;
                current.append(c);
                continue;
            }
            
            if (inString) {
                current.append(c);
                continue;
            }
            
            if (c == '(') {
                parenDepth++;
                current.append(c);
                continue;
            } else if (c == ')') {
                parenDepth--;
                current.append(c);
                continue;
            }
            
            // Check for ?? at depth 0 only
            if (parenDepth == 0 && i + 1 < expr.length() && c == '?' && expr.charAt(i + 1) == '?') {
                parts.add(current.toString());
                current = new StringBuilder();
                i++; // Skip the second ?
                continue;
            }
            
            current.append(c);
        }
        
        if (current.length() > 0) {
            parts.add(current.toString());
        }
        
        return parts;
    }
    

    
    /**
     * Find the end of the next single operand (not continuing past the next ?? operator).
     * This is used for the RIGHT operand of ?? to ensure we only capture one term at a time.
     * 
     * For: "first ?? second ?? third"
     * When at position after first "??", this should return end of "second" (not "second ?? third")
     */
    private static int findNextOperandEnd(String expr, int startIndex) {
        int pos = startIndex;
        int parenDepth = 0;
        boolean inString = false;
        boolean foundNonWhitespace = false;
        
        while (pos < expr.length()) {
            char c = expr.charAt(pos);
            
            // Handle string literals
            if (c == '"' && (pos == 0 || expr.charAt(pos - 1) != '\\')) {
                if (!inString) {
                    inString = true;
                    foundNonWhitespace = true;
                } else {
                    // End of string literal
                    return pos + 1;
                }
                pos++;
                continue;
            }
            
            if (inString) {
                pos++;
                continue;
            }
            
            // Handle parentheses
            if (c == '(') {
                parenDepth++;
                foundNonWhitespace = true;
                pos++;
                continue;
            } else if (c == ')') {
                if (parenDepth == 0) {
                    // End of outer expression
                    return pos;
                }
                parenDepth--;
                pos++;
                // If we just closed all parens and found something, this completes the operand
                if (parenDepth == 0 && foundNonWhitespace) {
                    return pos;
                }
                continue;
            }
            
            // At depth 0, look for operators and boundaries
            if (parenDepth == 0) {
                // Whitespace handling
                if (Character.isWhitespace(c)) {
                    if (foundNonWhitespace) {
                        // We've captured an operand, now check if next non-space is ?? or other delimiter
                        int lookahead = pos + 1;
                        while (lookahead < expr.length() && Character.isWhitespace(expr.charAt(lookahead))) {
                            lookahead++;
                        }
                        if (lookahead + 1 < expr.length()) {
                            if (expr.charAt(lookahead) == '?' && expr.charAt(lookahead + 1) == '?') {
                                // Next operator is ?? - stop here
                                return pos;
                            }
                            // Other delimiters
                            char next = expr.charAt(lookahead);
                            if (next == ';' || next == ',' || next == ')') {
                                return pos;
                            }
                        }
                    }
                    pos++;
                    continue;
                }
                
                // Check for ?? operator immediately (without whitespace before)
                if (foundNonWhitespace && pos + 1 < expr.length() && c == '?' && expr.charAt(pos + 1) == '?') {
                    return pos;
                }
                
                // Statement/expression delimiters
                if (c == ';' || c == ',') {
                    return pos;
                }
                
                // Track that we found content
                foundNonWhitespace = true;
            }
            
            pos++;
        }
        
        return expr.length(); // End of string
    }
    
    /**
     * Find the start index of the left operand for a ?? operator.
     * Handles method calls with (), property chains with ., and parenthesized expressions.
     */
    private static int findLeftOperandStart(String expr, int operatorIndex) {
        int pos = operatorIndex - 1;
        
        // Skip trailing whitespace
        while (pos >= 0 && Character.isWhitespace(expr.charAt(pos))) {
            pos--;
        }
        
        // Track parentheses depth for complex expressions
        int parenDepth = 0;
        boolean inString = false;
        
        // Work backwards
        while (pos >= 0) {
            char c = expr.charAt(pos);
            
            // Handle string literals
            if (c == '"' && (pos == 0 || expr.charAt(pos - 1) != '\\')) {
                inString = !inString;
                pos--;
                continue;
            }
            
            if (inString) {
                pos--;
                continue;
            }
            
            // Handle parentheses
            if (c == ')') {
                parenDepth++;
            } else if (c == '(') {
                parenDepth--;
                if (parenDepth < 0) {
                    // We've gone past the start of this expression
                    return pos + 1;
                }
            }
            
            // If we're at depth 0 and hit a boundary character, stop
            if (parenDepth == 0) {
                if (c == ';' || c == ',' || c == '=' || c == '+' || c == '-' || 
                    c == '*' || c == '/' || c == '&' || c == '|' || c == '<' || c == '>') {
                    // Check if it's part of an operator like ==, !=, <=, >=, etc.
                    if (pos > 0 && (expr.charAt(pos - 1) == '=' || expr.charAt(pos - 1) == '!' || 
                                    expr.charAt(pos - 1) == '<' || expr.charAt(pos - 1) == '>')) {
                        pos--;
                        continue;
                    }
                    return pos + 1;
                }
            }
            
            pos--;
        }
        
        return 0; // Start of string
    }
    
    /**
     * Process if statements with pattern matching support
     */
    private static void processIfStatement(Element element, StringBuilder javaCode, int depth) {
        Element condition = getChildElement(element, "condition");
        Element block = getChildElement(element, "block");
        Element elseBlock = null;
        
        // Check for else
        NodeList elseNodes = element.getElementsByTagName("else");
        if (elseNodes.getLength() > 0) {
            elseBlock = (Element) elseNodes.item(0);
        }
        
        javaCode.append(getIndent(depth)).append("if (");
        
        if (condition != null) {
            String conditionText = getTextContent(condition);
            conditionText = convertPatternMatching(conditionText);
            conditionText = convertExpression(conditionText); // Apply all expression conversions
            javaCode.append(conditionText);
        }
        
        javaCode.append(") {\n");
        
        if (block != null) {
            processChildren(block, javaCode, depth + 1);
        }
        
        javaCode.append(getIndent(depth)).append("}");
        
        if (elseBlock != null) {
            javaCode.append(" else {\n");
            Element elseBlockContent = getChildElement(elseBlock, "block");
            if (elseBlockContent != null) {
                processChildren(elseBlockContent, javaCode, depth + 1);
            }
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
            if (init != null) {
                String initText = getTextContent(init).trim();
                // Remove any trailing semicolons from init
                initText = initText.replaceAll(";+$", "");
                javaCode.append(initText);
            }
            javaCode.append("; ");
            if (condition != null) {
                String condText = getTextContent(condition).trim();
                // Remove any trailing semicolons from condition
                condText = condText.replaceAll(";+$", "");
                javaCode.append(condText);
            }
            javaCode.append("; ");
            if (incr != null) {
                String incrText = getTextContent(incr).trim();
                // Remove any trailing semicolons from increment
                incrText = incrText.replaceAll(";+$", "");
                javaCode.append(incrText);
            }
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
    
    /**
     * Get direct child element text (not nested)
     */
    private static String getDirectChildElementText(Element parent, String childTagName) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element && ((Element) child).getTagName().equals(childTagName)) {
                return child.getTextContent().trim();
            }
        }
        return null;
    }
    
    /**
     * Get direct child element (not descendants)
     */
    private static Element getDirectChildElement(Element parent, String childTagName) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element && ((Element) child).getTagName().equals(childTagName)) {
                return (Element) child;
            }
        }
        return null;
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
                if (type.equals("void")) {
                    return "void";
                }
                // Check for nullable modifier: <modifier>?</modifier>
                Element modifierElement = getChildElement(typeElement, "modifier");
                if (modifierElement != null && "?".equals(modifierElement.getTextContent().trim())) {
                    type = type + "?";
                }
                // Convert nullable types (int? -> Integer, char? -> Character)
                type = convertNullableType(type);
                return type.replace("string", "String");
            }
        }
        return "void";
    }
    
    private static String getMethodParameters(Element methodElement) {
        Element paramListElement = getChildElement(methodElement, "parameter_list");
        if (paramListElement != null) {
            StringBuilder params = new StringBuilder();
            NodeList paramElements = paramListElement.getElementsByTagName("parameter");
            
            for (int i = 0; i < paramElements.getLength(); i++) {
                Element paramElement = (Element) paramElements.item(i);
                Element declElement = getChildElement(paramElement, "decl");
                
                if (declElement != null) {
                    Element typeElement = getChildElement(declElement, "type");
                    
                    // Get parameter name - it's a direct child of decl, not nested in type
                    String paramName = null;
                    NodeList declChildren = declElement.getChildNodes();
                    for (int j = 0; j < declChildren.getLength(); j++) {
                        Node child = declChildren.item(j);
                        if (child instanceof Element && ((Element) child).getTagName().equals("name") 
                            && child.getParentNode() == declElement) {
                            paramName = child.getTextContent().trim();
                            break;
                        }
                    }
                    
                    if (typeElement != null && paramName != null) {
                        String type = getChildElementText(typeElement, "name");
                        
                        // Check for nullable modifier
                        Element modifierElement = getChildElement(typeElement, "modifier");
                        if (modifierElement != null && "?".equals(modifierElement.getTextContent().trim())) {
                            type = type + "?";
                        }
                        
                        // Convert nullable and other types
                        type = convertNullableType(type);
                        type = convertType(type);
                        type = type.replace("string", "String");
                        
                        // Handle 'this' keyword for extension methods
                        paramName = paramName.replaceAll("\\bthis\\s+", "");
                        
                        if (i > 0) {
                            params.append(", ");
                        }
                        params.append(type).append(" ").append(paramName);
                    }
                }
            }
            
            return params.toString();
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
        
        // Remove whitespace around ?
        type = type.replaceAll("\\s*\\?", "?");
        
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
     * FIXED: Ignore pseudo parameter lists (from lambda expressions)
     */
    private static String getGenerics(Element element) {
        NodeList paramLists = element.getElementsByTagName("parameter_list");
        for (int i = 0; i < paramLists.getLength(); i++) {
            Element paramList = (Element) paramLists.item(i);
            // Skip pseudo parameter lists (from lambda expressions in properties)
            String type = paramList.getAttribute("type");
            if (type == null || !type.equals("pseudo")) {
                // Check if it's a direct child of the class/interface element
                if (paramList.getParentNode() == element) {
                    String generics = getTextContent(paramList);
                    if (!generics.isEmpty()) {
                        return "<" + generics + ">";
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Get base class from class declaration
     */
    private static String getBaseClass(Element element) {
        // Check for super_list (C# style: class Derived : Base)
        Element superList = getChildElement(element, "super_list");
        if (superList != null) {
            Element superClass = getChildElement(superList, "super");
            if (superClass != null) {
                String className = getChildElementText(superClass, "name");
                // If it doesn't start with I (interface convention), it's likely a base class
                if (className != null && !(className.startsWith("I") && className.length() > 1 && Character.isUpperCase(className.charAt(1)))) {
                    return className;
                }
            }
        }
        
        // Fallback: check for super with extends
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
        // Check for super_list (C# style: class MyClass : IInterface1, IInterface2)
        Element superList = getChildElement(element, "super_list");
        if (superList != null) {
            NodeList supers = superList.getElementsByTagName("super");
            StringBuilder interfaces = new StringBuilder();
            
            for (int i = 0; i < supers.getLength(); i++) {
                Element sup = (Element) supers.item(i);
                String name = getChildElementText(sup, "name");
                if (name != null) {
                    // If it starts with I and next char is uppercase, it's likely an interface
                    if (name.startsWith("I") && name.length() > 1 && Character.isUpperCase(name.charAt(1))) {
                        if (interfaces.length() > 0) interfaces.append(", ");
                        interfaces.append(name);
                    } else if (i > 0) {
                        // After first one, rest are interfaces in C#
                        if (interfaces.length() > 0) interfaces.append(", ");
                        interfaces.append(name);
                    }
                }
            }
            
            if (interfaces.length() > 0) {
                return interfaces.toString();
            }
        }
        
        // Fallback: check for super with implements
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
        StringBuilder specifiers = new StringBuilder();
        NodeList specifierElements = element.getElementsByTagName("specifier");
        
        for (int i = 0; i < specifierElements.getLength(); i++) {
            Element specEl = (Element) specifierElements.item(i);
            // Only get direct children, not nested ones
            if (specEl.getParentNode() == element) {
                String spec = getTextContent(specEl);
                if (spec != null && !spec.isEmpty()) {
                    if (specifiers.length() > 0) specifiers.append(" ");
                    specifiers.append(spec);
                }
            }
        }
        
        if (specifiers.length() > 0) {
            return convertModifiers(specifiers.toString());
        }
        return null;
    }
    
    /**
     * Convert C# modifiers to Java modifiers
     */
    private static String convertModifiers(String modifiers) {
        if (modifiers == null) return null;
        
        // sealed -> final
        modifiers = modifiers.replace("sealed", "final");
        
        // Remove C#-specific modifiers that don't exist in Java
        modifiers = modifiers.replace("virtual", "");
        modifiers = modifiers.replace("override", "");
        modifiers = modifiers.replace("new", "");
        modifiers = modifiers.replace("partial", "");
        modifiers = modifiers.replace("internal", "");
        modifiers = modifiers.replace("unsafe", "");
        modifiers = modifiers.replace("readonly", "final");
        modifiers = modifiers.replace("const", "static final");
        
        // Clean up extra spaces
        modifiers = modifiers.trim().replaceAll("\\s+", " ");
        
        return modifiers;
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
     * Check if property has getter (handles lambda-style, block-style, and auto-property accessors)
     */
    private static boolean hasGetter(Element propertyElement) {
        // Check for <get> tag (old style)
        NodeList accessors = propertyElement.getElementsByTagName("get");
        if (accessors.getLength() > 0) return true;
        
        Element block = getChildElement(propertyElement, "block");
        if (block != null) {
            // Check for lambda-style getter (get => ...)
            NodeList stmts = block.getElementsByTagName("expr_stmt");
            for (int i = 0; i < stmts.getLength(); i++) {
                Element stmt = (Element) stmts.item(i);
                String text = getTextContent(stmt);
                if (text.contains("get") && text.contains("=>")) {
                    return true;
                }
            }
            
            // Check for block-style getter (<function><name>get</name>)
            NodeList functions = block.getElementsByTagName("function");
            for (int i = 0; i < functions.getLength(); i++) {
                Element func = (Element) functions.item(i);
                String funcName = getChildElementText(func, "name");
                if ("get".equals(funcName)) {
                    return true;
                }
            }
            
            // Check for auto-property getter (<function_decl><name>get</name>)
            NodeList funcDecls = block.getElementsByTagName("function_decl");
            for (int i = 0; i < funcDecls.getLength(); i++) {
                Element funcDecl = (Element) funcDecls.item(i);
                String funcName = getChildElementText(funcDecl, "name");
                if ("get".equals(funcName)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Check if property has setter (handles lambda-style, block-style, and auto-property accessors)
     */
    private static boolean hasSetter(Element propertyElement) {
        // Check for <set> tag (old style)
        NodeList accessors = propertyElement.getElementsByTagName("set");
        if (accessors.getLength() > 0) return true;
        
        Element block = getChildElement(propertyElement, "block");
        if (block != null) {
            // Check for lambda-style setter (set => ...)
            NodeList stmts = block.getElementsByTagName("expr_stmt");
            for (int i = 0; i < stmts.getLength(); i++) {
                Element stmt = (Element) stmts.item(i);
                String text = getTextContent(stmt);
                if (text.contains("set") && text.contains("=>")) {
                    return true;
                }
            }
            
            // Check for block-style setter (<function><name>set</name>)
            NodeList functions = block.getElementsByTagName("function");
            for (int i = 0; i < functions.getLength(); i++) {
                Element func = (Element) functions.item(i);
                String funcName = getChildElementText(func, "name");
                if ("set".equals(funcName)) {
                    return true;
                }
            }
            
            // Check for auto-property setter (<function_decl><name>set</name>)
            NodeList funcDecls = block.getElementsByTagName("function_decl");
            for (int i = 0; i < funcDecls.getLength(); i++) {
                Element funcDecl = (Element) funcDecls.item(i);
                String funcName = getChildElementText(funcDecl, "name");
                if ("set".equals(funcName)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Get event type from event declaration
     */
    private static String getEventType(Element eventElement) {
        Element typeElement = getChildElement(eventElement, "type");
        if (typeElement != null) {
            // Check for generic type with <argument_list type="generic">
            Element nameElement = getChildElement(typeElement, "name");
            if (nameElement != null) {
                Element argumentList = getChildElement(nameElement, "argument_list");
                if (argumentList != null && "generic".equals(argumentList.getAttribute("type"))) {
                    // Generic type: Action<T>, EventHandler<T>, etc.
                    StringBuilder typeBuilder = new StringBuilder();
                    String baseName = getDirectTextContent(nameElement); // e.g., "Action"
                    typeBuilder.append(baseName);
                    
                    // Extract generic arguments
                    NodeList arguments = argumentList.getElementsByTagName("argument");
                    if (arguments.getLength() > 0) {
                        typeBuilder.append("<");
                        for (int i = 0; i < arguments.getLength(); i++) {
                            if (i > 0) typeBuilder.append(", ");
                            Element arg = (Element) arguments.item(i);
                            String argType = getTextContent(arg).trim();
                            typeBuilder.append(convertType(argType));
                        }
                        typeBuilder.append(">");
                    }
                    return typeBuilder.toString();
                } else {
                    // Non-generic type
                    return getTextContent(nameElement);
                }
            }
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
        // Navigate: foreach -> control -> init -> decl
        Element controlElement = getChildElement(loopElement, "control");
        if (controlElement != null) {
            Element initElement = getChildElement(controlElement, "init");
            if (initElement != null) {
                Element declElement = getChildElement(initElement, "decl");
                if (declElement != null) {
                    // Get type and name, skip the "in" part
                    String typeText = getDirectChildElementText(declElement, "type");
                    String nameText = getDirectChildElementText(declElement, "name");
                    if (typeText != null && nameText != null) {
                        String javaType = convertType(typeText);
                        return javaType + " " + nameText;
                    }
                }
            }
        }
        return "var item";
    }
    
    /**
     * Get loop collection for foreach
     */
    private static String getLoopCollection(Element loopElement) {
        // Navigate: foreach -> control -> init -> decl -> range -> expr
        Element controlElement = getChildElement(loopElement, "control");
        if (controlElement != null) {
            Element initElement = getChildElement(controlElement, "init");
            if (initElement != null) {
                Element declElement = getChildElement(initElement, "decl");
                if (declElement != null) {
                    Element rangeElement = getChildElement(declElement, "range");
                    if (rangeElement != null) {
                        Element exprElement = getChildElement(rangeElement, "expr");
                        if (exprElement != null) {
                            return getTextContent(exprElement).trim();
                        }
                    }
                }
            }
        }
        return "collection";
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
     * Check if property has an existing backing field
     */
    private static boolean hasExistingBackingField(Element propertyElement) {
        Element block = getChildElement(propertyElement, "block");
        if (block != null) {
            NodeList stmts = block.getElementsByTagName("expr_stmt");
            for (int i = 0; i < stmts.getLength(); i++) {
                Element stmt = (Element) stmts.item(i);
                Element lambdaElem = getChildElement(stmt, "expr");
                if (lambdaElem != null) {
                    lambdaElem = getChildElement(lambdaElem, "lambda");
                    if (lambdaElem != null) {
                        Element blockContent = getChildElement(lambdaElem, "block");
                        if (blockContent != null) {
                            String content = getTextContent(blockContent);
                            // Check if it references a field (starts with _ or lowercase)
                            if (content.contains("_") || (content.matches(".*[a-z][a-zA-Z0-9]*.*"))) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Get backing field name from property lambda expressions
     */
    private static String getBackingFieldName(Element propertyElement) {
        Element block = getChildElement(propertyElement, "block");
        if (block != null) {
            NodeList stmts = block.getElementsByTagName("expr_stmt");
            for (int i = 0; i < stmts.getLength(); i++) {
                Element stmt = (Element) stmts.item(i);
                String text = getTextContent(stmt);
                // Look for field references in get => _field or set => _field = value
                if (text.contains("=>")) {
                    String[] parts = text.split("=>");
                    if (parts.length > 1) {
                        String body = parts[1].trim();
                        // Extract field name (first identifier)
                        String[] tokens = body.split("[\\s=;]+");
                        for (String token : tokens) {
                            if (token.matches("[_a-zA-Z][_a-zA-Z0-9]*")) {
                                return token;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Get getter body from lambda or block-style accessor
     */
    private static String getGetterBody(Element propertyElement) {
        Element block = getChildElement(propertyElement, "block");
        if (block != null) {
            // Check for lambda-style (get => _health)
            NodeList stmts = block.getElementsByTagName("expr_stmt");
            for (int i = 0; i < stmts.getLength(); i++) {
                Element stmt = (Element) stmts.item(i);
                String text = getTextContent(stmt);
                if (text.contains("get") && text.contains("=>")) {
                    String[] parts = text.split("=>");
                    if (parts.length > 1) {
                        return parts[1].replace(";", "").trim();
                    }
                }
            }
            
            // Check for block-style (get { return _health; })
            NodeList functions = block.getElementsByTagName("function");
            for (int i = 0; i < functions.getLength(); i++) {
                Element func = (Element) functions.item(i);
                String funcName = getChildElementText(func, "name");
                if ("get".equals(funcName)) {
                    // Extract return statement
                    NodeList returns = func.getElementsByTagName("return");
                    if (returns.getLength() > 0) {
                        Element returnStmt = (Element) returns.item(0);
                        Element expr = getChildElement(returnStmt, "expr");
                        if (expr != null) {
                            return getTextContent(expr);
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Get setter body from lambda or block-style accessor
     */
    private static String getSetterBody(Element propertyElement) {
        Element block = getChildElement(propertyElement, "block");
        if (block != null) {
            // Check for lambda-style (set => _health = value)
            NodeList stmts = block.getElementsByTagName("expr_stmt");
            for (int i = 0; i < stmts.getLength(); i++) {
                Element stmt = (Element) stmts.item(i);
                String text = getTextContent(stmt);
                if (text.contains("set") && text.contains("=>")) {
                    String[] parts = text.split("=>");
                    if (parts.length > 1) {
                        return parts[1].replace(";", "").trim();
                    }
                }
            }
            
            // Check for block-style (set { _health = value; })
            NodeList functions = block.getElementsByTagName("function");
            for (int i = 0; i < functions.getLength(); i++) {
                Element func = (Element) functions.item(i);
                String funcName = getChildElementText(func, "name");
                if ("set".equals(funcName)) {
                    // Extract assignment statement
                    Element funcBlock = getChildElement(func, "block");
                    if (funcBlock != null) {
                        NodeList setStmts = funcBlock.getElementsByTagName("expr_stmt");
                        if (setStmts.getLength() > 0) {
                            Element setStmt = (Element) setStmts.item(0);
                            Element expr = getChildElement(setStmt, "expr");
                            if (expr != null) {
                                return getTextContent(expr);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Generate event listener pattern (add/remove/fire methods + interface)
     */
    private static void generateEventListenerPattern(String eventName, String eventType, StringBuilder javaCode, int depth) {
        // Remove "On" prefix if it exists
        String cleanEventName = eventName.startsWith("On") ? eventName.substring(2) : eventName;
        
        String listenerField = "on" + cleanEventName + "Listeners";
        String listenerInterface = "On" + cleanEventName + "Listener";
        
        // Generate listener list field
        javaCode.append(getIndent(depth)).append("private java.util.List<").append(listenerInterface)
               .append("> ").append(listenerField).append(" = new java.util.ArrayList<>();\n");
        
        // Generate add listener method
        javaCode.append(getIndent(depth)).append("public void add").append(listenerInterface)
               .append("(").append(listenerInterface).append(" listener) {\n");
        javaCode.append(getIndent(depth + 1)).append(listenerField).append(".add(listener);\n");
        javaCode.append(getIndent(depth)).append("}\n");
        
        // Generate remove listener method
        javaCode.append(getIndent(depth)).append("public void remove").append(listenerInterface)
               .append("(").append(listenerInterface).append(" listener) {\n");
        javaCode.append(getIndent(depth + 1)).append(listenerField).append(".remove(listener);\n");
        javaCode.append(getIndent(depth)).append("}\n");
        
        // Generate fire method
        javaCode.append(getIndent(depth)).append("protected void fire").append(cleanEventName).append("() {\n");
        javaCode.append(getIndent(depth + 1)).append("for (").append(listenerInterface)
               .append(" listener : ").append(listenerField).append(") {\n");
        javaCode.append(getIndent(depth + 2)).append("listener.on").append(cleanEventName).append("();\n");
        javaCode.append(getIndent(depth + 1)).append("}\n");
        javaCode.append(getIndent(depth)).append("}\n");
        
        // Generate listener interface
        javaCode.append(getIndent(depth)).append("public interface ").append(listenerInterface).append(" {\n");
        javaCode.append(getIndent(depth + 1)).append("void on").append(cleanEventName).append("();\n");
        javaCode.append(getIndent(depth)).append("}\n");
    }
    
    /**
     * Parse Java source code to CompilationUnit
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static CompilationUnit parseJavaCode(String javaCode, String fileName) {
        try {
            Map options = JavaCore.getOptions();
            options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_11);
            options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_11);
            options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_11);
            
            ASTParser parser = ASTParser.newParser(AST.JLS11);
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
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java SrcMLBasedCSharpProcessor <csharp-file>");
            System.exit(1);
        }
        
        String fileName = args[0];
        try {
            File file = new File(fileName);
            String content = new String(Files.readAllBytes(file.toPath()));
            CompilationUnit cu = transformCSharpToJavaAST(content, fileName);
            if (cu != null) {
                System.out.println("Successfully created CompilationUnit with " + cu.types().size() + " types");
            } else {
                System.err.println("Failed to create CompilationUnit");
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Extract modifiers (public, private, static, etc.) from an expression element
     */
    private static String extractModifiersFromExpr(Element exprElement) {
        StringBuilder modifiers = new StringBuilder();
        NodeList specifiers = exprElement.getElementsByTagName("specifier");
        for (int i = 0; i < specifiers.getLength(); i++) {
            String spec = specifiers.item(i).getTextContent().trim();
            if (!spec.isEmpty() && !isTypeKeyword(spec)) {
                if (modifiers.length() > 0) modifiers.append(" ");
                modifiers.append(spec);
            }
        }
        return modifiers.length() > 0 ? modifiers.toString() : "public";
    }
    
    /**
     * Extract return type from an expression element
     */
    private static String extractTypeFromExpr(Element exprElement) {
        // First try to find a type element
        Element typeElement = getChildElement(exprElement, "type");
        if (typeElement != null) {
            return getTextContent(typeElement).trim();
        }
        
        // Otherwise, look through specifiers for type keywords
        NodeList specifiers = exprElement.getElementsByTagName("specifier");
        for (int i = 0; i < specifiers.getLength(); i++) {
            String spec = specifiers.item(i).getTextContent().trim();
            if (isTypeKeyword(spec)) {
                return convertType(spec);
            }
        }
        
        // Look for name element that could be the type
        Element nameElement = getChildElement(exprElement, "name");
        if (nameElement != null) {
            String name = getTextContent(nameElement).trim();
            // Filter out actual variable names (heuristic: if it's a known type)
            if (isTypeKeyword(name) || name.matches("[A-Z][a-zA-Z0-9]*")) {
                return convertType(name);
            }
        }
        
        return "Object"; // Default fallback
    }
    
    /**
     * Get direct text content of an element (immediate text, not nested)
     */
    private static String getDirectTextContent(Element element) {
        if (element == null) return "";
        StringBuilder content = new StringBuilder();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                content.append(child.getTextContent());
            } else if (child instanceof Element) {
                Element childEl = (Element) child;
                if ("name".equals(childEl.getTagName())) {
                    content.append(getTextContent(childEl));
                    break;
                }
            }
        }
        return content.toString().trim();
    }
    
    /**
     * Check if a string is a C# type keyword
     */
    private static boolean isTypeKeyword(String word) {
        return word.matches("(int|string|bool|double|float|decimal|long|short|byte|void|object|" +
                           "List|Dictionary|Array|Tuple|Func|Action|Task|void|var)");
    }
}