package Syntaxer.ast.declaration;

import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.component.Block;
import Syntaxer.ast.component.Param;

import java.util.List;

public final class ConstructorDeclaration extends MemberDeclaration {
    public final List<Param> params;
    public final Block body;

    public ConstructorDeclaration(List<Param> params, Block body) {
        super();
        this.params = params;
        this.body = body;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
