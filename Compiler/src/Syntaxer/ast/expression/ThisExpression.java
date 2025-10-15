package Syntaxer.ast.expression;

import Syntaxer.ast.ASTVisitor;

public final class ThisExpression extends Expression {
    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
