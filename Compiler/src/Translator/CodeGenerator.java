package Translator;

import Syntaxer.ast.*;
import Syntaxer.ast.declaration.*;
import Syntaxer.ast.expression.*;
import Syntaxer.ast.statement.*;
import Syntaxer.ast.literal.*;
import Syntaxer.ast.component.*;
import Semanticer.Components.Types.VariableType;

import java.util.*;

public class CodeGenerator implements ASTVisitor<Void> {
    StringBuilder result = new StringBuilder();

    private final Map<String, Integer> typeIds = new HashMap<>();
    private final Map<String, Integer> vtableOffsets = new HashMap<>();
    private final Map<String, List<String>> methodTables = new HashMap<>();
    private final Map<String, Map<String, Integer>> fieldOffsets = new HashMap<>();
    private final Map<String, Integer> functionAddresses = new HashMap<>();
    private int nextTypeId = 0;
    private int currentFunctionAddress = 0x1000;

    // Current context
    private ClassDeclaration currentClass;
    private MethodDeclaration currentMethod;
    private int localCounter = 0;
    private Map<String, Integer> localIndices = new HashMap<>();
    private int labelCounter = 0;

    public StringBuilder generate(Program program) {
        // Initialize standard types
        initializeStandardTypes();

        // Generate WASM module header
        result.append("(module\n");
        result.append("  (memory 1)\n");
        result.append("  (global $heap_ptr (mut i32) (i32.const 0x1000))\n");
        result.append("  (export \"memory\" (memory 0))\n\n");

        // Calculate field offsets
        calculateLayout(program);

        // Generate VTables
        generateVTables(program);

        // Generate functions for all methods
        result.append("\n  ;; -------Translated Program start--------\n");
        for (ClassDeclaration cls : program.classes) {
            currentClass = cls;
            for (MethodDeclaration method : cls.methodDeclarations) {
                generateMethod(method);
            }
            // If the class has no explicit constructor, generate a default one
            if (cls.constructorDeclarations.isEmpty()) {
                generateDefaultConstructor(cls);
            } else {
                for (ConstructorDeclaration ctor : cls.constructorDeclarations) {
                    generateConstructor(ctor);
                }
            }
        }
        result.append("\n  ;; -------Translated Program end--------\n\n");

        // Generate standard library methods
        generateStandardLibraryMethods();

        // Generate runtime functions
        generateRuntime();

        // Generate program entry point
        generateEntryPoint(program);

        result.append(")\n");

        return result;
    }

    private void generateDefaultConstructor(ClassDeclaration cls) {
    String funcName = cls.name + ".constructor";
    functionAddresses.put(funcName, currentFunctionAddress);
    currentFunctionAddress += 0x100;

    result.append("  (func $" + funcName + " (result i32)\n");
    result.append("    (local $obj i32)\n");
    result.append("    (local $temp i32)\n");

    int objectSize = calculateObjectSize(cls);
    result.append("    ;;; Allocate object of size " + objectSize + "\n");
    result.append("    i32.const " + objectSize + "\n");
    result.append("    call $allocate\n");
    result.append("    local.set $obj\n");

    result.append("    local.get $obj\n");
    result.append("    i32.const " + vtableOffsets.get(cls.name) + "\n");
    result.append("    i32.store\n");
    result.append("    local.get $obj\n");
    result.append("    i32.const " + typeIds.get(cls.name) + "\n");
    result.append("    i32.store offset=4\n");

    Map<String, Integer> offsets = fieldOffsets.get(cls.name);
    if (offsets != null) {
        for (Map.Entry<String, Integer> e : offsets.entrySet()) {
            int fieldOffset = e.getValue();
            result.append("    local.get $obj\n");
            result.append("    i32.const 0\n");
            result.append("    i32.store offset=" + fieldOffset + "\n");
        }
    }

    result.append("    local.get $obj\n");
    result.append("  )\n\n");

    // Update VTable
    updateVTable(cls.name, funcName);
}
 
    private void initializeStandardTypes() {
        typeIds.put("Integer", nextTypeId++);
        typeIds.put("Real", nextTypeId++);
        typeIds.put("Boolean", nextTypeId++);
        typeIds.put("Array", nextTypeId++);
        typeIds.put("List", nextTypeId++);
        typeIds.put("AnyValue", nextTypeId++);
        typeIds.put("AnyRef", nextTypeId++);
        typeIds.put("Class", nextTypeId++);
    }

    private void calculateLayout(Program program) {
        for (ClassDeclaration cls : program.classes) {
            calculateClassLayout(cls);
        }

        // Calculate standard type layouts
        calculateStandardTypeLayout("Integer", 12); // header(8) + value(4)
        calculateStandardTypeLayout("Real", 16); // header(8) + value(8)
        calculateStandardTypeLayout("Boolean", 12); // header(8) + value(4)
        calculateStandardTypeLayout("Array", 20); // header(8) + length(4) + data_ptr(4) + capacity(4)
        calculateStandardTypeLayout("List", 24); // header(8) + length(4) + capacity(4) + data_ptr(4) + size(4)
    }

    private void calculateClassLayout(ClassDeclaration cls) {
        Map<String, Integer> offsets = new HashMap<>();
        int currentOffset = 8; // Start after header (vtable_ptr + type_id)

        // Add fields from parent class first
        if (cls.superClass != null) {
            Map<String, Integer> parentOffsets = fieldOffsets.get(cls.superClass.name);
            if (parentOffsets != null) {
                offsets.putAll(parentOffsets);
                currentOffset = parentOffsets.values().stream().mapToInt(Integer::intValue).max().orElse(8) + 4;
            }
        }

        // Add own fields
        for (FieldDeclaration field : cls.fieldDeclarations) {
            offsets.put(field.name, currentOffset);
            currentOffset += 4; // Each field is 4 bytes
        }

        fieldOffsets.put(cls.name, offsets);
        typeIds.put(cls.name, nextTypeId++);
    }

    private void calculateStandardTypeLayout(String typeName, int size) {
        Map<String, Integer> offsets = new HashMap<>();
        // Standard types have fixed layouts
        if (typeName.equals("Integer") || typeName.equals("Boolean")) {
            offsets.put("value", 8);
        } else if (typeName.equals("Real")) {
            offsets.put("value", 8);
        } else if (typeName.equals("Array")) {
            offsets.put("length", 8);
            offsets.put("data_ptr", 12);
            offsets.put("capacity", 16);
        } else if (typeName.equals("List")) {
            offsets.put("length", 8);
            offsets.put("capacity", 12);
            offsets.put("data_ptr", 16);
            offsets.put("size", 20);
        }
        fieldOffsets.put(typeName, offsets);
    }

