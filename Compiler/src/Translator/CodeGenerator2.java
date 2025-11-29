package Translator;

import java.util.HashMap;

import Syntaxer.ast.Program;
import Syntaxer.ast.declaration.ClassDeclaration;
import Syntaxer.ast.declaration.FieldDeclaration;

public class CodeGenerator2 {
    private final Program program;
    private final StringBuilder builder = new StringBuilder();

    private final HashMap<String, HashMap<String, Integer>> layots = new HashMap<>();
    private final HashMap<String, Integer> classEndByte = new HashMap<>();

    public CodeGenerator2(Program p) {
        program = p;
    }

    public StringBuilder translate() {
        // Pre construct things for user classes
        prepareClasses();

        return builder;
    }

    private void prepareClasses() {
        // Calculate layout
        calculateStaticLayout();
        for (ClassDeclaration c : program.classes) {
            calculateLayout(c);
        }

        // Generate Vtables
        builder.append(";; == Virtual method tables for user defined types types ==");
        for (ClassDeclaration c : program.classes) {
            generateVTable(c);
        }
    }

    private void calculateStaticLayout() {
        HashMap<String, Integer> l = new HashMap<>();

        // Atomic types
        l.put("value", 8); // value of atomic types
        layots.put("Integer", l);
        layots.put("Real", l);
        layots.put("Boolean", l);

        // Compound types
        // Array
        l = new HashMap<>();
        l.put("size", 8); // size of array
        l.put("values", 12); // array elements start
        layots.put("Array", l);

        // List
        l = new HashMap<>();
        l.put("size", 8); // size of list
        l.put("capacity", 12); // capacity of list (size of current array)
        layots.put("List", l);
    }

    private void calculateLayout(ClassDeclaration c) {
        if (layots.containsKey(c.name)) {
            return;
        }

        HashMap<String, Integer> l = new HashMap<>();
        int curOffset = calculateClassOffset(c);
        if (c.superClass != null) {
            calculateLayout(c.superClass);
            l.putAll(layots.get(c.superClass.name));
        }
        for (FieldDeclaration f : c.fieldDeclarations) {
            l.put(f.name, curOffset);
            curOffset += 4; // 4 bytes for 1 pointer to object
        }
        layots.put(c.name, l);
    }

    private int calculateClassOffset(ClassDeclaration c) {
        // If already calculated
        if (classEndByte.containsKey(c.name)) {
            return classEndByte.get(c.name);
        }

        // Base class case
        if (c.superClass == null) {
            classEndByte.put(c.name, 4 * c.fieldDeclarations.size() + 8);
            return 8;
        } else {
            calculateClassOffset(c.superClass);
            int superEnd = classEndByte.get(c.superClass.name);
            classEndByte.put(c.name, 4 * c.fieldDeclarations.size());
            return superEnd;
        }
    }

    private void generateVTable(ClassDeclaration c) {
        builder.append(";;; ").append(c.name);
        builder.append("(table $").append(c.name).append("_methods ").append(2).append(" funcref)");
    }
}
