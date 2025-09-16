package transformation;

import com.github.gumtreediff.tree.DefaultTree;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.Type;

public class SrcMLNodeType extends DefaultTree {

    public static final String COMMENT = "comment";

    public static final String INDEX = "index";
    public static final String RANGE = "range";
    public static final String NAMESPACE = "namespace";
    public static final String ARGUMENT_LIST = "argument_list";
    public static final String ARGUMENT = "argument";
    public static final String IF = "if";
    public static final String NAME = "name";
    public static final String CALL = "call";
    public static final String EXPR = "expr";
    public static final String EXPR_STMT = "expr_stmt";
    public static final String LITERAL = "literal";
    public static final String UNIT = "unit";
    public static final String USING = "using";
    public static final String CLASS = "class";
    public static final String BLOCK = "block";
    public static final String FUNCTION = "function";
    public static final String TYPE = "type";
    public static final String SPECIFIER = "specifier";
    public static final String PARAMETER_LIST = "parameter_list";
    public static final String PARAMETER = "parameter";
    public static final String BLOCK_CONTENT = "block_content";
    public static final String OPERATOR = "operator";
    public static final String ESCAPE = "escape";
    public static final String BREAK = "break";
    public static final String CASE = "case";
    public static final String CONTINUE = "continue";
    public static final String DEFAULT = "default";
    public static final String DO = "do";
    public static final String EMPTY_STMT = "empty_stmt";
    public static final String FIXED = "fixed";
    public static final String FOR = "for";
    public static final String FOREACH = "foreach";
    public static final String GOTO = "goto";
    public static final String IF_STMT = "if_stmt";
    public static final String LABEL = "label";
    public static final String LOCK = "lock";
    public static final String RETURN = "return";
    public static final String SWITCH = "switch";
    public static final String UNSAFE = "unsafe";
    public static final String USING_STMT = "using_stmt";
    public static final String WHILE = "while";
    public static final String CONDITION = "condition";
    public static final String CONTROL = "control";
    public static final String ELSE = "else";
    public static final String INCR = "incr";
    public static final String THEN = "then";
    public static final String INIT = "init";
    public static final String DELEGATE = "delegate";
    public static final String FUNCTION_DECL = "function_decl";
    public static final String LAMBDA = "lambda";
    public static final String MODIFIER = "modifier";
    public static final String DECL = "decl";
    public static final String DECL_STMT = "decl_stmt";
    public static final String CONSTRUCTOR = "constructor";
    public static final String DESTRCUCTOR = "destructor";
    public static final String ENUM = "enum";
    public static final String EVENT = "event";
    public static final String SUPER_LIST = "super_list";
    public static final String SUPER = "super";
    public static final String INTERFACE = "interface";
    public static final String PROPERTY = "property";
    public static final String STRUCT = "struct";
    public static final String TERNARY = "ternary";
    public static final String ATTRIBUTE = "attribute";
    public static final String CHECKED = "checked";
    public static final String TYPEOF = "typeof";
    public static final String SIZEOF = "sizeof";
    public static final String UNCHECKED = "unchecked";
    public static final String CONSTRAINT = "constraint"; // constraint new
    public static final String CATCH = "catch";
    public static final String FINALLY = "finally";
    public static final String THROW = "throw";
    public static final String TRY = "try";
    public static final String BY = "by";
    public static final String EQUALS = "equals";
    public static final String FROM = "from";
    public static final String GROUP = "group";
    public static final String IN = "in";
    public static final String INTO = "into";
    public static final String JOIN = "join";
    public static final String LET = "let";
    public static final String LINQ = "linq";
    public static final String ON = "on";
    public static final String ORDERBY = "orderby";
    public static final String SELECT = "select";
    public static final String WHERE = "where";

    public SrcMLNodeType(Type type) {
        super(type);
    }

    public SrcMLNodeType(Type type, String label) {
        super(type, label);
    }

    protected SrcMLNodeType(Tree other) {
        super(other);
    }
}

class CommentNode extends SrcMLNodeType {
    public static final String TYPE = "comment";

    public CommentNode(Type type) {
        super(type);
    }

    public CommentNode(Type type, String label) {
        super(type, label);
    }

