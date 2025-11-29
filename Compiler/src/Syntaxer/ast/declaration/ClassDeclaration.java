package Syntaxer.ast.declaration;

import Syntaxer.ast.ASTNode;
import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.component.ExtensionType;

import java.util.*;

import Semanticer.Components.Types.VariableType;
import Syntaxer.ast.component.Param;

public class ClassDeclaration extends ASTNode {
    public final String name;
    public final ExtensionType baseClass; // null if none
    public final List<MemberDeclaration> members;

    public final List<MethodDeclaration> methodDeclarations = new ArrayList<>();
    public final List<FieldDeclaration> fieldDeclarations = new ArrayList<>();
    public final List<ConstructorDeclaration> constructorDeclarations = new ArrayList<>();

    public List<FieldDeclaration> fieldsList = new ArrayList<>();
    public List<MethodDeclaration> methodsList = new ArrayList<>();
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

    public void fillFieldsList() {
        if (superClass != null) {
            fieldsList.addAll(superClass.fieldDeclarations);
        }
        fieldsList.addAll(fieldDeclarations);
    }

    public int getIndexByField(FieldDeclaration input) {
        for (int i = fieldsList.size() - 1; i >= 0; i--) {
            FieldDeclaration cur = fieldsList.get(i);
            if (cur.name.equals(input.name) && cur.type.type.equals(input.type.type)) {
                return i;
            }
        }
        return -1;
    }

    public void fillMethodsList() {
        if (superClass != null) {
            methodsList.addAll(superClass.methodDeclarations);
        }
        methodsList.addAll(methodDeclarations);
    }

    public int getIndexByMethod(MethodDeclaration input) {
        for (int i = methodsList.size() - 1; i >= 0; i--) {
            MethodDeclaration cur = methodsList.get(i);
            if (cur.name.equals(input.name) && cur.returnType.equals(input.returnType)
                    && cur.params.equals(input.params)) {
                return i;
            }
        }
        return -1;
    }
}
