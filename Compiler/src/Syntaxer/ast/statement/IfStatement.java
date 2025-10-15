package Syntaxer.ast.statement;

import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.expression.Expression;

import java.util.List;

public final class IfStatement extends Statement {
    public final Expression cond;
    public final List<Statement> thenBody;
    public final List<Statement> elseBody;

    public IfStatement(Expression cond, List<Statement> thenBlock, List<Statement> elseBody) {
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