    private void generateVTables(Program program) {
        result.append("  ;; Virtual Method Tables\n");

        // Calculate VTable offsets
        int vtableStart = 0x100;

        // Standard types first
        String[] standardTypes = { "Integer", "Real", "Boolean", "Array", "List" };
        for (String type : standardTypes) {
            vtableOffsets.put(type, vtableStart);
            List<String> methods = getStandardTypeMethods(type);
            methodTables.put(type, methods);
            generateVTableData(type, vtableStart, methods);
            vtableStart += 4 + (methods.size() * 4);
        }

        // User classes
        for (ClassDeclaration cls : program.classes) {
            vtableOffsets.put(cls.name, vtableStart);
            List<String> methods = collectAllMethods(cls);
            methodTables.put(cls.name, methods);
            generateVTableData(cls.name, vtableStart, methods);
            vtableStart += 4 + (methods.size() * 4);
        }
        result.append("\n");
    }

    private void generateVTableData(String typeName, int vtableStart, List<String> methods) {
        // Write VTable size
        result.append("  (data (i32.const " + vtableStart + ") \"\\" +
                String.format("%02x", methods.size()) + "\\00\\00\\00\")  ;;; vtable size for " + typeName + "\n");

        // Reserve space for method pointers
        for (int i = 0; i < methods.size(); i++) {
            result.append("  (data (i32.const " + (vtableStart + 4 + i * 4) + ") \"\\00\\00\\00\\00\")  ;;; method " + i
                    + "\n");
        }
    }

    private List<String> getStandardTypeMethods(String type) {
        List<String> methods = new ArrayList<>();
        switch (type) {
            case "Integer":
                methods.add("Integer.toReal");
                methods.add("Integer.toBoolean");
                methods.add("Integer.UnaryMinus");
                methods.add("Integer.Plus");
                methods.add("Integer.Minus");
                methods.add("Integer.Mult");
                methods.add("Integer.Div");
                methods.add("Integer.Rem");
                methods.add("Integer.Less");
                methods.add("Integer.LessEqual");
                methods.add("Integer.Greater");
                methods.add("Integer.GreaterEqual");
                methods.add("Integer.Equal");
                break;
            case "Real":
                methods.add("Real.toInteger");
                methods.add("Real.UnaryMinus");
                methods.add("Real.Plus");
                methods.add("Real.Minus");
                methods.add("Real.Mult");
                methods.add("Real.Div");
                methods.add("Real.Rem");
                methods.add("Real.Less");
                methods.add("Real.LessEqual");
                methods.add("Real.Greater");
                methods.add("Real.GreaterEqual");
                methods.add("Real.Equal");
                break;
            case "Boolean":
                methods.add("Boolean.toInteger");
                methods.add("Boolean.Or");
                methods.add("Boolean.And");
                methods.add("Boolean.Xor");
                methods.add("Boolean.Not");
                break;
            case "Array":
                methods.add("Array.toList");
                methods.add("Array.Length");
                methods.add("Array.get");
                methods.add("Array.set");
                break;
            case "List":
                methods.add("List.append");
                methods.add("List.head");
                methods.add("List.tail");
                methods.add("List.size");
                break;
        }
        return methods;
    }

    private List<String> collectAllMethods(ClassDeclaration cls) {
        List<String> methods = new ArrayList<>();

        // Add methods from parent class first
        if (cls.superClass != null && methodTables.containsKey(cls.superClass.name)) {
            methods.addAll(methodTables.get(cls.superClass.name));
        }

        // Add/overwrite with own methods
        for (MethodDeclaration method : cls.methodDeclarations) {
            String mangledName = getMangledMethodName(cls.name, method.name, method.params);
            methods.add(mangledName);
        }

        return methods;
    }

    private void generateMethod(MethodDeclaration method) {
        currentMethod = method;

        String funcName = getMangledMethodName(currentClass.name, method.name, method.params);
        functionAddresses.put(funcName, currentFunctionAddress);
        currentFunctionAddress += 0x100; // Reserve space for function

        result.append("  (func $" + funcName);

        // first parameter is always 'this'
        result.append(" (param $this i32)");
        for (Param param : method.params) {
            result.append(" (param $" + param.name + " i32)");
        }

        // Return type
        if (!method.returnType.name.equals("null")) {
            result.append(" (result i32)");
        }

        result.append("\n");

        // Collect body variable declarations for local
        List<VariableDeclaration> bodyVariables = new ArrayList<>();
        for (Statement stmt : method.body) {
            if (stmt instanceof VariableDeclaration) {
                bodyVariables.add((VariableDeclaration) stmt);
            }
        }

        // Reset local tracking for the function
        localCounter = 0;
        localIndices.clear();

        // Pre-declare helper locals used by generator
        result.append("    (local $temp i32)\n");
        result.append("    (local $result i32)\n");

        localIndices.put("this", -1);
        for (Param param : method.params) {
            localIndices.put(param.name, -1);
        }

        // Declare numeric locals for body variables and register their numeric indices
        for (VariableDeclaration varDecl : bodyVariables) {
            result.append("    (local $" + localCounter + " i32)\n");
            localIndices.put(varDecl.name, localCounter);
            localCounter++;
        }

        // Method body comment
        result.append("    ;;; Method body for " + funcName + "\n");

        // Generate body statements
        for (Statement stmt : method.body) {
            stmt.accept(this);
        }

        // Default return if no explicit return
        if (method.returnType.name.equals("null") &&
                (method.body.isEmpty() || !(method.body.get(method.body.size() - 1) instanceof ReturnStatement))) {
            result.append("    return\n");
        }

        result.append("  )\n\n");

        // Update VTable with actual function address
        updateVTable(currentClass.name, funcName);
    }

    private void updateVTable(String className, String methodName) {
        List<String> methods = methodTables.get(className);
        if (methods != null) {
            int methodIndex = methods.indexOf(methodName);
            if (methodIndex != -1) {
                int vtableOffset = vtableOffsets.get(className);
                int methodPtrOffset = vtableOffset + 4 + (methodIndex * 4);
                int funcAddress = functionAddresses.getOrDefault(methodName, 0);

                // Write the actual function address to VTable
                result.append("  (data (i32.const " + methodPtrOffset + ") \"\\" +
                        String.format("%02x", funcAddress & 0xFF) + "\\" +
                        String.format("%02x", (funcAddress >> 8) & 0xFF) + "\\" +
                        String.format("%02x", (funcAddress >> 16) & 0xFF) + "\\" +
                        String.format("%02x", (funcAddress >> 24) & 0xFF) +
                        "\")  ;;; " + methodName + "\n");
            }
        }
    }

