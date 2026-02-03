# C# to Java AST Transformation Process

Detailed technical documentation of how RefactoringMiner transforms C# source code into Eclipse JDT Java AST structures.

---

## The 5-Stage Transformation Pipeline

```
┌─────────────────────────────────────────────────────────────────────┐
│ Stage 1: C# Source Code (String)                                    │
│ Input: Raw C# code as text                                          │
└─────────────────────┬───────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────────────────────────┐
│ Stage 2: srcML Parser (External Tool)                               │
│ Tool: SrcmlCsTreeGenerator (GumTree wrapper for srcML)              │
│ Output: XML-based tree structure                                    │
└─────────────────────┬───────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────────────────────────┐
│ Stage 3: GumTree Tree Representation                                │
│ Tool: TransformationUtils.transformTree()                           │
│ Output: Typed nodes (UnitNode, ClassNode, FunctionNode, etc.)       │
└─────────────────────┬───────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────────────────────────┐
│ Stage 4: SrcMLTreeVisitor Pattern Matching                          │
│ Tool: SrcMLTreeVisitor.visit() methods                              │
│ Process: Pattern matching on node types, building JDT AST nodes     │
└─────────────────────┬───────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────────────────────────┐
│ Stage 5: Eclipse JDT CompilationUnit AST                            │
│ Output: Final in-memory tree structure ready for analysis           │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Breakdown

### Stage 1: C# Source Code Input

**Entry Point:** `transformation.Transformation.transform_csharp_to_java(String content)`

**Example Input:**
```csharp
public class MyClass {
    private int count = 0;
    
    public void Increment() {
        count++;
    }
}
```

---

### Stage 2: srcML Parser → XML Tree

**Code Location:** [transformation/Transformation.java](CPatMinerV2/AtomicASTChangeMining/src/transformation/Transformation.java)

```java
SrcmlCsTreeGenerator l = new SrcmlCsTreeGenerator();
TreeContext tc = l.generateFrom().string(content);
Tree tree_csharp = tc.getRoot();
```

**What Happens:**
1. `SrcmlCsTreeGenerator` invokes the **srcML external tool** (C/C++/C# parser)
2. srcML parses C# code into **XML representation**
3. GumTree converts XML DOM into a `Tree` data structure

**Example srcML XML Output:**

```xml
<unit xmlns="http://www.srcML.org/srcML/src">
  <class>
    <specifier>public</specifier>
    <name>MyClass</name>
    <block>{
      <decl_stmt>
        <decl>
          <type>
            <specifier>private</specifier>
            <name>int</name>
          </type>
          <name>count</name>
          <init>=
            <expr>
              <literal type="number">0</literal>
            </expr>
          </init>
        </decl>;
      </decl_stmt>
      
      <function>
        <type>
          <specifier>public</specifier>
          <name>void</name>
        </type>
        <name>Increment</name>
        <parameter_list>()</parameter_list>
        <block>{
          <expr_stmt>
            <expr>
              <name>count</name>
              <operator>++</operator>
            </expr>;
          </expr_stmt>
        }</block>
      </function>
    }</block>
  </class>
