package Syntaxer.ast;

import Syntaxer.ast.component.Block;
import Syntaxer.ast.component.Param;
import Syntaxer.ast.declaration.ClassDeclaration;
import Syntaxer.ast.declaration.ConstructorDeclaration;
import Syntaxer.ast.declaration.FieldDeclaration;
import Syntaxer.ast.declaration.VarDeclaration;
import Syntaxer.ast.expression.*;
import Syntaxer.ast.declaration.MethodDeclaration;
import Syntaxer.ast.literal.BoolLiteral;
import Syntaxer.ast.literal.IntLiteral;
import Syntaxer.ast.literal.RealLiteral;
import Syntaxer.ast.statement.*;

public interface ASTVisitor<R> {
    // R is return type of visit methods (e.g. Void, String, Type, etc.)

    // Top-level
    R visit(Program n);

    R visit(FieldDeclaration n);

    R visit(ClassDeclaration n);

    R visit(VarDeclaration n);

    R visit(MethodDeclaration n);

    R visit(ConstructorDeclaration n);

    R visit(Param n);

    // Statements
    R visit(Block n);

    R visit(Assign n);

    R visit(IfStatement n);

    R visit(WhileStatement n);

    R visit(ReturnStatement n);

    R visit(AssignmentStatement n);

    // Expressions
    R visit(IntLiteral n);

    R visit(RealLiteral n);

    R visit(BoolLiteral n);

    R visit(ThisExpression n);

    R visit(VariableReference n);

    R visit(MemberAccess n);

    R visit(MethodCall n);

    R visit(ConstructorCall n);
}
