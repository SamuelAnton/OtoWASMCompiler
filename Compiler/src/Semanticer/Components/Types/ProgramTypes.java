package Semanticer.Components.Types;

import java.util.HashMap;

public class ProgramTypes {
    public static final VariableType Void = new VariableType("Void", null);
    public static final VariableType Class = new VariableType("Class", null);

    public static final VariableType AnyValue = new VariableType("AnyValue", Class);

    public static final VariableType Integer = new VariableType("Integer", AnyValue);
    public static final VariableType Real = new VariableType("Real", AnyValue);
    public static final VariableType Boolean = new VariableType("Boolean", AnyValue);

    public static final VariableType AnyRef = new VariableType("AnyRef", Class);

    public static final VariableType Array = new VariableType("Array", AnyRef);
    public static final VariableType List = new VariableType("List", AnyRef);

    public static final VariableType AnyUsersClass = new VariableType("AnyUsersClass", Class);

    private static final HashMap<String, VariableType> userTypes = new HashMap<>();

    static public VariableType newType(String type) {
        VariableType nt = new VariableType(type, AnyUsersClass);
        userTypes.put(type, nt);
        return nt;
    }

    static public VariableType toVariableType(String type) {
        switch (type) {
            case "Class":
                return Class;
            case "AnyValue":
                return AnyValue;
            case "Integer":
                return Integer;
            case "Real":
                return Real;
            case "Boolean":
                return Boolean;
            case "AnyRef":
                return AnyRef;
            case "Array":
                return Array;
            case "List":
                return List;
            case null:
                return Void;
            default:
                return userTypes.get(type);
        }
    }

    public static boolean canCast(VariableType toType, VariableType fromType) {
        while (fromType != null) {
            if (fromType == toType) {
                break;
            }
            fromType = fromType.baseType;
        }
        return toType == fromType;
    }
}
