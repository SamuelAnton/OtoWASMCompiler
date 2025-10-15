package Syntaxer.ast.component;

import Syntaxer.ast.ASTNode;

public final class Block extends ASTNode {
    public final java.util.List<ASTNode> statements;

    public Block(java.util.List<ASTNode> statements) {
        super(-1, -1);
        this.statements = statements;
    }
}