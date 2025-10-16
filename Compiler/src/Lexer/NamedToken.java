package Lexer;

// Token with additional string for user's identifiers
public class NamedToken {
    public Token token;
    public String value;

    NamedToken(Token t) {
        token = t;
        value = t.label;
    }

    NamedToken(Token t, String v) {
        token = t;
        value = v;
    }

    public int getType() {
        return token.value;
    }

    public String getValue() {
        return value;
    }

    public Token getToken() {
        return token;
    }
}