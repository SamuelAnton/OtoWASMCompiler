package Syntaxer.ast.component;

import Syntaxer.ast.ASTNode;
import Syntaxer.ast.ASTVisitor;

public final class Param extends ASTNode {
    public final String name;
    public final String type;

    public Param(String name, String type) {
        super(-1, -1);
        this.name = name;
        this.type = type;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
