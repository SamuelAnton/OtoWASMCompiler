package Semanticer.Components.Checkers;

import java.util.ArrayList;

import Semanticer.Components.Exceptions.ValidationException;
import Syntaxer.ast.declaration.*;
import Syntaxer.ast.declaration.ConstructorDeclaration;

public class ClassMemberAnalyzer {
    private final ClassDeclaration cls;

    public ClassMemberAnalyzer(ClassDeclaration c) {
        cls = c;
    }

    public void analyze() {
        ArrayList<ConstructorDeclaration> constructors = new ArrayList<>();
        ArrayList<FieldDeclaration> fields = new ArrayList<>();
        ArrayList<MethodDeclaration> methods = new ArrayList<>();

        for (MemberDeclaration m : cls.members) {
            if (m instanceof ConstructorDeclaration) {
                constructors.add((ConstructorDeclaration) m);
            } else if (m instanceof FieldDeclaration) {
                fields.add((FieldDeclaration) m);
            } else if (m instanceof MethodDeclaration) {
                methods.add((MethodDeclaration) m);
            } else {
                throw new ValidationException(
                        "Cannot resolve class member to any existing ones (constructor, field, method).");
            }
        }
        cls.constructorDeclarations.addAll(constructors);
        cls.methodDeclarations.addAll(methods);
        cls.fieldDeclarations.addAll(fields);
    }
}
