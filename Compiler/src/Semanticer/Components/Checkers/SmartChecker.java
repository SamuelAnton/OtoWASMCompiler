package Semanticer.Components.Checkers;

import Syntaxer.ast.ASTNode;
import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.Program;
import Syntaxer.ast.component.*;
import Syntaxer.ast.declaration.*;
import Syntaxer.ast.expression.*;
import Syntaxer.ast.literal.*;
import Syntaxer.ast.statement.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import Semanticer.Components.Exceptions.ValidationException;
import Semanticer.Components.Types.ProgramTypes;
import Semanticer.Components.Types.VariableType;

public class SmartChecker implements ASTVisitor<Void> {
    private ClassDeclaration curClass;
    private MethodDeclaration curMethod;
    private ConstructorDeclaration curConstructor;
    private Boolean inMethod;
    private Boolean inConstructor;

    private HashMap<String, VariableType> classScope = new HashMap<>();
    private HashMap<String, VariableType> methodScope = new HashMap<>();

    private String where() {
        if (inMethod) {
            return " in method " + curMethod.name + " in class " + curMethod.baseClass.name;
        } else if (inConstructor) {
            return " in constructor of class " + curConstructor.baseClass.name;
        }
        return "";
    }

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
        curClass = n;
        classScope = new HashMap<>();
        methodScope = new HashMap<>();
        forEach(n.fieldDeclarations);
        forEach(n.methodDeclarations);
        forEach(n.constructorDeclarations);
        return null;
    }

    @Override
    public Void visit(FieldDeclaration n) {
        n.init.accept(this);
        n.type = n.init.type;
        curClass.type.fields.put(n.name, n.type);
        return null;
    }

    @Override
    public Void visit(MethodDeclaration n) {
        methodScope = new HashMap<>();
        curMethod = n;
        inMethod = true;
        inConstructor = false;
        n.returnType.accept(this);

        // Check params
        HashSet<String> paramNames = new HashSet<>();
        for (Param p : n.params) {
            p.accept(this);
            if (paramNames.contains(p.name)) {
                throw new ValidationException(
                        "Duplication of parameter " + p.name + where());
            }
            paramNames.add(p.name);
        }

        forEach(n.body);
        return null;
    }

    @Override
    public Void visit(ConstructorDeclaration n) {
        methodScope = new HashMap<>();
        curConstructor = n;
        inMethod = false;
        inConstructor = true;
        // Check params
        HashSet<String> paramNames = new HashSet<>();
        for (Param p : n.params) {
            p.accept(this);
            if (paramNames.contains(p.name)) {
                throw new ValidationException(
                        "Duplication of parameter " + p.name + where());
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
            throw new ValidationException("Undeclared type: " + n.t.name + " of parameter " + n.name + where());
        }
        return null;
    }

    @Override
    public Void visit(AssignmentStatement n) {
        n.target.accept(this);
        n.value.accept(this);
        if (!ProgramTypes.canCast(n.target.type, n.value.type)) {
            throw new ValidationException(
                    "Cannot assign value of " + n.value.type + " type to variable of type " + n.target.type
                            + where());
        }
        return null;
    }

    @Override
    public Void visit(IfStatement n) {
        n.cond.accept(this);
        if (!ProgramTypes.canCast(ProgramTypes.Boolean, n.cond.type)) {
            throw new ValidationException("Condition expression inside if statement is not of Boolean type" + where());
        }
        n.thenBody.accept(this);
        n.elseBody.accept(this);
        return null;
    }

    @Override
    public Void visit(WhileStatement n) {
        n.cond.accept(this);
        if (!ProgramTypes.canCast(ProgramTypes.Boolean, n.cond.type)) {
            throw new ValidationException(
                    "Condition expression inside loop statement is not of Boolean type" + where());
        }
        forEach(n.body);
        return null;
    }

    @Override
    public Void visit(ReturnStatement n) {
        n.value.accept(this);

        if (!ProgramTypes.canCast(ProgramTypes.toVariableType(curMethod.returnType.name), n.value.type)) {
            throw new ValidationException("Method " + curMethod.name + " returns value of type "
                    + curMethod.returnType.name + " not " + n.value.type.type);
        }
        return null;
    }

    @Override
    public Void visit(VariableDeclaration n) {
        n.init.accept(this);

        if (methodScope.containsKey(n.name)) {
            throw new ValidationException("Variable " + n.name + " has already been initialized" + where());
        }
        methodScope.put(n.name, n.init.type);
        return null;
    }

    @Override
    public Void visit(VariableReference n) {
        if (methodScope.containsKey(n.name)) {
            n.type = methodScope.get(n.name);
        } else if (classScope.containsKey(n.name)) {
            n.type = classScope.get(n.name);
        } else {
            throw new ValidationException(
                    "Reference of variable " + n.name + " that has not been initialized" + where());
        }
        return null;
    }

    @Override
    public Void visit(MemberAccess n) {
        n.target.accept(this);
        n.member.accept(this);

        // if (n.target.type.fields.containsKey(n.member.))
        return null;
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
