package Lexer;

public enum Token {
    tkIdentifier, // user's var
    tkClass, // class
    tkIs, // is
    tkEnd, // end
    tkReturn, // return
    tkThis, // this
    tkMethod, // method
    tkShortBody, // =>
    tkExtends, // extends
    tkAssignment, // : (var x : Animal())
}