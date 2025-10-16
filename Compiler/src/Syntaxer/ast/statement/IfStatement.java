package Syntaxer.ast.statement;

import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.expression.Expression;

public final class IfStatement extends Statement {
    public final Expression cond;
    public final ThenStatement thenBody;
    public final ElseStatement elseBody;

    public IfStatement(Expression cond, ThenStatement thenBlock, ElseStatement elseBody) {
        super();
        this.cond = cond;
        this.thenBody = thenBlock;
        this.elseBody = elseBody;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
