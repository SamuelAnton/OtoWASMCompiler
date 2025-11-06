package Syntaxer.ast.statement;

import Semanticer.Components.Types.VariableType;
import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.expression.Expression;

public final class ReturnStatement extends Statement {
    public final Expression value;
    public VariableType type;

    public ReturnStatement(Expression value) {
        super();
        this.value = value;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}