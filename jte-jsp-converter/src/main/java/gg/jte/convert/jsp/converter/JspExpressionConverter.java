package gg.jte.convert.jsp.converter;

import org.apache.el.lang.ExpressionBuilder;
import org.apache.el.parser.*;

import java.util.HashMap;
import java.util.Map;

public class JspExpressionConverter {

    public static String convertAttributeValue(String value) {
        if (value != null) {
            if ( value.trim().startsWith("${") ) {
                return new JspExpressionConverter(value).getJavaCode();
            } else {
                return "\"" + value + "\"";
            }
        }

        return "???";
    }

    private final String el;
    private final Node root;
    private final StringBuilder result;
    private final Map<Class<? extends Node>, Visitor> visitorMap = new HashMap<>();

    public JspExpressionConverter(String el) {
        this.el = el;

        if (el == null || el.isBlank()) {
            root = null;
            result = new StringBuilder();
        } else {
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
            visitorMap.put(AstNotEqual.class, new AstNotEqualVisitor());
            visitorMap.put(AstInteger.class, new AstIntegerVisitor());
            visitorMap.put(AstDotSuffix.class, new AstDotSuffixVisitor());
            visitorMap.put(AstChoice.class, new AstChoiceVisitor());
            visitorMap.put(AstString.class, new AstStringVisitor());
            visitorMap.put(AstFloatingPoint.class, new AstFloatingPointVisitor());
            visitorMap.put(AstPlus.class, new AstPlusVisitor());
            visitorMap.put(AstMinus.class, new AstMinusVisitor());
            visitorMap.put(AstMult.class, new AstMultVisitor());
            visitorMap.put(AstDiv.class, new AstDivVisitor());
            visitorMap.put(AstMod.class, new AstModVisitor());
            visitorMap.put(AstNegative.class, new AstNegativeVisitor());
            visitorMap.put(AstGreaterThan.class, new AstGreaterThanVisitor());
            visitorMap.put(AstLessThan.class, new AstLessThanVisitor());
            visitorMap.put(AstLiteralExpression.class, new AstLiteralExpressionVisitor());
            visitorMap.put(AstFunction.class, new AstFunctionVisitor());
            visitorMap.put(AstNull.class, new AstNullVisitor());
            visitorMap.put(AstBracketSuffix.class, new AstBracketSuffixVisitor());
            visitorMap.put(AstCompositeExpression.class, new AstCompositeExpressionVisitor());
            visitorMap.put(AstDynamicExpression.class, new AstDynamicExpressionVisitor());

            process(root);
        }
    }

    public String getJavaCode() {
        return result.toString();
    }

    private void process(Node node) {
        Visitor visitor = visitorMap.get(node.getClass());
        if (visitor != null) {
            visitor.visit(node);
        } else {
            throw new UnsupportedOperationException("Unknown AST node " + node.getClass() + ". Expression was '" + el + "'");
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
            result.append("isEmpty(");
            process(node.jjtGetChild(0));
            result.append(")");
        }
    }

    private class AstNotVisitor implements Visitor {
        @Override
        public void visit(Node node) {
            result.append('!');
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
            result.append(' ').append(getOperator()).append(' ');
            process(node.jjtGetChild(1));

            if (parenthesisNeeded) {
                result.append(')');
            }
        }

        protected abstract String getOperator();

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
        protected String getOperator() {
            return "||";
        }
    }

    private class AstAndVisitor extends AstBinaryOperatorVisitor {
        @Override
        protected String getOperator() {
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
        protected String getOperator() {
            return "==";
        }
    }

    private class AstNotEqualVisitor extends AstBinaryOperatorVisitor {
        @Override
        protected String getOperator() {
            return "!=";
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

    private class AstFloatingPointVisitor implements Visitor {
        @Override
        public void visit(Node node) {
            result.append(node.getImage());
        }
    }

    private class AstPlusVisitor extends AstBinaryOperatorVisitor {
        @Override
        protected String getOperator() {
            return "+";
        }
    }

    private class AstMinusVisitor extends AstBinaryOperatorVisitor {
        @Override
        protected String getOperator() {
            return "-";
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    private class AstMultVisitor extends AstBinaryOperatorVisitor {
        @Override
        protected String getOperator() {
            return "*";
        }
    }

    private class AstDivVisitor extends AstBinaryOperatorVisitor {
        @Override
        protected String getOperator() {
            return "/";
        }
    }

    private class AstModVisitor extends AstBinaryOperatorVisitor {
        @Override
        protected String getOperator() {
            return "%";
        }
    }

    private class AstGreaterThanVisitor extends AstBinaryOperatorVisitor {
        @Override
        protected String getOperator() {
            return ">";
        }
    }

    private class AstLessThanVisitor extends AstBinaryOperatorVisitor {
        @Override
        protected String getOperator() {
            return "<";
        }
    }

    private class AstLiteralExpressionVisitor implements Visitor {
        @Override
        public void visit(Node node) {
            result.append(node.getImage());
        }
    }

    private class AstFunctionVisitor implements Visitor {
        @Override
        public void visit( Node node ) {
            AstFunction function = (AstFunction)node;
            result.append(function.getPrefix());
            result.append(':');
            result.append(function.getLocalName());

            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                if (i > 0) {
                    result.append(", ");
                }
                process(node.jjtGetChild(i));
            }
        }
    }

    private class AstNullVisitor implements Visitor {
        @Override
        public void visit(Node node) {
            result.append("null");
        }
    }

    private class AstNegativeVisitor implements Visitor {
        @Override
        public void visit(Node node) {
            result.append('-');
            process(node.jjtGetChild(0));
        }
    }

    private class AstBracketSuffixVisitor implements Visitor {
        @Override
        public void visit(Node node) {
            result.append(".get(");
            process(node.jjtGetChild(0));
            result.append(")");
        }
    }

    private class AstCompositeExpressionVisitor implements Visitor {
        @Override
        public void visit(Node node) {
            result.append("@`");
            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                process(node.jjtGetChild(i));
            }
            result.append("`");
        }
    }

    private class AstDynamicExpressionVisitor implements Visitor {
        @Override
        public void visit(Node node) {
            result.append("${");
            process(node.jjtGetChild(0));
            result.append("}");
        }
    }
}
