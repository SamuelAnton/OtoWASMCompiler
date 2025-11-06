package Syntaxer.ast;

import Syntaxer.ast.component.*;
import Syntaxer.ast.declaration.*;
import Syntaxer.ast.expression.*;
import Syntaxer.ast.literal.*;
import Syntaxer.ast.statement.*;

import java.util.List;

public class PrettyPrinter implements ASTVisitor<Void> {
    private int indent = 0;

    private void printIndent() {
        System.out.print("  ".repeat(indent));
    }

    private void println(String s) {
        printIndent();
        System.out.println(s);
    }

    private void printList(List<? extends ASTNode> list) {
        if (list != null) {
            for (ASTNode node : list) {
                if (node != null)
                    node.accept(this);
            }
        }
    }

    @Override
    public Void visit(Program n) {
        println("Program");
        indent++;
        printList(n.classes);
        indent--;
        return null;
    }

    @Override
    public Void visit(ClassDeclaration n) {
        println("ClassDeclaration " + n.name +
                (n.baseClass != null ? " extends " + n.baseClass.name : ""));
        indent++;
        printList(n.members);
        indent--;
        return null;
    }

    @Override
    public Void visit(FieldDeclaration n) {
        println("FieldDeclaration " + n.name);
        indent++;
        if (n.init != null)
            n.init.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(MethodDeclaration n) {
        println("MethodDeclaration " + n.name + " : " +
                (n.returnType != null ? " returning " + n.returnType.name : ""));
        indent++;
        if (n.params != null && !n.params.isEmpty()) {
            println("Parameters:");
            indent++;
            printList(n.params);
            indent--;
        }
        if (n.body != null && !n.body.isEmpty()) {
            println("Body:");
            indent++;
            printList(n.body);
            indent--;
        }
        indent--;
        return null;
    }

    @Override
    public Void visit(ConstructorDeclaration n) {
        println("ConstructorDeclaration");
        indent++;
        if (n.params != null && !n.params.isEmpty()) {
            println("Parameters:");
            indent++;
            printList(n.params);
            indent--;
        }
        if (n.body != null && !n.body.isEmpty()) {
            println("Body:");
            indent++;
            printList(n.body);
            indent--;
        }
        indent--;
        return null;
    }

    @Override
    public Void visit(Param n) {
        println("Param " + n.name + " : " + n.type.name);
        return null;
    }

    @Override
    public Void visit(AssignmentStatement n) {
        println("AssignmentStatement");
        indent++;
        println("Target:");
        indent++;
        n.target.accept(this);
        indent--;
        println("Value:");
        indent++;
        n.value.accept(this);
        indent -= 2;
        return null;
    }

    @Override
    public Void visit(IfStatement n) {
        println("IfStatement");
        indent++;
        println("Condition:");
        indent++;
        n.cond.accept(this);
        indent--;
        if (n.thenBody != null) {
            indent++;
            n.thenBody.accept(this);
            indent--;
        }
        if (n.elseBody != null && !n.elseBody.body.isEmpty()) {
            indent++;
            n.elseBody.accept(this);
            indent--;
        }
        indent--;
        return null;
    }

    @Override
    public Void visit(WhileStatement n) {
        println("WhileStatement");
        indent++;
        println("Condition:");
        indent++;
        n.cond.accept(this);
        indent--;
        println("Body:");
        indent++;
        printList(n.body);
        indent -= 2;
        return null;
    }

    @Override
    public Void visit(ReturnStatement n) {
        println("ReturnStatement");
        if (n.value != null) {
            indent++;
            n.value.accept(this);
            indent--;
        }
        return null;
    }

    @Override
    public Void visit(VariableReference n) {
        println("VariableReference " + n.name);
        return null;
    }

    @Override
    public Void visit(MemberAccess n) {
        println("MemberAccess");
        indent++;
        println("Target:");
        indent++;
        n.target.accept(this);
        indent--;
        println("Member:");
        indent++;
        n.member.accept(this);
        indent -= 2;
        return null;
    }

    @Override
    public Void visit(MethodCall n) {
        println("MethodCall");
        indent++;
        println("Target:");
        indent++;
        n.target.accept(this);
        indent--;
        if (n.args != null && !n.args.isEmpty()) {
            println("Arguments:");
            indent++;
            printList(n.args);
            indent--;
        }
        indent--;
        return null;
    }

    @Override
    public Void visit(ConstructorCall n) {
        println("ConstructorCall " + n.className);
        indent++;
        printList(n.args);
        indent--;
        return null;
    }

    @Override
    public Void visit(ThisExpression n) {
        println("ThisExpression");
        return null;
    }

    @Override
    public Void visit(VariableDeclaration n) {
        println("VariableDeclaration " + n.name);
        indent++;
        if (n.init != null)
            n.init.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(ExpressionStatement n) {
        println("expressionStatement");
        indent++;
        if (n.value != null)
            n.value.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(BooleanLiteral n) {
        println("BoolLiteral " + n.value);
        return null;
    }

    @Override
    public Void visit(IntegerLiteral n) {
        println("IntegerLiteral " + n.value);
        return null;
    }

    @Override
    public Void visit(RealLiteral n) {
        println("RealLiteral " + n.value);
        return null;
    }

    @Override
    public Void visit(StringLiteral n) {
        println("StringLiteral " + n.value);
        return null;
    }

    @Override
    public Void visit(ExtensionType n) {
        println("ExtensionType " + n.name);
        return null;
    }

    @Override
    public Void visit(ReturnType n) {
        println("ReturnType " + n.name);
        return null;
    }

    @Override
    public Void visit(ArrayLiteral n) {
        println("ArrayLiteral of type " + n.type + (n.size > 0 ? " of size " + n.size : ""));
        return null;
    }

    @Override
    public Void visit(ListLiteral n) {
        println("ListLiteral of type " + n.type);
        return null;
    }

    @Override
    public Void visit(Type n) {
        println("Type: " + n.name);
        return null;
    }

    @Override
    public Void visit(ElseStatement n) {
        println("Else:");
        printList(n.body);
        return null;
    }

    @Override
    public Void visit(ThenStatement n) {
        println("Then:");
        printList(n.body);
        return null;
    }

    @Override
    public Void visit(SuperConstructorCall n) {
        println("SuperConstructorCall");
        indent++;
        if (n.args != null && !n.args.isEmpty()) {
            println("Arguments:");
            indent++;
            printList(n.args);
            indent--;
        }
        indent--;
        return null;
    }
}
