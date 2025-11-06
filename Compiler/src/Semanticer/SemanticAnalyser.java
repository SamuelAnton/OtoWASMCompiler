package Semanticer;

import Semanticer.Components.Checkers.ClassMemberAnalyzer;
import Semanticer.Components.Exceptions.ValidationException;
import Syntaxer.ast.Program;
import Syntaxer.ast.declaration.ClassDeclaration;
import Syntaxer.ast.declaration.ConstructorDeclaration;
import Syntaxer.ast.declaration.FieldDeclaration;
import Syntaxer.ast.declaration.MethodDeclaration;
import Syntaxer.ast.statement.ReturnStatement;
import Syntaxer.ast.statement.Statement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SemanticAnalyser {
    public Program program;

    private final HashMap<String, ClassDeclaration> nameToClass = new HashMap<>();
    private final HashMap<String, Set<MethodDeclaration>> classToMethods = new HashMap<>();
    private final HashMap<String, Set<String>> classToFields = new HashMap<>();

    public SemanticAnalyser(Program p) {
        program = p;
    }

    public void process() {
        constructClassTree();
        analyzeClassMembers();
        analyzeKeywordUsage();
    }

    private void constructClassTree() {
        List<ClassDeclaration> classes = program.classes;
        for (ClassDeclaration c : classes) {
            if (nameToClass.containsKey(c.name)) {
                throw new ValidationException("Class " + c.name + " have already been declared.");
            }
            nameToClass.put(c.name, c);
        }
        for (ClassDeclaration c : classes) {
            if (c.baseClass != null) {
                c.superClass = nameToClass.get(c.baseClass.name);

                checkInheretence(c);

                if (c.superClass == null) {
                    throw new ValidationException("Class " + c.name + " extends undefined class " + c.baseClass.name);
                }
            }
        }
    }

    private void checkInheretence(ClassDeclaration c) {
        ClassDeclaration cur = c;
        StringBuilder cs = new StringBuilder();
        cs.append(cur.name);
        cur = cur.superClass;
        while (cur != null) {
            cs.append(" -> ").append(cur.name);
            if (cur.name == c.name) {
                throw new ValidationException("Circle inheretence of classes: " + cs.toString());
            }
        }
    }

    private void analyzeClassMembers() {
        for (ClassDeclaration c : nameToClass.values()) {
            analyzeClassMember(c);
        }
    }

    private void analyzeClassMember(ClassDeclaration c) {
        if (classToFields.containsKey(c.name)) {
            return;
        }

        ClassMemberAnalyzer analyzer = new ClassMemberAnalyzer(c);
        analyzer.analyze();

        HashSet<String> fields = new HashSet<>();
        for (FieldDeclaration f : c.fieldDeclarations) {
            if (fields.contains(f.name)) {
                throw new ValidationException(
                        "Several fields of same name: '" + f.name + "' are in class " + c.name);
            }
            fields.add(f.name);
        }
        classToFields.put(c.name, fields);

        HashSet<MethodDeclaration> methods = new HashSet<>();
        for (MethodDeclaration m : c.methodDeclarations) {
            for (MethodDeclaration md : methods) {
                if (m.sameSignature(md)) {
                    throw new ValidationException(
                            "Several methods with same signature: '" + m.name + "' are in class " + c.name);
                }
            }
            methods.add(m);
        }
        classToMethods.put(c.name, methods);

        if (c.superClass != null) {
            analyzeClassMember(c.superClass);

        }
    }

    private void analyzeKeywordUsage() {
        for (ClassDeclaration cls : nameToClass.values()) {
            for (ConstructorDeclaration ctor : cls.constructorDeclarations) {
                checkNoReturnInConstructor(cls, ctor);
            }
        }
    }

    private void checkNoReturnInConstructor(ClassDeclaration cls, ConstructorDeclaration ctor) {
        for (Statement stmt : ctor.body) {
            checkNoReturnRecursive(stmt, cls.name);
        }
    }

    private void checkNoReturnRecursive(Statement stmt, String className) {
        if (stmt instanceof ReturnStatement) {
            throw new ValidationException(
                    "Return statement is not allowed inside constructor of class '" + className + "'");
        }
    }
}
