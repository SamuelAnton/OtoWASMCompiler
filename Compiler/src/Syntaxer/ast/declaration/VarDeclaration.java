package Syntaxer.ast.declaration;

import Syntaxer.ast.ASTNode;
import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.expression.Expression;

public class VarDeclaration extends ASTNode {
    public final String name;
    public final Expression init;

    public VarDeclaration(String name, Expression init) {
        super(-1, -1);
        this.name = name;
        this.init = init;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
