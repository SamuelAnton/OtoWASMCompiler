package Lexer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class App {
    public static void main(String[] args) {
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
        System.out.println();
        System.out.println();
        ArrayList<NamedToken> tokens = l.process(inputString);
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).token == Token.tkIdentifier) {
                System.out.println(tokens.get(i).token + " " + tokens.get(i).identifier);
            } else {
                System.out.println(tokens.get(i).token);
            }
        }
    }
}
