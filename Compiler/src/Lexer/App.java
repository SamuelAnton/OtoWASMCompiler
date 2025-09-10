package Lexer;

import java.util.ArrayList;

public class App {
    public static void main(String[] args) {
        // TODO: Args check in another class
        Lexer l = new Lexer();
        ArrayList<Token> tokens = l.process("class  end   class");
        for (int i = 0; i < tokens.size(); i++) {
            System.out.println(tokens.get(i));
        }
    }
}