    private void generateConstructor(ConstructorDeclaration ctor) {
    String funcName = currentClass.name + ".constructor";
    functionAddresses.put(funcName, currentFunctionAddress);
    currentFunctionAddress += 0x100;

    result.append("  (func $" + funcName);

    for (Param param : ctor.params) {
        result.append(" (param $" + param.name + " i32)");
    }
    result.append(" (result i32)\n");

    List<VariableDeclaration> bodyVariables = new ArrayList<>();
    for (Statement stmt : ctor.body) {
        if (stmt instanceof VariableDeclaration) {
            bodyVariables.add((VariableDeclaration) stmt);
        }
    }

    // Reset local tracking
    localCounter = 0;
    localIndices.clear();

    // Declare fixed locals
    result.append("    (local $obj i32)\n");
    result.append("    (local $temp i32)\n");

    // Register parameter names in localIndices with -1 (so they use param names)
    for (Param param : ctor.params) {
        localIndices.put(param.name, -1);
    }

    // Reserve numeric local slots for body variables
    for (VariableDeclaration varDecl : bodyVariables) {
        result.append("    (local $" + localCounter + " i32)\n");
        localIndices.put(varDecl.name, localCounter);
        localCounter++;
    }

    // Allocate object
    int objectSize = calculateObjectSize(currentClass);
    result.append("    ;;; Allocate object of size " + objectSize + "\n");
    result.append("    i32.const " + objectSize + "\n");
    result.append("    call $allocate\n");
    result.append("    local.set $obj\n");

    // Initialize object header
    result.append("    ;;; Initialize VTable pointer\n");
    result.append("    local.get $obj\n");
    result.append("    i32.const " + vtableOffsets.get(currentClass.name) + "\n");
    result.append("    i32.store\n");

    result.append("    ;;; Initialize type ID\n");
    result.append("    local.get $obj\n");
    result.append("    i32.const " + typeIds.get(currentClass.name) + "\n");
    result.append("    i32.store offset=4\n");

    // Initialize fields
    for (FieldDeclaration field : currentClass.fieldDeclarations) {
        result.append("    ;;; Initialize field " + field.name + "\n");
        int fieldOffset = fieldOffsets.get(currentClass.name).get(field.name);
        if (field.init != null) {
            result.append("    local.get $obj\n");
            field.init.accept(this);
            result.append("    i32.store offset=" + fieldOffset + "\n");
        } else {
            result.append("    local.get $obj\n");
            result.append("    i32.const 0\n");
            result.append("    i32.store offset=" + fieldOffset + "\n");
        }
    }

    // Handle super constructor call
    boolean hasExplicitSuperCall = false;
    for (Statement stmt : ctor.body) {
        if (stmt instanceof ExpressionStatement) {
            Expression expr = ((ExpressionStatement) stmt).value;
            if (expr instanceof SuperConstructorCall) {
                hasExplicitSuperCall = true;
                break;
            }
        }
    }
    if (currentClass.superClass != null && !hasExplicitSuperCall) {
        result.append("    ;;; Call parent constructor implicitly\n");
        result.append("    local.get $obj\n");
        for (Param param : ctor.params) {
            result.append("    local.get $" + param.name + "\n");
        }
        result.append("    call $" + currentClass.superClass.name + ".constructor\n");
        result.append("    drop\n");
    }

    // Generate constructor body statements
    for (Statement stmt : ctor.body) {
        stmt.accept(this);
    }

    // Return the object
    result.append("    local.get $obj\n");
    result.append("  )\n\n");
}

    private int calculateObjectSize(ClassDeclaration cls) {
        Map<String, Integer> offsets = fieldOffsets.get(cls.name);
        int maxOffset = offsets.values().stream().mapToInt(Integer::intValue).max().orElse(8);
        return maxOffset + 4; // Add padding
    }

    private void generateStandardLibraryMethods() {
        // Generate Integer methods
        generateIntegerMethod("Plus", "i32.add");
        generateIntegerMethod("Minus", "i32.sub");
        generateIntegerMethod("Mult", "i32.mul");
        generateIntegerMethod("Div", "i32.div_s");
        generateIntegerMethod("Rem", "i32.rem_s");
        generateIntegerMethod("Less", "i32.lt_s");
        generateIntegerMethod("LessEqual", "i32.le_s");
        generateIntegerMethod("Greater", "i32.gt_s");
        generateIntegerMethod("GreaterEqual", "i32.ge_s");
        generateIntegerMethod("Equal", "i32.eq");

        // Generate Integer.UnaryMinus
        result.append("  (func $Integer.UnaryMinus (param $this i32) (result i32)\n");
        result.append("    local.get $this\n");
        result.append("    i32.load offset=8\n");
        result.append("    i32.const 0\n");
        result.append("    i32.sub\n");
        result.append("    call $create_integer\n");
        result.append("  )\n\n");

        // Generate Integer.toReal
        result.append("  (func $Integer.toReal (param $this i32) (result i32)\n");
        result.append("    local.get $this\n");
        result.append("    i32.load offset=8\n");
        result.append("    f64.convert_i32_s\n");
        result.append("    call $create_real\n");
        result.append("  )\n\n");

        // Generate Integer.toBoolean
        result.append("  (func $Integer.toBoolean (param $this i32) (result i32)\n");
        result.append("    local.get $this\n");
        result.append("    i32.load offset=8\n");
        result.append("    i32.const 0\n");
        result.append("    i32.ne\n");
        result.append("    call $create_boolean\n");
        result.append("  )\n\n");

        result.append("  (func $Real.Rem (param $this i32) (param $other i32) (result i32)\n");
        result.append("    local.get $this\n");
        result.append("    f64.load offset=8\n");
        result.append("    local.get $other\n");
        result.append("    f64.load offset=8\n");
        result.append("    call $real_rem\n");
        result.append("    call $create_real\n");
        result.append("  )\n\n");

        // Generate Real methods
        generateRealMethod("Plus", "f64.add");
        generateRealMethod("Minus", "f64.sub");
        generateRealMethod("Mult", "f64.mul");
        generateRealMethod("Div", "f64.div");
        // generateRealMethod("Rem", "f64.rem");

        // Generate Real comparison methods
        generateRealComparisonMethod("Less", "f64.lt");
        generateRealComparisonMethod("LessEqual", "f64.le");
        generateRealComparisonMethod("Greater", "f64.gt");
        generateRealComparisonMethod("GreaterEqual", "f64.ge");
        generateRealComparisonMethod("Equal", "f64.eq");

        // Generate Real.UnaryMinus
        result.append("  (func $Real.UnaryMinus (param $this i32) (result i32)\n");
        result.append("    local.get $this\n");
        result.append("    f64.load offset=8\n");
        result.append("    f64.neg\n");
        result.append("    call $create_real\n");
        result.append("  )\n\n");

        // Generate Real.toInteger
        result.append("  (func $Real.toInteger (param $this i32) (result i32)\n");
        result.append("    local.get $this\n");
        result.append("    f64.load offset=8\n");
        result.append("    i32.trunc_f64_s\n");
        result.append("    call $create_integer\n");
        result.append("  )\n\n");

        // Generate Boolean methods
        generateBooleanMethod("Or", "i32.or");
        generateBooleanMethod("And", "i32.and");
        generateBooleanMethod("Xor", "i32.xor");

        result.append("  (func $Boolean.Not (param $this i32) (result i32)\n");
        result.append("    local.get $this\n");
        result.append("    i32.load offset=8\n");
        result.append("    i32.eqz\n");
        result.append("    call $create_boolean\n");
        result.append("  )\n\n");

        result.append("  (func $Boolean.toInteger (param $this i32) (result i32)\n");
        result.append("    local.get $this\n");
        result.append("    i32.load offset=8\n");
        result.append("    call $create_integer\n");
        result.append("  )\n\n");

        // Generate Array methods
        result.append("  (func $Array.Length (param $this i32) (result i32)\n");
        result.append("    local.get $this\n");
        result.append("    i32.load offset=8\n");
        result.append("    call $create_integer\n");
        result.append("  )\n\n");

        // Generate List methods
        result.append("  (func $List.size (param $this i32) (result i32)\n");
        result.append("    local.get $this\n");
        result.append("    i32.load offset=8\n");
        result.append("    call $create_integer\n");
        result.append("  )\n\n");
    }

