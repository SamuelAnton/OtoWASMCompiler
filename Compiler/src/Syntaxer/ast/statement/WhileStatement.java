package Syntaxer.ast.statement;

import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.component.Block;
import Syntaxer.ast.expression.Expression;

public final class WhileStatement extends Statement {
    public final Expression cond;
    public final Block body;

    public WhileStatement(Expression cond, Block body) {
        this.cond = cond;
        this.body = body;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
