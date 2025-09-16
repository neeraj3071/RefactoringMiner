package transformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import com.github.gumtreediff.tree.Tree;

import transformation.TransformationUtils.ReturnPair;
import static transformation.TransformationUtils.capitalizeFirstLetter;
import static transformation.TransformationUtils.containsParenthesis;
import static transformation.TransformationUtils.createNewExprNode;
import static transformation.TransformationUtils.isAssignment;
import static transformation.TransformationUtils.isPostfix;
import static transformation.TransformationUtils.isPrefix;
import static transformation.TransformationUtils.string_doesnt_contain_operator;
import static transformation.TransformationUtils.type_literal;

public class SrcMLTreeVisitor {

    AST asn = AST.newAST(AST.JLS8);

    void visit(CommentNode node) {
        // Do nothing since comments are not needed
    }

    List<Object> visit(BlockContentNode node) {
        List<Object> transformed_statements = new ArrayList<>();
        List<Tree> statements = node.getChildren();
        for (Tree statement : statements) {
            if (statement instanceof DeclStmtNode) {
                transformed_statements.add(this.visit((DeclStmtNode) statement));
            } else if (statement instanceof ExprStmtNode) {
                transformed_statements.add(this.visit((ExprStmtNode) statement));
            } else if (statement instanceof ExprNode) {
                Expression exp = this.visit((ExprNode) statement);
                ExpressionStatement exs = asn.newExpressionStatement(exp);
                transformed_statements.add(exs);
            } else if (statement instanceof ReturnNode) {
                transformed_statements.add(this.visit((ReturnNode) statement));
            } else if (statement instanceof IfStmtNode) {
                transformed_statements.add(this.visit((IfStmtNode) statement));
            } else if (statement instanceof WhileNode) {
                transformed_statements.add(this.visit((WhileNode) statement));
            } else if (statement instanceof DoNode) {
                transformed_statements.add(this.visit((DoNode) statement));
            } else if (statement instanceof ForNode) {
                transformed_statements.add(this.visit((ForNode) statement));
            } else if (statement instanceof ForeachNode) {
                transformed_statements.add(this.visit((ForeachNode) statement));
            } else if (statement instanceof SwitchNode) {
                transformed_statements.add(this.visit((SwitchNode) statement));
            } else if (statement instanceof BreakNode) {
                transformed_statements.add(this.visit((BreakNode) statement));
            } else if (statement instanceof ContinueNode) {
                transformed_statements.add(this.visit((ContinueNode) statement));
            } else if (statement instanceof TryNode) {
                transformed_statements.add(this.visit((TryNode) statement));
            } else if (statement instanceof ThrowNode) {
                transformed_statements.add(this.visit((ThrowNode) statement));
            } else if (statement instanceof UsingStmtNode) {
                transformed_statements.add(this.visit((UsingStmtNode) statement));
            } else if (statement instanceof UnsafeNode) {
                for (Object bb : this.visit((UnsafeNode) statement))
                    transformed_statements.add(bb);
            } else if (statement instanceof LockNode) {
                transformed_statements.add(this.visit((LockNode) statement));
            } else if (statement instanceof EmptyStmtNode) {
                transformed_statements.add(this.visit((EmptyStmtNode) statement));
            } else if (statement instanceof FixedNode) {
                transformed_statements.add(this.visit((FixedNode) statement));
            } else if (statement instanceof CheckedNode) {
                for (Object bb : this.visit((CheckedNode) statement))
                    transformed_statements.add(bb);
            } else if (statement instanceof UncheckedNode) {
                for (Object bb : this.visit((UncheckedNode) statement))
                    transformed_statements.add(bb);
            }
        }
        return transformed_statements;
    }

    Expression visit(LiteralNode node) {
        return type_literal(asn, node);
    }

    Name visit(NameNode node) {
        List<Tree> children = node.getChildren();
        if (children.size() == 0) {
            try {
                Name n = asn.newSimpleName(node.getLabel().replace("~", "")); //if it's a destructor
                n.setSourceRange(node.getPos(), node.getLength());
                return n;
            } catch (Exception e) {
                try {
                    Name n = asn.newSimpleName(capitalizeFirstLetter(node.getLabel()));
                    n.setSourceRange(node.getPos(), node.getLength());
                    return n;
                } catch (Exception ee) {
                    return null;
                }
            }
        }
        if (children.size() == 3 && children.get(0) instanceof NameNode && children.get(2) instanceof NameNode) { // ex: app.status
            Name name_1 = this.visit((NameNode) children.get(0));
            SimpleName sm = (SimpleName) this.visit((NameNode) children.get(2));
            if (name_1 != null && sm != null) {
                Name n = asn.newQualifiedName(name_1, sm);
                n.setSourceRange(node.getPos(), node.getLength());
                return n;
            }
        }
        boolean b = string_doesnt_contain_operator(children.get(0).getLabel());
        if (children.get(0) instanceof NameNode && b) {
            try {
                Name n = asn.newSimpleName(children.get(0).getLabel());
                n.setSourceRange(children.get(0).getPos(), children.get(0).getLength());
                return n;
            } catch (Exception e) {
                try {
                    Name n = asn.newSimpleName(capitalizeFirstLetter(children.get(0).getLabel()));
                    n.setSourceRange(children.get(0).getPos(), children.get(0).getLength());
                    return n;
                } catch (Exception ee) {
                }
            }
        }
        return null;
    }

    Expression visitNameSpecial(NameNode node) {
        List<Tree> children = node.getChildren();
        if (children.size() == 2) {
            ArrayAccess arrayAccess = asn.newArrayAccess();
            arrayAccess.setSourceRange(node.getPos(), node.getLength());
            if (children.get(0) instanceof NameNode)
                arrayAccess.setArray(this.visit((NameNode) children.get(0)));
            if (children.get(1) instanceof IndexNode) {
                Expression exp = this.visit((IndexNode) children.get(1));
                if (exp != null)
                    arrayAccess.setIndex(exp);
            }
            return arrayAccess;
        }
        if (children.size() == 4) {
            ArrayAccess arrayAccess = asn.newArrayAccess();
            arrayAccess.setSourceRange(node.getPos(), node.getLength());
            if (children.get(0) instanceof NameNode) {
                NameNode new_node = new NameNode(children.get(0));
                for (int i = 0; i < 3; i++)
                    new_node.getChildren().add(children.get(i));
                Name n = this.visit(new_node);
                if (n != null)
                    arrayAccess.setArray(n);
            }
            if (children.get(3) instanceof IndexNode) {
                Expression exp = this.visit((IndexNode) children.get(3));
                if (exp != null)
                    arrayAccess.setIndex(exp);
            }
            return arrayAccess;
        } else return this.visit(node);
    }

    Expression visit(IndexNode node) {
        if (!node.getChildren().isEmpty()) {
            Tree child = node.getChildren().get(0);
            if (child instanceof ExprNode)
                return this.visit((ExprNode) child);
        }
        return null;
    }

