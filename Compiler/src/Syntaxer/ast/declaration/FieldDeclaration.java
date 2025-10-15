package Syntaxer.ast.declaration;

import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.expression.Expression;

public final class FieldDeclaration extends MemberDeclaration {
    public final String name;
    public final Expression init;

    public FieldDeclaration(String name, Expression init) {
        super();
        this.name = name;
        this.init = init;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
