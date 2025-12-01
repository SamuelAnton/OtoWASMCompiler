package Semanticer;

import Semanticer.Components.Checkers.ClassMemberAnalyzer;
import Semanticer.Components.Checkers.SmartChecker;
import Semanticer.Components.Exceptions.ValidationException;
import Semanticer.Components.Types.ProgramTypes;
import Semanticer.optimizer.ProgramOptimizer;
import Syntaxer.ast.Program;
import Syntaxer.ast.component.Param;
import Syntaxer.ast.declaration.ClassDeclaration;
import Syntaxer.ast.declaration.ConstructorDeclaration;
import Syntaxer.ast.declaration.FieldDeclaration;
import Syntaxer.ast.declaration.MethodDeclaration;
import Syntaxer.ast.expression.ConstructorCall;
import Syntaxer.ast.expression.Expression;
import Syntaxer.ast.expression.SuperConstructorCall;
import Syntaxer.ast.statement.*;

import java.util.*;

public class SemanticAnalyser {
    public Program program;

    private final HashMap<String, ClassDeclaration> nameToClass = new HashMap<>();
    private final HashMap<String, Set<MethodDeclaration>> classToMethods = new HashMap<>();
    private final HashMap<String, Set<ConstructorDeclaration>> classToConstructors = new HashMap<>();
    private final HashMap<String, Set<FieldDeclaration>> classToFields = new HashMap<>();

    public SemanticAnalyser(Program p) {
        new ProgramTypes().fillTypes();
        program = p;
    }

