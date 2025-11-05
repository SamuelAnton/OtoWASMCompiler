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
    private final HashMap<String, Set<String>> classToMethods = new HashMap<>();
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
                if (c.superClass == null) {
                    throw new ValidationException("Class " + c.name + " extends undefined class " + c.baseClass.name);
                }
            }
        }
    }

    private void analyzeClassMembers() {
        for (ClassDeclaration c : nameToClass.values()) {
            ClassMemberAnalyzer analyzer = new ClassMemberAnalyzer(c);
            analyzer.analyze();

            HashSet<String> fields = new HashSet<>();
            for (FieldDeclaration f : c.fieldDeclarations) {
                fields.add(f.name);
            }
            classToFields.put(c.name, fields);

            HashSet<String> methods = new HashSet<>();
            for (MethodDeclaration m : c.methodDeclarations) {
                methods.add(m.name);
            }
            classToMethods.put(c.name, methods);
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
