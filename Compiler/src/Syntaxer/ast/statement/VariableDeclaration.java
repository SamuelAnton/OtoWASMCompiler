package Syntaxer.ast.statement;

import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.expression.Expression;

public class VariableDeclaration extends Statement{
    public final String name;
    public final Expression init;

    public VariableDeclaration(String name, Expression init) {
        super();
        this.name = name;
        this.init = init;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