</unit>
```

**Key XML Elements:**
- `<unit>` - Root compilation unit
- `<class>` - Class declaration
- `<function>` - Method/function declaration
- `<decl_stmt>` - Variable declaration statement
- `<expr_stmt>` - Expression statement
- `<name>` - Identifier
- `<type>` - Type information
- `<specifier>` - Modifier (public, private, static, etc.)
- `<operator>` - Operators (+, -, ++, etc.)
- `<literal>` - Literal values

---

### Stage 3: Transform to Typed Nodes

**Code Location:** [transformation/Transformation.java](CPatMinerV2/AtomicASTChangeMining/src/transformation/Transformation.java)

```java
Tree transformedTree = TransformationUtils.transformTree(tree_csharp);
```

**Purpose:** Convert generic `Tree` nodes into **strongly-typed node classes**

**Node Type Hierarchy:**

```
Tree (GumTree base)
├── UnitNode (compilation unit)
├── ClassNode (class declaration)
├── StructNode (struct declaration)
├── FunctionNode (method/function)
├── DeclStmtNode (variable declaration)
├── ExprStmtNode (expression statement)
├── ExprNode (expression)
├── CallNode (method call)
├── NameNode (identifier)
├── LiteralNode (literal value)
├── OperatorNode (operator)
├── SpecifierNode (modifier)
├── IfStmtNode (if statement)
├── WhileNode (while loop)
├── ForNode (for loop)
├── ForeachNode (foreach loop)
├── ReturnNode (return statement)
└── ... (many more)
```

**Example Transformation:**

```
Generic Tree:
  Tree("class")
  ├── Tree("specifier", "public")
  ├── Tree("name", "MyClass")
  └── Tree("block")
      └── ...

Typed Tree:
  ClassNode
  ├── SpecifierNode("public")
  ├── NameNode("MyClass")
  └── BlockNode
      └── ...
```

---

### Stage 4: SrcMLTreeVisitor - Pattern Matching

**Code Location:** [transformation/SrcMLTreeVisitor.java](CPatMinerV2/AtomicASTChangeMining/src/transformation/SrcMLTreeVisitor.java) (2028 lines)

**Core Pattern:** Visitor pattern with type-specific processing methods

```java
SrcMLTreeVisitor visitor = new SrcMLTreeVisitor();
if (transformedTree instanceof UnitNode) {
    CompilationUnit m = visitor.visit((UnitNode) transformedTree);
    return m;
}
```

#### **Key Visitor Methods**

| Visitor Method | Input Node | Output AST | Purpose |
|---------------|-----------|-----------|---------|
| `visit(UnitNode)` | UnitNode | CompilationUnit | Root AST node |
| `visit(ClassNode)` | ClassNode | TypeDeclaration | Class/interface |
| `visit(FunctionNode)` | FunctionNode | MethodDeclaration | Method/constructor |
| `visit(DeclStmtNode)` | DeclStmtNode | VariableDeclarationStatement | Variable declaration |
| `visit(ExprStmtNode)` | ExprStmtNode | ExpressionStatement | Expression as statement |
| `visit(ExprNode)` | ExprNode | Expression | General expressions |
| `visit(CallNode)` | CallNode | MethodInvocation | Method calls |
| `visit(NameNode)` | NameNode | Name/SimpleName | Identifiers |
| `visit(LiteralNode)` | LiteralNode | Literal | Literal values |
| `visit(OperatorNode)` | OperatorNode | Operator | Operators |
| `visit(IfStmtNode)` | IfStmtNode | IfStatement | If statements |
| `visit(WhileNode)` | WhileNode | WhileStatement | While loops |
| `visit(ForNode)` | ForNode | ForStatement | For loops |
| `visit(ReturnNode)` | ReturnNode | ReturnStatement | Return statements |
| `visit(BlockContentNode)` | BlockContentNode | List\<Statement\> | Statement lists |
| `visit(SpecifierNode)` | SpecifierNode | Modifier | Access modifiers |


### Stage 5: Building Eclipse JDT AST Nodes

**Eclipse JDT AST Factory:**

```java
AST asn = AST.newAST(AST.JLS8);  // Java Language Specification 8
```

#### **Example: Building a Method Declaration**

**C# Input:**
```csharp
public void Increment() {
    count++;
}
```

**Visitor Code:**
```java
MethodDeclaration visit(FunctionNode node) {
    MethodDeclaration md = asn.newMethodDeclaration();
    
    // 1. Set modifiers (public, private, static, etc.)
    for (SpecifierNode spec : node.getSpecifiers()) {
        Modifier modifier = visit(spec);
        if (modifier != null) {
            md.modifiers().add(modifier);
        }
    }
    
    // 2. Set return type
    Type returnType = visitType(node.getTypeNode());
    if (returnType != null) {
        md.setReturnType2(returnType);
    }
    
    // 3. Set method name
    SimpleName name = asn.newSimpleName(node.getNameNode().getLabel());
    md.setName(name);
    
    // 4. Set parameters
    for (ParameterNode param : node.getParameters()) {
        SingleVariableDeclaration svd = visit(param);
        if (svd != null) {
            md.parameters().add(svd);
        }
    }
    
    // 5. Set method body
    Block body = visit(node.getBlockNode());
    md.setBody(body);
    
    // 6. Set source range (line/column info)
    md.setSourceRange(node.getPos(), node.getLength());
    
    return md;
}
```

**Output AST Structure:**

```
MethodDeclaration
├─ modifiers: [Modifier(PUBLIC)]
├─ returnType: PrimitiveType(VOID)
├─ name: SimpleName("Increment")
├─ parameters: []
├─ body: Block
│  └─ statements: [ExpressionStatement]
│     └─ expression: PostfixExpression
│        ├─ operand: SimpleName("count")
│        └─ operator: INCREMENT (++)
└─ sourceRange: {pos: 45, length: 35}
```

---

## Pattern Matching Examples

### Example 1: Expression Evaluation

**C# Code:** `count++`

**srcML:** `<expr><name>count</name><operator>++</operator></expr>`

**Visitor Logic:**
```java
Expression visit(ExprNode node) {
    List<Tree> children = node.getChildren();
    
    if (children.size() == 2) {
        if (isPostfix(children.get(1))) {
            // Pattern: identifier + postfix operator
            PostfixExpression postfix = asn.newPostfixExpression();
            postfix.setOperand(visit((NameNode) children.get(0)));
            postfix.setOperator(visitPostfix((OperatorNode) children.get(1)));
            return postfix;
        }
    }
    // ... more patterns
}
```

**Output:**
```
PostfixExpression
├─ operand: SimpleName("count")
└─ operator: INCREMENT
```

---

### Example 2: Method Call

**C# Code:** `Console.WriteLine("Hello")`

**srcML:**
```xml
<call>
  <name>
    <name>Console</name>
    <operator>.</operator>
    <name>WriteLine</name>
  </name>
  <argument_list>(
    <argument><expr><literal>"Hello"</literal></expr></argument>
  )</argument_list>
