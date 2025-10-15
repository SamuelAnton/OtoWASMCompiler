package Syntaxer.ast.expression;


import Syntaxer.ast.component.Block;
import Syntaxer.ast.component.Param;

import java.util.List;

public final class MethodDeclaration extends MemberDeclaration {
    public final String name;
    public final List<Param> params;
    public final String returnType;
    public final Block body;

    public MethodDeclaration(String name, List<Param> params, String returnType, Block body) {
        super();
        this.name = name;
        this.params = params;
        this.returnType = returnType;
        this.body = body;
    }
}
