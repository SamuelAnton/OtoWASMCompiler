package Syntaxer.ast.expression;

import Syntaxer.ast.ASTVisitor;

import java.util.List;

public final class ConstructorCall extends Expression {
    public final String className;
    public final List<Expression> arguments;

    public ConstructorCall(String className, List<Expression> arguments) {
        this.className = className;
        this.arguments = arguments;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
