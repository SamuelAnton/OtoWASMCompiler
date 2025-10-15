package Syntaxer.ast.statement;

import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.expression.Expression;

public final class AssignmentStatement extends Statement {
    public final Expression target;
    public final Expression value;

    public AssignmentStatement(Expression target, Expression value) {
        super();
        this.target = target;
        this.value = value;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