    private void generateIntegerMethod(String methodName, String wasmOp) {
        result.append("  (func $Integer." + methodName + " (param $this i32) (param $other i32) (result i32)\n");
        result.append("    local.get $this\n");
        result.append("    i32.load offset=8\n");
        result.append("    local.get $other\n");
        result.append("    i32.load offset=8\n");
        result.append("    " + wasmOp + "\n");
        result.append("    call $create_integer\n");
        result.append("  )\n\n");
    }

    private void generateRealMethod(String methodName, String wasmOp) {
        result.append("  (func $Real." + methodName + " (param $this i32) (param $other i32) (result i32)\n");
        result.append("    local.get $this\n");
        result.append("    f64.load offset=8\n");
        result.append("    local.get $other\n");
        result.append("    f64.load offset=8\n");
        result.append("    " + wasmOp + "\n");
        result.append("    call $create_real\n");
        result.append("  )\n\n");
    }

    private void generateRealComparisonMethod(String methodName, String wasmOp) {
        result.append("  (func $Real." + methodName + " (param $this i32) (param $other i32) (result i32)\n");
        result.append("    local.get $this\n");
        result.append("    f64.load offset=8\n");
        result.append("    local.get $other\n");
        result.append("    f64.load offset=8\n");
        result.append("    " + wasmOp + "\n");
        result.append("    call $create_boolean\n");
        result.append("  )\n\n");
    }

    private void generateBooleanMethod(String methodName, String wasmOp) {
        result.append("  (func $Boolean." + methodName + " (param $this i32) (param $other i32) (result i32)\n");
        result.append("    local.get $this\n");
        result.append("    i32.load offset=8\n");
        result.append("    local.get $other\n");
        result.append("    i32.load offset=8\n");
        result.append("    " + wasmOp + "\n");
        result.append("    call $create_boolean\n");
        result.append("  )\n\n");
    }

