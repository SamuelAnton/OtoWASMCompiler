package Semanticer.Components.Types;

public class VariableType {
    public final String type;
    public VariableType baseType;

    public VariableType(String type, VariableType baseType) {
        this.type = type;
        this.baseType = baseType;
    }
}
