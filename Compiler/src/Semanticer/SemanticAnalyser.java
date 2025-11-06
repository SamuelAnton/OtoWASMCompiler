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
    private final HashMap<String, Set<ConstructorDeclaration>> classToConstructors = new HashMap<>();
    private final HashMap<String, Set<FieldDeclaration>> classToFields = new HashMap<>();

    public SemanticAnalyser(Program p) {
        program = p;
    }

    // Process of semantic analysis (several tree traverses with checks and
    // optimizations)
    public void process() {
        constructClassTree(); // Get classes with check on circular inheritance, same name
        analyzeClassMembers(); // Get class members with checks
        analyzeKeywordUsage();
    }

    // Get classes with check on circular inheritance, same name
    private void constructClassTree() {
        List<ClassDeclaration> classes = program.classes;
        // Collect all names
        for (ClassDeclaration c : classes) {
            if (nameToClass.containsKey(c.name)) {
                throw new ValidationException("Class " + c.name + " have already been declared.");
            }
            nameToClass.put(c.name, c);
        }
        // Resole inheretence
        for (ClassDeclaration c : classes) {
            if (c.baseClass != null) {
                c.superClass = nameToClass.get(c.baseClass.name);

                // Check circular inheretence
                checkInheretence(c);

                // Check inheretence from existing class
                if (c.superClass == null) {
                    throw new ValidationException("Class " + c.name + " extends undefined class " + c.baseClass.name);
                }
            }
        }
    }

    // Check circular inheretence
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

    // Analyze each class
    private void analyzeClassMembers() {
        for (ClassDeclaration c : nameToClass.values()) {
            analyzeClassMember(c);
        }
    }

    // Analyze class with checks
    private void analyzeClassMember(ClassDeclaration c) {
        // If class was already analyzed
        if (classToFields.containsKey(c.name)) {
            return;
        }

        // Differ class members into fields, methods and constructors
        ClassMemberAnalyzer analyzer = new ClassMemberAnalyzer(c);
        analyzer.analyze();

        // Collect fields
        HashSet<FieldDeclaration> fields = new HashSet<>();
        for (FieldDeclaration f : c.fieldDeclarations) {
            // Check name duplication
            for (FieldDeclaration fd : fields) {
                if (fd.name == f.name) {
                    throw new ValidationException(
                            "Several fields of same name: '" + f.name + "' are in class " + c.name);
                }
            }
            // Add field
            fields.add(f);
        }
        classToFields.put(c.name, fields);

        // Collect methods
        HashSet<MethodDeclaration> methods = new HashSet<>();
        for (MethodDeclaration m : c.methodDeclarations) {
            // Check method signature duplication
            for (MethodDeclaration md : methods) {
                if (m.sameSignature(md)) {
                    throw new ValidationException(
                            "Several methods with same signature: '" + m.name + "' are in class " + c.name);
                }
            }
            // Add method
            methods.add(m);
        }
        classToMethods.put(c.name, methods);

        // Collect constructors

        // Collect values from super class
        if (c.superClass != null) {
            // Make sure super class has been analyzed
            analyzeClassMember(c.superClass);

            // Add not shadowed fields
            for (FieldDeclaration f : classToFields.get(c.superClass.name)) {
                for (FieldDeclaration fd : fields) {
                    if (fd.name != f.name) {
                        fields.add(f);
                    }
                }
            }

            // Add noot shadowed methods
            for (MethodDeclaration m : classToMethods.get(c.superClass.name)) {
                for (MethodDeclaration md : methods) {
                    if (!md.sameSignature(m)) {
                        methods.add(m);
                    }
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
            throw new ValidationException(
                    "Return statement is not allowed inside constructor of class '" + className + "'");
        }
    }
}
