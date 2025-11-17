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
    public ClassDeclaration baseClass;

    public MethodDeclaration(String name, List<Param> params, ReturnType returnType, List<Statement> body) {
        super();
        this.name = name;
        this.params = params;
        this.body = body;

        if (returnType == null) {
            this.returnType = new ReturnType("null");
        } else {
            this.returnType = returnType;
        }
    }

    public Boolean sameSignature(MethodDeclaration m) {
        if (m.name != name || m.returnType.name != returnType.name || m.params.size() != params.size()) {
            return false;
        }
        for (int i = 0; i < params.size(); i++) {
            if (params.get(i).t.name != m.params.get(i).t.name) {
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
