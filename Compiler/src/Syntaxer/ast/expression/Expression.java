package Syntaxer.ast.expression;

import Syntaxer.ast.ASTNode;

public abstract class Expression extends ASTNode {
    protected Expression() {
        super(-1, -1);
    }
}
