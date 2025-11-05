package Syntaxer.ast.declaration;

import Syntaxer.ast.ASTNode;
import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.component.ExtensionType;

import java.util.List;

public class ClassDeclaration extends ASTNode {
    public final String name;
    public final ExtensionType baseClass; // null if none
    public final List<MemberDeclaration> members;

    public List<MethodDeclaration> methodDeclarations;
    public List<FieldDeclaration> fieldDeclarations;
    public List<ConstructorDeclaration> constructorDeclarations;
    public ClassDeclaration superClass;

    public ClassDeclaration(String name, ExtensionType baseClass, List<MemberDeclaration> members) {
        super(-1, -1);
        this.name = name;
        this.baseClass = baseClass;
        this.members = members;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
