package Lexer;

public enum Token {
    tkIdentifier("var"), // user's var
    tkClass("class"), // class
    tkIs("is"), // is
    tkEnd("end"), // end
    tkReturn("return"), // return
    tkThis("this"), // this
    tkMethod("method"), // method
    tkShortBody("=>"), // =>
    tkExtends("extends"), // extends
    tkAssignment1(":"), // : Example: (var x : Animal())
    tkCall("."), // . Example: c.get()
    tkAssignment2(":="), // := Example: n := v
    tkWhile("while"), // while
    tkLoop("loop"), // loop
    tkIf("if"), // if
    tkOpenCircle("("), // (
    tkCloseCircle(")"), // )
    tkTkOpenSquare("["), // [
    tkCloseSquare("]"), // ]
    ;
    public final String label;
    Token(String label) {
        this.label = label;
    }
}