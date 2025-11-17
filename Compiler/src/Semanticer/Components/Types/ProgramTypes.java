package Semanticer.Components.Types;

import java.util.ArrayList;
import java.util.HashMap;

import Syntaxer.ast.component.ReturnType;
import Syntaxer.ast.declaration.MethodDeclaration;

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
            case "null":
                return Void;
            default:
                return userTypes.get(type);
        }
    }

    static public VariableType toVariableType(ReturnType r) {
        if (r == null) {
            return toVariableType("null");
        } else {
            return toVariableType(r.name);
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
        Integer.fields.put("Min", Integer);
        Integer.fields.put("Max", Integer);
        Integer.methods.put("toReal",
                new MethodDeclaration("toReal", new ArrayList<>(), new ReturnType("Real"), new ArrayList<>()));
        Integer.methods.put("toBoolean",
                new MethodDeclaration("toBoolean", new ArrayList<>(), new ReturnType("Boolean"), new ArrayList<>()));
        Integer.methods.put("UnaryMinus",
                new MethodDeclaration("UnaryMinus", new ArrayList<>(), new ReturnType("Integer"), new ArrayList<>()));

        Integer.methods.put("Plus",
                new MethodDeclaration("Plus", new ArrayList<>(), new ReturnType("Integer"), new ArrayList<>()));
        Integer.methods.put("Minus",
                new MethodDeclaration("Minus", new ArrayList<>(), new ReturnType("Integer"), new ArrayList<>()));
        Integer.methods.put("Mult",
                new MethodDeclaration("Mult", new ArrayList<>(), new ReturnType("Integer"), new ArrayList<>()));
        Integer.methods.put("Div",
                new MethodDeclaration("Div", new ArrayList<>(), new ReturnType("Real"), new ArrayList<>()));
        Integer.methods.put("Rem",
                new MethodDeclaration("Rem", new ArrayList<>(), new ReturnType("Integer"), new ArrayList<>()));
        Integer.methods.put("Less",
                new MethodDeclaration("Less", new ArrayList<>(), new ReturnType("Boolean"), new ArrayList<>()));
        Integer.methods.put("LessEqual",
                new MethodDeclaration("LessEqual", new ArrayList<>(), new ReturnType("Boolean"), new ArrayList<>()));
        Integer.methods.put("Greater",
                new MethodDeclaration("Greater", new ArrayList<>(), new ReturnType("Boolean"), new ArrayList<>()));
        Integer.methods.put("GreaterEqual",
                new MethodDeclaration("GreaterEqual", new ArrayList<>(), new ReturnType("Boolean"), new ArrayList<>()));
        Integer.methods.put("Equal",
                new MethodDeclaration("Equal", new ArrayList<>(), new ReturnType("Boolean"), new ArrayList<>()));
    }

    private void fillReal() {
        Real.fields.put("Min", Real);
        Real.fields.put("Max", Real);
        Real.fields.put("Epsilon", Real);

        Real.methods.put("toInteger",
                new MethodDeclaration("toInteger", new ArrayList<>(), new ReturnType("Integer"), new ArrayList<>()));
        Real.methods.put("UnaryMinus",
                new MethodDeclaration("UnaryMinus", new ArrayList<>(), new ReturnType("Real"), new ArrayList<>()));
        Real.methods.put("Plus",
                new MethodDeclaration("Plus", new ArrayList<>(), new ReturnType("Real"), new ArrayList<>()));
        Real.methods.put("Minus",
                new MethodDeclaration("Minus", new ArrayList<>(), new ReturnType("Real"), new ArrayList<>()));
        Real.methods.put("Mult",
                new MethodDeclaration("Mult", new ArrayList<>(), new ReturnType("Real"), new ArrayList<>()));
        Real.methods.put("Div",
                new MethodDeclaration("Div", new ArrayList<>(), new ReturnType("Real"), new ArrayList<>()));
        Real.methods.put("Rem",
                new MethodDeclaration("Rem", new ArrayList<>(), new ReturnType("Real"), new ArrayList<>()));
        Real.methods.put("Less",
                new MethodDeclaration("Less", new ArrayList<>(), new ReturnType("Boolean"), new ArrayList<>()));
        Real.methods.put("LessEqual",
                new MethodDeclaration("LessEqual", new ArrayList<>(), new ReturnType("Boolean"), new ArrayList<>()));
        Real.methods.put("Greater",
                new MethodDeclaration("Greater", new ArrayList<>(), new ReturnType("Boolean"), new ArrayList<>()));
        Real.methods.put("GreaterEqual",
                new MethodDeclaration("GreaterEqual", new ArrayList<>(), new ReturnType("Boolean"), new ArrayList<>()));
        Real.methods.put("Equal",
                new MethodDeclaration("Equal", new ArrayList<>(), new ReturnType("Boolean"), new ArrayList<>()));
    }

    private void fillBoolean() {
        Boolean.methods.put("toInteger",
                new MethodDeclaration("toInteger", new ArrayList<>(), new ReturnType("Integer"), new ArrayList<>()));
        Boolean.methods.put("Or",
                new MethodDeclaration("Or", new ArrayList<>(), new ReturnType("Boolean"), new ArrayList<>()));
        Boolean.methods.put("And",
                new MethodDeclaration("And", new ArrayList<>(), new ReturnType("Boolean"), new ArrayList<>()));
        Boolean.methods.put("Xor",
                new MethodDeclaration("Xor", new ArrayList<>(), new ReturnType("Boolean"), new ArrayList<>()));
        Boolean.methods.put("Not",
                new MethodDeclaration("Not", new ArrayList<>(), new ReturnType("Boolean"), new ArrayList<>()));
    }

    private void fillArray() {
        Array.methods.put("toList",
                new MethodDeclaration("toList", new ArrayList<>(), new ReturnType("List"), new ArrayList<>()));
        Array.methods.put("Length",
                new MethodDeclaration("Length", new ArrayList<>(), new ReturnType("Integer"), new ArrayList<>()));
        Array.methods.put("get",
                new MethodDeclaration("get", new ArrayList<>(), new ReturnType("null"), new ArrayList<>()));
        Array.methods.put("set",
                new MethodDeclaration("set", new ArrayList<>(), new ReturnType("null"), new ArrayList<>()));
    }

    private void fillList() {
        List.methods.put("append",
                new MethodDeclaration("append", new ArrayList<>(), new ReturnType("null"), new ArrayList<>()));
        List.methods.put("head",
                new MethodDeclaration("head", new ArrayList<>(), new ReturnType("null"), new ArrayList<>()));
        List.methods.put("tail",
                new MethodDeclaration("tail", new ArrayList<>(), new ReturnType("null"), new ArrayList<>()));

    }

}
