package Lexer;

// Enum for different tokens
public enum Token {
    // Special tokens
    tkEOF("EOF", 0),
    tkIdentifier("IDENTIFIER", 257), // Users variable
    tkNumber("NUMBER", 259), // const int
    tkRealLiteral("REAL_LITERAL", 260), // const real
    tkBooleanLiteral("BOOLEAN_LITERAL", 261), // const bool
    tkStringLiteral("STRING_LITERAL", 258), // const string
    // Keywords
    tkVar("var", 262), // var
    tkClass("class", 263), // class
    tkIs("is", 264), // is
    tkEnd("end", 265), // end
    tkReturn("return", 266), // return
    tkThis("this", 267), // this
    tkMethod("method", 268), // method
    tkExtends("extends", 269), // extends
    tkWhile("while", 270), // while
    tkLoop("loop", 271), // loop
    tkIf("if", 272), // if
    tkElse("else", 273), // else
    tkThen("then", 274), // then
    // Operators and delimeters
    tkColon(":", 275), // : Example: (var x : Animal())
    tkDot(".", 276), // . Example: c.get()
    tkComma(",", 277), // ,
    tkLParen("(", 278), // (
    tkRParen(")", 279), // )
    tkLBracket("[", 280), // [
    tkRBracket("]", 281), // ]
    tkShortBody("=>", 282), // =>
    tkAssign(":=", 283), // := Example: n := v
    // Complex data structures
    tkArray("Array", 284), // array
    tkList("List", 285);

    // Label to automate HashMap fill
    public final String label;
    public final int value; // value for bison parser

    Token(String label, int value) {
        this.label = label;
        this.value = value;
    }
}