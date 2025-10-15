package Syntaxer.ast.literal;

import Syntaxer.ast.expression.Expression;

public final class IntLiteral extends Expression {
    public final int value;

    public IntLiteral(int v) {
        super();
        this.value = v;
    }
}
