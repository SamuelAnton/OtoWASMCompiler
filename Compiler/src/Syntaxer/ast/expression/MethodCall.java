package Syntaxer.ast.expression;

import Syntaxer.ast.ASTVisitor;

import java.util.List;

public final class MethodCall extends Expression {
    public final Expression target;
    public final String methodName;
    public final List<Expression> arguments;

    public MethodCall(Expression target, String methodName, List<Expression> arguments) {
        this.target = target;
        this.methodName = methodName;
        this.arguments = arguments;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}