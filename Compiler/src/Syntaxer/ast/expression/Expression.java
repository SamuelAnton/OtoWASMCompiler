package Syntaxer.ast.expression;

import Semanticer.Components.Types.VariableType;
import Syntaxer.ast.ASTNode;

public abstract class Expression extends ASTNode {
    public VariableType type;

    protected Expression(int line, int column) {
        super(line, column);
    }
}
