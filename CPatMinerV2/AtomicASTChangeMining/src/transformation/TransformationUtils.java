package transformation;

import com.github.gumtreediff.tree.Tree;
import org.eclipse.jdt.core.dom.*;

import java.util.regex.Pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

public class TransformationUtils {

    public static class ReturnPair<T, U> {
        private T first;
        private U second;

        public ReturnPair(T first, U second) {
            this.first = first;
            this.second = second;
        }

        public T getFirst() {
            return first;
        }

        public U getSecond() {
            return second;
        }
    }

    static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty())
            return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    static boolean containsParenthesis(Tree node) {
        for (Tree child : node.getChildren()) {
            if (child instanceof OperatorNode && Objects.equals(child.getLabel(), "("))
                return true;
        }
        return false;
    }

    static boolean isAssignment(Tree node) {
        List<String> l = new ArrayList<>();
        l.add("=");
        l.add("+=");
        l.add("-=");
        l.add("*=");
        l.add("/=");
        l.add("%=");
        l.add("&=");
        l.add("^=");
        l.add("|=");
        l.add("<<=");
        l.add(">>=");
        l.add(">>>=");
        return l.contains(node.getLabel());
    }

    static boolean isPostfix(Tree node) {
        List<String> l = new ArrayList<>();
        l.add("++");
        l.add("--");
        return l.contains(node.getLabel());
    }

    static boolean string_doesnt_contain_operator(String label) {
        String regex = ".*[!@#$%^&*()+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*";
        return !Pattern.matches(regex, label);
    }
    static boolean isPrefix(Tree node) {
        List<String> l = new ArrayList<>();
        l.add("++");
        l.add("--");
        l.add("!");
        l.add("-");
        l.add("~");
        l.add("+");
        return l.contains(node.getLabel());
    }

    static ExprNode createNewExprNode(Tree node, int first_element){
        // Create a new Expression node without the first n elements in input
        ExprNode exp = new ExprNode(node);
        for (int i = first_element; i< node.getChildren().size(); i++)
            exp.addChild( node.getChildren().get(i));
        return exp;
    }


    public static boolean isBoolean(String input) {
        return Objects.equals(input, "true") || Objects.equals(input, "false");
    }

    public static boolean isNumber(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            try {
                Double.parseDouble(input);
                return true;
            } catch (NumberFormatException e2) {
                return false;
            }
        }
    }

    static Expression type_literal(AST asn, Tree node) {
        String input = node.getLabel();
        if (isBoolean(input)) {
            BooleanLiteral o = asn.newBooleanLiteral(Boolean.parseBoolean(input));
            o.setSourceRange(node.getPos(),node.getLength());
            return o;
        } else if (isNumber(input)) {
            NumberLiteral o = asn.newNumberLiteral(input);
            o.setSourceRange(node.getPos(),node.getLength());
            return o;
        } else {
            StringLiteral stringLiteral = asn.newStringLiteral();
            stringLiteral.setLiteralValue(input);
            stringLiteral.setSourceRange(node.getPos(),node.getLength());
            return stringLiteral;
        }
    }

    public static Tree transformTree(Tree inputTree) {
        String nodeType = inputTree.getType().toString();
        List<Tree> children = inputTree.getChildren();
        children = transformChildren(children);
        Tree new_tree;
        if (Objects.equals(nodeType, SrcMLNodeType.ARGUMENT))
            new_tree = new ArgumentNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.NAME))
            new_tree = new NameNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.COMMENT))
            new_tree = new CommentNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.NAMESPACE))
            new_tree = new NamespaceNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.ARGUMENT_LIST))
            new_tree = new ArgumentListNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.CALL))
            new_tree = new CallNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.EXPR))
            new_tree = new ExprNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.EXPR_STMT))
            new_tree = new ExprStmtNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.LITERAL))
            new_tree = new LiteralNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.UNIT))
            new_tree = new UnitNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.USING))
            new_tree = new UsingNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.CLASS))
            new_tree = new ClassNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.BLOCK))
            new_tree = new BlockNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.FUNCTION))
            new_tree = new FunctionNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.TYPE))
            new_tree = new TypeNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.SPECIFIER))
            new_tree = new SpecifierNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.PARAMETER_LIST))
            new_tree = new ParameterListNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.PARAMETER))
            new_tree = new ParameterNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.BLOCK_CONTENT))
            new_tree = new BlockContentNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.OPERATOR))
            new_tree = new OperatorNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.ESCAPE))
            new_tree = new EscapeNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.BREAK))
            new_tree = new BreakNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.CASE))
            new_tree = new CaseNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.CONTINUE))
            new_tree = new ContinueNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.DEFAULT))
            new_tree = new DefaultNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.DO))
            new_tree = new DoNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.EMPTY_STMT))
            new_tree = new EmptyStmtNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.FIXED))
            new_tree = new FixedNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.FOR))
            new_tree = new ForNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.FOREACH))
            new_tree = new ForeachNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.GOTO))
            new_tree = new GotoNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.IF_STMT))
            new_tree = new IfStmtNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.LABEL))
            new_tree = new LabelNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.LOCK))
            new_tree = new LockNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.RETURN))
            new_tree = new ReturnNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.SWITCH))
            new_tree = new SwitchNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.UNSAFE))
            new_tree = new UnsafeNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.USING_STMT))
            new_tree = new UsingStmtNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.INDEX))
            new_tree = new IndexNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.WHILE))
            new_tree = new WhileNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.CONDITION))
            new_tree = new ConditionNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.CONTROL))
            new_tree = new ControlNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.ELSE))
            new_tree = new ElseNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.INCR))
            new_tree = new IncrNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.THEN))
            new_tree = new ThenNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.INIT))
            new_tree = new InitNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.DELEGATE))
            new_tree = new DelegateNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.FUNCTION_DECL))
            new_tree = new FunctionDeclNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.LAMBDA))
            new_tree = new LambdaNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.MODIFIER))
            new_tree = new ModifierNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.DECL))
            new_tree = new DeclNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.DECL_STMT))
            new_tree = new DeclStmtNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.RANGE))
            new_tree = new RangeNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.CONSTRUCTOR))
            new_tree = new ConstructorNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.DESTRCUCTOR))
            new_tree = new DestrctorNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.ENUM))
            new_tree = new EnumNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.EVENT))
            new_tree = new EventNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.SUPER_LIST))
            new_tree = new SuperListNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.SUPER))
            new_tree = new SuperNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.INTERFACE))
            new_tree = new InterfaceNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.PROPERTY))
            new_tree = new PropertyNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.STRUCT))
            new_tree = new StructNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.TERNARY))
            new_tree = new TernaryNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.ATTRIBUTE))
            new_tree = new AttributeNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.CHECKED))
            new_tree = new CheckedNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.TYPEOF))
            new_tree = new TypeOfNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.SIZEOF))
            new_tree = new SizeOfNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.UNCHECKED))
            new_tree = new UncheckedNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.CONSTRAINT))
            new_tree = new ConstraintNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.CATCH))
            new_tree = new CatchNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.FINALLY))
            new_tree = new FinallyNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.THROW))
            new_tree = new ThrowNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.TRY))
            new_tree = new TryNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.BY))
            new_tree = new ByNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.EQUALS))
            new_tree = new EqualsNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.FROM))
            new_tree = new FromNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.GROUP))
            new_tree = new GroupNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.IN))
            new_tree = new InNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.INTO))
            new_tree = new IntoNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.JOIN))
            new_tree = new JoinNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.LET))
            new_tree = new LetNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.LINQ))
            new_tree = new LinqNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.ON))
            new_tree = new OnNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.ORDERBY))
            new_tree = new OrderByNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.SELECT))
            new_tree = new SelectNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.WHERE))
            new_tree = new WhereNode(inputTree);
        else if (Objects.equals(nodeType, SrcMLNodeType.IF))
            new_tree = new IfNode(inputTree);
        else
            new_tree = new SrcMLNodeType(inputTree);
        new_tree.setChildren(children);
        return new_tree;
    }

    public static List<Tree> transformChildren(List<Tree> children) {
        List<Tree> transformedChildren = new ArrayList<>();
        for (Tree child : children) {
            transformedChildren.add(TransformationUtils.transformTree(child));
        }
        return transformedChildren;
    }

}
