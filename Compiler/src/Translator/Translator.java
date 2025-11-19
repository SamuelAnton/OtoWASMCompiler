package Translator;

import Syntaxer.ast.Program;

public class Translator {
    final Program program;
    StringBuilder result = new StringBuilder();

    public Translator(Program p) {
        program = p;
    }

    public StringBuilder translate() {
        // PreTranslator t = new PreTranslator();
        // String res = t.generate(program);

        CodeGenerator c = new CodeGenerator();
        StringBuilder b = c.generate(program);

        return b;
    }

    public void generateFile() {

    }
}
