package Translator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

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

        result.append(getBase());

        CodeGenerator2 c = new CodeGenerator2(program);
        result.append(c.translate());

        result.append(")");

        return result;
    }

    public StringBuilder getBase() {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader("wasm_program_base.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder;
    }
}
