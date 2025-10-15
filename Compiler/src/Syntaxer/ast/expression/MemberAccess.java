package Syntaxer.ast.expression;

import Syntaxer.ast.ASTVisitor;

public final class MemberAccess extends Expression {
    public final Expression target;
    public final String memberName;

    public MemberAccess(Expression target, String memberName) {
        this.target = target;
        this.memberName = memberName;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
