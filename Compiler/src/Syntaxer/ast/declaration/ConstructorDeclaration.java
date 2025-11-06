package Syntaxer.ast.declaration;

import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.component.Param;
import Syntaxer.ast.statement.Statement;

import java.util.List;

public final class ConstructorDeclaration extends MemberDeclaration {
    public final List<Param> params;
    public List<Statement> body;
    public ClassDeclaration baseClass;

    public ConstructorDeclaration(List<Param> params, List<Statement> body) {
        super();
        this.params = params;
        this.body = body;
    }

    public Boolean sameSignature(ConstructorDeclaration c) {
        if (c.params.size() != params.size()) {
            return false;
        }
        for (int i = 0; i < params.size(); i++) {
            if (params.get(i).t.name != c.params.get(i).t.name) {
                return false;
            }
        }
        return true;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
