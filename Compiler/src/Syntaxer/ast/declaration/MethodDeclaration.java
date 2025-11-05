package Syntaxer.ast.declaration;


import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.component.Param;
import Syntaxer.ast.component.ReturnType;
import Syntaxer.ast.statement.Statement;

import java.util.List;

public final class MethodDeclaration extends MemberDeclaration {
    public final String name;
    public final List<Param> params;
    public final ReturnType returnType;
    public List<Statement> body;

    public MethodDeclaration(String name, List<Param> params, ReturnType returnType, List<Statement> body) {
        super();
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
