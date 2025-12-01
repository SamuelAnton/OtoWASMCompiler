package utils;

import Syntaxer.ast.statement.Statement;
import Syntaxer.ast.statement.VariableDeclaration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Utils {

    public Set<Statement> findLocalsInMethod(List<Statement> input) {
        Set<Statement> locals = new HashSet<>();
        for (Statement cur : input) {
            if (cur instanceof VariableDeclaration) {
                locals.add(cur);
            }
        }
        return locals;
    }
}
