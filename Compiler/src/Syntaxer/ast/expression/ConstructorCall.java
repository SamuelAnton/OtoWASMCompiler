package Syntaxer.ast.expression;

import Syntaxer.ast.ASTVisitor;

import java.util.List;

public final class ConstructorCall extends Expression {
    public final String className;
    public final List<Expression> args;

    public ConstructorCall(String className, List<Expression> args) {
        super(-1, -1);
        this.className = className;
        this.args = args;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