    Type visitType(NameNode node) {
        List<Tree> children = node.getChildren();
        if (children.size() == 0) {
            Type t = asn.newSimpleType(asn.newSimpleName(capitalizeFirstLetter(node.getLabel())));
            t.setSourceRange(node.getPos(), node.getLength());
            return t;
        }
        if (children.size() > 1 && children.get(1) instanceof IndexNode) {
            if (!children.get(0).getLabel().isEmpty()) {
                SimpleType simpleType = asn.newSimpleType(asn.newSimpleName(capitalizeFirstLetter(children.get(0).getLabel())));
                simpleType.setSourceRange(node.getPos(), node.getLength());
                return asn.newArrayType(simpleType);
            }
        }
        if (children.size() > 1 && children.get(1) instanceof ArgumentListNode) {
            Type t = asn.newSimpleType(this.visit((NameNode) children.get(0)));
            t.setSourceRange(children.get(0).getPos(), children.get(0).getLength());
            ParameterizedType returnType = asn.newParameterizedType(t);
            returnType.setSourceRange(node.getPos(), node.getLength());
            for (Expression exp : this.visit((ArgumentListNode) children.get(1))) {
                if (exp instanceof SimpleName) {
                    Type tt = asn.newSimpleType((Name) exp);
                    tt.setSourceRange(exp.getStartPosition(), exp.getLength());
                    returnType.typeArguments().add(tt);
                }
            }
            return returnType;
        }
        if (children.size() > 1) {
            String res = "";
            for (Tree child : children) {
                if (child.getChildren().size() == 0)
                    res += child.getLabel();
                else if (child.getChildren().get(0) instanceof NameNode)
                    res += child.getChildren().get(0).getLabel();
            }
            try {
                Type t = asn.newSimpleType(asn.newName(res));
                t.setSourceRange(node.getPos(), node.getLength());
                return t;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    MethodInvocation visit(CallNode node) {
        MethodInvocation methodInvocation = asn.newMethodInvocation();
        methodInvocation.setSourceRange(node.getPos(), node.getLength());
        List<Tree> children = node.getChildren();
        if (children.size() > 0) {
            Tree nameNode = children.get(0);
            if (nameNode.getChildren().size() == 0) { // if we only have one method call like do(...)
                if (nameNode instanceof NameNode) {
                    methodInvocation.setName((SimpleName) this.visit((NameNode) nameNode));
                }
            } else if (nameNode.getChildren().size() == 3) { // we have something like obj.do()
                List<Tree> method_calls = nameNode.getChildren();
                if (method_calls.get(0) instanceof NameNode)
                    methodInvocation.setExpression(this.visit((NameNode) method_calls.get(0)));
                if (method_calls.get(2) instanceof NameNode) {
                    Name n = this.visit((NameNode) method_calls.get(2));
                    if (n != null && n.isSimpleName())
                        methodInvocation.setName((SimpleName) n);
                }
            }
        }
        if (children.size() > 1) {
            Tree argNode = children.get(1);
            if (argNode instanceof ArgumentListNode) {
                for (Expression exp : this.visit((ArgumentListNode) argNode)) {
                    if (exp != null)
                        methodInvocation.arguments().add(exp);
                }
            }
        }
        return methodInvocation;
    }

    Expression evaluateNode(Tree child) {
        if (child instanceof LinqNode)
            return this.visit((LinqNode) child);
        if (child instanceof LambdaNode)
            return this.visit((LambdaNode) child);
        if (child instanceof LiteralNode)
            return this.visit((LiteralNode) child);
        if (child instanceof NameNode)
            return this.visitNameSpecial((NameNode) child);
        if (child instanceof CallNode)
            return this.visit((CallNode) child);
        if (child instanceof TypeOfNode)
            return this.visit((TypeOfNode) child);
        if (child instanceof SizeOfNode)
            return this.visit((SizeOfNode) child);
        if (child instanceof TernaryNode)
            return this.visit((TernaryNode) child);
        if (child instanceof ExprNode)
            return this.visit((ExprNode) child);
        if (child instanceof DefaultNode) {
            MethodInvocation methodInvocation = asn.newMethodInvocation();
            methodInvocation.setSourceRange(child.getPos(), child.getLength());
            methodInvocation.setName(asn.newSimpleName("Default"));
            if (child.getChildren().size() > 0) {
                Tree argNode = child.getChildren().get(0);
                if (argNode instanceof ArgumentListNode) {
                    for (Expression exp : this.visit((ArgumentListNode) argNode)) {
                        if (exp != null)
                            methodInvocation.arguments().add(exp);
                    }
                }
            }
            return methodInvocation;
        }
        return null;
    }

    Expression visit(ExprNode node) {
        List<Tree> children = node.getChildren();
        if (children.size() == 1) {
            return this.evaluateNode(children.get(0));
        } else if (children.size() == 2) {
            if (Objects.equals(children.get(0).getLabel(), "new")) {// expression with new MyClass(...)
                ClassInstanceCreation classInstanceCreation = asn.newClassInstanceCreation();
                classInstanceCreation.setSourceRange(node.getPos(), node.getLength());
                if (children.get(1) instanceof CallNode) {
                    children = children.get(1).getChildren();
                    if (children.get(0) instanceof NameNode) { // Class name
                        Type class_type = this.visitType((NameNode) children.get(0));
                        if (class_type != null)
                            classInstanceCreation.setType(class_type);
                    }
                    if (children.get(1) instanceof ArgumentListNode) { // arguments
                        for (Expression exp : this.visit((ArgumentListNode) children.get(1))) {
                            if (exp != null)
                                classInstanceCreation.arguments().add(exp);
                        }
                    }
                }
                return classInstanceCreation;
            } else if (isPostfix(children.get(1))) { // a++ or a--
                PostfixExpression postfixExpression = asn.newPostfixExpression();
                postfixExpression.setSourceRange(node.getPos(), node.getLength());
                if (children.get(0) instanceof NameNode)
                    postfixExpression.setOperand(this.visit((NameNode) children.get(0)));
                if (children.get(1) instanceof OperatorNode)
                    postfixExpression.setOperator(this.visitPostfix((OperatorNode) children.get(1)));
                return postfixExpression;
            } else if (isPrefix(children.get(0))) { // !a
                PrefixExpression prefixExpression = asn.newPrefixExpression();
                prefixExpression.setSourceRange(node.getPos(), node.getLength());
                if (children.get(1) instanceof NameNode)
                    prefixExpression.setOperand(this.visit((NameNode) children.get(1)));
                if (children.get(0) instanceof OperatorNode)
                    prefixExpression.setOperator(this.visitPrefix((OperatorNode) children.get(0)));
                return prefixExpression;
            } else if (Objects.equals(children.get(0).getLabel(), "$") && children.get(1) instanceof LiteralNode) { // string with $ sign
                return this.visit((LiteralNode) children.get(1));
            } else if (children.get(0) instanceof OperatorNode && children.get(1) instanceof CallNode) { // await methodcall()
                return this.visit((CallNode) children.get(1));
            }
        } else if (children.size() > 2) { // expression with one or more operators a+b-c*d
            if (children.get(1) instanceof OperatorNode && isAssignment(children.get(1))) { // assignement
                Assignment assignment = asn.newAssignment();
                assignment.setSourceRange(node.getPos(), node.getLength());
                if (children.get(0) instanceof NameNode)
                    assignment.setLeftHandSide(this.visitNameSpecial((NameNode) children.get(0)));
                if (children.get(1) instanceof OperatorNode)
                    assignment.setOperator(this.visitAssig((OperatorNode) children.get(1)));
                // Since after the variable and the assign operator, we pretty much just have an Expr, we can just visit
                ExprNode copy_without_assig = createNewExprNode(node, 2);
                Expression exp = this.visit(copy_without_assig);
                if (exp != null)
                    assignment.setRightHandSide(exp);
                return assignment;
            }
            if (containsParenthesis(node)) {
                return this.visitExprNodeParenthesis(node);
            } else {
                InfixExpression infixExpression = asn.newInfixExpression();
                infixExpression.setSourceRange(node.getPos(), node.getLength());
                if (children.get(0) instanceof OperatorNode) {
                    if (isPrefix(children.get(0))) {// !(a == b)
                        PrefixExpression preExpression = asn.newPrefixExpression();
                        preExpression.setSourceRange(node.getPos(), node.getLength());
                        if (children.get(0) instanceof OperatorNode)
                            preExpression.setOperator(this.visitPrefix((OperatorNode) children.get(0)));
                        ExprNode copy_without_first_element = createNewExprNode(node, 1);
                        preExpression.setOperand(this.visit(copy_without_first_element));
                        return preExpression;
                    }
                } else {
                    Expression exp = this.evaluateNode(children.get(0));
                    if (exp != null)
                        infixExpression.setLeftOperand(exp);
                }
                if (children.get(1) instanceof OperatorNode) {
                    InfixExpression.Operator op = this.visit((OperatorNode) children.get(1));
                    if (op != null)
                        infixExpression.setOperator(op);
                }
                // Visit the rest minus the first two elements
                ExprNode copy_without_first_two_elements = createNewExprNode(node, 2);
                try {
                    infixExpression.setRightOperand(this.visit(copy_without_first_two_elements));
                } catch (Exception e) {
                    //System.out.print(e);
                }
                return infixExpression;
            }
        }
        return asn.newInfixExpression();
    }

    Expression visitExprNodeParenthesis(Tree node) {
        Stack<Object> operands = new Stack<>();
        Stack<Tree> operators = new Stack<>();
        List<Tree> children = node.getChildren();
        for (int i = 0; i < children.size() - 1; i++) {
            Tree child = children.get(i);
            if (child instanceof OperatorNode) {
                if (Objects.equals(child.getLabel(), "("))
                    operators.push(child);

                else if (Objects.equals(child.getLabel(), ")")) {
                    while (!Objects.equals(operators.peek().getLabel(), "(") && operands.size() >= 2) {
                        operands.push(performOperation(operators.pop(), operands.pop(), operands.pop(), true));
                    }
                    if (!operands.isEmpty() && operands.peek() instanceof NameNode && node.getChildren().size() >= 4) { // Cast expression
                        CastExpression castExpression = asn.newCastExpression();
                        castExpression.setSourceRange(node.getPos(), node.getLength());
                        NameNode n = (NameNode) operands.pop();
                        Type t = asn.newSimpleType(this.visit(n));
                        t.setSourceRange(n.getPos(), n.getLength());
                        castExpression.setType(t);
                        if (!(children.get(i + 1) instanceof OperatorNode)) {
                            Expression e = this.evaluateNode(children.remove(i + 1));
                            if (e != null)
                                castExpression.setExpression(e);
                        }
                        operands.push(castExpression);
                    }
                    operators.pop(); // Pop the opening parenthesis
                } else
                    operators.push(child);
            } else
                operands.push(child);
        }
        while (!operators.isEmpty() && operands.size() >= 2) {
            operands.push(performOperation(operators.pop(), operands.pop(), operands.pop(), false));
        }
        if (!operands.isEmpty() && operands.peek() instanceof NameNode)
            return this.visit((NameNode) operands.pop());
        if (!operands.isEmpty() && operands.peek() instanceof CastExpression && operands.size() == 2) {
            CastExpression e = (CastExpression) operands.pop();
            if (operands.peek() instanceof Tree)
                e.setExpression(this.evaluateNode((Tree) operands.pop()));
            else if (operands.peek() instanceof Expression)
                e.setExpression((Expression) operands.peek());
            return e;
        }
        if (!operands.isEmpty() && operands.peek() instanceof Expression)
            return (Expression) operands.pop();
        return asn.newParenthesizedExpression();
    }

    Expression performOperation(Tree operator, Object operand2, Object operand1, Boolean is_parenthesis) {
        InfixExpression innerInfixExpression = asn.newInfixExpression();
        InfixExpression.Operator op = this.visit((OperatorNode) operator);
        if (op != null)
            innerInfixExpression.setOperator(op);
        if (operand1 instanceof Tree) {
            Expression exp = this.evaluateNode((Tree) operand1);
            if (exp != null)
                innerInfixExpression.setLeftOperand(exp);
        } else
            innerInfixExpression.setLeftOperand((Expression) operand1);

        if (operand2 instanceof Tree) {
            Expression exp = this.evaluateNode((Tree) operand2);
            if (exp != null)
                innerInfixExpression.setRightOperand(exp);
        } else
            innerInfixExpression.setRightOperand((Expression) operand2);

        if (operand1 instanceof Tree && operand2 instanceof Tree) {
            int first_pos = ((Tree) operand1).getPos();
            int length = ((Tree) operand2).getEndPos() - first_pos + 1;
            innerInfixExpression.setSourceRange(first_pos, length);
        }
        if (is_parenthesis) {
            ParenthesizedExpression parenthesis = asn.newParenthesizedExpression();
            if (innerInfixExpression.getStartPosition() > 0)
                parenthesis.setSourceRange(innerInfixExpression.getStartPosition() - 1, innerInfixExpression.getLength() + 2);
            parenthesis.setExpression(innerInfixExpression);
            return parenthesis;
        }
        return innerInfixExpression;
    }

    Modifier visit(SpecifierNode node) {
        Modifier.ModifierKeyword modif = null;
        if (Objects.equals(node.getLabel(), SrcMLNodeSpecifier.PUBLIC))
            modif = Modifier.ModifierKeyword.PUBLIC_KEYWORD;
        if (Objects.equals(node.getLabel(), SrcMLNodeSpecifier.STATIC))
            modif = Modifier.ModifierKeyword.STATIC_KEYWORD;
        if (Objects.equals(node.getLabel(), SrcMLNodeSpecifier.CONST))
            modif = Modifier.ModifierKeyword.FINAL_KEYWORD;
        if (Objects.equals(node.getLabel(), SrcMLNodeSpecifier.EXPLICIT))
            modif = null;
        if (Objects.equals(node.getLabel(), SrcMLNodeSpecifier.IN))
            modif = null;
        if (Objects.equals(node.getLabel(), SrcMLNodeSpecifier.ASYNC))
            modif = null;
        if (Objects.equals(node.getLabel(), SrcMLNodeSpecifier.EXTERN))
            modif = null;
        if (Objects.equals(node.getLabel(), SrcMLNodeSpecifier.IMPLICIT))
            modif = null;
        if (Objects.equals(node.getLabel(), SrcMLNodeSpecifier.INTERNAL))
            modif = null;
        if (Objects.equals(node.getLabel(), SrcMLNodeSpecifier.OUT))
            modif = null;
        if (Objects.equals(node.getLabel(), SrcMLNodeSpecifier.OVERRIDE))
            modif = null;
        if (Objects.equals(node.getLabel(), SrcMLNodeSpecifier.PARAMS))
            modif = null;
        if (Objects.equals(node.getLabel(), SrcMLNodeSpecifier.PARAMS))
            modif = null;
        if (Objects.equals(node.getLabel(), SrcMLNodeSpecifier.PARTIAL))
            modif = null;
        if (Objects.equals(node.getLabel(), SrcMLNodeSpecifier.PRIVATE))
            modif = Modifier.ModifierKeyword.PRIVATE_KEYWORD;
        if (Objects.equals(node.getLabel(), SrcMLNodeSpecifier.PROTECTED))
            modif = Modifier.ModifierKeyword.PROTECTED_KEYWORD;
        if (Objects.equals(node.getLabel(), SrcMLNodeSpecifier.READONLY))
            modif = Modifier.ModifierKeyword.FINAL_KEYWORD;
        if (Objects.equals(node.getLabel(), SrcMLNodeSpecifier.REF))
            modif = null;
        if (Objects.equals(node.getLabel(), SrcMLNodeSpecifier.SEALED))
            modif = Modifier.ModifierKeyword.FINAL_KEYWORD;
        if (Objects.equals(node.getLabel(), SrcMLNodeSpecifier.STACKALLOC))
            modif = null;
        if (Objects.equals(node.getLabel(), SrcMLNodeSpecifier.VIRTUAL))
            modif = null;
        if (Objects.equals(node.getLabel(), SrcMLNodeSpecifier.VOLATILE))
            modif = Modifier.ModifierKeyword.VOLATILE_KEYWORD;
        if (Objects.equals(node.getLabel(), SrcMLNodeSpecifier.YIELD))
            modif = null;
        if (Objects.equals(node.getLabel(), SrcMLNodeSpecifier.THIS))
            modif = null;
        if (Objects.equals(node.getLabel(), SrcMLNodeSpecifier.NEW))
            modif = null;
        if (Objects.equals(node.getLabel(), SrcMLNodeSpecifier.ABSTRACT))
            modif = Modifier.ModifierKeyword.ABSTRACT_KEYWORD;

        if (modif != null) {
            Modifier m = asn.newModifier(modif);
            m.setSourceRange(node.getPos(), node.getLength());
            return m;
        }
        return null;
    }

    InfixExpression.Operator visit(OperatorNode node) {
        if (Objects.equals(node.getLabel(), "*")) {
            return InfixExpression.Operator.TIMES;
        } else if (Objects.equals(node.getLabel(), "+")) {
            return InfixExpression.Operator.PLUS;
        } else if (Objects.equals(node.getLabel(), "-")) {
            return InfixExpression.Operator.MINUS;
        } else if (Objects.equals(node.getLabel(), "/")) {
            return InfixExpression.Operator.DIVIDE;
        } else if (Objects.equals(node.getLabel(), "%")) {
            return InfixExpression.Operator.REMAINDER;
        } else if (Objects.equals(node.getLabel(), ".")) {
            return null; // do nthg
        } else if (Objects.equals(node.getLabel(), "new")) {
            return null; // do nthg
        } else if (Objects.equals(node.getLabel(), "==")) {
            return InfixExpression.Operator.EQUALS;
        } else if (Objects.equals(node.getLabel(), "!=")) {
            return InfixExpression.Operator.NOT_EQUALS;
        } else if (Objects.equals(node.getLabel(), "(")) {
            return null; // TODO
        } else if (Objects.equals(node.getLabel(), ")")) {
            return null; // TODO
        } else if (Objects.equals(node.getLabel(), ">")) {
            return InfixExpression.Operator.GREATER;
        } else if (Objects.equals(node.getLabel(), "<")) {
            return InfixExpression.Operator.LESS;
        } else if (Objects.equals(node.getLabel(), ">=")) {
            return InfixExpression.Operator.GREATER_EQUALS;
        } else if (Objects.equals(node.getLabel(), "<=")) {
            return InfixExpression.Operator.LESS_EQUALS;
        } else if (Objects.equals(node.getLabel(), "&")) {
            return InfixExpression.Operator.AND;
        } else if (Objects.equals(node.getLabel(), "<<")) {
            return InfixExpression.Operator.LEFT_SHIFT;
        } else if (Objects.equals(node.getLabel(), ">>")) {
            return InfixExpression.Operator.RIGHT_SHIFT_SIGNED;
        } else if (Objects.equals(node.getLabel(), "|")) {
            return InfixExpression.Operator.OR;
        } else if (Objects.equals(node.getLabel(), ">>>")) {
            return InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED;
        } else if (Objects.equals(node.getLabel(), "^")) {
            return InfixExpression.Operator.XOR;
        } else if (Objects.equals(node.getLabel(), "&&")) {
            return InfixExpression.Operator.CONDITIONAL_AND;
        } else if (Objects.equals(node.getLabel(), "||")) {
            return InfixExpression.Operator.CONDITIONAL_OR;
        } else
            return null;
    }

    Assignment.Operator visitAssig(OperatorNode node) {
        if (Objects.equals(node.getLabel(), "=")) {
            return Assignment.Operator.ASSIGN;
        } else if (Objects.equals(node.getLabel(), "*=")) {
            return Assignment.Operator.TIMES_ASSIGN;
        } else if (Objects.equals(node.getLabel(), "-=")) {
            return Assignment.Operator.MINUS_ASSIGN;
        } else if (Objects.equals(node.getLabel(), "+=")) {
            return Assignment.Operator.PLUS_ASSIGN;
        } else if (Objects.equals(node.getLabel(), "/=")) {
            return Assignment.Operator.DIVIDE_ASSIGN;
        } else if (Objects.equals(node.getLabel(), "%=")) {
            return Assignment.Operator.REMAINDER_ASSIGN;
        } else if (Objects.equals(node.getLabel(), "&=")) {
            return Assignment.Operator.BIT_AND_ASSIGN;
        } else if (Objects.equals(node.getLabel(), "^=")) {
            return Assignment.Operator.BIT_XOR_ASSIGN;
        } else if (Objects.equals(node.getLabel(), "|=")) {
            return Assignment.Operator.BIT_OR_ASSIGN;
        } else if (Objects.equals(node.getLabel(), "<<=")) {
            return Assignment.Operator.LEFT_SHIFT_ASSIGN;
        } else if (Objects.equals(node.getLabel(), ">>=")) {
            return Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN;
        } else if (Objects.equals(node.getLabel(), ">>>=")) {
            return Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN;
        } else
            return null;
    }

    PostfixExpression.Operator visitPostfix(OperatorNode node) {
        if (Objects.equals(node.getLabel(), "--")) {
            return PostfixExpression.Operator.DECREMENT;
        } else if (Objects.equals(node.getLabel(), "++")) {
            return PostfixExpression.Operator.INCREMENT;
        } else
            return null;
    }

    PrefixExpression.Operator visitPrefix(OperatorNode node) {
        if (Objects.equals(node.getLabel(), "--")) {
            return PrefixExpression.Operator.DECREMENT;
        } else if (Objects.equals(node.getLabel(), "++")) {
            return PrefixExpression.Operator.INCREMENT;
        } else if (Objects.equals(node.getLabel(), "!")) {
            return PrefixExpression.Operator.NOT;
        } else if (Objects.equals(node.getLabel(), "-")) {
            return PrefixExpression.Operator.MINUS;
        } else if (Objects.equals(node.getLabel(), "+")) {
            return PrefixExpression.Operator.PLUS;
        } else if (Objects.equals(node.getLabel(), "~")) {
            return PrefixExpression.Operator.COMPLEMENT;
        } else
            return null;
    }

    List<Expression> visit(ArgumentListNode node) {
        List<Expression> results = new ArrayList<>();
        List<Tree> args = node.getChildren();
        for (Tree arg : args) {
            if (arg instanceof ArgumentNode)
                results.add(this.visit((ArgumentNode) arg));
        }
        return results;
    }

    Expression visit(ArgumentNode node) {
        for (Tree child : node.getChildren()) {
            if (child instanceof ExprNode)
                return this.visit((ExprNode) child);
            if (child instanceof NameNode)
                return this.visit((NameNode) child);
        }
        return null;
    }

    MethodDeclaration visit(FunctionDeclNode node) {
        MethodDeclaration methoddec = asn.newMethodDeclaration();
        methoddec.setSourceRange(node.getPos(), node.getLength());
        for (Tree child : node.getChildren()) {
            // function name
            if (child instanceof NameNode)
                methoddec.setName((SimpleName) this.visit((NameNode) child));
            else if (child instanceof TypeNode) {
                for (Tree type_child : child.getChildren()) {
                    // function specifier
                    if (type_child instanceof SpecifierNode) {
                        Modifier m = this.visit((SpecifierNode) type_child);
                        if (m != null)
                            methoddec.modifiers().add(m);
                    }
                    // function return type
                    if (type_child instanceof NameNode)
                        methoddec.setReturnType2(this.visitType((NameNode) type_child));
                }
            }
            // function params
            if (child instanceof ParameterListNode) {
                for (VariableDeclaration vdec : this.visit((ParameterListNode) child))
                    methoddec.parameters().add(vdec);
            }
        }
        return methoddec;
    }

    MethodDeclaration visit(FunctionNode node) {
        MethodDeclaration methoddec = asn.newMethodDeclaration();
        methoddec.setSourceRange(node.getPos(), node.getLength());
        List<Tree> children = node.getChildren();
        for (Tree child : children) {
            // function name
            if (child instanceof NameNode) {
                Name n = this.visit((NameNode) child);
                if (n != null && n.isSimpleName())
                    methoddec.setName((SimpleName) n);
                if (child.getChildren().size() == 2) { // if FunctionName<A>
                    Tree l = child.getChildren().get(1);
                    if (l instanceof ArgumentListNode) {
                        for (Expression exp : this.visit((ArgumentListNode) l)) {
                            TypeParameter typeParameter = asn.newTypeParameter();
                            typeParameter.setName((SimpleName) exp);
                            typeParameter.setSourceRange(exp.getStartPosition(), exp.getLength());
                            methoddec.typeParameters().add(typeParameter);
                        }
                    }
                }
            }

            if (child instanceof TypeNode) {
                for (Tree type_child : child.getChildren()) {
                    // function specifier
                    if (type_child instanceof SpecifierNode) {
                        Modifier m = this.visit((SpecifierNode) type_child);
                        if (m != null)
                            methoddec.modifiers().add(m);
                    }
                    // function return type
                    if (type_child instanceof NameNode)
                        methoddec.setReturnType2(this.visitType((NameNode) type_child));
                }
            }
            // function params
            if (child instanceof ParameterListNode) {
                for (VariableDeclaration vdec : this.visit((ParameterListNode) child))
                    methoddec.parameters().add(vdec);
            }
            // function body
            if (child instanceof BlockNode) {
                methoddec.setBody((Block) this.visit((BlockNode) child));
            }
            if (child instanceof AttributeNode) {
                for (NormalAnnotation an : this.visit((AttributeNode) child))
                    methoddec.modifiers().add(an);
            }
        }
        return methoddec;
    }

    List<SingleVariableDeclaration> visit(ParameterListNode node) {
        List<SingleVariableDeclaration> results = new ArrayList<>();
        List<Tree> params = node.getChildren();
        for (Tree param : params) {
            if (param instanceof ParameterNode)
                results.add(this.visit((ParameterNode) param));
        }
        return results;
    }

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
            parameter.setSourceRange(node.getPos(), node.getLength());
        }
        return parameter;
    }

    Object visit(TypeNode node) {
        if (!node.getChildren().isEmpty()) {
            Tree node_type = node.getChildren().get(0);
            if (node_type.getChildren().size() > 0) {// an array type type->name->[name:..,index:..] or Foo<T>
                Tree param_type = node_type.getChildren().get(0);
                if (node_type.getChildren().size() > 1) {
                    Tree arr_index = node_type.getChildren().get(1);
                    if (arr_index instanceof IndexNode) { // an array type
                        Type type = this.visitType((NameNode) param_type);
                        ArrayType arrayType = asn.newArrayType(type);
                        arrayType.setSourceRange(type.getStartPosition(), type.getLength());
                        return arrayType;
                    }
                    if (arr_index instanceof ArgumentListNode)  // a Foo<T>
                        return this.visitType((NameNode) node_type);
                    if (node_type instanceof NameNode)
                        return this.visitType((NameNode) node_type);
                }
            }
        }
        if (node.getParents().get(3) instanceof ClassNode) { // field in class
            VariableDeclarationFragment variableDeclarationFragment = asn.newVariableDeclarationFragment();
            for (Tree namenode : node.getParents().get(0).getChildren()) {
                if (namenode instanceof NameNode) {
                    Name n = this.visit((NameNode) namenode);
                    if (n != null && n.isSimpleName())
                        variableDeclarationFragment.setName((SimpleName) n);
                    variableDeclarationFragment.setSourceRange(namenode.getPos(), namenode.getLength());
                    break;
                }
            }
            FieldDeclaration fieldDeclaration = asn.newFieldDeclaration(variableDeclarationFragment);
            fieldDeclaration.setSourceRange(node.getPos(), node.getLength());
            for (Tree child : node.getChildren()) {
                if (child instanceof SpecifierNode) {
                    Modifier m = this.visit((SpecifierNode) child);
                    if (m != null)
                        fieldDeclaration.modifiers().add(m);
                } else if (child instanceof NameNode) {
                    Type t = this.visitType((NameNode) child);
                    if (t != null)
                        fieldDeclaration.setType(t);
                }
            }
            return fieldDeclaration;
        }
        for (Tree child : node.getChildren()) {
            if (child instanceof NameNode)  // type->name:...
                return this.visitType((NameNode) child);
        }
        return null;
    }

    Object visit(BlockNode node) {
        if (!node.getChildren().isEmpty()) {
            Tree child = node.getChildren().get(0);
            if (child instanceof BlockContentNode) {
                Block b = asn.newBlock();
                b.setSourceRange(node.getPos(), node.getLength());
                for (Object obj : this.visit((BlockContentNode) child)) {
                    if (obj != null)
                        b.statements().add(obj);
                }
                return b;
            }
            if (child instanceof ContinueNode) {
                Block b = asn.newBlock();
                b.setSourceRange(node.getPos(), node.getLength());
                b.statements().add(this.visit((ContinueNode) child));
                return b;
            }
        }
        if (node.getParent() instanceof NamespaceNode) {
            List<Object> class_and_interface_nodes = new ArrayList<>();
            for (Tree class_or_interface_node : node.getChildren()) {
                if (class_or_interface_node instanceof ClassNode)
                    class_and_interface_nodes.add(this.visit((ClassNode) class_or_interface_node));
                if (class_or_interface_node instanceof InterfaceNode)
                    class_and_interface_nodes.add(this.visit((InterfaceNode) class_or_interface_node));
                if (class_or_interface_node instanceof EnumNode)
                    class_and_interface_nodes.add(this.visit((EnumNode) class_or_interface_node));
                if (class_or_interface_node instanceof StructNode)
                    class_and_interface_nodes.add(this.visit((StructNode) class_or_interface_node));
            }
            return class_and_interface_nodes;
        }
        List<Object> results = new ArrayList<>();
        for (Tree child2 : node.getChildren()) { // in a class or interface
            if (child2 instanceof FunctionNode)
                results.add(this.visit((FunctionNode) child2));
            else if (child2 instanceof FunctionDeclNode)
                results.add(this.visit((FunctionDeclNode) child2));
            else if (child2 instanceof DeclStmtNode)  // field, here we treat this in a special way
                results.add(this.visit((DeclStmtNode) child2));
            else if (child2 instanceof ExprStmtNode) { // ex: public event SampleEventHandler SampleEvent;
                EventNode en = new EventNode(child2);
                en.setChildren(child2.getChildren());
                en.setParent(child2.getParent());
                results.add(this.visit(en));
            } else if (child2 instanceof PropertyNode) {
                for (Object result : this.visit((PropertyNode) child2))
                    results.add(result);
            } else if (child2 instanceof ConstructorNode)
                results.add(this.visit((ConstructorNode) child2));
            else if (child2 instanceof DestrctorNode)
                results.add(this.visit((DestrctorNode) child2));
        }
        return results;
    }

    Object visit(DeclStmtNode node) {
        if (!node.getChildren().isEmpty()) {
            Tree declaration = node.getChildren().get(0);
            if (declaration instanceof DeclNode) {
                if (node.getParents().get(1) instanceof ClassNode || node.getParents().get(1) instanceof InterfaceNode) {
                    Tree field = declaration.getChildren().get(0); // don't pass by the declaration node, go straight to type
                    if (field instanceof TypeNode) {
                        return this.visit((TypeNode) field);
                    }
                }
                ReturnPair<VariableDeclarationFragment, Object> k = this.visit((DeclNode) declaration);
                VariableDeclarationStatement variableDeclaration;
                Object t = k.getSecond();
                if (t != null) {
                    if (t.getClass().getName().contains("FieldDeclaration")) {
                        FieldDeclaration fd = (FieldDeclaration) t;
                        fd.setSourceRange(node.getPos(), node.getLength());
                        //fd.fragments().add(k.getFirst());
                        return fd;
                    } else {
                        variableDeclaration = asn.newVariableDeclarationStatement(k.getFirst());
                        variableDeclaration.setSourceRange(node.getPos(), node.getLength());
                        variableDeclaration.setType((Type) t);
                        return variableDeclaration;
                    }
                }

            }
        }
        return null;
    }

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

    Expression visit(InitNode node) {
        if (!node.getChildren().isEmpty()) {
            Tree child = node.getChildren().get(0);
            if (child instanceof ExprNode)
                return this.visit((ExprNode) child);
            if (child instanceof DeclNode) {
                ReturnPair<VariableDeclarationFragment, Object> k = this.visit((DeclNode) child);
                VariableDeclarationExpression initExpression = asn.newVariableDeclarationExpression(k.getFirst());
                initExpression.setSourceRange(node.getPos(), node.getLength());
                initExpression.setType((Type) k.getSecond());
                return initExpression;
            }
        }
        return null;
    }

    ExpressionStatement visit(ExprStmtNode node) {
        if (!node.getChildren().isEmpty() && node.getChildren().get(0) instanceof ExprNode) {
            Expression exp = this.visit((ExprNode) node.getChildren().get(0));
            if (exp != null) {
                ExpressionStatement exp_st = asn.newExpressionStatement(exp);
                exp_st.setSourceRange(node.getPos(), node.getLength());
                return exp_st;
            }
        }
        return null;
    }

    ReturnPair<PackageDeclaration, List<Object>> visit(NamespaceNode node) {
        PackageDeclaration packageDeclaration = asn.newPackageDeclaration();

        List<Object> classes = new ArrayList<>();
        for (Tree child : node.getChildren()) {
            if (child instanceof NameNode) {
                packageDeclaration.setName(this.visit((NameNode) child));
                packageDeclaration.setSourceRange(node.getPos(), child.getLength() + (child.getPos() - node.getPos()));
            }
            if (child instanceof BlockNode)
                classes = (List<Object>) this.visit((BlockNode) child);
        }
        return new ReturnPair<>(packageDeclaration, classes);
    }

    public CompilationUnit visit(UnitNode node) {
        CompilationUnit compilationUnit = asn.newCompilationUnit();
        compilationUnit.setSourceRange(node.getPos(), node.getLength());
        for (Tree child : node.getChildren()) {
            if (child instanceof UsingNode) {
                compilationUnit.imports().add(this.visit((UsingNode) child));
            } else if (child instanceof NamespaceNode) {
                ReturnPair<PackageDeclaration, List<Object>> results = this.visit((NamespaceNode) child);
                compilationUnit.setPackage(results.getFirst());
                for (Object t : results.getSecond()) {
                    compilationUnit.types().add(t);
                }
            } else if (child instanceof ClassNode) {
                compilationUnit.types().add(this.visit((ClassNode) child));
            } else if (child instanceof InterfaceNode) {
                compilationUnit.types().add(this.visit((InterfaceNode) child));
            } else if (child instanceof DelegateNode) {
                compilationUnit.types().add(this.visit((DelegateNode) child));
            } else if (child instanceof StructNode) {
                compilationUnit.types().add(this.visit((StructNode) child));
            } else if (child instanceof EnumNode)
                compilationUnit.types().add(this.visit((EnumNode) child));
        }
        return compilationUnit;
    }

    TypeDeclaration visit(ClassNode node) {
        List<Tree> children = node.getChildren();
        TypeDeclaration classDeclaration = asn.newTypeDeclaration();
        try {
            classDeclaration.setSourceRange(node.getPos(), node.getLength());
        } catch (Exception e) {
        }
        List<TypeParameter> type_params = new ArrayList<>();
        for (Tree child : children) {
            if (child instanceof SpecifierNode) {
                Modifier m = this.visit((SpecifierNode) child);
                if (m != null)
                    classDeclaration.modifiers().add(m);

            } else if (child instanceof NameNode) {
                Name n = this.visit((NameNode) child);
                if (n!= null && n.isSimpleName())
                    classDeclaration.setName((SimpleName) n);
                if (child.getChildren().size() == 2) { // if ClassName<A>
                    Tree l = child.getChildren().get(1);
                    if (l instanceof ArgumentListNode) {
                        for (Expression exp : this.visit((ArgumentListNode) l)) {
                            TypeParameter typeParameter = asn.newTypeParameter();
                            typeParameter.setSourceRange(exp.getStartPosition(), exp.getLength());
                            typeParameter.setName((SimpleName) exp);
                            type_params.add(typeParameter);
                        }
                    }
                }
            } else if (child instanceof WhereNode) {
                List<Tree> where_children = child.getChildren();
                for (TypeParameter typeparam : type_params) {
                    if (typeparam.getName().toString().equals(where_children.get(0).getLabel())) {
                        if (where_children.get(1) instanceof ConstraintNode) {
                            SimpleType typeBound = this.visit((ConstraintNode) where_children.get(1));
                            if (typeBound != null)
                                typeparam.typeBounds().add(typeBound);
                        }
                    }
                }
            } else if (child instanceof SuperListNode) {
                List<Type> types = this.visit((SuperListNode) child);
                if (types.size() > 1) {
                    for (Type t : types)
                        classDeclaration.superInterfaceTypes().add(t);
                } else if (types.size() == 1)
                    classDeclaration.setSuperclassType(types.get(0));
            } else if (child instanceof BlockNode) {
                Object obj = this.visit((BlockNode) child);
                if (obj instanceof List) {
                    List<Object> lm = (List<Object>) obj;
                    for (Object b : lm) {
                        try {
                            classDeclaration.bodyDeclarations().add(b);
                        } catch (Exception e) {
                        }
                    }
                }
            } else if (child instanceof AttributeNode) {
                for (NormalAnnotation an : this.visit((AttributeNode) child))
                    classDeclaration.modifiers().add(an);
            }
        }
        for (TypeParameter tp : type_params) {
            classDeclaration.typeParameters().add(tp);
        }
        return classDeclaration;
    }

    IfStatement visit(IfNode node) {
        IfStatement ifStatement = asn.newIfStatement();
        ifStatement.setSourceRange(node.getPos(), node.getLength());
        for (Tree child : node.getChildren()) {
            if (child instanceof ConditionNode) {
                Expression exp = this.visit((ConditionNode) child);
                if (exp != null)
                    ifStatement.setExpression(exp);
            }
            if (child instanceof BlockNode) {
                Object obj = this.visit((BlockNode) child);
                if (obj instanceof Block) {
                    Block exp = (Block) obj;
                    ifStatement.setThenStatement(exp);
                }
            }
        }
        return ifStatement;
    }

    Block visit(ElseNode node) {
        if (!node.getChildren().isEmpty()) {
            Tree child = node.getChildren().get(0);
            if (child instanceof BlockNode)
                return (Block) this.visit((BlockNode) child);
        }
        return asn.newBlock();
    }

    IfStatement visit(IfStmtNode node) {
        List<Tree> children = node.getChildren();
        IfStatement ifStatement = asn.newIfStatement();
        ifStatement.setSourceRange(node.getPos(), node.getLength());
        if (children.size() < 3) { // if or if-else
            for (Tree child : children) {
                if (child instanceof IfNode) {
                    ifStatement = this.visit((IfNode) child);
                } else if (child instanceof ElseNode) {
                    ifStatement.setElseStatement(this.visit((ElseNode) child));
                }
            }
            return ifStatement;
        }
        // if elseif elseif.... else
        IfStatement[] statements = new IfStatement[children.size()];
        if (children.get(0) instanceof IfNode)
            statements[0] = this.visit((IfNode) children.get(0));
        int index = 1; // different index because there could be children that are comments, we want to ignore those
        for (int i = 1; i < children.size(); i++) {
            Tree child = children.get(i);
            if (child instanceof IfNode) {
                statements[index] = this.visit((IfNode) child);
                statements[index - 1].setElseStatement(statements[i]);
                index++;
            } else if (child instanceof ElseNode)
                statements[index - 1].setElseStatement(this.visit((ElseNode) child));
        }
        return statements[0];
    }

    ReturnStatement visit(ReturnNode node) {
        ReturnStatement returnStatement = asn.newReturnStatement();
        returnStatement.setSourceRange(node.getPos(), node.getLength());
        for (Tree child : node.getChildren()) {
            if (child instanceof ExprNode)
                returnStatement.setExpression(this.visit((ExprNode) child));
        }
        return returnStatement;
    }

    WhileStatement visit(WhileNode node) {
        WhileStatement whileStatement = asn.newWhileStatement();
        whileStatement.setSourceRange(node.getPos(), node.getLength());
        for (Tree child : node.getChildren()) {
            if (child instanceof ConditionNode) {
                Expression exp = this.visit((ConditionNode) child);
                if (exp != null)
                    whileStatement.setExpression(exp);
            }
            if (child instanceof BlockNode) {
                Block exp = (Block) this.visit((BlockNode) child);
                whileStatement.setBody(exp);
            }
        }
        return whileStatement;
    }

    Expression visit(ConditionNode node) {
        if (!node.getChildren().isEmpty()) {
            Tree expr = node.getChildren().get(0);
            if (expr instanceof ExprNode) {
                return this.visit((ExprNode) expr);
            }
        }
        return null;
    }

    List<Type> visit(SuperListNode node) {
        List<Type> supers = new ArrayList<>();
        for (Tree child : node.getChildren()) {
            if (child instanceof SuperNode)
                supers.add(this.visit((SuperNode) child));
        }
        return supers;
    }

    Type visit(SuperNode node) {
        if (node.getChildren().size() > 0) {
            Tree name = node.getChildren().get(0);
            if (name instanceof NameNode)
                return this.visitType((NameNode) name);
        }
        return null;
    }

    ForStatement visit(ForNode node) {
        ForStatement forStatement = asn.newForStatement();
        forStatement.setSourceRange(node.getPos(), node.getLength());
        for (Tree child : node.getChildren()) {
            if (child instanceof ControlNode)
                forStatement = this.visit((ControlNode) child);
            if (child instanceof BlockNode)
                forStatement.setBody((Block) this.visit((BlockNode) child));
        }
        return forStatement;
    }

    ForStatement visit(ControlNode node) {
        ForStatement forStatement = asn.newForStatement();
        forStatement.setSourceRange(node.getParent().getPos(), node.getParent().getLength()); // we want the ForNode
        for (Tree child : node.getChildren()) {
            if (child instanceof InitNode) {
                Expression exp = this.visit((InitNode) child);
                if (exp != null)
                    forStatement.initializers().add(exp);
            } else if (child instanceof ConditionNode) {
                Expression exp = this.visit((ConditionNode) child);
                if (exp != null)
                    forStatement.setExpression(exp);
            } else if (child instanceof IncrNode) {
                Expression exp = this.visit((IncrNode) child);
                if (exp != null)
                    forStatement.updaters().add(exp);
            }
        }
        return forStatement;
    }

    EnhancedForStatement visitEnhanced(ControlNode node) {
        // Will be done manually
        EnhancedForStatement foreachStatement = asn.newEnhancedForStatement();
        foreachStatement.setSourceRange(node.getParent().getPos(), node.getParent().getLength()); // We want the ForeachNode
        if (node.getChildren().size() > 0) {
            Tree child = node.getChildren().get(0);
            SingleVariableDeclaration variableDeclaration = asn.newSingleVariableDeclaration();
            if (child instanceof InitNode) {
                child = child.getChildren().get(0);
                if (child instanceof DeclNode) {
                    for (Tree c : child.getChildren()) {
                        if (c instanceof NameNode) {
                            Name n = this.visit((NameNode) c);
                            if (n != null && n.isSimpleName())
                                variableDeclaration.setName((SimpleName) n);
                        } else if (c instanceof TypeNode)
                            variableDeclaration.setType((Type) this.visit((TypeNode) c));
                        else if (c instanceof RangeNode)
                            foreachStatement.setExpression(this.visit((RangeNode) c));
                    }
                    foreachStatement.setParameter(variableDeclaration);
                }
            }
        }
        return foreachStatement;
    }

    Expression visit(IncrNode node) {
        List<Tree> children = node.getChildren();
        for (Tree child : children) {
            if (child instanceof ExprNode)
                return this.visit((ExprNode) child);
        }
        if (children.size() > 0)
            return this.visit(createNewExprNode(node, 0));
        return null;
    }

    EnhancedForStatement visit(ForeachNode node) {
        EnhancedForStatement foreachStatement = asn.newEnhancedForStatement();
        foreachStatement.setSourceRange(node.getPos(), node.getLength());
        for (Tree child : node.getChildren()) {
            if (child instanceof ControlNode)
                foreachStatement = this.visitEnhanced((ControlNode) child);
            if (child instanceof BlockNode) {
                Object obj = this.visit((BlockNode) child);
                if (obj instanceof Block)
                    foreachStatement.setBody((Block) obj);
            }
        }
        return foreachStatement;
    }

    Expression visit(RangeNode node) {
        if (!node.getChildren().isEmpty()) {
            Tree child = node.getChildren().get(0);
            if (child instanceof ExprNode)
                return this.visit((ExprNode) child);
        }
        return null;
    }

    SwitchStatement visit(SwitchNode node) {
        SwitchStatement switchStatement = asn.newSwitchStatement();
        switchStatement.setSourceRange(node.getPos(), node.getLength());
        for (Tree child : node.getChildren()) {
            if (child instanceof ConditionNode)
                switchStatement.setExpression(this.visit((ConditionNode) child));
            if (child instanceof BlockNode) { // Will develop this here
                child = child.getChildren().get(0);
                if (child instanceof BlockContentNode) {
                    List<Tree> c = child.getChildren();
                    Block casebody = asn.newBlock();
                    casebody.setSourceRange(child.getPos(), child.getLength());
                    for (int i = 0; i < c.size(); i++) {
                        Tree statement = c.get(i);
                        if (statement instanceof CaseNode) {
                            if (casebody.statements().size() > 0) {
                                switchStatement.statements().add(casebody);
                                casebody = asn.newBlock();
                                casebody.setSourceRange(statement.getPos(), statement.getLength());
                            }
                            switchStatement.statements().add(this.visit((CaseNode) statement));
                        } else if (statement instanceof BreakNode) {
                            BreakStatement b = this.visit((BreakNode) statement);
                            casebody.statements().add(b);
                            switchStatement.statements().add(casebody);
                            casebody = asn.newBlock();
                            casebody.setSourceRange(statement.getPos(), statement.getLength());
                        } else if (statement instanceof DefaultNode) {
                            if (casebody.statements().size() > 0) {
                                switchStatement.statements().add(casebody);
                                casebody = asn.newBlock();
                                casebody.setSourceRange(statement.getPos(), statement.getLength());
                            }
                            switchStatement.statements().add(this.visit((DefaultNode) statement));
                        } else if (statement instanceof ExprStmtNode) {
                            ExpressionStatement e = this.visit((ExprStmtNode) statement);
                            casebody.statements().add(e);
                        }
                    }
                }
            }
        }
        return switchStatement;
    }

    DoStatement visit(DoNode node) {
        DoStatement doStatement = asn.newDoStatement();
        doStatement.setSourceRange(node.getPos(), node.getLength());
        for (Tree child : node.getChildren()) {
            if (child instanceof BlockNode)
                doStatement.setBody((Block) this.visit((BlockNode) child));
            else if (child instanceof ConditionNode)
                doStatement.setExpression(this.visit((ConditionNode) child));
        }
        return doStatement;

    }

    BreakStatement visit(BreakNode node) {
        BreakStatement breakStatement = asn.newBreakStatement();
        breakStatement.setSourceRange(node.getPos(), node.getLength());
        return breakStatement;
    }

    SwitchCase visit(CaseNode node) {
        SwitchCase caseStatement = asn.newSwitchCase();
        caseStatement.setSourceRange(node.getPos(), node.getLength());
        if (!node.getChildren().isEmpty()) {
            Tree child = node.getChildren().get(0);
            if (child instanceof ExprNode)
                caseStatement.setExpression(this.visit((ExprNode) child));
        }
        return caseStatement;
    }

    SwitchCase visit(DefaultNode node) {
        SwitchCase defaultCase = asn.newSwitchCase();
        defaultCase.setSourceRange(node.getPos(), node.getLength());
        defaultCase.setExpression(null);
        return defaultCase;
    }

    ContinueStatement visit(ContinueNode node) {
        ContinueStatement continueStatement = asn.newContinueStatement();
        continueStatement.setSourceRange(node.getPos(), node.getLength());
        return continueStatement;
    }

    TypeDeclaration visit(InterfaceNode node) {
        TypeDeclaration interfaceDeclaration = asn.newTypeDeclaration();
        interfaceDeclaration.setSourceRange(node.getPos(), node.getLength());
        interfaceDeclaration.setInterface(true);
        for (Tree child : node.getChildren()) {
            if (child instanceof SpecifierNode) {
                Modifier m = this.visit((SpecifierNode) child);
                if (m != null)
                    interfaceDeclaration.modifiers().add(m);

            } else if (child instanceof NameNode) {
                interfaceDeclaration.setName((SimpleName) this.visit((NameNode) child));
                if (child.getChildren().size() == 2) { // if InterfaceName<A>
                    Tree l = child.getChildren().get(1);
                    if (l instanceof ArgumentListNode) {
                        for (Expression exp : this.visit((ArgumentListNode) l)) {
                            TypeParameter typeParameter = asn.newTypeParameter();
                            typeParameter.setSourceRange(exp.getStartPosition(), exp.getLength());
                            typeParameter.setName((SimpleName) exp);
                            interfaceDeclaration.typeParameters().add(typeParameter);
                        }
                    }
                }
            } else if (child instanceof SuperListNode) {
                List<Type> types = this.visit((SuperListNode) child);
                for (Type t : types) {
                    interfaceDeclaration.superInterfaceTypes().add(t);
                }
            } else if (child instanceof BlockNode) {
                List<Object> lm = (List<Object>) this.visit((BlockNode) child);
                for (Object b : lm) {
                    try {
                        interfaceDeclaration.bodyDeclarations().add(b);
                    } catch (Exception e) {

                    }
                }
            }
        }
        return interfaceDeclaration;
    }

    MethodDeclaration visit(ConstructorNode node) {
        // treat as function
        FunctionNode f = new FunctionNode(node);
        f.setChildren(node.getChildren());
        MethodDeclaration md = this.visit(f);
        md.setConstructor(true);
        return md;
    }

    MethodDeclaration visit(DestrctorNode node) {
        // there is no destructor in java, so treat as function
        FunctionNode f = new FunctionNode(node);
        f.setChildren(node.getChildren());
        return this.visit(f);
    }

    EnumDeclaration visit(EnumNode node) {
        EnumDeclaration enumDeclaration = asn.newEnumDeclaration();
        enumDeclaration.setSourceRange(node.getPos(), node.getLength());
        for (Tree child : node.getChildren()) {
            if (child instanceof SpecifierNode) {
                Modifier m = this.visit((SpecifierNode) child);
                if (m != null)
                    enumDeclaration.modifiers().add(m);
            }
            if (child instanceof NameNode)
                enumDeclaration.setName((SimpleName) this.visit((NameNode) child));
            if (child instanceof BlockNode) { // will treat this here because it's too specific
                for (Tree dec_child : child.getChildren()) {
                    if (dec_child instanceof DeclNode) {
                        EnumConstantDeclaration constant1 = asn.newEnumConstantDeclaration();
                        constant1.setSourceRange(dec_child.getPos(), dec_child.getLength());
                        Tree name_node = dec_child.getChildren().get(0);
                        if (name_node instanceof NameNode)
                            constant1.setName((SimpleName) this.visit((NameNode) name_node));
                        enumDeclaration.enumConstants().add(constant1);
                    }
                }
            }
        }
        return enumDeclaration;
    }

    ThrowStatement visit(ThrowNode node) {
        ThrowStatement throwStatement = asn.newThrowStatement();
        throwStatement.setSourceRange(node.getPos(), node.getLength());
        if (!node.getChildren().isEmpty()) {
            Tree child = node.getChildren().get(0);
            if (child instanceof ExprNode) {
                Expression exp = this.visit((ExprNode) child);
                if (exp != null)
                    throwStatement.setExpression(exp);
            }
        }
        return throwStatement;
    }

    TryStatement visit(TryNode node) {
        TryStatement tryStatement = asn.newTryStatement();
        tryStatement.setSourceRange(node.getPos(), node.getLength());
        for (Tree child : node.getChildren()) {
            if (child instanceof BlockNode)
                tryStatement.setBody((Block) this.visit((BlockNode) child));
            else if (child instanceof CatchNode)
                tryStatement.catchClauses().add(this.visit((CatchNode) child));
            else if (child instanceof FinallyNode)
                tryStatement.setFinally((this.visit((FinallyNode) child)));
        }
        return tryStatement;
    }

    CatchClause visit(CatchNode node) {
        CatchClause catchClause = asn.newCatchClause();
        catchClause.setSourceRange(node.getPos(), node.getLength());
        for (Tree child : node.getChildren()) {
            if (child instanceof ParameterListNode) {
                List<SingleVariableDeclaration> l = this.visit((ParameterListNode) child);
                if (!l.isEmpty())
                    catchClause.setException(l.get(0));
            } else if (child instanceof BlockNode) {
                catchClause.setBody((Block) this.visit((BlockNode) child));
            }
        }
        return catchClause;
    }

    Block visit(FinallyNode node) {
        if (!node.getChildren().isEmpty()) {
            Tree child = node.getChildren().get(0);
            if (child instanceof BlockNode) {
                return (Block) this.visit((BlockNode) child);
            }
        }
        return asn.newBlock();
    }

    ImportDeclaration visit(UsingNode node) {
        ImportDeclaration importDeclaration = asn.newImportDeclaration();
        importDeclaration.setSourceRange(node.getPos(), node.getLength());
        if (!node.getChildren().isEmpty()) {
            Tree child = node.getChildren().get(0);
            if (child instanceof NameNode) {
                Name n = this.visit((NameNode) child);
                if (n != null)
                    importDeclaration.setName(n);
            }
        }
        return importDeclaration;
    }

    TryStatement visit(UsingStmtNode node) {
        TryStatement tryStatement = asn.newTryStatement();
        tryStatement.setSourceRange(node.getPos(), node.getLength());
        for (Tree child : node.getChildren()) {
            if (child instanceof InitNode) {
                try {
                    tryStatement.resources().add(this.visit((InitNode) child));
                } catch (Exception e) {
                }
            } else if (child instanceof BlockNode)
                tryStatement.setBody((Block) this.visit((BlockNode) child));
        }
        return tryStatement;
    }

    List<Object> visit(UnsafeNode node) {
        // No unsafe in java, so add the block to the main block
        if (!node.getChildren().isEmpty()) {
            Tree child = node.getChildren().get(0);
            if (child instanceof BlockNode) {
                child = child.getChildren().get(0);
                if (child instanceof BlockContentNode)
                    return this.visit((BlockContentNode) child);
            }
        }
        return new ArrayList();
    }

    SynchronizedStatement visit(LockNode node) {
        SynchronizedStatement synchronizedStatement = asn.newSynchronizedStatement();
        synchronizedStatement.setSourceRange(node.getPos(), node.getLength());
        for (Tree child : node.getChildren()) {
            if (child instanceof InitNode)
                synchronizedStatement.setExpression((Expression) this.visit((InitNode) child));
            if (child instanceof BlockNode)
                synchronizedStatement.setBody((Block) this.visit((BlockNode) child));
        }
        return synchronizedStatement;
    }

    void visit(GotoNode node) {
        // do nothing, there is no equivalent in java for this
    }

    void visit(LabelNode node) {
        // do nothing, there is no equivalent in java for this
    }

    ConditionalExpression visit(TernaryNode node) {
        ConditionalExpression ternaryExpression = asn.newConditionalExpression();
        ternaryExpression.setSourceRange(node.getPos(), node.getLength());
        for (Tree child : node.getChildren()) {
            if (child instanceof ConditionNode)
                ternaryExpression.setExpression(this.visit((ConditionNode) child));
            if (child instanceof ThenNode) {
                Expression exp = this.visit((ThenNode) child);
                if (exp != null)
                    ternaryExpression.setThenExpression(exp);
            }
            if (child instanceof ElseNode) {
                Tree c = child.getChildren().get(0);
                if (c instanceof ExprNode)
                    ternaryExpression.setElseExpression(this.visit((ExprNode) c));
            }
        }
        return ternaryExpression;
    }

    Expression visit(ThenNode node) {
        if (!node.getChildren().isEmpty()) {
            Tree child = node.getChildren().get(0);
            if (child instanceof ExprNode)
                return this.visit((ExprNode) child);
        }
        return null;
    }

    Object visit(DelegateNode node) {
        // equivalent is to create an interface
        TypeDeclaration interfaceDeclaration = asn.newTypeDeclaration();
        interfaceDeclaration.setSourceRange(node.getPos(), node.getLength());
        if (node.getChildren().size() > 1 && node.getChildren().get(1) instanceof NameNode) {
            interfaceDeclaration.setName((SimpleName) this.visit((NameNode) node.getChildren().get(1)));
            interfaceDeclaration.setInterface(true);
            FunctionDeclNode f = new FunctionDeclNode(node);
            f.setChildren(node.getChildren());
            MethodDeclaration m = this.visit(f);
            interfaceDeclaration.bodyDeclarations().add(m);
        }
        return interfaceDeclaration;
    }

    void visit(EscapeNode node) {
        // do nothing
    }

    EmptyStatement visit(EmptyStmtNode node) {
        EmptyStatement emptyStatement = asn.newEmptyStatement();
        emptyStatement.setSourceRange(node.getPos(), node.getLength());
        return emptyStatement;
    }

    TryStatement visit(FixedNode node) {
        UsingStmtNode us = new UsingStmtNode(node);
        us.setChildren(node.getChildren());
        us.setParent(node.getParent());
        return this.visit(us);
    }

    void visit(ModifierNode node) {
        // no equivalent in java
    }

    Object visit(EventNode node) {
        List<Tree> children = node.getChildren();
        if (children.size() == 1 && children.get(0) instanceof ExprNode) {
            children = children.get(0).getChildren();
            VariableDeclarationFragment variableDeclarationFragment = asn.newVariableDeclarationFragment();

            FieldDeclaration fieldDeclaration = asn.newFieldDeclaration(variableDeclarationFragment);
            fieldDeclaration.setSourceRange(node.getPos(), node.getLength());
            for (Tree child : children) {
                if (child instanceof SpecifierNode) {
                    Modifier m = this.visit((SpecifierNode) child);
                    if (m != null)
                        fieldDeclaration.modifiers().add(m);
                } else if (child instanceof NameNode) {
                    Name n = this.visit((NameNode) child);
                    if (variableDeclarationFragment.getName().toString().equals("MISSING") && n.isSimpleName()) {
                        variableDeclarationFragment.setName((SimpleName) n);
                        variableDeclarationFragment.setSourceRange(child.getPos(), child.getLength());
                    } else {
                        Type t = asn.newSimpleType(n);
                        t.setSourceRange(n.getStartPosition(), n.getLength());
                        fieldDeclaration.setType(t);
                    }
                }
            }
            return fieldDeclaration;
        }
        return null;
    }

    List<Object> visit(PropertyNode node) {
        List<Object> results = new ArrayList<>();
        List<Tree> children = node.getChildren();

        // declare the field: Dec_stmt -> decl -> type + name from node
        DeclStmtNode dsn = new DeclStmtNode(node);
        dsn.setParent(node.getParent());
        DeclNode dn = new DeclNode(node);
        List<Tree> new_children = new ArrayList<>();
        if (children.size() > 1) {
            new_children.add(children.get(0));
            new_children.add(children.get(1));
            dn.setChildren(new_children);
            List<Tree> new_children_dsn = new ArrayList<>();
            new_children_dsn.add(dn);
            dsn.setChildren(new_children_dsn);
            results.add(this.visit(dsn));
        }
        // create the getters and setters
        if (children.size() >= 3 && children.get(2) instanceof BlockNode) {
            for (Tree block_child : children.get(2).getChildren()) {
                if (block_child instanceof FunctionNode)
                    results.add(this.visit((FunctionNode) block_child));
                else if (block_child instanceof FunctionDeclNode)
                    results.add(this.visit((FunctionDeclNode) block_child));
            }
        }
        return results;
    }

    TypeDeclaration visit(StructNode node) {
        // treat as a class
        ClassNode an = new ClassNode(node);
        an.setChildren(node.getChildren());
        return this.visit(an);
    }

    List<NormalAnnotation> visit(AttributeNode node) {
        // Annotations in java
        List<NormalAnnotation> an = new ArrayList<>();
        for (Tree child : node.getChildren()) {
            if (child instanceof ExprNode) {
                NormalAnnotation annotation = asn.newNormalAnnotation();
                annotation.setSourceRange(child.getPos(), child.getLength());
                child = child.getChildren().get(0);
                if (child instanceof CallNode) { // treat here because special case
                    for (Tree call_child : child.getChildren()) {
                        if (call_child instanceof NameNode)
                            annotation.setTypeName(this.visit((NameNode) call_child));
                        else if (call_child instanceof ArgumentListNode) {
                            for (Tree arg_child : call_child.getChildren()) {
                                MemberValuePair memberValuePair = asn.newMemberValuePair();
                                memberValuePair.setSourceRange(arg_child.getPos(), arg_child.getLength());
                                if (arg_child instanceof ArgumentNode) {
                                    Tree expr_child = arg_child.getChildren().get(0);
                                    if (expr_child instanceof ExprNode) {
                                        List<Tree> expr_children = expr_child.getChildren();
                                        if (expr_children.size() == 1) {
                                            memberValuePair.setName(asn.newSimpleName("value"));
                                            Tree literal_child = expr_children.get(0);
                                            if (literal_child instanceof LiteralNode)
                                                memberValuePair.setValue(this.visit((LiteralNode) literal_child));
                                            annotation.values().add(memberValuePair);
                                        } else if (expr_children.size() > 1) {
                                            for (Tree expr_expr_child : expr_children) {
                                                if (expr_expr_child instanceof NameNode) {
                                                    Name n = this.visit((NameNode) expr_expr_child);
                                                    if (n != null && n.isSimpleName())
                                                        memberValuePair.setName((SimpleName) n);
                                                }
                                                if (expr_expr_child instanceof LiteralNode)
                                                    memberValuePair.setValue(this.visit((LiteralNode) expr_expr_child));
                                            }

                                            annotation.values().add(memberValuePair);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                an.add(annotation);
            }
        }
        return an;
    }

    List<Object> visit(CheckedNode node) {
        // No unsafe in java, so add the block to the main block
        if (!node.getChildren().isEmpty()) {
            Tree child = node.getChildren().get(0);
            if (child instanceof BlockNode) {
                child = child.getChildren().get(0);
                if (child instanceof BlockContentNode)
                    return this.visit((BlockContentNode) child);
            }
        }
        return new ArrayList();
    }

    List<Object> visit(UncheckedNode node) {
        // No unsafe in java, so add the block to the main block
        if (!node.getChildren().isEmpty()) {
            Tree child = node.getChildren().get(0);
            if (child instanceof BlockNode) {
                child = child.getChildren().get(0);
                if (child instanceof BlockContentNode)
                    return this.visit((BlockContentNode) child);
            }
        }
        return new ArrayList();
    }


    MethodInvocation visit(TypeOfNode node) {
        MethodInvocation methodInvocation = asn.newMethodInvocation();
        methodInvocation.setSourceRange(node.getPos(), node.getLength());
        List<Tree> children = node.getChildren();
        methodInvocation.setName(asn.newSimpleName("typeof"));
        if (!children.isEmpty()) {
            Tree argNode = children.get(0);
            if (argNode instanceof ArgumentListNode) {
                for (Expression exp : this.visit((ArgumentListNode) argNode))
                    methodInvocation.arguments().add(exp);
            }
        }
        return methodInvocation;
    }

    MethodInvocation visit(SizeOfNode node) {
        MethodInvocation methodInvocation = asn.newMethodInvocation();
        methodInvocation.setSourceRange(node.getPos(), node.getLength());
        List<Tree> children = node.getChildren();
        methodInvocation.setName(asn.newSimpleName("sizeof"));
        if (!children.isEmpty()) {
            Tree argNode = children.get(0);
            if (argNode instanceof ArgumentListNode) {
                for (Expression exp : this.visit((ArgumentListNode) argNode))
                    methodInvocation.arguments().add(exp);
            }
        }
        return methodInvocation;
    }

    SimpleType visit(ConstraintNode node) {
        if (!node.getChildren().isEmpty()) {
            Tree child = node.getChildren().get(0);
            if (child instanceof NameNode) {
                SimpleType t = asn.newSimpleType(this.visit((NameNode) child));
                t.setSourceRange(node.getPos(), node.getLength());
                return t;
            }
        }
        return null;
    }

    LambdaExpression visit(LambdaNode node) {
        LambdaExpression lambda = asn.newLambdaExpression();
        lambda.setSourceRange(node.getPos(), node.getLength());
        for (Tree child : node.getChildren()) {
            if (child instanceof ParameterListNode) {
                for (VariableDeclaration vdec : this.visit((ParameterListNode) child))
                    lambda.parameters().add(vdec);
            }
            if (child instanceof BlockNode) {
                Object obj = this.visit((BlockNode) child);
                if (obj instanceof Block)
                    lambda.setBody((Block) obj);
            }
        }
        return lambda;
    }

    Expression visit(LinqNode node) {
        // no equivalent, transform into method
        List<MethodInvocation> chainedlist = new ArrayList<>();
        for (Tree child : node.getChildren()) {
            if (child instanceof FromNode)
                chainedlist.add(this.visit((FromNode) child));
            else if (child instanceof SelectNode)
                chainedlist.add(this.visit((SelectNode) child));
            else if (child instanceof GroupNode)
                chainedlist.add(this.visit((GroupNode) child));
            else if (child instanceof OrderByNode)
                chainedlist.add(this.visit((OrderByNode) child));
            else if (child instanceof WhereNode)
                chainedlist.add(this.visit((WhereNode) child));
            else if (child instanceof JoinNode)
                chainedlist.add(this.visit((JoinNode) child));
            else if (child instanceof LetNode)
                chainedlist.add(this.visit((LetNode) child));
        }
        for (int i = 1; i < chainedlist.size(); i++) {
            chainedlist.get(i).setExpression(chainedlist.get(i - 1));
        }
        return chainedlist.get(chainedlist.size() - 1);
    }

    MethodInvocation visit(FromNode node) {
        MethodInvocation fromMethodInvocation = asn.newMethodInvocation();
        fromMethodInvocation.setSourceRange(node.getPos(), node.getLength());
        fromMethodInvocation.setName(asn.newSimpleName("from"));
        LambdaExpression lambda = asn.newLambdaExpression();
        lambda.setSourceRange(node.getPos(), node.getLength());
        fromMethodInvocation.arguments().add(lambda);

        for (Tree child : node.getChildren()) {
            if (child instanceof ExprNode) {
                SingleVariableDeclaration sv = asn.newSingleVariableDeclaration();
                sv.setSourceRange(child.getPos(), child.getLength());
                Expression exp = this.visit((ExprNode) child);
                if (exp instanceof SimpleName)
                    sv.setName((SimpleName) exp);
                sv.setType(asn.newSimpleType(asn.newSimpleName("var")));
                lambda.parameters().add(sv);
            }
            if (child instanceof InNode)
                lambda.setBody(this.visit((InNode) child));
        }
        return fromMethodInvocation;
    }

    Expression visit(InNode node) {
        if (!node.getChildren().isEmpty()) {
            Tree child = node.getChildren().get(0);
            if (child instanceof ExprNode)
                return this.visit((ExprNode) child);
        }
        return asn.newSimpleName("table");
    }

    MethodInvocation visit(SelectNode node) {
        MethodInvocation selectMethodInvocation = asn.newMethodInvocation();
        selectMethodInvocation.setSourceRange(node.getPos(), node.getLength());
        selectMethodInvocation.setName(asn.newSimpleName("select"));
        if (!node.getChildren().isEmpty()) {
            Tree child = node.getChildren().get(0);
            if (child instanceof ExprNode)
                selectMethodInvocation.arguments().add(this.visit((ExprNode) child));
        }
        return selectMethodInvocation;
    }

    MethodInvocation visit(WhereNode node) {
        MethodInvocation whereMethodInvocation = asn.newMethodInvocation();
        whereMethodInvocation.setSourceRange(node.getPos(), node.getLength());
        whereMethodInvocation.setName(asn.newSimpleName("where"));
        if (!node.getChildren().isEmpty()) {
            Tree child = node.getChildren().get(0);
            if (child instanceof ExprNode) {
                Expression exp = this.visit((ExprNode) child);
                if (exp != null)
                    whereMethodInvocation.arguments().add(exp);
            }
        }
        return whereMethodInvocation;
    }

    MethodInvocation visit(OrderByNode node) {
        MethodInvocation orderbyMethodInvocation = asn.newMethodInvocation();
        orderbyMethodInvocation.setSourceRange(node.getPos(), node.getLength());
        orderbyMethodInvocation.setName(asn.newSimpleName("orderBy"));
        for (Tree child : node.getChildren()) {
            if (child instanceof ExprNode)
                orderbyMethodInvocation.arguments().add(this.visit((ExprNode) child));
            if (child instanceof NameNode)
                orderbyMethodInvocation.arguments().add(this.visit((NameNode) child));
        }
        return orderbyMethodInvocation;
    }

    MethodInvocation visit(GroupNode node) {
        MethodInvocation groupMethodInvocation = asn.newMethodInvocation();
        groupMethodInvocation.setSourceRange(node.getPos(), node.getLength());
        groupMethodInvocation.setName(asn.newSimpleName("group"));
        for (Tree child : node.getChildren()) {
            if (child instanceof ExprNode){
                Expression exp = this.visit((ExprNode) child);
                if (exp != null)
                    groupMethodInvocation.arguments().add(exp);
            }

            if (child instanceof ByNode)
                groupMethodInvocation.arguments().add(this.visit((ByNode) child));
            if (child instanceof IntoNode)
                groupMethodInvocation.arguments().add(this.visit((IntoNode) child));
        }
        return groupMethodInvocation;
    }

    Expression visit(ByNode node) {
        if (!node.getChildren().isEmpty()) {
            Tree child = node.getChildren().get(0);
            if (child instanceof ExprNode)
                return this.visit((ExprNode) child);
        }
        return null;
    }

    Expression visit(IntoNode node) {
        if (!node.getChildren().isEmpty()) {
            Tree child = node.getChildren().get(0);
            if (child instanceof ExprNode)
                return this.visit((ExprNode) child);
        }
        return null;
    }

    MethodInvocation visit(JoinNode node) {
        MethodInvocation joinMethodInvocation = asn.newMethodInvocation();
        joinMethodInvocation.setSourceRange(node.getPos(), node.getLength());
        joinMethodInvocation.setName(asn.newSimpleName("join"));
        LambdaExpression lambda = asn.newLambdaExpression();
        lambda.setSourceRange(node.getPos(), node.getLength());
        joinMethodInvocation.arguments().add(lambda);
        for (Tree child : node.getChildren()) {
            if (child instanceof ExprNode) {
                SingleVariableDeclaration sv = asn.newSingleVariableDeclaration();
                sv.setSourceRange(child.getPos(), child.getLength());
                sv.setName((SimpleName) this.visit((ExprNode) child));
                sv.setType(asn.newSimpleType(asn.newSimpleName("var")));
                lambda.parameters().add(sv);
            }
            if (child instanceof InNode)
                lambda.setBody(this.visit((InNode) child));
            if (child instanceof OnNode)
                joinMethodInvocation.arguments().add(this.visit((OnNode) child));
            if (child instanceof EqualsNode)
                joinMethodInvocation.arguments().add(this.visit((EqualsNode) child));
        }
        return joinMethodInvocation;
    }

    Expression visit(EqualsNode node) {
        if (!node.getChildren().isEmpty()) {
            Tree child = node.getChildren().get(0);
            if (child instanceof ExprNode)
                return this.visit((ExprNode) child);
        }
        return null;
    }

    Expression visit(OnNode node) {
        if (!node.getChildren().isEmpty()) {
            Tree child = node.getChildren().get(0);
            if (child instanceof ExprNode)
                return this.visit((ExprNode) child);
        }
        return null;
    }

    MethodInvocation visit(LetNode node) {
        MethodInvocation letMethodInvocation = asn.newMethodInvocation();
        letMethodInvocation.setSourceRange(node.getPos(), node.getLength());
        letMethodInvocation.setName(asn.newSimpleName("let"));
        for (Tree child : node.getChildren()) {
            if (child instanceof ExprNode)
                letMethodInvocation.arguments().add(this.visit((ExprNode) child));
        }
        return letMethodInvocation;
    }
}
