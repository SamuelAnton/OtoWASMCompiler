package Semanticer.Components.Checkers;

import java.util.ArrayList;

import Semanticer.Components.Exceptions.ValidationException;
import Syntaxer.ast.declaration.*;
import Syntaxer.ast.declaration.ConstructorDeclaration;

public class ClassMemberAnalyzer {
    private final ClassDeclaration cls;
    private final ArrayList<ConstructorDeclaration> constructors = new ArrayList<>();
    private final ArrayList<FieldDeclaration> fields = new ArrayList<>();
    private final ArrayList<MethodDeclaration> methods = new ArrayList<>();

    public ClassMemberAnalyzer(ClassDeclaration c) {
        cls = c;
    }

    public void analyze() {
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
    }

    public ArrayList<ConstructorDeclaration> getConstructors() {
        return constructors;
    }

    public ArrayList<FieldDeclaration> getFields() {
        return fields;
    }

    public ArrayList<MethodDeclaration> getMethods() {
        return methods;
    }
}