    private void generateRuntime() {
        result.append("  ;; Runtime functions\n\n");

        // Allocation function
        result.append("  (func $allocate (param $size i32) (result i32)\n");
        result.append("    (local $ptr i32)\n");
        result.append("    global.get $heap_ptr\n");
        result.append("    local.set $ptr\n");
        result.append("    global.get $heap_ptr\n");
        result.append("    local.get $size\n");
        result.append("    i32.add\n");
        result.append("    global.set $heap_ptr\n");
        result.append("    local.get $ptr\n");
        result.append("  )\n\n");

        // Create integer object
        result.append("  (func $create_integer (param $value i32) (result i32)\n");
        result.append("    (local $obj i32)\n");
        result.append("    i32.const 12  ;;; size: header(8) + value(4)\n");
        result.append("    call $allocate\n");
        result.append("    local.set $obj\n");
        result.append("    local.get $obj\n");
        result.append("    i32.const " + vtableOffsets.get("Integer") + "  ;;; Integer vtable\n");
        result.append("    i32.store\n");
        result.append("    local.get $obj\n");
        result.append("    i32.const " + typeIds.get("Integer") + "  ;;; Integer type ID\n");
        result.append("    i32.store offset=4\n");
        result.append("    local.get $obj\n");
        result.append("    local.get $value\n");
        result.append("    i32.store offset=8\n");
        result.append("    local.get $obj\n");
        result.append("  )\n\n");

        // Create real object
        result.append("  (func $create_real (param $value f64) (result i32)\n");
        result.append("    (local $obj i32)\n");
        result.append("    i32.const 16  ;;; size: header(8) + value(8)\n");
        result.append("    call $allocate\n");
        result.append("    local.set $obj\n");
        result.append("    local.get $obj\n");
        result.append("    i32.const " + vtableOffsets.get("Real") + "  ;;; Real vtable\n");
        result.append("    i32.store\n");
        result.append("    local.get $obj\n");
        result.append("    i32.const " + typeIds.get("Real") + "  ;;; Real type ID\n");
        result.append("    i32.store offset=4\n");
        result.append("    local.get $obj\n");
        result.append("    local.get $value\n");
        result.append("    f64.store offset=8\n");
        result.append("    local.get $obj\n");
        result.append("  )\n\n");

        // Create boolean object
        result.append("  (func $create_boolean (param $value i32) (result i32)\n");
        result.append("    (local $obj i32)\n");
        result.append("    i32.const 12  ;;; size: header(8) + value(4)\n");
        result.append("    call $allocate\n");
        result.append("    local.set $obj\n");
        result.append("    local.get $obj\n");
        result.append("    i32.const " + vtableOffsets.get("Boolean") + "  ;;; Boolean vtable\n");
        result.append("    i32.store\n");
        result.append("    local.get $obj\n");
        result.append("    i32.const " + typeIds.get("Boolean") + "  ;;; Boolean type ID\n");
        result.append("    i32.store offset=4\n");
        result.append("    local.get $obj\n");
        result.append("    local.get $value\n");
        result.append("    i32.store offset=8\n");
        result.append("    local.get $obj\n");
        result.append("  )\n\n");

        // Create array object
        result.append("  (func $create_array (param $size i32) (result i32)\n");
        result.append("    (local $obj i32)\n");
        result.append("    (local $array_data i32)\n");
        result.append("    (local $i i32)\n");
        result.append("    ;;; Calculate total size: header(8) + length(4) + data_ptr(4) + capacity(4)\n");
        result.append("    i32.const 20\n");
        result.append("    call $allocate\n");
        result.append("    local.set $obj\n");
        result.append("    \n");
        result.append("    ;;; Initialize header\n");
        result.append("    local.get $obj\n");
        result.append("    i32.const " + vtableOffsets.get("Array") + "  ;;; Array vtable\n");
        result.append("    i32.store\n");
        result.append("    local.get $obj\n");
        result.append("    i32.const " + typeIds.get("Array") + "  ;;; Array type ID\n");
        result.append("    i32.store offset=4\n");
        result.append("    \n");
        result.append("    ;;; Store array length and capacity\n");
        result.append("    local.get $obj\n");
        result.append("    local.get $size\n");
        result.append("    i32.store offset=8\n");
        result.append("    local.get $obj\n");
        result.append("    local.get $size\n");
        result.append("    i32.store offset=16\n");
        result.append("    \n");
        result.append("    ;;; Allocate and initialize array data\n");
        result.append("    local.get $size\n");
        result.append("    i32.const 4\n");
        result.append("    i32.mul\n");
        result.append("    call $allocate\n");
        result.append("    local.set $array_data\n");
        result.append("    \n");
        result.append("    ;;; Store data pointer\n");
        result.append("    local.get $obj\n");
        result.append("    local.get $array_data\n");
        result.append("    i32.store offset=12\n");
        result.append("    \n");
        result.append("    ;;; Initialize array elements to null (0)\n");
        result.append("    i32.const 0\n");
        result.append("    local.set $i\n");
        result.append("    loop $init_loop\n");
        result.append("      local.get $i\n");
        result.append("      local.get $size\n");
        result.append("      i32.lt_s\n");
        result.append("      if\n");
        result.append("        local.get $array_data\n");
        result.append("        local.get $i\n");
        result.append("        i32.const 4\n");
        result.append("        i32.mul\n");
        result.append("        i32.add\n");
        result.append("        i32.const 0\n");
        result.append("        i32.store\n");
        result.append("        local.get $i\n");
        result.append("        i32.const 1\n");
        result.append("        i32.add\n");
        result.append("        local.set $i\n");
        result.append("        br $init_loop\n");
        result.append("      end\n");
        result.append("    end\n");
        result.append("    \n");
        result.append("    local.get $obj\n");
        result.append("  )\n\n");

        // Create list object
        result.append("  (func $create_list (result i32)\n");
        result.append("    (local $obj i32)\n");
        result.append("    (local $data_ptr i32)\n");
        result.append("    i32.const 24  ;;; size: header(8) + length(4) + capacity(4) + data_ptr(4) + size(4)\n");
        result.append("    call $allocate\n");
        result.append("    local.set $obj\n");
        result.append("    \n");
        result.append("    ;;; Initialize header\n");
        result.append("    local.get $obj\n");
        result.append("    i32.const " + vtableOffsets.get("List") + "  ;;; List vtable\n");
        result.append("    i32.store\n");
        result.append("    local.get $obj\n");
        result.append("    i32.const " + typeIds.get("List") + "  ;;; List type ID\n");
        result.append("    i32.store offset=4\n");
        result.append("    \n");
        result.append("    ;;; Initialize list fields\n");
        result.append("    local.get $obj\n");
        result.append("    i32.const 0   ;;; initial length\n");
        result.append("    i32.store offset=8\n");
        result.append("    local.get $obj\n");
        result.append("    i32.const 10  ;;; initial capacity\n");
        result.append("    i32.store offset=12\n");
        result.append("    local.get $obj\n");
        result.append("    i32.const 0   ;;; initial size\n");
        result.append("    i32.store offset=20\n");
        result.append("    \n");
        result.append("    ;;; Allocate initial data array\n");
        result.append("    i32.const 40  ;;; 10 elements * 4 bytes\n");
        result.append("    call $allocate\n");
        result.append("    local.set $data_ptr\n");
        result.append("    local.get $obj\n");
        result.append("    local.get $data_ptr\n");
        result.append("    i32.store offset=16\n");
        result.append("    \n");
        result.append("    local.get $obj\n");
        result.append("  )\n\n");

        // Array get method
        result.append("  (func $Array.get (param $this i32) (param $index i32) (result i32)\n");
        result.append("    (local $data_ptr i32)\n");
        result.append("    (local $length i32)\n");
        result.append("    ;;; Bounds check\n");
        result.append("    local.get $this\n");
        result.append("    i32.load offset=8\n");
        result.append("    local.set $length\n");
        result.append("    local.get $index\n");
        result.append("    i32.const 0\n");
        result.append("    i32.lt_s\n");
        result.append("    local.get $index\n");
        result.append("    local.get $length\n");
        result.append("    i32.ge_s\n");
        result.append("    i32.or\n");
        result.append("    if\n");
        result.append("      i32.const 0  ;;; return null on out of bounds\n");
        result.append("      return\n");
        result.append("    end\n");
        result.append("    \n");
        result.append("    ;;; Load element\n");
        result.append("    local.get $this\n");
        result.append("    i32.load offset=12\n");
        result.append("    local.set $data_ptr\n");
        result.append("    local.get $data_ptr\n");
        result.append("    local.get $index\n");
        result.append("    i32.const 4\n");
        result.append("    i32.mul\n");
        result.append("    i32.add\n");
        result.append("    i32.load\n");
        result.append("  )\n\n");

        // Array set method
        result.append("  (func $Array.set (param $this i32) (param $index i32) (param $value i32)\n");
        result.append("    (local $data_ptr i32)\n");
        result.append("    (local $length i32)\n");
        result.append("    ;;; Bounds check\n");
        result.append("    local.get $this\n");
        result.append("    i32.load offset=8\n");
        result.append("    local.set $length\n");
        result.append("    local.get $index\n");
        result.append("    i32.const 0\n");
        result.append("    i32.lt_s\n");
        result.append("    local.get $index\n");
        result.append("    local.get $length\n");
        result.append("    i32.ge_s\n");
        result.append("    i32.or\n");
        result.append("    if\n");
        result.append("      return  ;;; ignore out of bounds\n");
        result.append("    end\n");
        result.append("    \n");
        result.append("    ;;; Store element\n");
        result.append("    local.get $this\n");
        result.append("    i32.load offset=12\n");
        result.append("    local.set $data_ptr\n");
        result.append("    local.get $data_ptr\n");
        result.append("    local.get $index\n");
        result.append("    i32.const 4\n");
        result.append("    i32.mul\n");
        result.append("    i32.add\n");
        result.append("    local.get $value\n");
        result.append("    i32.store\n");
        result.append("  )\n\n");

        result.append("  (func $real_rem (param $a f64) (param $b f64) (result f64)\n");
        result.append("    local.get $a\n");
        result.append("    local.get $b\n");
        result.append("    f64.div\n");
        result.append("    f64.trunc\n");
        result.append("    local.get $b\n");
        result.append("    f64.mul\n");
        result.append("    local.get $a\n");
        result.append("    f64.sub\n");
        result.append("  )\n\n");

        // Array toList method
        result.append("  (func $Array.toList (param $this i32) (result i32)\n");
        result.append("    (local $list i32)\n");
        result.append("    (local $length i32)\n");
        result.append("    (local $i i32)\n");
        result.append("    \n");
        result.append("    ;;; Create new list\n");
        result.append("    call $create_list\n");
        result.append("    local.set $list\n");
        result.append("    \n");
        result.append("    ;;; Get array length\n");
        result.append("    local.get $this\n");
        result.append("    i32.load offset=8\n");
        result.append("    local.set $length\n");
        result.append("    \n");
        result.append("    ;;; Copy elements from array to list\n");
        result.append("    i32.const 0\n");
        result.append("    local.set $i\n");
        result.append("    loop $copy_loop\n");
        result.append("      local.get $i\n");
        result.append("      local.get $length\n");
        result.append("      i32.lt_s\n");
        result.append("      if\n");
        result.append("        local.get $list\n");
        result.append("        local.get $this\n");
        result.append("        local.get $i\n");
        result.append("        call $Array.get\n");
        result.append("        call $List.append\n");
        result.append("        drop\n");
        result.append("        local.get $i\n");
        result.append("        i32.const 1\n");
        result.append("        i32.add\n");
        result.append("        local.set $i\n");
        result.append("        br $copy_loop\n");
        result.append("      end\n");
        result.append("    end\n");
        result.append("    \n");
        result.append("    local.get $list\n");
        result.append("  )\n\n");

        // List append method
        result.append("  (func $List.append (param $this i32) (param $value i32) (result i32)\n");
        result.append("    (local $length i32)\n");
        result.append("    (local $capacity i32)\n");
        result.append("    (local $data_ptr i32)\n");
        result.append("    (local $new_data_ptr i32)\n");
        result.append("    (local $i i32)\n");
        result.append("    \n");
        result.append("    ;;; Load current state\n");
        result.append("    local.get $this\n");
        result.append("    i32.load offset=8\n");
        result.append("    local.set $length\n");
        result.append("    local.get $this\n");
        result.append("    i32.load offset=12\n");
        result.append("    local.set $capacity\n");
        result.append("    local.get $this\n");
        result.append("    i32.load offset=16\n");
        result.append("    local.set $data_ptr\n");
        result.append("    \n");
        result.append("    ;;; Check if we need to resize\n");
        result.append("    local.get $length\n");
        result.append("    local.get $capacity\n");
        result.append("    i32.ge_s\n");
        result.append("    if\n");
        result.append("      ;;; Double the capacity\n");
        result.append("      local.get $capacity\n");
        result.append("      i32.const 2\n");
        result.append("      i32.mul\n");
        result.append("      local.set $capacity\n");
        result.append("      \n");
        result.append("      ;;; Allocate new data array\n");
        result.append("      local.get $capacity\n");
        result.append("      i32.const 4\n");
        result.append("      i32.mul\n");
        result.append("      call $allocate\n");
        result.append("      local.set $new_data_ptr\n");
        result.append("      \n");
        result.append("      ;;; Copy old data\n");
        result.append("      i32.const 0\n");
        result.append("      local.set $i\n");
        result.append("      loop $copy_loop\n");
        result.append("        local.get $i\n");
        result.append("        local.get $length\n");
        result.append("        i32.lt_s\n");
        result.append("        if\n");
        result.append("          local.get $new_data_ptr\n");
        result.append("          local.get $i\n");
        result.append("          i32.const 4\n");
        result.append("          i32.mul\n");
        result.append("          i32.add\n");
        result.append("          local.get $data_ptr\n");
        result.append("          local.get $i\n");
        result.append("          i32.const 4\n");
        result.append("          i32.mul\n");
        result.append("          i32.add\n");
        result.append("          i32.load\n");
        result.append("          i32.store\n");
        result.append("          local.get $i\n");
        result.append("          i32.const 1\n");
        result.append("          i32.add\n");
        result.append("          local.set $i\n");
        result.append("          br $copy_loop\n");
        result.append("        end\n");
        result.append("      end\n");
        result.append("      \n");
        result.append("      ;;; Update list state\n");
        result.append("      local.get $this\n");
        result.append("      local.get $capacity\n");
        result.append("      i32.store offset=12\n");
        result.append("      local.get $this\n");
        result.append("      local.get $new_data_ptr\n");
        result.append("      i32.store offset=16\n");
        result.append("      local.get $new_data_ptr\n");
        result.append("      local.set $data_ptr\n");
        result.append("    end\n");
        result.append("    \n");
        result.append("    ;;; Append the value\n");
        result.append("    local.get $data_ptr\n");
        result.append("    local.get $length\n");
        result.append("    i32.const 4\n");
        result.append("    i32.mul\n");
        result.append("    i32.add\n");
        result.append("    local.get $value\n");
        result.append("    i32.store\n");
        result.append("    \n");
        result.append("    ;;; Update length and size\n");
        result.append("    local.get $this\n");
        result.append("    local.get $length\n");
        result.append("    i32.const 1\n");
        result.append("    i32.add\n");
        result.append("    i32.store offset=8\n");
        result.append("    local.get $this\n");
        result.append("    local.get $length\n");
        result.append("    i32.const 1\n");
        result.append("    i32.add\n");
        result.append("    i32.store offset=20\n");
        result.append("    \n");
        result.append("    ;;; Return the list for chaining\n");
        result.append("    local.get $this\n");
        result.append("  )\n\n");

        // List head method
        result.append("  (func $List.head (param $this i32) (result i32)\n");
        result.append("    (local $length i32)\n");
        result.append("    local.get $this\n");
        result.append("    i32.load offset=8\n");
        result.append("    local.set $length\n");
        result.append("    local.get $length\n");
        result.append("    i32.const 0\n");
        result.append("    i32.gt_s\n");
        // This if returns an i32 from either branch, so declare the if's result
        result.append("    if (result i32)\n");
        result.append("      local.get $this\n");
        result.append("      i32.load offset=16\n");
        result.append("      i32.load\n");
        result.append("    else\n");
        result.append("      i32.const 0  ;;; return null for empty list\n");
        result.append("    end\n");
        result.append("  )\n\n");

        // List tail method
        result.append("  (func $List.tail (param $this i32) (result i32)\n");
        result.append("    (local $new_list i32)\n");
        result.append("    (local $length i32)\n");
        result.append("    (local $i i32)\n");
        result.append("    \n");
        result.append("    local.get $this\n");
        result.append("    i32.load offset=8\n");
        result.append("    local.set $length\n");
        result.append("    \n");
        result.append("    local.get $length\n");
        result.append("    i32.const 1\n");
        result.append("    i32.le_s\n");
        result.append("    if\n");
        result.append("      call $create_list  ;;; return empty list\n");
        result.append("      return\n");
        result.append("    end\n");
        result.append("    \n");
        result.append("    ;;; Create new list\n");
        result.append("    call $create_list\n");
        result.append("    local.set $new_list\n");
        result.append("    \n");
        result.append("    ;;; Copy all but first element\n");
        result.append("    i32.const 1\n");
        result.append("    local.set $i\n");
        result.append("    loop $copy_loop\n");
        result.append("      local.get $i\n");
        result.append("      local.get $length\n");
        result.append("      i32.lt_s\n");
        result.append("      if\n");
        result.append("        local.get $new_list\n");
        result.append("        local.get $this\n");
        result.append("        i32.load offset=16\n");
        result.append("        local.get $i\n");
        result.append("        i32.const 4\n");
        result.append("        i32.mul\n");
        result.append("        i32.add\n");
        result.append("        i32.load\n");
        result.append("        call $List.append\n");
        result.append("        drop\n");
        result.append("        local.get $i\n");
        result.append("        i32.const 1\n");
        result.append("        i32.add\n");
        result.append("        local.set $i\n");
        result.append("        br $copy_loop\n");
        result.append("      end\n");
        result.append("    end\n");
        result.append("    \n");
        result.append("    local.get $new_list\n");
        result.append("  )\n\n");

        // Dynamic dispatch function
        result.append("  (func $dynamic_dispatch (param $obj i32) (param $method_index i32) (result i32)\n");
        result.append("    (local $vtable i32)\n");
        result.append("    (local $method_ptr i32)\n");
        result.append("    \n");
        result.append("    ;;; Load vtable pointer from object header\n");
        result.append("    local.get $obj\n");
        result.append("    i32.load\n");
        result.append("    local.set $vtable\n");
        result.append("    \n");
        result.append("    ;;; Calculate method pointer address: vtable + 4 (skip size) + method_index * 4\n");
        result.append("    local.get $vtable\n");
        result.append("    i32.const 4\n");
        result.append("    i32.add\n");
        result.append("    local.get $method_index\n");
        result.append("    i32.const 4\n");
        result.append("    i32.mul\n");
        result.append("    i32.add\n");
        result.append("    i32.load\n");
        result.append("    local.set $method_ptr\n");
        result.append("    \n");
        result.append("    local.get $method_ptr\n");
        result.append("  )\n\n");

        // Error handling functions
        result.append("  (func $unknown_method (result i32)\n");
        result.append("    i32.const 0  ;;; return null\n");
        result.append("  )\n\n");

        result.append("  (func $method_not_found (result i32)\n");
        result.append("    i32.const 0  ;;; return null\n");
        result.append("  )\n\n");

        // Utility functions
        result.append("  (func $print_integer (param $value i32)\n");
        result.append("    ;;; This would interface with WASM host environment for printing\n");
        result.append("    ;;; For now, it's a no-op\n");
        result.append("    local.get $value\n");
        result.append("    drop\n");
        result.append("  )\n\n");

        result.append("  (func $print_real (param $value i32)\n");
        result.append("    ;;; This would interface with WASM host environment for printing\n");
        result.append("    ;;; For now, it's a no-op\n");
        result.append("    local.get $value\n");
        result.append("    drop\n");
        result.append("  )\n\n");

        result.append("  (func $print_boolean (param $value i32)\n");
        result.append("    ;;; This would interface with WASM host environment for printing\n");
        result.append("    ;;; For now, it's a no-op\n");
        result.append("    local.get $value\n");
        result.append("    drop\n");
        result.append("  )\n\n");

        result.append("  (func $print_string (param $str i32) (param $len i32)\n");
        result.append("    ;;; This would interface with WASM host environment for printing\n");
        result.append("    ;;; For now, it's a no-op\n");
        result.append("    local.get $str\n");
        result.append("    drop\n");
        result.append("    local.get $len\n");
        result.append("    drop\n");
        result.append("  )\n\n");

        // Memory management utilities
        result.append("  (func $gc_collect\n");
        result.append("    ;;; Simple garbage collection - reset heap pointer for now\n");
        result.append("    ;;; In a real implementation, this would be more sophisticated\n");
        result.append("    i32.const 0x1000\n");
        result.append("    global.set $heap_ptr\n");
        result.append("  )\n\n");

        result.append("  (func $get_heap_size (export \"get_heap_size\") (result i32)\n");
        result.append("    global.get $heap_ptr\n");
        result.append("    i32.const 0x1000\n");
        result.append("    i32.sub\n");
        result.append("  )\n\n");
    }