    protected CommentNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }

}

class OperatorNode extends SrcMLNodeType {
    public static final String TYPE = "operator";

    public OperatorNode(Type type) {
        super(type);
    }

    public OperatorNode(Type type, String label) {
        super(type, label);
    }

    protected OperatorNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class NamespaceNode extends SrcMLNodeType {
    public static final String TYPE = "namespace";

    public NamespaceNode(Type type) {
        super(type);
    }

    public NamespaceNode(Type type, String label) {
        super(type, label);
    }

    protected NamespaceNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class RangeNode extends SrcMLNodeType {
    public static final String TYPE = "range";

    public RangeNode(Type type) {
        super(type);
    }

    public RangeNode(Type type, String label) {
        super(type, label);
    }

    protected RangeNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class IndexNode extends SrcMLNodeType {
    public static final String TYPE = "index";

    public IndexNode(Type type) {
        super(type);
    }

    public IndexNode(Type type, String label) {
        super(type, label);
    }

    protected IndexNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class ArgumentListNode extends SrcMLNodeType {
    public static final String TYPE = "argument_list";

    public ArgumentListNode(Type type) {
        super(type);
    }

    public ArgumentListNode(Type type, String label) {
        super(type, label);
    }

    protected ArgumentListNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class ArgumentNode extends SrcMLNodeType {
    public static final String TYPE = "argument";

    public ArgumentNode(Type type) {
        super(type);
    }

    public ArgumentNode(Type type, String label) {
        super(type, label);
    }

    protected ArgumentNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class NameNode extends SrcMLNodeType {
    public static final String TYPE = "name";

    public NameNode(Type type) {
        super(type);
    }

    public NameNode(Type type, String label) {
        super(type, label);
    }

    protected NameNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class CallNode extends SrcMLNodeType {
    public static final String TYPE = "call";

    public CallNode(Type type) {
        super(type);
    }

    public CallNode(Type type, String label) {
        super(type, label);
    }

    protected CallNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class ExprNode extends SrcMLNodeType {
    public static final String TYPE = "expr";

    public ExprNode(Type type) {
        super(type);
    }

    public ExprNode(Type type, String label) {
        super(type, label);
    }

    protected ExprNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class ExprStmtNode extends SrcMLNodeType {
    public static final String TYPE = "expr_stmt";

    public ExprStmtNode(Type type) {
        super(type);
    }

    public ExprStmtNode(Type type, String label) {
        super(type, label);
    }

    protected ExprStmtNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class LiteralNode extends SrcMLNodeType {
    public static final String TYPE = "literal";

    public LiteralNode(Type type) {
        super(type);
    }

    public LiteralNode(Type type, String label) {
        super(type, label);
    }

    protected LiteralNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}


class UsingNode extends SrcMLNodeType {
    public static final String TYPE = "using";

    public UsingNode(Type type) {
        super(type);
    }

    public UsingNode(Type type, String label) {
        super(type, label);
    }

    protected UsingNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class ClassNode extends SrcMLNodeType {
    public static final String TYPE = "class";

    public ClassNode(Type type) {
        super(type);
    }

    public ClassNode(Type type, String label) {
        super(type, label);
    }

    protected ClassNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class BlockNode extends SrcMLNodeType {
    public static final String TYPE = "block";

    public BlockNode(Type type) {
        super(type);
    }

    public BlockNode(Type type, String label) {
        super(type, label);
    }

    protected BlockNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class FunctionNode extends SrcMLNodeType {
    public static final String TYPE = "function";

    public FunctionNode(Type type) {
        super(type);
    }

    public FunctionNode(Type type, String label) {
        super(type, label);
    }

    protected FunctionNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class TypeNode extends SrcMLNodeType {
    public static final String TYPE = "type";

    public TypeNode(Type type) {
        super(type);
    }

    public TypeNode(Type type, String label) {
        super(type, label);
    }

    protected TypeNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class SpecifierNode extends SrcMLNodeType {
    public static final String TYPE = "specifier";

    public SpecifierNode(Type type) {
        super(type);
    }

    public SpecifierNode(Type type, String label) {
        super(type, label);
    }

    protected SpecifierNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class ParameterListNode extends SrcMLNodeType {
    public static final String TYPE = "parameter_list";

    public ParameterListNode(Type type) {
        super(type);
    }

    public ParameterListNode(Type type, String label) {
        super(type, label);
    }

    protected ParameterListNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class ParameterNode extends SrcMLNodeType {
    public static final String TYPE = "parameter";

    public ParameterNode(Type type) {
        super(type);
    }

    public ParameterNode(Type type, String label) {
        super(type, label);
    }

    protected ParameterNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class BlockContentNode extends SrcMLNodeType {
    public static final String TYPE = "block_content";

    public BlockContentNode(Type type) {
        super(type);
    }

    public BlockContentNode(Type type, String label) {
        super(type, label);
    }

    protected BlockContentNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class EscapeNode extends SrcMLNodeType {
    public static final String TYPE = "escape";

    public EscapeNode(Type type) {
        super(type);
    }

    public EscapeNode(Type type, String label) {
        super(type, label);
    }

    protected EscapeNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class BreakNode extends SrcMLNodeType {
    public static final String TYPE = "break";

    public BreakNode(Type type) {
        super(type);
    }

    public BreakNode(Type type, String label) {
        super(type, label);
    }

    protected BreakNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class CaseNode extends SrcMLNodeType {
    public static final String TYPE = "case";

    public CaseNode(Type type) {
        super(type);
    }

    public CaseNode(Type type, String label) {
        super(type, label);
    }

    protected CaseNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class ContinueNode extends SrcMLNodeType {
    public static final String TYPE = "continue";

    public ContinueNode(Type type) {
        super(type);
    }

    public ContinueNode(Type type, String label) {
        super(type, label);
    }

    protected ContinueNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class DefaultNode extends SrcMLNodeType {
    public static final String TYPE = "default";

    public DefaultNode(Type type) {
        super(type);
    }

    public DefaultNode(Type type, String label) {
        super(type, label);
    }

    protected DefaultNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class DoNode extends SrcMLNodeType {
    public static final String TYPE = "do";

    public DoNode(Type type) {
        super(type);
    }

    public DoNode(Type type, String label) {
        super(type, label);
    }

    protected DoNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class EmptyStmtNode extends SrcMLNodeType {
    public static final String TYPE = "empty_stmt";

    public EmptyStmtNode(Type type) {
        super(type);
    }

    public EmptyStmtNode(Type type, String label) {
        super(type, label);
    }

    protected EmptyStmtNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class FixedNode extends SrcMLNodeType {
    public static final String TYPE = "fixed";

    public FixedNode(Type type) {
        super(type);
    }

    public FixedNode(Type type, String label) {
        super(type, label);
    }

    protected FixedNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class ForNode extends SrcMLNodeType {
    public static final String TYPE = "for";

    public ForNode(Type type) {
        super(type);
    }

    public ForNode(Type type, String label) {
        super(type, label);
    }

    protected ForNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }

}

class ForeachNode extends SrcMLNodeType {
    public static final String TYPE = "foreach";

    public ForeachNode(Type type) {
        super(type);
    }

    public ForeachNode(Type type, String label) {
        super(type, label);
    }

    protected ForeachNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class GotoNode extends SrcMLNodeType {
    public static final String TYPE = "goto";

    public GotoNode(Type type) {
        super(type);
    }

    public GotoNode(Type type, String label) {
        super(type, label);
    }

    protected GotoNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class IfStmtNode extends SrcMLNodeType {
    public static final String TYPE = "if_stmt";

    public IfStmtNode(Type type) {
        super(type);
    }

    public IfStmtNode(Type type, String label) {
        super(type, label);
    }

    protected IfStmtNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class IfNode extends SrcMLNodeType {
    public static final String TYPE = "if";

    public IfNode(Type type) {
        super(type);
    }

    public IfNode(Type type, String label) {
        super(type, label);
    }

    protected IfNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class LabelNode extends SrcMLNodeType {
    public static final String TYPE = "label";

    public LabelNode(Type type) {
        super(type);
    }

    public LabelNode(Type type, String label) {
        super(type, label);
    }

    protected LabelNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class LockNode extends SrcMLNodeType {
    public static final String TYPE = "lock";

    public LockNode(Type type) {
        super(type);
    }

    public LockNode(Type type, String label) {
        super(type, label);
    }

    protected LockNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class ReturnNode extends SrcMLNodeType {
    public static final String TYPE = "return";

    public ReturnNode(Type type) {
        super(type);
    }

    public ReturnNode(Type type, String label) {
        super(type, label);
    }

    protected ReturnNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class SwitchNode extends SrcMLNodeType {
    public static final String TYPE = "switch";

    public SwitchNode(Type type) {
        super(type);
    }

    public SwitchNode(Type type, String label) {
        super(type, label);
    }

    protected SwitchNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class UnsafeNode extends SrcMLNodeType {
    public static final String TYPE = "unsafe";

    public UnsafeNode(Type type) {
        super(type);
    }

    public UnsafeNode(Type type, String label) {
        super(type, label);
    }

    protected UnsafeNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class UsingStmtNode extends SrcMLNodeType {
    public static final String TYPE = "using_stmt";

    public UsingStmtNode(Type type) {
        super(type);
    }

    public UsingStmtNode(Type type, String label) {
        super(type, label);
    }

    protected UsingStmtNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class WhileNode extends SrcMLNodeType {
    public static final String TYPE = "while";

    public WhileNode(Type type) {
        super(type);
    }

    public WhileNode(Type type, String label) {
        super(type, label);
    }

    protected WhileNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class ConditionNode extends SrcMLNodeType {
    public static final String TYPE = "condition";

    public ConditionNode(Type type) {
        super(type);
    }

    public ConditionNode(Type type, String label) {
        super(type, label);
    }

    protected ConditionNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class ControlNode extends SrcMLNodeType {
    public static final String TYPE = "control";

    public ControlNode(Type type) {
        super(type);
    }

    public ControlNode(Type type, String label) {
        super(type, label);
    }

    protected ControlNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class ElseNode extends SrcMLNodeType {
    public static final String TYPE = "else";

    public ElseNode(Type type) {
        super(type);
    }

    public ElseNode(Type type, String label) {
        super(type, label);
    }

    protected ElseNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class IncrNode extends SrcMLNodeType {
    public static final String TYPE = "incr";

    public IncrNode(Type type) {
        super(type);
    }

    public IncrNode(Type type, String label) {
        super(type, label);
    }

    protected IncrNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class ThenNode extends SrcMLNodeType {
    public static final String TYPE = "then";

    public ThenNode(Type type) {
        super(type);
    }

    public ThenNode(Type type, String label) {
        super(type, label);
    }

    protected ThenNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class InitNode extends SrcMLNodeType {
    public static final String TYPE = "init";

    public InitNode(Type type) {
        super(type);
    }

    public InitNode(Type type, String label) {
        super(type, label);
    }

    protected InitNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class DelegateNode extends SrcMLNodeType {
    public static final String TYPE = "delegate";

    public DelegateNode(Type type) {
        super(type);
    }

    public DelegateNode(Type type, String label) {
        super(type, label);
    }

    protected DelegateNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class FunctionDeclNode extends SrcMLNodeType {
    public static final String TYPE = "function_decl";

    public FunctionDeclNode(Type type) {
        super(type);
    }

    public FunctionDeclNode(Type type, String label) {
        super(type, label);
    }

    protected FunctionDeclNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class LambdaNode extends SrcMLNodeType {
    public static final String TYPE = "lambda";

    public LambdaNode(Type type) {
        super(type);
    }

    public LambdaNode(Type type, String label) {
        super(type, label);
    }

    protected LambdaNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class ModifierNode extends SrcMLNodeType {
    public static final String TYPE = "modifier";

    public ModifierNode(Type type) {
        super(type);
    }

    public ModifierNode(Type type, String label) {
        super(type, label);
    }

    protected ModifierNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class DeclNode extends SrcMLNodeType {
    public static final String TYPE = "decl";

    public DeclNode(Type type) {
        super(type);
    }

    public DeclNode(Type type, String label) {
        super(type, label);
    }

    protected DeclNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class DeclStmtNode extends SrcMLNodeType {
    public static final String TYPE = "decl_stmt";

    public DeclStmtNode(Type type) {
        super(type);
    }

    public DeclStmtNode(Type type, String label) {
        super(type, label);
    }

    protected DeclStmtNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class ConstructorNode extends SrcMLNodeType {
    public static final String TYPE = "constructor";

    public ConstructorNode(Type type) {
        super(type);
    }

    public ConstructorNode(Type type, String label) {
        super(type, label);
    }

    protected ConstructorNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class DestrctorNode extends SrcMLNodeType {
    public static final String TYPE = "destructor";

    public DestrctorNode(Type type) {
        super(type);
    }

    public DestrctorNode(Type type, String label) {
        super(type, label);
    }

    protected DestrctorNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class EnumNode extends SrcMLNodeType {
    public static final String TYPE = "enum";

    public EnumNode(Type type) {
        super(type);
    }

    public EnumNode(Type type, String label) {
        super(type, label);
    }

    protected EnumNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class EventNode extends SrcMLNodeType {
    public static final String TYPE = "event";

    public EventNode(Type type) {
        super(type);
    }

    public EventNode(Type type, String label) {
        super(type, label);
    }

    protected EventNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class SuperListNode extends SrcMLNodeType {
    public static final String TYPE = "super_list";

    public SuperListNode(Type type) {
        super(type);
    }

    public SuperListNode(Type type, String label) {
        super(type, label);
    }

    protected SuperListNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class SuperNode extends SrcMLNodeType {
    public static final String TYPE = "super";

    public SuperNode(Type type) {
        super(type);
    }

    public SuperNode(Type type, String label) {
        super(type, label);
    }

    protected SuperNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class InterfaceNode extends SrcMLNodeType {
    public static final String TYPE = "interface";

    public InterfaceNode(Type type) {
        super(type);
    }

    public InterfaceNode(Type type, String label) {
        super(type, label);
    }

    protected InterfaceNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class PropertyNode extends SrcMLNodeType {
    public static final String TYPE = "property";

    public PropertyNode(Type type) {
        super(type);
    }

    public PropertyNode(Type type, String label) {
        super(type, label);
    }

    protected PropertyNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class StructNode extends SrcMLNodeType {
    public static final String TYPE = "struct";

    public StructNode(Type type) {
        super(type);
    }

    public StructNode(Type type, String label) {
        super(type, label);
    }

    protected StructNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class TernaryNode extends SrcMLNodeType {
    public static final String TYPE = "ternary";

    public TernaryNode(Type type) {
        super(type);
    }

    public TernaryNode(Type type, String label) {
        super(type, label);
    }

    protected TernaryNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class AttributeNode extends SrcMLNodeType {
    public static final String TYPE = "attribute";

    public AttributeNode(Type type) {
        super(type);
    }

    public AttributeNode(Type type, String label) {
        super(type, label);
    }

    protected AttributeNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class CheckedNode extends SrcMLNodeType {
    public static final String TYPE = "checked";

    public CheckedNode(Type type) {
        super(type);
    }

    public CheckedNode(Type type, String label) {
        super(type, label);
    }

    protected CheckedNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class TypeOfNode extends SrcMLNodeType {
    public static final String TYPE = "typeof";

    public TypeOfNode(Type type) {
        super(type);
    }

    public TypeOfNode(Type type, String label) {
        super(type, label);
    }

    protected TypeOfNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class SizeOfNode extends SrcMLNodeType {
    public static final String TYPE = "sizeof";

    public SizeOfNode(Type type) {
        super(type);
    }

    public SizeOfNode(Type type, String label) {
        super(type, label);
    }

    protected SizeOfNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class UncheckedNode extends SrcMLNodeType {
    public static final String TYPE = "unchecked";

    public UncheckedNode(Type type) {
        super(type);
    }

    public UncheckedNode(Type type, String label) {
        super(type, label);
    }

    protected UncheckedNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class ConstraintNode extends SrcMLNodeType {
    public static final String TYPE = "constraint";

    public ConstraintNode(Type type) {
        super(type);
    }

    public ConstraintNode(Type type, String label) {
        super(type, label);
    }

    protected ConstraintNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class CatchNode extends SrcMLNodeType {
    public static final String TYPE = "catch";

    public CatchNode(Type type) {
        super(type);
    }

    public CatchNode(Type type, String label) {
        super(type, label);
    }

    protected CatchNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class FinallyNode extends SrcMLNodeType {
    public static final String TYPE = "finally";

    public FinallyNode(Type type) {
        super(type);
    }

    public FinallyNode(Type type, String label) {
        super(type, label);
    }

    protected FinallyNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class ThrowNode extends SrcMLNodeType {
    public static final String TYPE = "throw";

    public ThrowNode(Type type) {
        super(type);
    }

    public ThrowNode(Type type, String label) {
        super(type, label);
    }

    protected ThrowNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class TryNode extends SrcMLNodeType {
    public static final String TYPE = "try";

    public TryNode(Type type) {
        super(type);
    }

    public TryNode(Type type, String label) {
        super(type, label);
    }

    protected TryNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class ByNode extends SrcMLNodeType {
    public static final String TYPE = "by";

    public ByNode(Type type) {
        super(type);
    }

    public ByNode(Type type, String label) {
        super(type, label);
    }

    protected ByNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class EqualsNode extends SrcMLNodeType {
    public static final String TYPE = "equals";

    public EqualsNode(Type type) {
        super(type);
    }

    public EqualsNode(Type type, String label) {
        super(type, label);
    }

    protected EqualsNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class FromNode extends SrcMLNodeType {
    public static final String TYPE = "from";

    public FromNode(Type type) {
        super(type);
    }

    public FromNode(Type type, String label) {
        super(type, label);
    }

    protected FromNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class GroupNode extends SrcMLNodeType {
    public static final String TYPE = "group";

    public GroupNode(Type type) {
        super(type);
    }

    public GroupNode(Type type, String label) {
        super(type, label);
    }

    protected GroupNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class InNode extends SrcMLNodeType {
    public static final String TYPE = "in";

    public InNode(Type type) {
        super(type);
    }

    public InNode(Type type, String label) {
        super(type, label);
    }

    protected InNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class IntoNode extends SrcMLNodeType {
    public static final String TYPE = "into";

    public IntoNode(Type type) {
        super(type);
    }

    public IntoNode(Type type, String label) {
        super(type, label);
    }

    protected IntoNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class JoinNode extends SrcMLNodeType {
    public static final String TYPE = "join";

    public JoinNode(Type type) {
        super(type);
    }

    public JoinNode(Type type, String label) {
        super(type, label);
    }

    protected JoinNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class LetNode extends SrcMLNodeType {
    public static final String TYPE = "let";

    public LetNode(Type type) {
        super(type);
    }

    public LetNode(Type type, String label) {
        super(type, label);
    }

    protected LetNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class LinqNode extends SrcMLNodeType {
    public static final String TYPE = "linq";

    public LinqNode(Type type) {
        super(type);
    }

    public LinqNode(Type type, String label) {
        super(type, label);
    }

    protected LinqNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class OnNode extends SrcMLNodeType {
    public static final String TYPE = "on";

    public OnNode(Type type) {
        super(type);
    }

    public OnNode(Type type, String label) {
        super(type, label);
    }

    protected OnNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class OrderByNode extends SrcMLNodeType {
    public static final String TYPE = "orderby";

    public OrderByNode(Type type) {
        super(type);
    }

    public OrderByNode(Type type, String label) {
        super(type, label);
    }

    protected OrderByNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class SelectNode extends SrcMLNodeType {
    public static final String TYPE = "select";

    public SelectNode(Type type) {
        super(type);
    }

    public SelectNode(Type type, String label) {
        super(type, label);
    }

    protected SelectNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}

class WhereNode extends SrcMLNodeType {
    public static final String TYPE = "where";

    public WhereNode(Type type) {
        super(type);
    }

    public WhereNode(Type type, String label) {
        super(type, label);
    }

    protected WhereNode(Tree other) {
        super(other);
    }

    void accept(SrcMLTreeVisitor visitor) {
        visitor.visit(this);
    }
}