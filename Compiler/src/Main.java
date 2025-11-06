import Lexer.Lexer;

import Semanticer.optimizer.ProgramOptimizer;
import Syntaxer.ast.PrettyPrinter;
import Syntaxer.ast.Program;
import Syntaxer.parser.Parser;

import Semanticer.SemanticAnalyser;
import Semanticer.Components.Exceptions.ValidationException;

import java.io.BufferedReader;
import java.io.FileReader;
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
        // PrettyPrinter p = new PrettyPrinter();
        // res.accept(p);

        System.out.println();
        System.out.println();

        // SemanticAnalyser analyser = new SemanticAnalyser(res);
        // analyser.process();
    }
}