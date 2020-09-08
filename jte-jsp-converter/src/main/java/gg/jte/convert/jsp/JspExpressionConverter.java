package gg.jte.convert.jsp;

import org.apache.el.lang.ExpressionBuilder;
import org.apache.el.parser.*;

import java.util.HashMap;
import java.util.Map;

public class JspExpressionConverter {

    private final Node root;
    private final StringBuilder result;
    private final Map<Class<? extends Node>, Visitor> visitorMap = new HashMap<>();

    public JspExpressionConverter(String el) {
        root = ExpressionBuilder.createNode(el);
        result = new StringBuilder(el.length());

        visitorMap.put(AstMethodParameters.class, new AstMethodParametersVisitor());
        visitorMap.put(AstIdentifier.class, new AstIdentifierVisitor());
        visitorMap.put(AstEmpty.class, new AstEmptyVisitor());
        visitorMap.put(AstNot.class, new AstNotVisitor());
        visitorMap.put(AstOr.class, new AstOrVisitor());
        visitorMap.put(AstAnd.class, new AstAndVisitor());
        visitorMap.put(AstTrue.class, new AstTrueVisitor());
        visitorMap.put(AstFalse.class, new AstFalseVisitor());
        visitorMap.put(AstValue.class, new AstValueVisitor());
        visitorMap.put(AstEqual.class, new AstEqualVisitor());
        visitorMap.put(AstInteger.class, new AstIntegerVisitor());
        visitorMap.put(AstDotSuffix.class, new AstDotSuffixVisitor());
        visitorMap.put(AstChoice.class, new AstChoiceVisitor());
        visitorMap.put(AstString.class, new AstStringVisitor());

        process(root);
    }

    public String getJavaCode() {
        return result.toString();
    }

    private void process(Node node) {
        Visitor visitor = visitorMap.get(node.getClass());
        if (visitor != null) {
            visitor.visit(node);
        } else {
            throw new UnsupportedOperationException("Unknown AST node " + node.getClass());
        }
    }

    private interface Visitor {
        void visit(Node node);
    }

    private class AstMethodParametersVisitor implements Visitor {
        @Override
        public void visit(Node node) {
            result.append('(');
            for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
                if (i > 0) {
                    result.append(", ");
                }
                process(node.jjtGetChild(i));
            }
            result.append(')');
        }
    }

    private class AstIdentifierVisitor implements Visitor {
        @Override
        public void visit(Node node) {
            result.append(node.getImage());
        }
    }

    private class AstEmptyVisitor implements Visitor {
        @Override
        public void visit(Node node) {
            process(node.jjtGetChild(0));
            if (node.jjtGetParent() instanceof AstNot) {
                result.append(" != null");
            } else {
                result.append(" == null");
            }
        }
    }

    private class AstNotVisitor implements Visitor {
        @Override
        public void visit(Node node) {
            process(node.jjtGetChild(0));
        }
    }

    private abstract class AstBinaryOperatorVisitor implements Visitor {
        @Override
        public void visit(Node node) {
            boolean parenthesisNeeded = isParenthesisNeeded(node);
            if (parenthesisNeeded) {
                result.append('(');
            }

            process(node.jjtGetChild(0));
            result.append(' ').append(mapOperator()).append(' ');
            process(node.jjtGetChild(1));

            if (parenthesisNeeded) {
                result.append(')');
            }
        }

        protected abstract String mapOperator();

        @SuppressWarnings("RedundantIfStatement")
        private boolean isParenthesisNeeded(Node node) {
            if (node == root) {
                return false;
            }

            if (node.jjtGetParent() instanceof AstChoice) {
                return false;
            }

            return true;
        }
    }

    private class AstOrVisitor extends AstBinaryOperatorVisitor {
        @Override
        protected String mapOperator() {
            return "||";
        }
    }

    private class AstAndVisitor extends AstBinaryOperatorVisitor {
        @Override
        protected String mapOperator() {
            return "&&";
        }
    }

    private class AstTrueVisitor implements Visitor {
        @Override
        public void visit(Node node) {
            result.append("true");
        }
    }

    private class AstFalseVisitor implements Visitor {
        @Override
        public void visit(Node node) {
            result.append("false");
        }
    }

    private class AstValueVisitor implements Visitor {
        @Override
        public void visit(Node node) {
            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                process(node.jjtGetChild(i));
            }
        }
    }

    private class AstEqualVisitor extends AstBinaryOperatorVisitor {
        @Override
        protected String mapOperator() {
            return "==";
        }
    }

    private class AstIntegerVisitor implements Visitor {
        @Override
        public void visit(Node node) {
            result.append(node.getImage());
        }
    }

    private class AstStringVisitor implements Visitor {
        @Override
        public void visit(Node node) {
            AstString astString = (AstString)node;
            result.append('"').append(astString.getString()).append('"');
        }
    }

    private class AstDotSuffixVisitor implements Visitor {
        @Override
        public void visit(Node node) {
            result.append(".").append(node.getImage());
        }
    }

    private class AstChoiceVisitor implements Visitor {
        @Override
        public void visit(Node node) {
            process(node.jjtGetChild(0));
            result.append(" ? ");
            process(node.jjtGetChild(1));
            result.append(" : ");
            process(node.jjtGetChild(2));
        }
    }
}
