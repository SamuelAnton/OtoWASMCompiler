package Syntaxer.ast.component;

import Syntaxer.ast.ASTNode;
import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.literal.ArrayLiteral;
import Syntaxer.ast.literal.ListLiteral;

public final class Type extends ASTNode {
    public final String name;

    public Type(String name) {
        super(-1, -1);
        this.name = name;
    }

    public Type(ArrayLiteral ar) {
        super(-1, -1);
        name = "Array of type " + ar.type;
    }

    public Type(ListLiteral l) {
        super(-1, -1);
        name = "Array of type " + l.type;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
