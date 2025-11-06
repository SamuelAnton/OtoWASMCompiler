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

    public void fillTypes() {
        fillInteger();
        fillReal();
        fillBoolean();
        fillArray();
        fillList();
    }

    public void fillInteger() {
        Integer.fields.put("Min", null);
        Integer.fields.put("Max", null);
        Integer.methods.put("toReal", null);
        Integer.methods.put("toBoolean", null);
        Integer.methods.put("UnaryMinus", null);

        Integer.methods.put("Plus", null);
        Integer.methods.put("Minus", null);
        Integer.methods.put("Mult", null);
        Integer.methods.put("Div", null);
        Integer.methods.put("Rem", null);
        Integer.methods.put("Less", null);
        Integer.methods.put("LessEqual", null);
        Integer.methods.put("Greater", null);
        Integer.methods.put("GreaterEqual", null);
        Integer.methods.put("Equal", null);
    }

    private void fillReal() {
        Real.fields.put("Min", null);
        Real.fields.put("Max", null);
        Real.fields.put("Epsilon", null);

        Real.methods.put("toInteger", null);
        Real.methods.put("UnaryMinus", null);
        Real.methods.put("Plus", null);
        Real.methods.put("Minus", null);
        Real.methods.put("Mult", null);
        Real.methods.put("Div", null);
        Real.methods.put("Rem", null);
        Real.methods.put("Less", null);
        Real.methods.put("LessEqual", null);
        Real.methods.put("Greater", null);
        Real.methods.put("GreaterEqual", null);
        Real.methods.put("Equal", null);
    }

    private void fillBoolean() {
        Boolean.methods.put("toInteger", null);
        Boolean.methods.put("Or", null);
        Boolean.methods.put("And", null);
        Boolean.methods.put("Xor", null);
        Boolean.methods.put("Not", null);
    }

    private void fillArray() {
        Array.methods.put("toList", null);
        Array.methods.put("Length", null);
        Array.methods.put("get", null);
        Array.methods.put("set", null);
    }

    private void fillList() {
        List.methods.put("append", null);
        List.methods.put("head", null);
        List.methods.put("tail", null);

    }

}