</call>
```

**Visitor Logic:**
```java
MethodInvocation visit(CallNode node) {
    MethodInvocation mi = asn.newMethodInvocation();
    List<Tree> children = node.getChildren();
    
    if (children.size() > 0) {
        Tree nameNode = children.get(0);
        
        // Pattern: object.method()
        if (nameNode.getChildren().size() == 3) {
            List<Tree> parts = nameNode.getChildren();
            // parts[0] = "Console", parts[1] = ".", parts[2] = "WriteLine"
            
            mi.setExpression(visit((NameNode) parts.get(0)));  // Console
            mi.setName((SimpleName) visit((NameNode) parts.get(2)));  // WriteLine
        }
    }
    
    // Add arguments
    if (children.size() > 1 && children.get(1) instanceof ArgumentListNode) {
        for (Expression arg : visit((ArgumentListNode) children.get(1))) {
            mi.arguments().add(arg);
        }
    }
    
    return mi;
}
```

**Output:**
```
MethodInvocation
├─ expression: SimpleName("Console")
├─ name: SimpleName("WriteLine")
└─ arguments: [StringLiteral("Hello")]
```

---

### Example 3: Variable Declaration

**C# Code:** `private int count = 0;`

**srcML:**
```xml
<decl_stmt>
  <decl>
    <type>
      <specifier>private</specifier>
      <name>int</name>
    </type>
    <name>count</name>
    <init>=<expr><literal>0</literal></expr></init>
  </decl>;
