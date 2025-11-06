package Syntaxer.ast.declaration;

import Syntaxer.ast.ASTNode;
import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.component.ExtensionType;

import java.util.ArrayList;
import java.util.List;

import Semanticer.Components.Types.VariableType;

public class ClassDeclaration extends ASTNode {
    public final String name;
    public final ExtensionType baseClass; // null if none
    public final List<MemberDeclaration> members;

    public final List<MethodDeclaration> methodDeclarations = new ArrayList<>();
    public final List<FieldDeclaration> fieldDeclarations = new ArrayList<>();
    public final List<ConstructorDeclaration> constructorDeclarations = new ArrayList<>();
    public ClassDeclaration superClass;

    public VariableType type;

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
