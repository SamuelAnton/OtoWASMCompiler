package Syntaxer.ast.declaration;


import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.component.Block;
import Syntaxer.ast.component.Param;

import java.util.List;

public final class MethodDeclaration extends MemberDeclaration {
    public final String name;
    public final List<Param> params;
    public final String returnType;
    public final Block body;

    public MethodDeclaration(String name, List<Param> params, String returnType, Block body) {
        this.name = name;
        this.params = params;
        this.returnType = returnType;
        this.body = body;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
