package Syntaxer.ast.expression;

import Syntaxer.ast.ASTVisitor;

public final class MemberAccess extends Expression {
    public final Expression target;
    public final Expression member;

    public MemberAccess(Expression target, Expression member) {
        super(-1, -1);
        this.target = target;
        this.member = member;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
