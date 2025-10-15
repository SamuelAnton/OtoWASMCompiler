package Syntaxer.ast;

import Syntaxer.ast.expression.Expression;
import Syntaxer.ast.statement.Statement;

public final class AssignmentStatement extends Statement {
    public final Expression target;
    public final Expression value;

    public AssignmentStatement(Expression target, Expression value) {
        this.target = target;
        this.value = value;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
