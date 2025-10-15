package Syntaxer.ast.component;

import Syntaxer.ast.ASTNode;

public final class Param extends ASTNode {
    public final String name;
    public final String typeName;

    public Param(String name, String typeName) {
        super(-1, -1);
        this.name = name;
        this.typeName = typeName;
    }
}
