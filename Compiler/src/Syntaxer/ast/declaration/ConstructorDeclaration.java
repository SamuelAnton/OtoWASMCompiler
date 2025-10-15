package Syntaxer.ast.declaration;

import Syntaxer.ast.component.Block;
import Syntaxer.ast.component.Param;
import Syntaxer.ast.expression.MemberDeclaration;

import java.util.List;

public final class ConstructorDeclaration extends MemberDeclaration {
    public final List<Param> params;
    public final Block body;

    public ConstructorDeclaration(List<Param> params, Block body) {
        super();
        this.params = params;
        this.body = body;
    }
}
