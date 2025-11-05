package Syntaxer.ast;

import Syntaxer.ast.component.*;
import Syntaxer.ast.declaration.*;
import Syntaxer.ast.expression.*;
import Syntaxer.ast.literal.*;
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

    R visit(VariableDeclaration n);

    // Expressions
    R visit(VariableReference n);

    R visit(MemberAccess n);

    R visit(MethodCall n);

    R visit(ConstructorCall n);

    R visit(ThisExpression n);

    R visit(ExpressionStatement expressionStatement);

    R visit(BooleanLiteral boolLiteral);

    R visit(IntegerLiteral intLiteral);

    R visit(RealLiteral realLiteral);

    R visit(StringLiteral stringLiteral);

    R visit(ExtensionType extensionType);

    R visit(ReturnType returnType);

    R visit(ArrayLiteral arrayLiteral);

    R visit(ListLiteral listiteral);

    R visit(Type type);

    R visit(ElseStatement elseStatement);

    R visit(ThenStatement thenStatement);

    R visit(SuperConstructorCall superConstructorCall);
}
