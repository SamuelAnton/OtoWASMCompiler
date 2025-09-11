package Lexer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

// Demonstration of Lexer work. Will disapear in future
public class App {
    public static void main(String[] args) {
        // Collecting program text from file
        StringBuilder builder = new StringBuilder();
        Lexer l = new Lexer();
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

        // Lexer work
        ArrayList<NamedToken> tokens = l.process(inputString);

        // Output produced tokens
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).token == Token.tkIdentifier) {
                System.out.println(tokens.get(i).token + " " + tokens.get(i).identifier);
            } else {
                System.out.println(tokens.get(i).token);
            }
        }
    }
}