    private String getMangledMethodName(String className, String methodName, List<Param> params) {
        StringBuilder mangledName = new StringBuilder(className + "." + methodName);
        for (Param param : params) {
            mangledName.append("_").append(param.t.name);
        }
        return mangledName.toString();
    }

    private void generateEntryPoint(Program program) {
        result.append("  ;; Program entry point\n");
        result.append("  (func $main (export \"_start\")\n");
        result.append("    (local $temp i32)\n");

        if (!program.classes.isEmpty()) {
            ClassDeclaration firstClass = program.classes.get(0);
            result.append("    ;;; Create instance of " + firstClass.name + "\n");
            if (!firstClass.constructorDeclarations.isEmpty()) {
                ConstructorDeclaration ctor = firstClass.constructorDeclarations.get(0);
                // Push default arguments
                for (int i = 0; i < ctor.params.size(); i++) {
                    result.append("    i32.const 0  ;;; default argument " + i + "\n");
                }
                result.append("    call $" + firstClass.name + ".constructor\n");
                result.append("    drop\n");
            } else {
                // Default constructor
                result.append("    call $" + firstClass.name + ".constructor\n");
                result.append("    drop\n");
            }
        } else {
            result.append("    i32.const 42\n");
            result.append("    call $create_integer\n");
            result.append("    drop\n");
        }

        result.append("  )\n\n");
    }

