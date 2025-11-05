package Semanticer.optimizer;

import Syntaxer.ast.Program;
import Syntaxer.ast.declaration.ClassDeclaration;
import Syntaxer.ast.declaration.ConstructorDeclaration;
import Syntaxer.ast.declaration.MethodDeclaration;
import Syntaxer.ast.statement.*;

import java.util.ArrayList;
import java.util.List;

public class UnreachableCodeRemover {

    public void runOnProgram(Program p) {
        for (ClassDeclaration cls : p.classes) {
            if (cls.methodDeclarations != null) {
                for (MethodDeclaration method : cls.methodDeclarations) {
                    method.body = trimAfterReturn(method.body);
                }
            }
            if (cls.constructorDeclarations != null) {
                for (ConstructorDeclaration constructorDeclaration : cls.constructorDeclarations) {
                    constructorDeclaration.body = trimAfterReturn(constructorDeclaration.body);
                }
            }
        }
    }

    private List<Statement> trimAfterReturn(List<Statement> stmts) {
        List<Statement> out = new ArrayList<>();
        if (stmts == null) return out;
        boolean seenReturn = false;

        for (Statement s : stmts) {
            if (seenReturn) break;

            if (s instanceof IfStatement ifs) {
                if (ifs.thenBody != null && ifs.thenBody.body != null) {
                    List<Statement> trimmedThen = trimAfterReturn(ifs.thenBody.body);
                    ifs.thenBody = new ThenStatement(trimmedThen);
                }
                if (ifs.elseBody != null && ifs.elseBody.body != null) {
                    List<Statement> trimmedElse = trimAfterReturn(ifs.elseBody.body);
                    ifs.elseBody = new ElseStatement(trimmedElse);
                }
                out.add(ifs);
            }
            else if (s instanceof WhileStatement ws) {
                ws.body = trimAfterReturn(ws.body);
                out.add(ws);
            }
            else {
                out.add(s);
                if (s instanceof ReturnStatement) {
                    seenReturn = true;
                }
            }
        }

        return out;
    }
}
