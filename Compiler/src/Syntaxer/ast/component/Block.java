package Syntaxer.ast.component;

import Syntaxer.ast.ASTNode;
import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.statement.Statement;

import java.util.List;

public final class Block extends Statement {
    public final List<ASTNode> statements;

    public Block(List<ASTNode> statements) {
        this.statements = statements;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}