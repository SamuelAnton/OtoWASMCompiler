import Lexer.Lexer;

import Semanticer.optimizer.ProgramOptimizer;
import Syntaxer.ast.PrettyPrinter;
import Syntaxer.ast.Program;
import Syntaxer.parser.Parser;
import Translator.CodeGenerator2;
import Translator.Translator;
import Semanticer.SemanticAnalyser;
import Semanticer.Components.Exceptions.ValidationException;
import Semanticer.Components.Types.ProgramTypes;
import Semanticer.Components.Types.VariableType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        // Collecting program text from file
        StringBuilder builder = new StringBuilder();
        // OtoWASMCompiler/input.txt | input.txt
        try (BufferedReader reader = new BufferedReader(new FileReader("input.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String inputString = builder.toString();

        // Just empty lines, to deal with trash values in output
        System.out.println();
        System.out.println();

        // Create lexer and parser
        Lexer lexer = new Lexer(inputString);
        // for (int i = 0; i < 30; i++) {
        // System.out.println(lexer.nextToken().value);
        // System.out.println();
        // }
        Parser parser = new Parser(lexer);
        parser.yyparse();
        Program res = parser.getParserResult();

        // System.out.println();
        // System.out.println();

        SemanticAnalyser analyser = new SemanticAnalyser(res);
        try {
            analyser.process();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }

        Translator translator = new Translator(res);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));
            writer.write(translator.translate().toString());
            writer.close();
        } catch (IOException e) {
            System.err.println("IOException");
        }

        // PrettyPrinter p = new PrettyPrinter();
        // res.accept(p);

        // CodeGenerator2 codeGenerator2 = new CodeGenerator2(res);
        // System.out.println(codeGenerator2.translate());
    }
}