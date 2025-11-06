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

        if (n.member instanceof VariableReference) {
            if (!n.target.type.fields.containsKey(((VariableReference) n.member).name)
                    && !n.target.type.methods.containsKey(((VariableReference) n.member).name)) {
                throw new ValidationException(
                        "Cannot find field or method " + ((VariableReference) n.member).name + " for type "
                                + n.target.type.type
                                + where());
            }
        } else if (!(n.member instanceof MemberAccess)) {
            throw new ValidationException("Not correct member access" + where());
        }
        n.type = n.member.type;
        return null;
    }

    @Override
    public Void visit(MethodCall n) {
        n.target.accept(this);
        forEach(n.args);

        n.type = n.target.type;
        return null;
    }

    @Override
    public Void visit(ConstructorCall n) {
        if (ProgramTypes.toVariableType(n.className) == null) {
            throw new ValidationException("Type " + n.className + " is not defined" + where());
        }
        n.type = ProgramTypes.toVariableType(n.className);
        forEach(n.args);
        return null;
    }

    @Override
    public Void visit(ThisExpression n) {
        n.type = curClass.type;
        return null;
    }

    @Override
    public Void visit(ExpressionStatement n) {
        n.value.accept(this);
        return null;
    }

    @Override
    public Void visit(BooleanLiteral n) {
        n.type = ProgramTypes.Boolean;
        return null;
    }

    @Override
    public Void visit(IntegerLiteral n) {
        n.type = ProgramTypes.Integer;
        return null;
    }

    @Override
    public Void visit(RealLiteral n) {
        n.type = ProgramTypes.Real;
        return null;
    }

    @Override
    public Void visit(StringLiteral n) {
        return null;
    }

    @Override
    public Void visit(ExtensionType n) {
        return null;
    }

    @Override
    public Void visit(ReturnType n) {
        if (ProgramTypes.toVariableType(n.name) == null) {
            throw new ValidationException("Type " + n.name + " is not defined" + where());
        }
        return null;
    }

    @Override
    public Void visit(ArrayLiteral n) {
        if (ProgramTypes.toVariableType(n.type) == null) {
            throw new ValidationException("Type " + n.type + " is not defined" + where());
        }
        return null;
    }

    @Override
    public Void visit(ListLiteral n) {
        if (ProgramTypes.toVariableType(n.type) == null) {
            throw new ValidationException("Type " + n.type + " is not defined" + where());
        }
        return null;
    }

    @Override
    public Void visit(Type n) {
        if (ProgramTypes.toVariableType(n.name) == null) {
            throw new ValidationException("Type " + n.name + " is not defined" + where());
        }
        return null;
    }

    @Override
    public Void visit(ElseStatement n) {
        forEach(n.body);
        return null;
    }

    @Override
    public Void visit(ThenStatement n) {
        forEach(n.body);
        return null;
    }

    @Override
    public Void visit(SuperConstructorCall n) {
        forEach(n.args);
        return null;
    }

}