</decl_stmt>
```

**Visitor Logic:**
```java
VariableDeclarationStatement visit(DeclStmtNode node) {
    VariableDeclarationStatement vds = asn.newVariableDeclarationStatement(null);
    
    // Get type
    Type type = visitType(node.getTypeNode());
    vds.setType(type);
    
    // Get modifiers
    for (SpecifierNode spec : node.getSpecifiers()) {
        vds.modifiers().add(visit(spec));
    }
    
    // Create variable fragment
    VariableDeclarationFragment fragment = asn.newVariableDeclarationFragment();
    fragment.setName(asn.newSimpleName(node.getNameNode().getLabel()));
    
    // Set initializer if present
    if (node.getInitializer() != null) {
        fragment.setInitializer(visit(node.getInitializer()));
    }
    
    vds.fragments().add(fragment);
    return vds;
}
```

**Output:**
```
VariableDeclarationStatement
├─ modifiers: [Modifier(PRIVATE)]
├─ type: PrimitiveType(INT)
└─ fragments: [VariableDeclarationFragment]
   ├─ name: SimpleName("count")
   └─ initializer: NumberLiteral(0)
```

---

## C# to Java Mapping Table

### Access Modifiers

| C# Modifier | Java Equivalent | Notes |
|-------------|----------------|-------|
| `public` | `public` | Direct mapping |
| `private` | `private` | Direct mapping |
| `protected` | `protected` | Direct mapping |
| `internal` | `null` (dropped) | No Java equivalent |
| `protected internal` | `protected` | Approximation |
| `private protected` | `private` | Approximation |
| `static` | `static` | Direct mapping |
| `const` | `final` | Similar concept |
| `readonly` | `final` | Similar concept |
| `virtual` | `null` (dropped) | Not needed in Java |
| `override` | `@Override` (annotation) | Different mechanism |
| `abstract` | `abstract` | Direct mapping |
| `sealed` | `final` | Similar concept |
| `async` | `null` (dropped) | No direct equivalent |
| `extern` | `null` (dropped) | No direct equivalent |

### Types

| C# Type | Java Type | Notes |
|---------|-----------|-------|
| `int` | `int` | Direct mapping |
| `string` | `String` | Capitalized |
| `bool` | `boolean` | Different name |
| `decimal` | `BigDecimal` | Approximation |
| `object` | `Object` | Capitalized |
| `var` | Inferred type | Resolved by visitor |
| `dynamic` | `Object` | Loss of dynamic typing |
| `List<T>` | `List<T>` | Direct mapping |
| `Dictionary<K,V>` | `Map<K,V>` | Different interface |
| `int[]` | `int[]` | Direct mapping |
| `int?` (nullable) | `Integer` | Boxing required |

### Statements

| C# Statement | Java Statement | Visitor Method |
|-------------|---------------|---------------|
| `if (x) {...}` | `if (x) {...}` | `visit(IfStmtNode)` |
| `while (x) {...}` | `while (x) {...}` | `visit(WhileNode)` |
| `for (;;) {...}` | `for (;;) {...}` | `visit(ForNode)` |
| `foreach (x in y)` | `for (x : y)` | `visit(ForeachNode)` |
| `switch (x) {...}` | `switch (x) {...}` | `visit(SwitchNode)` |
| `return x;` | `return x;` | `visit(ReturnNode)` |
| `break;` | `break;` | `visit(BreakNode)` |
| `continue;` | `continue;` | `visit(ContinueNode)` |
| `try {...} catch` | `try {...} catch` | `visit(TryNode)` |
| `throw ex;` | `throw ex;` | `visit(ThrowNode)` |
| `using (x) {...}` | `try-with-resources` | `visit(UsingStmtNode)` |
| `lock (x) {...}` | `synchronized (x)` | `visit(LockNode)` |

---

## Architecture Diagrams

### Complete Transformation Flow

```
┌────────────────────────────────────────────────────────────────┐
│                    CPatMinerExecutor.java                       │
│                 (RefactoringMiner Bridge)                       │
└───────────────────────┬────────────────────────────────────────┘
                        │ invokes via reflection
                        ↓
