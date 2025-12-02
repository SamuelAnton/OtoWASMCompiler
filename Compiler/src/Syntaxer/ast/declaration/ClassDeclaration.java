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
    public List<MemberDeclaration> methodsList = new ArrayList<>();
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
            fieldsList.addAll(superClass.fieldsList);
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
            methodsList.addAll(superClass.methodsList);
        }
        methodsList.addAll(constructorDeclarations);
        boolean setted = false;
        for (MethodDeclaration m : methodDeclarations) {
            for (int i = 0; i < methodsList.size(); i++) {
                if (methodsList.get(i) instanceof MethodDeclaration) {
                    if (checkSameSignature(m, (MethodDeclaration) methodsList.get(i))) {
                        methodsList.set(i, m);
                        setted = true;
                        break;
                    }
                }
            }
            if (!setted) {
                methodsList.add(m);
            }
        }
    }

    private boolean checkSameSignature(MethodDeclaration m1, MethodDeclaration m2) {
        if (m1.name == m2.name) {
            if (m1.params.size() == m2.params.size()) {
                for (int i = 0; i < m1.params.size(); i++) {
                    if (m1.params.get(i).type.type != m2.params.get(i).type.type) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public int getIndexByMethod(MemberDeclaration input) {
        for (int i = methodsList.size() - 1; i >= 0; i--) {
            MemberDeclaration cur = methodsList.get(i);
            if (cur instanceof MethodDeclaration curMethod && input instanceof MethodDeclaration inputMethod) {
                if (curMethod.name.equals(inputMethod.name) && curMethod.returnType.equals(inputMethod.returnType)
                        && curMethod.params.equals(inputMethod.params)) {
                    return i;
                }
            }
            if (cur instanceof ConstructorDeclaration curConstructor
                    && input instanceof ConstructorDeclaration inputCons) {
                if (curConstructor.params.equals(inputCons.params)) {
                    return i;
                }
            }
        }
        return -1;
    }
}
