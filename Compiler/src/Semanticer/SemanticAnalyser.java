package Semanticer;

import java.util.HashMap;
import java.util.List;

import Semanticer.Components.Exceptions.ValidationException;
import Syntaxer.ast.Program;
import Syntaxer.ast.declaration.ClassDeclaration;

public class SemanticAnalyser {
    public Program program;

    private final HashMap<String, ClassDeclaration> nameToClass = new HashMap<>();

    public SemanticAnalyser(Program p) {
        program = p;
    }

    public void process() {
        constructClassTree();
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
}
