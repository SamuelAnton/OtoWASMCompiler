package Translator;

import java.util.ArrayList;

import Semanticer.Components.Exceptions.ValidationException;
import Semanticer.Components.Types.ProgramTypes;
import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.Program;
import Syntaxer.ast.component.ExtensionType;
import Syntaxer.ast.component.Param;
import Syntaxer.ast.component.ReturnType;
import Syntaxer.ast.component.Type;
import Syntaxer.ast.declaration.ClassDeclaration;
import Syntaxer.ast.declaration.ConstructorDeclaration;
import Syntaxer.ast.declaration.FieldDeclaration;
import Syntaxer.ast.declaration.MethodDeclaration;
import Syntaxer.ast.expression.ConstructorCall;
import Syntaxer.ast.expression.Expression;
import Syntaxer.ast.expression.MemberAccess;
import Syntaxer.ast.expression.MethodCall;
import Syntaxer.ast.expression.SuperConstructorCall;
import Syntaxer.ast.expression.ThisExpression;
import Syntaxer.ast.literal.ArrayLiteral;
import Syntaxer.ast.literal.BooleanLiteral;
import Syntaxer.ast.literal.IntegerLiteral;
import Syntaxer.ast.literal.ListLiteral;
import Syntaxer.ast.literal.RealLiteral;
import Syntaxer.ast.literal.StringLiteral;
import Syntaxer.ast.statement.AssignmentStatement;
import Syntaxer.ast.statement.ElseStatement;
import Syntaxer.ast.statement.ExpressionStatement;
import Syntaxer.ast.statement.IfStatement;
import Syntaxer.ast.statement.ReturnStatement;
import Syntaxer.ast.statement.ThenStatement;
import Syntaxer.ast.statement.VariableDeclaration;
import Syntaxer.ast.statement.VariableReference;
import Syntaxer.ast.statement.WhileStatement;

public class PreTranslator implements ASTVisitor<String> {
    StringBuilder result = new StringBuilder();

    final ArrayList<ConstructorCall> queued = new ArrayList<>();

    @Override
    public String visit(Program n) {
        StringBuilder builder = new StringBuilder();
        builder.append("(module\n");

        builder.append("(global $heap_ptr (mut i32) (i32.const 0))");
        builder.append("(func $allocate (param $size i32) (result i32)\n" + //
                "  (local $ptr i32)\n" + //
                "  global.get $heap_ptr\n" + //
                "  local.set $ptr\n" + //
                "  global.get $heap_ptr\n" + //
                "  local.get $size\n" + //
                "  i32.add\n" + //
                "  global.set $heap_ptr\n" + //
                "  local.get $ptr\n" + //
                ")");
        builder.append("(table $type_info 10 anyfunc)\n" + //
                "(memory 1)");

        for (ClassDeclaration c : n.classes) {
            c.accept(this);
        }

        builder.append("\n)");
        return builder.toString();
    }

    @Override
    public String visit(ClassDeclaration n) {
        for (FieldDeclaration f : n.fieldDeclarations) {
            f.accept(this);
        }
        return null;
    }

    @Override
    public String visit(FieldDeclaration n) {
        if (n.init instanceof VariableReference) {
            = 0
        }
        else {
            n.inWASM = n.init.accept(this);
        }
    }

    @Override
    public String visit(MethodDeclaration n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(ConstructorDeclaration n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(Param n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(AssignmentStatement n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(IfStatement n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(WhileStatement n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(ReturnStatement n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(VariableDeclaration n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(VariableReference n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(MemberAccess n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(MethodCall n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(ConstructorCall n) {
        switch (n.className) {
            case "Integer":
                if (n.args.size() > 1) {
                    throw new ValidationException("Too many arguments for constructor call of class Integer!");
                }
                if (n.args.size() == 0) {
                    return "i32.const 0";
                }
                if (n.args.get(0) instanceof IntegerLiteral) {
                    return "i32.const " + ((IntegerLiteral) n.args.get(0)).value;
                }
                if (n.args.get(0).type == ProgramTypes.Integer) {
                    return n.args.get(0).accept(this);
                }
                if (n.args.get(0).type == ProgramTypes.Real) {
                    String s = n.args.get(0).accept(this);
                    return 
                }
            case "Real":
                if (n.args.get(0) instanceof RealLiteral) {
                    return "f32.const " + ((RealLiteral) n.args.get(0)).value;
                }
                else {
                    return n.args.get(0).accept(this);
                }
            case "Boolean"
            default:
                break;
        }

        return null;
    }

    @Override
    public String visit(ThisExpression n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(ExpressionStatement expressionStatement) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(BooleanLiteral boolLiteral) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(IntegerLiteral n) {
        // return "i32.const " + n.value;
        return null;
    }

    @Override
    public String visit(RealLiteral realLiteral) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(StringLiteral stringLiteral) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(ExtensionType extensionType) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(ReturnType returnType) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(ArrayLiteral arrayLiteral) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(ListLiteral listiteral) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(Type type) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(ElseStatement elseStatement) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(ThenStatement thenStatement) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(SuperConstructorCall superConstructorCall) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

}
