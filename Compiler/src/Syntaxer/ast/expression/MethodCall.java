package Syntaxer.ast.expression;

import Syntaxer.ast.ASTVisitor;

import java.util.List;

public final class MethodCall extends Expression {
    public final Expression target;
    public final List<Expression> args;

    public MethodCall(Expression target, List<Expression> args) {
        super(-1, -1);
        this.target = target;
        this.args = args;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}