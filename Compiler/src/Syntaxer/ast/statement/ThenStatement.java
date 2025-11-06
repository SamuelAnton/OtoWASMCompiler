package Syntaxer.ast.statement;

import Syntaxer.ast.ASTVisitor;

import java.util.List;

public final class ThenStatement extends Statement {
    public List<Statement> body;


    public ThenStatement(List<Statement> body) {
        super();
        this.body = body;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
