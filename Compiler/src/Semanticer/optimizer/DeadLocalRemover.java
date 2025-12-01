package Semanticer.optimizer;

import Syntaxer.ast.Program;
import Syntaxer.ast.declaration.ClassDeclaration;
import Syntaxer.ast.declaration.ConstructorDeclaration;
import Syntaxer.ast.declaration.MethodDeclaration;
import Syntaxer.ast.expression.ConstructorCall;
import Syntaxer.ast.expression.Expression;
import Syntaxer.ast.expression.MemberAccess;
import Syntaxer.ast.expression.MethodCall;
import Syntaxer.ast.literal.*;
import Syntaxer.ast.statement.*;

import java.util.*;

public class DeadLocalRemover {

    public void runOnProgram(Program p) {
        for (ClassDeclaration cls : p.classes) {
            if (!cls.methodDeclarations.isEmpty()) {
                for (MethodDeclaration m : cls.methodDeclarations) {
                    m.body = removeDeadLocals(m.body);
                }
            }
            if (!cls.constructorDeclarations.isEmpty()) {
                for (ConstructorDeclaration c : cls.constructorDeclarations) {
                    c.body = removeDeadLocals(c.body);
                }
            }
        }
    }

    private List<Statement> removeDeadLocals(List<Statement> stmts) {
        if (stmts == null) return stmts;

        Map<String, Boolean> isPureInit = new HashMap<>();
        collectDeclarations(stmts, isPureInit);

        Set<String> used = new HashSet<>();
        collectUsedNames(stmts, used);

        List<Statement> out = new ArrayList<>();
        for (Statement s : stmts) {
            if (s instanceof VariableDeclaration vd) {
                boolean usedLater = used.contains(vd.name);
                boolean pure = isPureInit.getOrDefault(vd.name, false);
                if (!usedLater && pure) {
                    continue;
                }
            } else if (s instanceof IfStatement ifs) {
                if (ifs.thenBody != null && ifs.thenBody.body != null) {
                    ifs.thenBody.body = removeDeadLocals(ifs.thenBody.body);
                }
                if (ifs.elseBody != null && ifs.elseBody.body != null) {
                    ifs.elseBody.body = removeDeadLocals(ifs.elseBody.body);
                }
            } else if (s instanceof WhileStatement ws) {
                ws.body = removeDeadLocals(ws.body);
            }

            out.add(s);
        }

        return out;
    }

    private void collectDeclarations(List<Statement> stmts, Map<String, Boolean> isPureInit) {
        if (stmts == null) return;
        for (Statement s : stmts) {
            if (s instanceof VariableDeclaration vd) {
                isPureInit.put(vd.name, isPure(vd.init));
            } else if (s instanceof IfStatement ifs) {
                if (ifs.thenBody != null)
                    collectDeclarations(ifs.thenBody.body, isPureInit);
                if (ifs.elseBody != null)
                    collectDeclarations(ifs.elseBody.body, isPureInit);
            } else if (s instanceof WhileStatement ws) {
                collectDeclarations(ws.body, isPureInit);
            }
        }
    }

    private void collectUsedNames(List<Statement> stmts, Set<String> used) {
        if (stmts == null) return;
        for (Statement s : stmts) {
            if (s instanceof AssignmentStatement as) {
                collectUsedInExpression(as.target, used);
                collectUsedInExpression(as.value, used);
            } else if (s instanceof ExpressionStatement es) {
                collectUsedInExpression(es.value, used);
            } else if (s instanceof ReturnStatement rs) {
                collectUsedInExpression(rs.value, used);
            } else if (s instanceof IfStatement ifs) {
                collectUsedInExpression(ifs.cond, used);
                if (ifs.thenBody != null)
                    collectUsedNames(ifs.thenBody.body, used);
                if (ifs.elseBody != null)
                    collectUsedNames(ifs.elseBody.body, used);
            } else if (s instanceof WhileStatement ws) {
                collectUsedInExpression(ws.cond, used);
                collectUsedNames(ws.body, used);
            } else if (s instanceof VariableDeclaration vd) {
                collectUsedInExpression(vd.init, used);
            }
        }
    }

    private void collectUsedInExpression(Expression e, Set<String> used) {
        switch (e) {
            case VariableReference vr -> used.add(vr.name);
            case MethodCall mc -> {
                collectUsedInExpression(mc.target, used);
                for (Expression a : mc.args) collectUsedInExpression(a, used);
            }
            case MemberAccess ma -> {
                collectUsedInExpression(ma.target, used);
                collectUsedInExpression(ma.member, used);
            }
            case ConstructorCall cc -> {
                for (Expression a : cc.args) collectUsedInExpression(a, used);
            }
            case null, default -> {
            }
        }
    }

    private boolean isPure(Expression e) {
        if (e == null) return true;

        return switch (e) {
            case IntegerLiteral il -> true;
            case RealLiteral rl -> true;
            case BooleanLiteral bl -> true;
            case StringLiteral sl -> true;
            case ArrayLiteral al -> al.size >= 0;
            default -> false;
        };
    }
}