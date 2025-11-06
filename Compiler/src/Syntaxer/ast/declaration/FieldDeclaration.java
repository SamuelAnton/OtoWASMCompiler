package Syntaxer.ast.declaration;

import Semanticer.Components.Types.VariableType;
import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.expression.Expression;

public final class FieldDeclaration extends MemberDeclaration {
    public final String name;
    public final Expression init;
    public VariableType dynamicType;
    public VariableType staticType;

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
