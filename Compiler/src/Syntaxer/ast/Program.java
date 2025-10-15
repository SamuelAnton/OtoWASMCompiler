package Syntaxer.ast;

import Syntaxer.ast.declaration.ClassDeclaration;

import java.util.List;

public class Program extends ASTNode {
    public final List<ClassDeclaration> classes;

    public Program(List<ClassDeclaration> classes) {
        super(-1, -1);
        this.classes = classes;
    }
}
