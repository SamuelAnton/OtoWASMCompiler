package Syntaxer.ast.declaration;

import Syntaxer.ast.ASTNode;
import Syntaxer.ast.expression.MemberDeclaration;

import java.util.List;

public class ClassDeclaration extends ASTNode {
    public final String name;
    public final String baseClass; // null if none
    public final List<MemberDeclaration> members;

    public ClassDeclaration(String name, String baseClass, List<MemberDeclaration> members) {
        super(-1, -1);
        this.name = name;
        this.baseClass = baseClass;
        this.members = members;
    }
}
