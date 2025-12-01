package Syntaxer.ast.expression;

import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.declaration.ConstructorDeclaration;

import java.util.List;

public class SuperConstructorCall extends Expression{
    public final List<Expression> args;
    public ConstructorCall constructorCall;
    public ConstructorDeclaration resolvedConstructor;

    public SuperConstructorCall(List<Expression> args) {
        super(-1, -1);
        this.args = args;
    }

     @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
