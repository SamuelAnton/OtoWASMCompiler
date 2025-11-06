package Semanticer.optimizer;

import Syntaxer.ast.Program;
import Syntaxer.ast.declaration.ClassDeclaration;
import Syntaxer.ast.declaration.ConstructorDeclaration;
import Syntaxer.ast.declaration.MethodDeclaration;
import Syntaxer.ast.statement.*;

import java.util.ArrayList;
import java.util.List;

public class UnreachableCodeRemover {

    public void runOnProgram(Program program) {
        for (ClassDeclaration cls : program.classes) {
            if (cls.methodDeclarations != null) {
                for (MethodDeclaration m : cls.methodDeclarations) {
                    m.body = trimAfterReturn(m.body);
                }
            }
            if (cls.constructorDeclarations != null) {
                for (ConstructorDeclaration c : cls.constructorDeclarations) {
                    c.body = trimAfterReturn(c.body);
                }
            }
        }
    }

    private List<Statement> trimAfterReturn(List<Statement> stmts) {
        if (stmts == null) return null;
        List<Statement> result = new ArrayList<>();
        boolean seenReturn = false;

        for (Statement s : stmts) {
            if (seenReturn) break;

            if (s instanceof ReturnStatement) {
                result.add(s);
                seenReturn = true;
            } else if (s instanceof IfStatement ifs) {
                if (ifs.thenBody != null && ifs.thenBody.body != null)
                    ifs.thenBody.body = trimAfterReturn(ifs.thenBody.body);
                if (ifs.elseBody != null && ifs.elseBody.body != null)
                    ifs.elseBody.body = trimAfterReturn(ifs.elseBody.body);
                result.add(ifs);
            } else if (s instanceof WhileStatement ws) {
                ws.body = trimAfterReturn(ws.body);
                result.add(ws);
            } else {
                result.add(s);
            }
        }

        return result;
    }
}
