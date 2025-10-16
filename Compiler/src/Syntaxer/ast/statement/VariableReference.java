package Syntaxer.ast.statement;

import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.expression.Expression;

public final class VariableReference extends Expression {
    public final String name;

    public VariableReference(String name) {
        super(-1, -1);
        this.name = name;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
