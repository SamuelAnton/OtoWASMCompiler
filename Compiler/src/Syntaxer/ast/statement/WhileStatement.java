package Syntaxer.ast.statement;

import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.expression.Expression;

import java.util.List;

public final class WhileStatement extends Statement {
    public final Expression cond;
    public List<Statement> body;

    public WhileStatement(Expression cond, List<Statement> body) {
        super();
        this.cond = cond;
        this.body = body;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
