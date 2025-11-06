package Semanticer.optimizer;


import Syntaxer.ast.Program;

public class ProgramOptimizer {

    private final DeadLocalRemover deadLocalRemover = new DeadLocalRemover();
    private final UnreachableCodeRemover unreachableCodeRemover = new UnreachableCodeRemover();
    private final IfSimplifier ifSimplifier = new IfSimplifier();

    public void optimize(Program program) {
        System.out.println("Running UnreachableCodeRemover...");
        unreachableCodeRemover.runOnProgram(program);

        System.out.println("Running DeadLocalRemover...");
        deadLocalRemover.runOnProgram(program);

        System.out.println("Running IfSimplifier...");
        ifSimplifier.runOnProgram(program);

        System.out.println("Optimization complete.");
    }
}
