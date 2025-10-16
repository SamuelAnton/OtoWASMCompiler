package Syntaxer.ast;

public abstract class ASTNode {
    public final int line;
    public final int column;

    protected ASTNode(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public abstract <R> R accept(ASTVisitor<R> visitor);
}