┌────────────────────────────────────────────────────────────────┐
│              transformation.Transformation.java                 │
│         transform_csharp_to_java(String content)                │
└───────────────────────┬────────────────────────────────────────┘
                        │
                        ↓
        ┌───────────────┴───────────────┐
        │                               │
        ↓                               ↓
┌──────────────────┐          ┌──────────────────┐
│ SrcmlCs          │          │ Transformation   │
│ TreeGenerator    │          │ Utils            │
│ (srcML wrapper)  │          │ (tree converter) │
└────────┬─────────┘          └────────┬─────────┘
         │                              │
         ↓                              ↓
    [XML Tree]                    [Typed Nodes]
         │                              │
         └──────────────┬───────────────┘
                        ↓
         ┌──────────────────────────┐
         │  SrcMLTreeVisitor.java   │
         │  (Pattern Matcher)       │
         └──────────────┬───────────┘
                        │
                        ↓
              ┌─────────────────┐
              │   AST.newAST()  │
              │ (JDT AST Factory)│
              └─────────┬───────┘
                        │
                        ↓
              ┌──────────────────┐
              │ CompilationUnit  │
              │   (Final AST)    │
              └──────────────────┘
```

### SrcMLTreeVisitor Pattern Matching Process

```
                 ┌─────────────────┐
                 │  Typed Node     │
                 │  (e.g., ExprNode)│
                 └────────┬────────┘
                          │
                          ↓
          ┌───────────────────────────────┐
          │ Identify Pattern:             │
          │ - Check children count        │
          │ - Check child types           │
          │ - Check labels/operators      │
          └───────────┬───────────────────┘
                      │
            ┌─────────┴─────────┐
            │                   │
            ↓                   ↓
    ┌──────────────┐    ┌──────────────┐
    │ Pattern A    │    │ Pattern B    │
    │ (e.g., new)  │    │ (e.g., a++)  │
    └──────┬───────┘    └──────┬───────┘
           │                   │
           ↓                   ↓
    ┌──────────────┐    ┌──────────────┐
    │ Create       │    │ Create       │
    │ ClassInstance│    │ Postfix      │
    │ Creation     │    │ Expression   │
    └──────┬───────┘    └──────┬───────┘
           │                   │
           └─────────┬─────────┘
                     ↓
              ┌──────────────┐
              │ Return JDT   │
              │ AST Node     │
              └──────────────┘
```

---

## Related Files

### Core Transformation Files

| File | Lines | Purpose |
|------|-------|---------|
| [Transformation.java](CPatMinerV2/AtomicASTChangeMining/src/transformation/Transformation.java) | ~70 | Entry point, orchestrates transformation |
| [SrcMLTreeVisitor.java](CPatMinerV2/AtomicASTChangeMining/src/transformation/SrcMLTreeVisitor.java) | 2028 | Pattern matching and AST building |
| [TransformationUtils.java](CPatMinerV2/AtomicASTChangeMining/src/transformation/TransformationUtils.java) | ~400 | Tree transformation utilities |
| [CPatMinerExecutor.java](src/main/java/org/refactoringminer/csharp/CPatMinerExecutor.java) | 236 | Bridge from RefactoringMiner |

### Node Type Definitions

Located in: `CPatMinerV2/AtomicASTChangeMining/src/transformation/`

- `UnitNode.java` - Compilation unit
- `ClassNode.java` - Class declarations
- `FunctionNode.java` - Method declarations
- `ExprNode.java` - Expressions
- `DeclStmtNode.java` - Variable declarations
- `NameNode.java` - Identifiers
- `OperatorNode.java` - Operators
- `LiteralNode.java` - Literal values
- And 50+ more...

---