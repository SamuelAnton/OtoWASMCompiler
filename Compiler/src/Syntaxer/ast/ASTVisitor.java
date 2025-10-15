package Syntaxer.ast;

import Syntaxer.ast.component.Block;
import Syntaxer.ast.component.Param;
import Syntaxer.ast.declaration.ClassDeclaration;
import Syntaxer.ast.declaration.ConstructorDeclaration;
import Syntaxer.ast.expression.CallOrAccess;
import Syntaxer.ast.expression.MethodDeclaration;
import Syntaxer.ast.expression.ThisExpression;
import Syntaxer.ast.expression.VarDeclaration;
import Syntaxer.ast.literal.BoolLiteral;
import Syntaxer.ast.literal.IntLiteral;
import Syntaxer.ast.literal.RealLiteral;
import Syntaxer.ast.statement.Assign;
import Syntaxer.ast.statement.IfStatement;
import Syntaxer.ast.statement.WhileStatement;

public interface ASTVisitor<R> {
    // R is return type of visit methods (e.g. Void, String, Type, etc.)

    // Top-level
    R visit(Program n);

    R visit(ClassDeclaration n);

    R visit(VarDeclaration n);

    R visit(MethodDeclaration n);

    R visit(ConstructorDeclaration n);

    R visit(Param n);

    R visit(Block n);

    // Statements
    R visit(Assign n);

    R visit(IfStatement n);

    R visit(WhileStatement n);

    // Expressions
    R visit(IntLiteral n);

    R visit(RealLiteral n);

    R visit(BoolLiteral n);

    R visit(ThisExpression n);

    R visit(CallOrAccess n);
}
