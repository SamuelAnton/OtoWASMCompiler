package Lexer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class App {
    public static void main(String[] args) {
        // TODO: Args check in another class
        StringBuilder builder = new StringBuilder();
        Lexer l = new Lexer();
        try (BufferedReader reader = new BufferedReader(new FileReader("input.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        String inputString = builder.toString();
        ArrayList<Token> tokens = l.process(inputString);
        for (int i = 0; i < tokens.size(); i++) {
            System.out.println(tokens.get(i));
        }
    }
}
