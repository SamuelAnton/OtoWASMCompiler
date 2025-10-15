package Syntaxer.ast;

import Syntaxer.ast.declaration.ClassDeclaration;

import java.util.List;

public class Program extends ASTNode {
    public final List<ClassDeclaration> classes;

    public Program(List<ClassDeclaration> classes) {
        super(-1, -1);
        this.classes = classes;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