    @Override
    public Void visit(MethodCall node) {
        // Evaluate receiver and store it
        node.target.accept(this);
        result.append("    local.set $temp\n");

        // Evaluate and push arguments (in order)
        for (Expression arg : node.args) {
            arg.accept(this);
        }

        String methodName = extractMethodName(node);
        VariableType targetType = node.target.type;

        boolean methodFound = false;

        if (targetType != null && targetType.type != null) {
            String typeName = targetType.type;

            List<Param> argParams = new ArrayList<>();
            for (Expression arg : node.args) {
                argParams.add(new Param("", new Type(arg.type.type)));
            }
            String mangledMethodName = getMangledMethodName(typeName, methodName, argParams);

            List<String> methods = methodTables.get(typeName);
            if (methods != null && methods.contains(mangledMethodName)) {
                result.append("    local.get $temp\n");
                result.append("    call $" + mangledMethodName + "\n");
                methodFound = true;
            }
        }

        if (!methodFound) {
            // If the method was not found, consume receiver and arguments from the stack
            for (int i = 0; i < node.args.size(); i++) {
                result.append("    drop\n");
            }
            result.append("    local.get $temp\n");
            result.append("    drop\n");

            // Push a null result
            result.append("    ;;; Method not found: " + methodName + "\n");
            result.append("    call $method_not_found\n");
        }

        return null;
    }

    private String extractMethodName(MethodCall node) {
        // Extract method name from the target expression
        if (node.target instanceof MemberAccess) {
            Expression member = ((MemberAccess) node.target).member;
            if (member instanceof VariableReference) {
                return ((VariableReference) member).name;
            }
        } else if (node.target instanceof VariableReference) {
            return ((VariableReference) node.target).name;
        }
        return "unknown";
    }

    @Override
    public Void visit(Program node) {
        return null;
    }

    @Override
    public Void visit(ClassDeclaration node) {
        return null;
    }

    @Override
    public Void visit(ConstructorDeclaration node) {
        return null;
    }

    @Override
    public Void visit(FieldDeclaration node) {
        return null;
    }

    @Override
    public Void visit(MethodDeclaration node) {
        return null;
    }

    @Override
    public Void visit(IntegerLiteral node) {
        result.append("    i32.const " + node.value + "\n");
        result.append("    call $create_integer\n");
        return null;
    }

    @Override
    public Void visit(BooleanLiteral node) {
        result.append("    i32.const " + (node.value ? "1" : "0") + "\n");
        result.append("    call $create_boolean\n");
        return null;
    }

    @Override
    public Void visit(RealLiteral node) {
        result.append("    f64.const " + node.value + "\n");
        result.append("    call $create_real\n");
        return null;
    }

    @Override
    public Void visit(ConstructorCall node) {
        switch (node.className) {
            case "Integer":
                if (node.args.isEmpty()) {
                    node.args.add(new IntegerLiteral(0));
                }
            case "Real":
                if (node.args.isEmpty()) {
                    node.args.add(new RealLiteral(0));
                }
            case "Boolean":
                if (node.args.isEmpty()) {
                    node.args.add(new BooleanLiteral(false));
                }
                node.args.get(0).accept(this);
                return null;
            default:
                for (Expression arg : node.args) {
                    arg.accept(this);
                }
                result.append("    call $" + node.className + ".constructor\n");
                return null;
        }
    }

