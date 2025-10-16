package Syntaxer.ast.component;

import Syntaxer.ast.ASTNode;
import Syntaxer.ast.ASTVisitor;

public final class ExtensionType extends ASTNode {
    public final String name;

    public ExtensionType(String name) {
        super(-1, -1);
        this.name = name;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
