package Syntaxer.ast.literal;

import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.expression.Expression;

public final class ListLiteral extends Expression {
    public final String t;

    public ListLiteral(String t) {
        super(-1, -1);
        this.t = t;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }

}
