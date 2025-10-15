package Syntaxer.ast.expression;

import java.util.List;

public final class CallOrAccess extends Expression {
    public final Expression target;       // null for bare identifier or constructor name
    public final String member;     // method name or identifier
    public final List<Expression> args; // null for access

    public CallOrAccess(Expression target, String member, List<Expression> args) {
        super();
        this.target = target;
        this.member = member;
        this.args = args;
    }
}
