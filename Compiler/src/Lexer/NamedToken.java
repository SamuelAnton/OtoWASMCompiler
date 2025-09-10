package Lexer;

public class NamedToken {
    public Token token;
    public String identifier;

    NamedToken(Token t) {
        token = t;
    }

    NamedToken(String s) {
        token = Token.tkIdentifier;
        identifier = s;
    }
}