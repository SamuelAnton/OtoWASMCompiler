package Semanticer.Components.Types;

import java.util.HashMap;

import Syntaxer.ast.declaration.MethodDeclaration;

public class VariableType {
    public final String type;
    public VariableType baseType;
    public HashMap<String, VariableType> fields = new HashMap<>();
    public HashMap<String, MethodDeclaration> methods = new HashMap<>();

    public VariableType(String type, VariableType baseType) {
        this.type = type;
        this.baseType = baseType;
    }
}
