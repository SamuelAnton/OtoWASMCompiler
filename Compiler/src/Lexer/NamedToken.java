package Lexer;

// Token with additional string for user's identifiers
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