    // Process of semantic analysis (several tree traverses with checks and
    // optimizations)
    public void process() {
        constructClassTree(); // Get classes with check on circular inheritance, same name
        analyzeClassMembers(); // Get class members with checks
        analyzeKeywordUsage();
        checkReturnCoverage();

        SmartChecker smartChecker = new SmartChecker();
        smartChecker.visit(program);

        ProgramOptimizer optimizer = new ProgramOptimizer();
        optimizer.optimize(program);
        checkConstructorsHaveSuperCalls();
        resolveSuperConstructorCalls();


        // PrettyPrinter printer = new PrettyPrinter();
        // program.accept(printer);
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

            // Define new types
            c.type = ProgramTypes.newType(c.name);
        }
        Set<String> foundClasses = new HashSet<>();
        for (ClassDeclaration curClass : classes) {
            if (curClass.baseClass != null) {
                if (!foundClasses.contains(curClass.baseClass.name)) {
                    throw new ValidationException("Incorrect order of classes!");
                }
            }
            foundClasses.add(curClass.name);
        }
        // Resole inheretence
        for (ClassDeclaration c : classes) {
            if (c.baseClass != null) {
                ClassDeclaration baseClass = nameToClass.get(c.baseClass.name);
                c.superClass = baseClass;
                c.type.baseType = baseClass.type;

                // Check circular inheretence
                checkInheretence(c);

                // Check inheretence from existing class
                if (c.superClass == null) {
                    throw new ValidationException("Class " + c.name + " extends undefined class " + c.baseClass.name);
                }
            }
        }
    }

    private void checkConstructorsHaveSuperCalls() {
        for (ClassDeclaration cls : program.classes) {
            if (cls.baseClass == null)
                continue;
            if (cls.constructorDeclarations.isEmpty()) {
                throw new ValidationException(
                        "Constructor of class '" + cls.name
                                + "' must call super(...), but constructor body is empty."
                );
            }
            for (ConstructorDeclaration cons : cls.constructorDeclarations) {
                if (cons.body == null || cons.body.isEmpty()) {
                    throw new ValidationException(
                            "Constructor of class '" + cls.name
                                    + "' must call super(...), but constructor body is empty."
                    );
                }

                Statement first = cons.body.getFirst();
                boolean hasSuperCall = false;
                if (first instanceof ExpressionStatement es &&
                        es.value instanceof SuperConstructorCall sc) {
                    hasSuperCall = true;
                }
                if (!hasSuperCall) {
                    throw new ValidationException(
                            "Constructor of class '" + cls.name
                                    + "' must begin with super(...). Missing super call."
                    );
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
            if (Objects.equals(cur.name, c.name)) {
                throw new ValidationException("Circle inheretence of classes: " + cs.toString());
            }
            cur = cur.superClass;
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
                if (Objects.equals(fd.name, f.name)) {
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
            m.baseClass = c;
            c.type.methods.put(m.name, m);
        }
        classToMethods.put(c.name, methods);

        // Collect constructors
        HashSet<ConstructorDeclaration> constructors = new HashSet<>();
        for (ConstructorDeclaration cons : c.constructorDeclarations) {
            for (ConstructorDeclaration cd : constructors) {
                if (cd.sameSignature(cons)) {
                    throw new ValidationException("Several constructors with same signature in class " + c.name);
                }
            }
            constructors.add(cons);
            cons.baseClass = c;
        }
        // Add constructors
        classToConstructors.put(c.name, constructors);

        // Collect values from super class
        if (c.superClass != null) {
            // Make sure super class has been analyzed
            analyzeClassMember(c.superClass);

            // Add not shadowed fields
            for (FieldDeclaration f : classToFields.get(c.superClass.name)) {
                for (FieldDeclaration fd : fields) {
                    if (!Objects.equals(fd.name, f.name)) {
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
        if (stmt instanceof IfStatement) {
            checkNoReturnRecursive(((IfStatement) stmt).thenBody, className);
            checkNoReturnRecursive(((IfStatement) stmt).elseBody, className);
        }
        if (stmt instanceof WhileStatement) {
            for (Statement s : ((WhileStatement) stmt).body)
                checkNoReturnRecursive(s, className);
        }
    }

    private void checkReturnCoverage() {
        for (ClassDeclaration cls : nameToClass.values()) {
            if (cls.methodDeclarations.isEmpty())
                continue;

            for (MethodDeclaration m : cls.methodDeclarations) {
                // skip methods without return type or explicitly "void"
                if (m.returnType == null || m.returnType.name == null || m.returnType.name.equals("null"))
                    continue;

                boolean hasReturnPath = hasReturnOnAllPaths(m.body);

                if (!hasReturnPath) {
                    throw new ValidationException(
                            "Method " + m.name +
                                    " in class " + cls.name +
                                    " does not return a value on all control paths.");
                }
            }
        }
    }

    private boolean hasReturnOnAllPaths(List<Statement> body) {
        if (body == null || body.isEmpty())
            return false;

        for (int i = 0; i < body.size(); i++) {
            Statement stmt = body.get(i);

            if (stmt instanceof ReturnStatement rs) {
                // must have an expression for typed methods
                return rs.value != null;
            }

            if (stmt instanceof IfStatement ifs) {
                boolean thenReturns = hasReturnOnAllPaths(ifs.thenBody.body);
                boolean elseReturns = ifs.elseBody != null && hasReturnOnAllPaths(ifs.elseBody.body);
                // only if both branches guarantee return, continue
                if (thenReturns && elseReturns)
                    return true;
            }

            if (stmt instanceof WhileStatement ws) {
                boolean loopReturns = hasReturnOnAllPaths(ws.body);
                if (loopReturns)
                    return true;
            }
        }

        // if no return found at end then fail
        return false;
    }

    private void resolveSuperConstructorCalls() {
        for (ClassDeclaration cls : program.classes) {
            if (cls.baseClass == null) continue;
            String baseName = cls.baseClass.name;
            ClassDeclaration superCls = nameToClass.get(baseName);

            if (superCls == null)
                throw new ValidationException("Unknown base class " + baseName);

            List<ConstructorDeclaration> superCons = superCls.constructorDeclarations;

            for (ConstructorDeclaration cons : cls.constructorDeclarations) {

                SuperConstructorCall call = findSuperConstructorCall(cons.body);
                if (call == null)
                    throw new ValidationException(cls.name +
                            " constructor must call super(...)");

                ConstructorDeclaration match =
                        resolveMatchingConstructor(superCons, call);

                if (match == null)
                    throw new ValidationException("No matching constructor in " +
                            baseName + " for super(...)");

                call.constructorCall = new ConstructorCall(baseName, call.args);
                call.resolvedConstructor = match;
            }
        }
    }

    private SuperConstructorCall findSuperConstructorCall(List<Statement> body) {
        if (body == null) return null;

        for (Statement s : body) {
            if (s instanceof ExpressionStatement es &&
                    es.value instanceof SuperConstructorCall sc) {
                return sc;
            }
        }
        return null;
    }

    private ConstructorDeclaration resolveMatchingConstructor(
            List<ConstructorDeclaration> superConstructors,
            SuperConstructorCall call) {
        for (ConstructorDeclaration cand : superConstructors) {
            if (cand.params.size() != call.args.size())
                continue;
            boolean ok = true;

            for (int i = 0; i < cand.params.size(); i++) {
                Param p = cand.params.get(i);
                Expression arg = call.args.get(i);
                String expectedType = p.t.name;

                if (!expectedType.equals(arg.type.type)) {
                    ok = false;
                    break;
                }
            }
            if (ok) return cand;
        }

        return null;
    }
}
