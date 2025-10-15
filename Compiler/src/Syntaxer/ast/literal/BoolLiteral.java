package Syntaxer.ast.literal;

import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.expression.Expression;

public final class BoolLiteral extends Expression {
    public final boolean value;

    public BoolLiteral(boolean v) {
        super();
        this.value = v;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }

}
