package Syntaxer.ast.statement;

import Syntaxer.ast.ASTNode;

public abstract class Statement extends ASTNode {
    protected Statement() {
        super(-1, -1);
    }
}
