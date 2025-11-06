package Syntaxer.ast.statement;

import Syntaxer.ast.ASTVisitor;

import java.util.List;

public final class ElseStatement extends Statement {
    public List<Statement> body;

    public ElseStatement(List<Statement> body) {
        super();
        this.body = body;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
