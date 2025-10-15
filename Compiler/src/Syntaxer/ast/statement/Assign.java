package Syntaxer.ast.statement;

import Syntaxer.ast.expression.Expression;

public final class Assign extends Statement {
    public final String left;
    public final Expression expression;

    public Assign(String left, Expression expression) {
        super();
        this.left = left;
        this.expression = expression;
    }
}
