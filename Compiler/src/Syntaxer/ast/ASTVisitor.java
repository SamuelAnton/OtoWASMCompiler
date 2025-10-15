package Syntaxer.ast;

import Syntaxer.ast.component.Param;
import Syntaxer.ast.declaration.ClassDeclaration;
import Syntaxer.ast.declaration.ConstructorDeclaration;
import Syntaxer.ast.declaration.FieldDeclaration;
import Syntaxer.ast.expression.*;
import Syntaxer.ast.declaration.MethodDeclaration;
import Syntaxer.ast.statement.*;

public interface ASTVisitor<R> {
    // R is return type of visit methods (e.g. Void, String, Type, etc.)

    R visit(Program n);

    R visit(ClassDeclaration n);

    R visit(FieldDeclaration n);

    R visit(MethodDeclaration n);

    R visit(ConstructorDeclaration n);

    R visit(Param n);

    // Statements
    R visit(AssignmentStatement n);

    R visit(IfStatement n);

    R visit(WhileStatement n);

    R visit(ReturnStatement n);

    // Expressions
    R visit(VariableReference n);

    R visit(MemberAccess n);

    R visit(MethodCall n);

    R visit(ConstructorCall n);

    R visit(ThisExpression n);
}
