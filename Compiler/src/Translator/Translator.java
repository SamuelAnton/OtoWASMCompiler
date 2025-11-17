package Translator;

import Syntaxer.ast.Program;

public class Translator {
    final Program program;
    StringBuilder result = new StringBuilder();

    public Translator(Program p) {
        program = p;
    }

    public String translate() {
        return "";
    }

    public void generateFile() {

    }
}
