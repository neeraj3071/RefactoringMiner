# Implementation Guide: Fixing CPatMiner C# Type Inference Issues

**Date**: February 20, 2026  
**Target**: CPatMinerV2/AtomicASTChangeMining  
**Files**: `src/transformation/SrcMLTreeVisitor.java`  
**Estimated Effort**: 4-6 weeks

---

## Problem Statement

CPatMiner currently passes the C# `var` keyword **literally** to RefactoringMiner instead of resolving it to the actual inferred type. This causes 0% precision on commits with `var` usage.

**Current Behavior**:
```java
// C# Input
var manifest = GameObject.Find(...).GetComponent<AggregationManifest>();

// CPatMiner Output (WRONG)
VariableDeclaration {
    type: SimpleType("var"),  // Literal "var" string!
    name: "manifest"
}

// Should Output (CORRECT)
VariableDeclaration {
    type: SimpleType("AggregationManifest"),  // Resolved type
    name: "manifest"
}
```

---

## Solution Overview

### Phase 1: Basic var Inference (2 weeks)
1. Detect when type is "var"
2. Analyze initializer expression
3. Resolve type from expression
4. Replace "var" with resolved type

### Phase 2: Advanced Patterns (2 weeks)
1. Generic method calls (`GetComponent<T>()`)
2. LINQ expressions
3. Method return types
4. Null-conditional operators

### Phase 3: Property Recognition (1-2 weeks)
1. Distinguish properties from fields
2. Mark property access patterns
3. Preserve metadata for RefactoringMiner

---

## Phase 1: Basic var Type Inference

### Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│ DeclStmtNode Visitor                                             │
│ var manifest = expression;                                       │
└──────────────────┬──────────────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────────────┐
│ Check if TypeNode contains "var"                                │
│ if (typeName.equals("var"))                                     │
└──────────────────┬──────────────────────────────────────────────┘
                   │
                   ↓ YES
┌─────────────────────────────────────────────────────────────────┐
│ TypeInferenceEngine.inferType(InitNode)                         │
│ - Extract initializer expression                                │
│ - Analyze expression type                                       │
│ - Return resolved Type object                                   │
└──────────────────┬──────────────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────────────┐
│ Replace "var" Type with resolved Type                           │
│ variableDeclaration.setType(resolvedType);                      │
└─────────────────────────────────────────────────────────────────┘
```

### Implementation: TypeInferenceEngine.java

Create a new class to handle type inference:

```java
package transformation;

