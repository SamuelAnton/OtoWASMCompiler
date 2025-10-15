package Syntaxer.ast.literal;

import Syntaxer.ast.expression.Expression;

public final class RealLiteral extends Expression {
    public final double value;

    public RealLiteral(double v) {
        super();
        this.value = v;
    }
}

