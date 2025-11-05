package Semanticer;

import java.util.HashMap;
import java.util.List;

import Semanticer.Components.Exceptions.ValidationException;
import Syntaxer.ast.Program;
import Syntaxer.ast.declaration.ClassDeclaration;

public class SemanticAnalyser {
    public Program program;

    private HashMap<String, ClassDeclaration> nameToClass;

    public SemanticAnalyser(Program p) {
        program = p;
    }

    public void process() {
        // Many tree traverses
        List<ClassDeclaration> classes = program.classes;
        constructClassTree(classes);
    }

    private void constructClassTree(List<ClassDeclaration> classes) {

    }
}