import org.eclipse.jdt.core.dom.*;
import transformation.nodes.*;
import com.github.gumtreediff.tree.Tree;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class TypeInferenceEngine {
    private AST ast;
    private SrcMLTreeVisitor visitor;
    
    // Cache for resolved types to avoid redundant work
    private Map<Tree, Type> typeCache = new HashMap<>();
    
    public TypeInferenceEngine(AST ast, SrcMLTreeVisitor visitor) {
        this.ast = ast;
        this.visitor = visitor;
    }
    
    /**
     * Main entry point: infer type from variable initializer
     * 
     * @param initNode The initialization node (right-hand side of =)
     * @return Resolved Type object, or null if cannot infer
     */
    public Type inferTypeFromInitializer(Tree initNode) {
        if (initNode == null || initNode.getChildren().isEmpty()) {
            return null;
        }
        
        // Check cache first
        if (typeCache.containsKey(initNode)) {
            return copyType(typeCache.get(initNode));
        }
        
        Tree exprNode = initNode.getChildren().get(0);
        Type inferredType = inferTypeFromExpression(exprNode);
        
        // Cache the result
        if (inferredType != null) {
            typeCache.put(initNode, inferredType);
        }
        
        return inferredType;
    }
    
    /**
     * Infer type from any expression node
     */
    private Type inferTypeFromExpression(Tree exprNode) {
        if (exprNode == null) {
            return null;
        }
        
        // Handle different expression types
        if (exprNode instanceof transformation.nodes.ExprNode) {
            return inferFromExprNode((transformation.nodes.ExprNode) exprNode);
        }
        
        return null;
    }
    
    /**
     * Infer type from ExprNode
     */
    private Type inferFromExprNode(transformation.nodes.ExprNode exprNode) {
        List<Tree> children = exprNode.getChildren();
        
        if (children.isEmpty()) {
            return null;
        }
        
        Tree firstChild = children.get(0);
        
        // Case 1: Literal (number, string, boolean, null)
        if (firstChild instanceof transformation.nodes.LiteralNode) {
            return inferFromLiteral((transformation.nodes.LiteralNode) firstChild);
        }
        
        // Case 2: Object creation (new Type())
        if (firstChild instanceof transformation.nodes.CallNode) {
            return inferFromCallNode((transformation.nodes.CallNode) firstChild);
        }
        
        // Case 3: Method call (GetComponent<T>())
        if (firstChild instanceof transformation.nodes.NameNode) {
            transformation.nodes.NameNode nameNode = (transformation.nodes.NameNode) firstChild;
            
            // Check if it's a method call with generic type
            if (children.size() > 1) {
                Tree secondChild = children.get(1);
                
                // Method call: name.call(args)
                if (secondChild instanceof transformation.nodes.CallNode) {
                    return inferFromMethodCall(nameNode, (transformation.nodes.CallNode) secondChild);
                }
                
                // Operator: expr operator expr
                if (secondChild instanceof transformation.nodes.OperatorNode) {
                    return inferFromOperator(nameNode, (transformation.nodes.OperatorNode) secondChild, children);
                }
            }
            
            // Simple name reference - type unknown without symbol table
            return createObjectType();
        }
        
        return null;
    }
    
    /**
     * Infer type from literal value
     */
    private Type inferFromLiteral(transformation.nodes.LiteralNode literal) {
        String value = literal.getLabel();
        String literalType = literal.getType();
        
        if (literalType != null) {
            switch (literalType) {
                case "number":
                    // Determine if int, long, float, double based on value
                    if (value.contains(".") || value.toLowerCase().contains("e")) {
                        return ast.newSimpleType(ast.newSimpleName("Double"));
                    } else if (value.endsWith("L") || value.endsWith("l")) {
                        return ast.newSimpleType(ast.newSimpleName("Long"));
                    } else if (value.endsWith("F") || value.endsWith("f")) {
                        return ast.newSimpleType(ast.newSimpleName("Float"));
                    } else {
                        return ast.newSimpleType(ast.newSimpleName("Int"));
                    }
                    
                case "string":
                    return ast.newSimpleType(ast.newSimpleName("String"));
                    
                case "char":
                    return ast.newSimpleType(ast.newSimpleName("Char"));
                    
                case "boolean":
                    return ast.newSimpleType(ast.newSimpleName("Boolean"));
                    
                case "null":
                    // Null type is tricky - return Object as safe default
                    return createObjectType();
            }
        }
        
        // Fallback: try to infer from value format
        if (value.equals("true") || value.equals("false")) {
            return ast.newSimpleType(ast.newSimpleName("Boolean"));
        }
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return ast.newSimpleType(ast.newSimpleName("String"));
        }
        if (value.matches("\\d+")) {
            return ast.newSimpleType(ast.newSimpleName("Int"));
        }
        
        return createObjectType();
    }
    
    /**
     * Infer type from object creation expression
     * Example: new GameObject()
     */
    private Type inferFromCallNode(transformation.nodes.CallNode callNode) {
        List<Tree> children = callNode.getChildren();
        
        if (!children.isEmpty() && children.get(0) instanceof transformation.nodes.NameNode) {
            transformation.nodes.NameNode typeNameNode = (transformation.nodes.NameNode) children.get(0);
            String typeName = extractTypeName(typeNameNode);
            
            if (typeName != null && !typeName.isEmpty()) {
                return ast.newSimpleType(ast.newSimpleName(typeName));
            }
        }
        
        return createObjectType();
    }
    
    /**
     * Infer type from method call with generic type argument
     * Example: GetComponent<AggregationManifest>()
     */
    private Type inferFromMethodCall(transformation.nodes.NameNode nameNode, 
                                     transformation.nodes.CallNode callNode) {
        
        // Check if the call node has generic type arguments
        List<Tree> callChildren = callNode.getChildren();
        
        for (Tree child : callChildren) {
            // Look for ArgumentListNode containing type arguments
            if (child instanceof transformation.nodes.ArgumentListNode) {
                transformation.nodes.ArgumentListNode argList = 
                    (transformation.nodes.ArgumentListNode) child;
                
                // Check if this is a generic type argument list (not regular args)
                // In srcML, generic arguments appear as: <type>name</type>
                List<Tree> argChildren = argList.getChildren();
                
                if (!argChildren.isEmpty() && argChildren.get(0) instanceof transformation.nodes.ArgumentNode) {
                    transformation.nodes.ArgumentNode firstArg = 
                        (transformation.nodes.ArgumentNode) argChildren.get(0);
                    
                    // If the argument looks like a type (capitalized, no operators)
                    Type genericType = extractGenericTypeArgument(firstArg);
                    if (genericType != null) {
                        return genericType;
                    }
                }
            }
        }
        
        // Check method name for known Unity/C# patterns
        String methodName = nameNode.getLabel();
        
        // Common Unity patterns
        if (methodName.contains("GetComponent")) {
            // Try to extract type from method name or context
            // Could parse: GetComponent<T>() from the original source
        }
        
        if (methodName.contains("Find")) {
            return ast.newSimpleType(ast.newSimpleName("GameObject"));
        }
        
        // Default: unknown return type
        return createObjectType();
    }
    
    /**
     * Infer type from operator expression
     */
    private Type inferFromOperator(transformation.nodes.NameNode left,
                                   transformation.nodes.OperatorNode op,
                                   List<Tree> children) {
        String operator = op.getLabel();
        
        // Arithmetic operators: +, -, *, /, %
        if (operator.matches("[+\\-*/%]")) {
            // Return numeric type (could be more sophisticated)
            return ast.newSimpleType(ast.newSimpleName("Int"));
        }
        
        // Comparison operators: ==, !=, <, >, <=, >=
        if (operator.matches("==|!=|<|>|<=|>=")) {
            return ast.newSimpleType(ast.newSimpleName("Boolean"));
        }
        
        // Logical operators: &&, ||
        if (operator.equals("&&") || operator.equals("||")) {
            return ast.newSimpleType(ast.newSimpleName("Boolean"));
        }
        
        // String concatenation
        if (operator.equals("+")) {
            // Could be int+int or string+string - need more context
            return createObjectType();
        }
        
        return createObjectType();
    }
    
    /**
     * Extract type name from NameNode
     */
    private String extractTypeName(transformation.nodes.NameNode nameNode) {
        StringBuilder typeName = new StringBuilder();
        
        // Handle simple name
        if (nameNode.getChildren().isEmpty()) {
            return nameNode.getLabel();
        }
        
        // Handle qualified name (e.g., GameObject.Transform)
        for (Tree child : nameNode.getChildren()) {
            if (child.getChildren().isEmpty()) {
                if (typeName.length() > 0) {
                    typeName.append(".");
                }
                typeName.append(child.getLabel());
            }
        }
        
        return typeName.toString();
    }
    
    /**
     * Extract generic type argument from ArgumentNode
     * Example: <AggregationManifest>
     */
    private Type extractGenericTypeArgument(transformation.nodes.ArgumentNode arg) {
        List<Tree> children = arg.getChildren();
        
        if (!children.isEmpty()) {
            Tree child = children.get(0);
            
            // Look for type pattern
            if (child instanceof transformation.nodes.ExprNode) {
                transformation.nodes.ExprNode expr = (transformation.nodes.ExprNode) child;
                
                if (!expr.getChildren().isEmpty() && 
                    expr.getChildren().get(0) instanceof transformation.nodes.NameNode) {
                    
                    transformation.nodes.NameNode typeName = 
                        (transformation.nodes.NameNode) expr.getChildren().get(0);
                    
                    String typeStr = extractTypeName(typeName);
                    
                    // Check if this looks like a type (capitalized)
                    if (typeStr != null && typeStr.length() > 0 && 
                        Character.isUpperCase(typeStr.charAt(0))) {
                        return ast.newSimpleType(ast.newSimpleName(typeStr));
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Create generic Object type as fallback
     */
    private Type createObjectType() {
        return ast.newSimpleType(ast.newSimpleName("Object"));
    }
    
    /**
     * Deep copy a Type object (JDT types are not reusable)
     */
    private Type copyType(Type type) {
        if (type == null) {
            return null;
        }
        
        if (type.isSimpleType()) {
            SimpleType st = (SimpleType) type;
            return ast.newSimpleType((Name) ASTNode.copySubtree(ast, st.getName()));
        }
        
        if (type.isArrayType()) {
            ArrayType at = (ArrayType) type;
            return ast.newArrayType(copyType(at.getElementType()));
        }
        
        if (type.isParameterizedType()) {
            ParameterizedType pt = (ParameterizedType) type;
            ParameterizedType newPt = ast.newParameterizedType(copyType(pt.getType()));
            for (Object typeArg : pt.typeArguments()) {
                newPt.typeArguments().add(copyType((Type) typeArg));
            }
            return newPt;
        }
        
        // Fallback: return Object
        return createObjectType();
    }
}
```

### Integration into SrcMLTreeVisitor.java

**Step 1**: Add TypeInferenceEngine field

```java
public class SrcMLTreeVisitor {
    private AST asn;
    private TypeInferenceEngine typeInferenceEngine;  // NEW
    
    public SrcMLTreeVisitor(AST ast) {
        this.asn = ast;
        this.typeInferenceEngine = new TypeInferenceEngine(ast, this);  // NEW
    }
    
    // ... existing code ...
}
```

**Step 2**: Modify `visit(DeclNode)` to detect and resolve var

**Location**: Line ~991  
**Current code**:
```java
ReturnPair<VariableDeclarationFragment, Object> visit(DeclNode node) {
    VariableDeclarationFragment variableFragment = asn.newVariableDeclarationFragment();
    variableFragment.setSourceRange(node.getPos(), node.getLength());
    Object t = null;
    for (Tree child : node.getChildren()) {
        if (child instanceof NameNode) {
            Name n = this.visit((NameNode) child);
            if (n != null && n.isSimpleName())
                variableFragment.setName((SimpleName) n);
        }
        if (child instanceof InitNode) {
            Expression type_literal = (Expression) this.visit((InitNode) child);
            variableFragment.setInitializer(type_literal);
        }
        if (child instanceof TypeNode)
            t = this.visit((TypeNode) child); // could be type or field declaration
    }
    return new ReturnPair<>(variableFragment, t);
}
```

**Modified code**:
```java
ReturnPair<VariableDeclarationFragment, Object> visit(DeclNode node) {
    VariableDeclarationFragment variableFragment = asn.newVariableDeclarationFragment();
    variableFragment.setSourceRange(node.getPos(), node.getLength());
    Object t = null;
    Tree initNode = null;  // NEW: Track initializer
    
    for (Tree child : node.getChildren()) {
        if (child instanceof NameNode) {
            Name n = this.visit((NameNode) child);
            if (n != null && n.isSimpleName())
                variableFragment.setName((SimpleName) n);
        }
        if (child instanceof InitNode) {
            initNode = child;  // NEW: Save for type inference
            Expression type_literal = (Expression) this.visit((InitNode) child);
            variableFragment.setInitializer(type_literal);
        }
        if (child instanceof TypeNode)
            t = this.visit((TypeNode) child); // could be type or field declaration
    }
    
    // NEW: Check if type is "var" and resolve it
    if (t != null && t instanceof Type) {
        Type type = (Type) t;
        if (isVarType(type) && initNode != null) {
            // Infer actual type from initializer
            Type inferredType = typeInferenceEngine.inferTypeFromInitializer(initNode);
            if (inferredType != null) {
                t = inferredType;  // Replace "var" with inferred type
            }
        }
    }
    
    return new ReturnPair<>(variableFragment, t);
}

// NEW: Helper method to check if type is "var"
private boolean isVarType(Type type) {
    if (type == null || !type.isSimpleType()) {
        return false;
    }
    
    SimpleType st = (SimpleType) type;
    Name name = st.getName();
    
    if (name.isSimpleName()) {
        SimpleName sn = (SimpleName) name;
        return "var".equals(sn.getIdentifier());
    }
    
    return false;
}
```

**Step 3**: Modify `visit(ParameterNode)` to handle var parameters

**Location**: Line ~829  
**Current code**:
```java
SingleVariableDeclaration visit(ParameterNode node) {
    SingleVariableDeclaration parameter = asn.newSingleVariableDeclaration();

    parameter.setType(asn.newSimpleType(asn.newSimpleName("var"))); // default value

    if (!node.getChildren().isEmpty() && node.getChildren().get(0) instanceof DeclNode) {
        for (Tree param_child : node.getChildren().get(0).getChildren()) {
            if (param_child instanceof TypeNode) {
                Type t = (Type) this.visit((TypeNode) param_child);
                if (t != null)
                    parameter.setType(t);
            }
            if (param_child instanceof NameNode) {
                Name n = this.visit((NameNode) param_child);
                if (n != null && n.isSimpleName())
                    parameter.setName((SimpleName) n);
            }
        }
    }
    return parameter;
}
```

**Modified code**:
```java
SingleVariableDeclaration visit(ParameterNode node) {
    SingleVariableDeclaration parameter = asn.newSingleVariableDeclaration();

    // NEW: Use Object as default instead of "var"
    parameter.setType(asn.newSimpleType(asn.newSimpleName("Object"))); // default value

    if (!node.getChildren().isEmpty() && node.getChildren().get(0) instanceof DeclNode) {
        for (Tree param_child : node.getChildren().get(0).getChildren()) {
            if (param_child instanceof TypeNode) {
                Type t = (Type) this.visit((TypeNode) param_child);
                if (t != null) {
                    // NEW: Check if type is "var" and handle appropriately
                    if (isVarType(t)) {
                        // For lambda parameters, var means inferred from context
                        // Without full type system, use Object as safe default
                        parameter.setType(asn.newSimpleType(asn.newSimpleName("Object")));
                    } else {
                        parameter.setType(t);
                    }
                }
            }
            if (param_child instanceof NameNode) {
                Name n = this.visit((NameNode) param_child);
                if (n != null && n.isSimpleName())
                    parameter.setName((SimpleName) n);
            }
        }
    }
    return parameter;
}
```

**Step 4**: Fix lambda parameter handling

**Location**: Line ~2257 and ~2364  
Replace:
```java
sv.setType(asn.newSimpleType(asn.newSimpleName("var")));
```

With:
```java
// For lambda parameters, use Object as type (context-dependent in C#)
sv.setType(asn.newSimpleType(asn.newSimpleName("Object")));
```

---

## Phase 2: Advanced Type Inference Patterns

### Generic Method Call Parsing

**Challenge**: Parse `GetComponent<AggregationManifest>()` to extract type

**Solution**: Enhance srcML parsing to recognize generic type arguments

```java
/**
 * Enhanced method to extract generic type from call expression
 */
private Type extractGenericTypeFromCall(transformation.nodes.CallNode callNode) {
    // srcML represents generics as:
    // <call>
    //   <name>GetComponent</name>
    //   <argument_list>
    //     <argument>
    //       <expr><name>AggregationManifest</name></expr>
    //     </argument>
    //   </argument_list>
    // </call>
    
    List<Tree> children = callNode.getChildren();
    
    for (Tree child : children) {
        if (child.getLabel().equals("argument_list")) {
            // Check if arguments are types (generic args) or values (regular args)
            List<Tree> args = child.getChildren();
            
            if (!args.isEmpty()) {
                Tree firstArg = args.get(0);
                
                // Heuristic: If first argument looks like a type name
                // (capitalized, no operators), treat as generic type
                String argText = extractArgumentText(firstArg);
                
                if (argText != null && isTypeName(argText)) {
                    return asn.newSimpleType(asn.newSimpleName(argText));
                }
            }
        }
    }
    
    return null;
}

private boolean isTypeName(String name) {
    // Type names in C# are PascalCase (start with capital)
    // Variable/parameter names are camelCase (start with lowercase)
    return name.length() > 0 && Character.isUpperCase(name.charAt(0));
}
```

### LINQ Expression Type Inference

**Challenge**: Infer types from LINQ queries

```csharp
var items = collection.Where(x => x > 5).Select(x => x.ToString());
// Should infer: IEnumerable<string>
```

**Solution**: Pattern matching on LINQ method chains

```java
private Type inferFromLinqExpression(transformation.nodes.ExprNode exprNode) {
    // Detect LINQ methods: Where, Select, OrderBy, etc.
    String methodChain = extractMethodChain(exprNode);
    
    if (methodChain.contains("Select")) {
        // Try to infer the selected type from lambda
        // For now, use IEnumerable<Object> as safe default
        SimpleType elementType = asn.newSimpleType(asn.newSimpleName("Object"));
        SimpleType enumerableType = asn.newSimpleType(asn.newSimpleName("IEnumerable"));
        ParameterizedType result = asn.newParameterizedType(enumerableType);
        result.typeArguments().add(elementType);
        return result;
    }
    
    if (methodChain.contains("Where") || methodChain.contains("OrderBy")) {
        // These maintain the collection type
        // Use IEnumerable<Object> as default
        SimpleType elementType = asn.newSimpleType(asn.newSimpleName("Object"));
        SimpleType enumerableType = asn.newSimpleType(asn.newSimpleName("IEnumerable"));
        ParameterizedType result = asn.newParameterizedType(enumerableType);
        result.typeArguments().add(elementType);
        return result;
    }
    
    return null;
}
```

---

## Phase 3: Property Recognition

### Problem

C# properties look like fields but behave differently:

```csharp
public class Selection {
    public static GameObject[] gameObjects { get; }  // Property
}

// Usage
var objects = Selection.gameObjects;  // Property access, not field
```

### Solution: Detect Property Declarations

**Location**: Add to `visit(PropertyNode)`

```java
// Mark this as a property in the AST metadata
// Option 1: Use custom annotation
@Property
public GameObject[] gameObjects;

// Option 2: Store in separate property list
private Set<String> properties = new HashSet<>();

Object visit(PropertyNode node) {
    // ... existing code ...
    
    // NEW: Track property names
    String propertyName = extractPropertyName(node);
    if (propertyName != null) {
        properties.add(propertyName);
    }
    
    // ... existing code ...
}
```

### Mark Property Access Patterns

When visiting name references, check if they're properties:

```java
Expression visit(NameNode node) {
    // ... existing code ...
    
    Name name = createNameFromNode(node);
    
    // NEW: Check if this is property access
    if (isPropertyAccess(name)) {
        // Add marker or annotation
        // This helps RefactoringMiner distinguish variables from properties
    }
    
    return name;
}

private boolean isPropertyAccess(Name name) {
    String fullName = name.getFullyQualifiedName();
    
    // Check against known properties
    if (properties.contains(fullName)) {
        return true;
    }
    
    // Check common Unity/C# property patterns
    if (fullName.matches(".*\\.(transform|gameObject|name|tag)")) {
        return true;  // Known Unity properties
    }
    
    return false;
}
```

---

## Testing Strategy

### Unit Tests

Create `TypeInferenceEngineTest.java`:

```java
@Test
public void testVarWithLiteral() {
    // var x = 42;
    String csharp = "var x = 42;";
    Type type = inferType(csharp);
    assertEquals("Int", getTypeName(type));
}

@Test
public void testVarWithObjectCreation() {
    // var obj = new GameObject();
    String csharp = "var obj = new GameObject();";
    Type type = inferType(csharp);
    assertEquals("GameObject", getTypeName(type));
}

@Test
public void testVarWithGenericMethod() {
    // var component = GetComponent<Rigidbody>();
    String csharp = "var component = GetComponent<Rigidbody>();";
    Type type = inferType(csharp);
    assertEquals("Rigidbody", getTypeName(type));
}

@Test
public void testVarWithLinq() {
    // var items = list.Where(x => x > 5);
    String csharp = "var items = list.Where(x => x > 5);";
    Type type = inferType(csharp);
    assertTrue(type instanceof ParameterizedType);
    assertEquals("IEnumerable", getBaseTypeName(type));
}
```

### Integration Tests

Test on the failing commits:

```java
@Test
public void testCommit5820914c() {
    // The commit that had 0% precision
    String repoPath = "temp-repos/cvr-sdk-unity";
    String commit = "5820914c27c2816a4c0c8913d2af5d115fccc2fd";
    
    List<Refactoring> refactorings = detectRefactorings(repoPath, commit);
    
    // Should now correctly detect Extract Method
    assertTrue(containsRefactoringType(refactorings, "Extract Method"));
    
    // Should NOT report contradictory variable renames
    assertFalse(hasContradictoryRenames(refactorings));
    
    // Precision should be > 50%
    double precision = calculatePrecision(refactorings);
    assertTrue(precision > 0.5);
}
```

### Regression Tests

Ensure existing functionality works:

```java
@Test
public void testExplicitTypes() {
    // int x = 42; (explicit type)
    String csharp = "int x = 42;";
    Type type = parseType(csharp);
    assertEquals("Int", getTypeName(type));
    // Should work exactly as before
}

@Test
public void testCommit9aaea6e6() {
    // Nested type issue - should still be detected
    String repoPath = "temp-repos/UniVRM";
    String commit = "9aaea6e6a1fe270842c05854714b7b24b0284097";
    
    List<Refactoring> refactorings = detectRefactorings(repoPath, commit);
    
    // Ensure the nested type fixes didn't break anything
    assertNoRegressions(refactorings);
}
```

---

## Implementation Checklist

### Week 1-2: Basic var Inference
- [ ] Create `TypeInferenceEngine.java`
- [ ] Implement `inferTypeFromInitializer()`
- [ ] Handle literal types
- [ ] Handle object creation
- [ ] Integrate into `SrcMLTreeVisitor`
- [ ] Write unit tests
- [ ] Test on simple var cases

### Week 3-4: Advanced Patterns
- [ ] Implement generic method parsing
- [ ] Extract type from `GetComponent<T>()`
- [ ] Handle LINQ expressions (basic)
- [ ] Handle method return types
- [ ] Fix lambda parameter types
- [ ] Write integration tests
- [ ] Test on commit 5820914c

### Week 5-6: Property Recognition & Polish
- [ ] Track property declarations
- [ ] Mark property access patterns
- [ ] Distinguish properties from fields
- [ ] Handle Unity-specific properties
- [ ] Full regression testing
- [ ] Test on all 5 commits
- [ ] Document limitations

---

## Expected Results

### Before Fix (Current State)
```
Commit 5820914c:
- Detected: 8 refactorings (all false positives)
- Precision: 0%
- Issues: var treated as literal type
```

### After Phase 1 Fix
```
Commit 5820914c:
- Detected: 4 refactorings (2-3 correct)
- Precision: 50-75%
- Improvement: var resolved to actual types
- Remaining issues: Complex LINQ, generics
```

### After Phase 2 Fix
```
Commit 5820914c:
- Detected: 2-3 refactorings (2 correct)
- Precision: 75-100%  
- Improvement: Generic methods handled
- Remaining issues: Advanced LINQ patterns
```

### After Phase 3 Fix
```
All 5 commits:
- Overall precision: 75-85% (up from 50% average)
- Critical failures eliminated
- Property/variable confusion resolved
```

---

## Known Limitations

Even after full implementation, some cases will remain challenging:

1. **Complex type inference**: C# compiler does full type checking; we can't replicate without full semantic analysis
2. **Dynamic types**: `dynamic` keyword cannot be resolved statically
3. **Implicit conversions**: C# has many implicit type conversions that require type system
4. **Extension methods**: LINQ uses extension methods that require semantic analysis
5. **Async/await**: Task<T> unwrapping requires understanding async semantics

**Mitigation**: Use `Object` as safe fallback when type cannot be inferred. This is correct (everything inherits from Object) and allows RefactoringMiner to continue analysis.

---

## Alternative Approaches

### Option 1: Use Roslyn (C# Compiler)
**Pros**: Perfect type inference, full semantic analysis  
**Cons**: Requires .NET runtime, major architectural change  
**Effort**: 3-6 months

### Option 2: Symbol Table + Type Checker
**Pros**: More accurate than pattern matching  
**Cons**: Complex to implement, maintain  
**Effort**: 2-3 months

### Option 3: Pattern Matching (Proposed)
**Pros**: Fast, practical, works for 80% of cases  
**Cons**: Not perfect, some edge cases remain  
**Effort**: 4-6 weeks ✅ **Recommended**

---

## Success Metrics

- [ ] Commit 5820914c precision > 50% (currently 0%)
- [ ] Overall precision across 5 commits > 75% (currently 57%)
- [ ] Zero "var" types passed to RefactoringMiner
- [ ] Extract Method detection works on commits with var
- [ ] No regression on commits without var
- [ ] Property access correctly distinguished from variables

---

## Conclusion

This implementation plan provides a **practical, incremental solution** to the var keyword problem. By implementing type inference through pattern matching, we can:

1. ✅ Resolve var to actual types in 70-80% of cases
2. ✅ Use safe fallbacks (Object) when uncertain
3. ✅ Dramatically improve precision on modern C# code
4. ✅ Complete in 4-6 weeks with testing

The approach is pragmatic: **perfect is enemy of good enough**. We don't need compiler-level precision; we need "good enough" type resolution to enable refactoring detection.

**Start with Phase 1** (2 weeks) and measure improvement. If results are promising, continue to Phase 2 & 3.
