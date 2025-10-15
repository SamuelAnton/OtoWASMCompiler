package Syntaxer.ast.statement;

import Syntaxer.ast.component.Block;
import Syntaxer.ast.expression.Expression;

public final class IfStatement extends Statement {
    public final Expression cond;
    public final Block thenBlock;
    public final Block elseBlock;

    public IfStatement(Expression cond, Block thenBlock, Block elseBlock) {
        super();
        this.cond = cond;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
    }
}
