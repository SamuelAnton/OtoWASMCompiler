package Lexer;

// Enum for different tokens
public enum Token {
    tkIdentifier(""), // Users variable
    tkVar("var"), // var
    tkClass("class"), // class
    tkIs("is"), // is
    tkEnd("end"), // end
    tkReturn("return"), // return
    tkThis("this"), // this
    tkMethod("method"), // method
    tkShortBody("=>"), // =>
    tkExtends("extends"), // extends
    tkColumn(":"), // : Example: (var x : Animal())
    tkCall("."), // . Example: c.get()
    tkAssignment(":="), // := Example: n := v
    tkWhile("while"), // while
    tkLoop("loop"), // loop
    tkIf("if"), // if
    tkThen("then"), // then
    tkElse("else"), // else
    tkOpenCircle("("), // (
    tkCloseCircle(")"), // )
    tkOpenSquare("["), // [
    tkCloseSquare("]"), // ]
    tkComma(","), // ,
    ;

    // Label to automate HashMap fill
    public final String label;

    Token(String label) {
        this.label = label;
    }
}