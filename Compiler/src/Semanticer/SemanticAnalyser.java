package Semanticer;

import Semanticer.Components.Checkers.ClassMemberAnalyzer;
import Semanticer.Components.Exceptions.ValidationException;
import Syntaxer.ast.Program;
import Syntaxer.ast.declaration.ClassDeclaration;
import Syntaxer.ast.declaration.ConstructorDeclaration;
import Syntaxer.ast.statement.ReturnStatement;
import Syntaxer.ast.statement.Statement;
import Syntaxer.ast.statement.WhileStatement;

import java.util.HashMap;
import java.util.List;

public class SemanticAnalyser {
    public Program program;

    private final HashMap<String, ClassDeclaration> nameToClass = new HashMap<>();

    private final ClassMemberAnalyzer classMemberAnalyzer;

    public SemanticAnalyser(Program p, ClassMemberAnalyzer classMemberAnalyzer) {
        program = p;
        this.classMemberAnalyzer = classMemberAnalyzer;
    }

    public void process() {
        constructClassTree();
        classMemberAnalyzer.analyze();
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
            throw new ValidationException("Return statement is not allowed inside constructor of class '" + className + "'");
        }
    }
}
