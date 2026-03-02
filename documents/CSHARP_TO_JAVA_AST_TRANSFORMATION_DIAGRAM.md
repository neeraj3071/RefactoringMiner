# C# to Java AST Transformation Pipeline (New CPatMiner Approach)

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                    STAGE 1: srcML C# Parser                                     │
│                    (External Tool - Parses C# to XML AST)                       │
└─────────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
        INPUT: C# Source Code
        ┌──────────────────────────────────────────────────┐
        │ foreach (var item in items)                      │
        │     Console.WriteLine(item ?? text);             │
        └──────────────────────────────────────────────────┘
                                    │
                                    ▼ srcML parsing
        ┌──────────────────────────────────────────────────┐
        │ srcML XML AST                                    │
        │  <foreach>                                       │
        │    <control>                                     │
        │      <decl><type>var</type><name>item</name></decl>│
        │      <range><expr>items</expr></range>            │
        │    </control>                                     │
        │    <block>                                        │
        │      <expr_stmt>                                  │
        │        <call>                                     │
        │          <name>Console.WriteLine</name>           │
        │          <argument_list>                          │
        │            <expr>                                 │
        │              <name>item</name>                    │
        │              <operator>??</operator>  ◄── [C3]   │
        │              <name>text</name>                    │
        │            </expr>                                │
        │          </argument_list>                         │
        │        </call>                                    │
        │      </expr_stmt>                                 │
        │    </block>                                       │
        │  </foreach>                                       │
        └──────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│           STAGE 2: CPatMiner SrcMLTreeVisitor (Direct AST Transformation)       │
│           Component: transformation/SrcMLTreeVisitor.java                       │
│           Process: Visit srcML nodes → Create Eclipse JDT AST nodes             │
└─────────────────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    │   Key Visitor Methods:        │
                    │   • visit(ForeachNode)        │
                    │   • visit(ExprNode)           │
                    │   • visit(CallNode)           │
                    │   • visit(OperatorNode)       │
                    │   • evaluateNode()            │
                    └───────────────┬───────────────┘
                                    │
                    ┌───────────────▼───────────────┐
                    │  NEW FIX: ?? Operator Handler │
                    │                               │
                    │  if (operator == "??") {      │
                    │    ConditionalExpression()    │
                    │    condition: item != null    │
                    │    then: item                 │
                    │    else: text                 │
                    │  }                            │
                    └───────────────┬───────────────┘
                                    │
                                    ▼
        ┌──────────────────────────────────────────────────┐
        │ Eclipse JDT Java AST (In-Memory)                 │
        │                                                   │
        │ EnhancedForStatement [C2]                        │
        │   └─ parameter: var String item                  │
        │   └─ expression: items                           │
        │   └─ body: Block                                 │
        │        └─ MethodInvocation [C1]                  │
        │             └─ expression: Console               │
        │             └─ name: WriteLine                   │
        │             └─ arguments:                        │
        │                  └─ ConditionalExpression [C3] ◄─┐│
        │                       └─ condition:              ││
        │                       │    InfixExpression       ││
        │                       │      left: item          ││
        │                       │      operator: !=        ││
        │                       │      right: null         ││
        │                       └─ thenExpression: item   ││
        │                       └─ elseExpression: text   ││
        │                                                  ││
        │  [C1] Console.WriteLine PRESERVED (not mapped)  ││
        │  [C2] Enhanced for loop from foreach            ││
        │  [C3] ?? → Ternary (ConditionalExpression) ✓   ││
        └──────────────────────────────────────────────────┘│
                                    │                       │
                                    ▼                  OUR FIX!
┌─────────────────────────────────────────────────────────────────────────────────┐
│                    STAGE 3: AST to String Serialization                         │
│                    Component: CPatMinerExecutor.astToString()                   │
│                    Process: CompilationUnit.toString()                          │
└─────────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
        ┌──────────────────────────────────────────────────┐
        │ Generated Java Source String                     │
        │                                                   │
        │ for (Var item : items) {                         │
        │   Console.WriteLine(item != null ? item : text); │
        │ }                                                 │
        │                                                   │
        │ • API names preserved (Console.WriteLine)        │
        │ • Ternary correctly generated from AST           │
        │ • Structure ready for UMLModel creation          │
        └──────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│              STAGE 4: RefactoringMiner UMLModelASTReader                        │
│              Component: gr.uom.java.xmi.UMLModelASTReader                       │
│              Process: Parse Java string → Create UMLModel                       │
└─────────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
        ┌──────────────────────────────────────────────────┐
        │ UMLModel (Refactoring Detection Ready)           │
        │                                                   │
        │ • Classes: TestClass                             │
        │ • Methods: TestMethod()                          │
        │ • Method Invocations: Console.WriteLine()        │
        │ • Expressions: ConditionalExpression             │
        │                                                   │
        │ ✓ All structural information preserved           │
        │ ✓ Ready for pattern matching                     │
        │ ✓ Refactoring detection enabled                  │
        └──────────────────────────────────────────────────┘
```

## Key Differences from Old Approach:

### OLD APPROACH (Text-based transformation):
```
C# Code → srcML XML → Syntax String Transformation → Java Code String → Eclipse JDT Parse
```

### NEW APPROACH (AST-level transformation):
```
C# Code → srcML XML → Direct AST Transformation → Eclipse JDT AST → String → Re-parse
```

## Why the New Approach is Better:

1. **AST-level transformation** - More accurate structural preservation
2. **No intermediate string manipulation** - Reduces parsing errors
3. **Direct node mapping** - srcML nodes → Eclipse JDT nodes
4. **Preserves API names** - No need for API mapping (Console.WriteLine kept as-is)
5. **Operator handling** - `??` transformed to proper ConditionalExpression AST node
6. **Type-safe** - Java AST types ensure structural validity

## Our Contribution:

**Updated SrcMLTreeVisitor.java (Line ~344-373):**
```java
// Handle null coalescing operator (??) 
if (children.size() >= 3 && children.get(1) instanceof OperatorNode && 
    Objects.equals(children.get(1).getLabel(), "??")) {
    ConditionalExpression conditionalExpression = asn.newConditionalExpression();
    
    // Create: item != null ? item : text
    Expression leftOperand = this.evaluateNode(children.get(0));
    if (leftOperand != null) {
        InfixExpression condition = asn.newInfixExpression();
        condition.setLeftOperand((Expression) ASTNode.copySubtree(asn, leftOperand));
        condition.setOperator(InfixExpression.Operator.NOT_EQUALS);
        condition.setRightOperand(asn.newNullLiteral());
        conditionalExpression.setExpression(condition);
        conditionalExpression.setThenExpression((Expression) ASTNode.copySubtree(asn, leftOperand));
    }
    
    ExprNode rightOperandNode = createNewExprNode(node, 2);
    Expression rightOperand = this.visit(rightOperandNode);
    if (rightOperand != null) {
        conditionalExpression.setElseExpression(rightOperand);
    }
    
    return conditionalExpression;
}
```

This fix ensures the `??` operator is correctly transformed to a Java ternary operator at the AST level, preserving the null-checking semantics for accurate refactoring detection.
