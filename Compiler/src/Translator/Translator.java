package Translator;

import Syntaxer.ast.Program;

public class Translator {
    final Program program;
    StringBuilder result = new StringBuilder();

    public Translator(Program p) {
        program = p;
    }

    public String translate() {
        PreTranslator t = new PreTranslator();
        t.visit(program);

        return "";
    }

    public void generateFile() {

    }
}
