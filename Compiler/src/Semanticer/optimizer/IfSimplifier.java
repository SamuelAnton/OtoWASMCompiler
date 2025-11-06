package Semanticer.optimizer;

import Syntaxer.ast.Program;
import Syntaxer.ast.declaration.ClassDeclaration;
import Syntaxer.ast.declaration.ConstructorDeclaration;
import Syntaxer.ast.declaration.MethodDeclaration;
import Syntaxer.ast.expression.Expression;
import Syntaxer.ast.literal.BooleanLiteral;
import Syntaxer.ast.statement.*;

import java.util.ArrayList;
import java.util.List;

public class IfSimplifier {

    public void runOnProgram(Program p) {
        for (ClassDeclaration cls : p.classes) {
            if (!cls.methodDeclarations.isEmpty()) {
                for (MethodDeclaration m : cls.methodDeclarations) {
                    m.body = simplifyStatements(m.body);
                }
            }
            if (!cls.constructorDeclarations.isEmpty()) {
                for (ConstructorDeclaration c : cls.constructorDeclarations) {
                    c.body = simplifyStatements(c.body);
                }
            }
        }
    }

    private List<Statement> simplifyStatements(List<Statement> stmts) {
        if (stmts == null) return stmts;
        List<Statement> out = new ArrayList<>();

        for (Statement s : stmts) {
            if (s instanceof IfStatement ifs) {
                List<Statement> simplified = simplifyIf(ifs);
                if (simplified != null)
                    out.addAll(simplified);
            } else if (s instanceof WhileStatement ws) {
                ws.body = simplifyStatements(ws.body);
                out.add(ws);
            } else if (s instanceof ThenStatement ts) {
                ts.body = simplifyStatements(ts.body);
                out.add(ts);
            } else if (s instanceof ElseStatement es) {
                es.body = simplifyStatements(es.body);
                out.add(es);
            } else {
                out.add(s);
            }
        }

        return out;
    }

    /**
     * Simplify an if-statement where condition is constant (True/False).
     * Returns a list of replacement statements (possibly empty).
     */
    private List<Statement> simplifyIf(IfStatement ifs) {
        Expression cond = ifs.cond;

        // recursively simplify inside blocks first
        if (ifs.thenBody != null && ifs.thenBody.body != null)
            ifs.thenBody.body = simplifyStatements(ifs.thenBody.body);

        if (ifs.elseBody != null && ifs.elseBody.body != null)
            ifs.elseBody.body = simplifyStatements(ifs.elseBody.body);

        // check condition literal
        if (cond instanceof BooleanLiteral bl) {
            if (bl.value) {
                // condition is always true then replace 'if' by its then-body
                return ifs.thenBody != null ? ifs.thenBody.body : new ArrayList<>();
            } else {
                // condition is always false then replace 'if' by else-body (if any)
                return ifs.elseBody != null ? ifs.elseBody.body : new ArrayList<>();
            }
        }

        // condition is not constant → keep the IF
        List<Statement> keep = new ArrayList<>();
        keep.add(ifs);
        return keep;
    }
}
