package Semanticer.Components.Checkers;

import Syntaxer.ast.ASTNode;
import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.Program;
import Syntaxer.ast.component.*;
import Syntaxer.ast.declaration.*;
import Syntaxer.ast.expression.*;
import Syntaxer.ast.literal.*;
import Syntaxer.ast.statement.*;

import java.util.HashSet;
import java.util.List;

import Semanticer.Components.Exceptions.ValidationException;
import Semanticer.Components.Types.ProgramTypes;

public class SmartChecker implements ASTVisitor<Void> {
    private void forEach(List<? extends ASTNode> list) {
        for (ASTNode node : list) {
            if (node != null)
                node.accept(this);
        }
    }

    @Override
    public Void visit(Program n) {
        forEach(n.classes);
        return null;
    }

    @Override
    public Void visit(ClassDeclaration n) {
        forEach(n.fieldDeclarations);
        forEach(n.methodDeclarations);
        forEach(n.constructorDeclarations);
        return null;
    }

    @Override
    public Void visit(FieldDeclaration n) {
        n.init.accept(this);
        n.dynamicType = n.init.type;
        n.staticType = n.init.type;
        return null;
    }

    @Override
    public Void visit(MethodDeclaration n) {
        n.returnType.accept(this);

        // Check params
        HashSet<String> paramNames = new HashSet<>();
        for (Param p : n.params) {
            p.baseMethod = n;
            p.accept(this);
            if (paramNames.contains(p.name)) {
                throw new ValidationException(
                        "Duplication of parameter " + p.name + " in method " + n.name + " in class " + n.baseClass);
            }
            paramNames.add(p.name);
        }

        forEach(n.body);
        return null;
    }

    @Override
    public Void visit(ConstructorDeclaration n) {
        // Check params
        HashSet<String> paramNames = new HashSet<>();
        for (Param p : n.params) {
            p.accept(this);
            if (paramNames.contains(p.name)) {
                throw new ValidationException(
                        "Duplication of parameter " + p.name + " in constructor of class " + n.baseClass);
            }
            paramNames.add(p.name);
        }

        forEach(n.body);
        return null;
    }

    @Override
    public Void visit(Param n) {
        n.type = ProgramTypes.toVariableType(n.t.name);
        if (n.type == null) {
            if (n.baseMethod != null) {
                throw new ValidationException("Undeclared type: " + n.t.name + " of parameter " + n.name + " in method "
                        + n.baseMethod.name + " in class " + n.baseMethod.baseClass);
            } else if (n.baseConstructor != null) {
                throw new ValidationException("Undeclared type: " + n.t.name + " of parameter " + n.name
                        + " in constructor of class " + n.baseConstructor.baseClass.name);
            } else {
                throw new ValidationException("Undeclared type:" + n.t.name + " of parameter " + n.name);
            }
        }
        return null;
    }

    @Override
    public Void visit(AssignmentStatement n) {
        n.target.accept(this);
        n.value.accept(this);
        if (!ProgramTypes.canCast(n.target.type, n.value.type)) {
            throw new ValidationException(
                    "Cannot assign value of " + n.value.type + " type to variable of type " + n.target.type);
        }
        return null;
    }

    @Override
    public Void visit(IfStatement n) {
        n.cond.accept(this);
        if (!ProgramTypes.canCast(ProgramTypes.Boolean, n.cond.type)) {
            throw new ValidationException("Condition expression is not of Boolean type");
        }
        n.thenBody.accept(this);
        n.elseBody.accept(this);
        return null;
    }

    @Override
    public Void visit(WhileStatement n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(ReturnStatement n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(VariableDeclaration n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(VariableReference n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(MemberAccess n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(MethodCall n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(ConstructorCall n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(ThisExpression n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(ExpressionStatement expressionStatement) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(BooleanLiteral boolLiteral) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(IntegerLiteral intLiteral) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(RealLiteral realLiteral) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(StringLiteral stringLiteral) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(ExtensionType extensionType) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(ReturnType returnType) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(ArrayLiteral arrayLiteral) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(ListLiteral listiteral) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(Type type) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(ElseStatement elseStatement) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(ThenStatement thenStatement) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(SuperConstructorCall superConstructorCall) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

}
