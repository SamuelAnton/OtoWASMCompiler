package Syntaxer.ast.expression;

public class VarDeclaration extends MemberDeclaration {
    public final String name;
    public final Expression init;

    public VarDeclaration(String name, Expression init) {
        super();
        this.name = name;
        this.init = init;
    }

}