    @Override
    public Void visit(MemberAccess node) {
        node.target.accept(this);
        if (node.member instanceof VariableReference) {
            String fieldName = ((VariableReference) node.member).name;
            VariableType targetType = node.target.type;
            if (targetType != null) {
                Map<String, Integer> offsets = fieldOffsets.get(targetType.type);
                if (offsets != null && offsets.containsKey(fieldName)) {
                    int offset = offsets.get(fieldName);
                    result.append("    i32.load offset=" + offset + "\n");
                    return null;
                }
            }
        }
        result.append("    ;;; Member access - assuming method\n");
        return null;
    }

    @Override
    public Void visit(ThisExpression node) {
        result.append("    local.get $this\n");
        return null;
    }

    @Override
    public Void visit(SuperConstructorCall node) {
        for (Expression arg : node.args) {
            arg.accept(this);
        }
        if (currentClass != null && currentClass.superClass != null) {
            result.append("    local.get $this\n");
            result.append("    call $" + currentClass.superClass.name + ".constructor\n");
            result.append("    drop\n");
        }
        return null;
    }

    @Override
public Void visit(AssignmentStatement node) {
    node.value.accept(this);
    if (node.target instanceof VariableReference) {
        String varName = ((VariableReference) node.target).name;
        Integer localIndex = localIndices.get(varName);
        if (localIndex != null) {
            if (localIndex == -1) {
                result.append("    local.set $" + varName + "\n");
            } else {
                result.append("    local.set $" + localIndex + "\n");
            }
        } else {
            // Handle field assignment - use $obj in constructors, $this in methods
            if (currentMethod == null && currentClass != null) {
                // Constructor
                result.append("    local.get $obj\n");
            } else {
                // Method  
                result.append("    local.get $this\n");
            }
            if (currentClass != null) {
                Map<String, Integer> offsets = fieldOffsets.get(currentClass.name);
                if (offsets != null && offsets.containsKey(varName)) {
                    int offset = offsets.get(varName);
                    result.append("    i32.store offset=" + offset + "\n");
                }
            }
        }
    } else if (node.target instanceof MemberAccess) {
        MemberAccess memberAccess = (MemberAccess) node.target;
        memberAccess.target.accept(this);
        if (memberAccess.member instanceof VariableReference) {
            String fieldName = ((VariableReference) memberAccess.member).name;
            VariableType targetType = memberAccess.target.type;
            if (targetType != null) {
                Map<String, Integer> offsets = fieldOffsets.get(targetType.type);
                if (offsets != null && offsets.containsKey(fieldName)) {
                    int offset = offsets.get(fieldName);
                    result.append("    i32.store offset=" + offset + "\n");
                }
            }
        }
    }
    return null;
    }

    @Override
public Void visit(VariableReference node) {
    Integer localIndex = localIndices.get(node.name);
    if (localIndex != null) {
        if (localIndex == -1) {
            result.append("    local.get $" + node.name + "\n");
        } else {
            result.append("    local.get $" + localIndex + "\n");
        }
    } else {
        if (currentMethod == null && currentClass != null) {
            // Constructor
            result.append("    local.get $obj\n");
        } else {
            // Method
            result.append("    local.get $this\n");
        }
        if (currentClass != null) {
            Map<String, Integer> offsets = fieldOffsets.get(currentClass.name);
            if (offsets != null && offsets.containsKey(node.name)) {
                int offset = offsets.get(node.name);
                result.append("    i32.load offset=" + offset + "\n");
            }
        }
    }
    return null;
}

    @Override
    public Void visit(WhileStatement node) {
        String loopLabel = "loop_" + labelCounter++;
        String endLabel = "end_" + labelCounter++;

        result.append("    block $" + endLabel + "\n");
        result.append("      loop $" + loopLabel + "\n");

        node.cond.accept(this);
        result.append("      i32.eqz\n");
        result.append("      br_if $" + endLabel + "\n");

        for (Statement stmt : node.body) {
            stmt.accept(this);
        }

        result.append("      br $" + loopLabel + "\n");
        result.append("    end\n");
        result.append("    end\n");
        return null;
    }

    @Override
    public Void visit(IfStatement node) {
        String elseLabel = "else_" + labelCounter++;
        String endLabel = "end_" + labelCounter++;

        node.cond.accept(this);
        result.append("    i32.eqz\n");
        result.append("    br_if $" + elseLabel + "\n");

        if (node.thenBody != null) {
            for (Statement stmt : node.thenBody.body) {
                stmt.accept(this);
            }
        }
        result.append("    br $" + endLabel + "\n");

        result.append("    $" + elseLabel + ":\n");
        if (node.elseBody != null) {
            for (Statement stmt : node.elseBody.body) {
                stmt.accept(this);
            }
        }

        result.append("    $" + endLabel + ":\n");
        return null;
    }

    @Override
    public Void visit(ReturnStatement node) {
        if (node.value != null) {
            node.value.accept(this);
        } else if (currentMethod != null && !currentMethod.returnType.name.equals("null")) {
            result.append("    i32.const 0  ;;; default return\n");
        }
        result.append("    return\n");
        return null;
    }

    @Override
    public Void visit(VariableDeclaration node) {
        Integer localIndex = localIndices.get(node.name);
        if (localIndex == null) {
            localIndex = localCounter;
            localIndices.put(node.name, localIndex);
            localCounter++;
        }

        if (node.init != null) {
            node.init.accept(this);
            result.append("    local.set $" + localIndex + "\n");
        } else {
            result.append("    i32.const 0\n");
            result.append("    local.set $" + localIndex + "\n");
        }

        return null;
    }

    @Override
    public Void visit(ExpressionStatement node) {
        node.value.accept(this);

        if (producesExpressionValue(node.value)) {
            result.append("    drop\n");
        }
        return null;
    }

    private boolean producesExpressionValue(Expression expr) {
        return expr instanceof MethodCall ||
                expr instanceof ConstructorCall ||
                expr instanceof IntegerLiteral ||
                expr instanceof RealLiteral ||
                expr instanceof BooleanLiteral ||
                expr instanceof VariableReference ||
                expr instanceof MemberAccess ||
                expr instanceof ThisExpression ||
                expr instanceof ArrayLiteral ||
                expr instanceof ListLiteral;
    }

    @Override
    public Void visit(ExtensionType node) {
        return null;
    }

    @Override
    public Void visit(Param node) {
        return null;
    }

    @Override
    public Void visit(ReturnType node) {
        return null;
    }

    @Override
    public Void visit(Type node) {
        return null;
    }

    @Override
    public Void visit(ArrayLiteral node) {
        result.append("    i32.const " + node.size + "\n");
        result.append("    call $create_array\n");
        return null;
    }

    @Override
    public Void visit(ListLiteral node) {
        result.append("    call $create_list\n");
        return null;
    }

    @Override
    public Void visit(ThenStatement node) {
        return null;
    }

    @Override
    public Void visit(ElseStatement node) {
        return null;
    }

    @Override
    public Void visit(StringLiteral stringLiteral) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }
}