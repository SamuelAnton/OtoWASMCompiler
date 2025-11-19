package Translator;

import Syntaxer.ast.*;
import Syntaxer.ast.declaration.*;
import Syntaxer.ast.expression.*;
import Syntaxer.ast.statement.*;
import Syntaxer.ast.literal.*;
import Syntaxer.ast.component.*;
import Semanticer.Components.Types.VariableType;
import Semanticer.Components.Types.ProgramTypes;

import java.io.FileWriter;
import java.io.IOException;
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
        for (ClassDeclaration cls : program.classes) {
            currentClass = cls;
            for (MethodDeclaration method : cls.methodDeclarations) {
                generateMethod(method);
            }
            for (ConstructorDeclaration ctor : cls.constructorDeclarations) {
                generateConstructor(ctor);
            }
        }

        // Generate standard library methods
        generateStandardLibraryMethods();

        // Generate runtime functions
        generateRuntime();

        // Generate program entry point
        generateEntryPoint(program);

        result.append(")\n");

        return result;
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
                String.format("%02x", methods.size()) + "\\00\\00\\00\")  ;; vtable size for " + typeName + "\n");

        // Reserve space for method pointers (will be filled as we generate functions)
        for (int i = 0; i < methods.size(); i++) {
            result.append("  (data (i32.const " + (vtableStart + 4 + i * 4) + ") \"\\00\\00\\00\\00\")  ;; method " + i
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
        if (cls.superClass != null) {
            methods.addAll(methodTables.get(cls.superClass.name));
        }

        // Add own methods
        for (MethodDeclaration method : cls.methodDeclarations) {
            methods.add(cls.name + "." + method.name);
        }

        return methods;
    }

    private void generateMethod(MethodDeclaration method) {
        currentMethod = method;
        localCounter = 0;
        localIndices.clear();

        String funcName = currentClass.name + "." + method.name;
        functionAddresses.put(funcName, currentFunctionAddress);
        currentFunctionAddress += 0x100; // Reserve space for function

        result.append("  (func $" + funcName);

        // Parameters - first parameter is always 'this'
        result.append(" (param $this i32)");
        for (Param param : method.params) {
            result.append(" (param $" + param.name + " i32)");
        }

        // Return type
        if (!method.returnType.name.equals("null")) {
            result.append(" (result i32)");
        }

        result.append("\n");

        // Local variables
        result.append("    (local $temp i32)\n");
        result.append("    (local $result i32)\n");

        // Method body
        result.append("    ;; Method body for " + funcName + "\n");

        // Setup locals for parameters
        localIndices.put("this", localCounter++);
        for (Param param : method.params) {
            localIndices.put(param.name, localCounter++);
        }

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
                        "\")  ;; " + methodName + "\n");
            }
        }
    }

    private void generateConstructor(ConstructorDeclaration ctor) {
        String funcName = currentClass.name + ".constructor";
        functionAddresses.put(funcName, currentFunctionAddress);
        currentFunctionAddress += 0x100;

        result.append("  (func $" + funcName);

        // Parameters
        for (Param param : ctor.params) {
            result.append(" (param $" + param.name + " i32)");
        }
        result.append(" (result i32)\n");

        result.append("    (local $obj i32)\n");
        result.append("    (local $temp i32)\n");

        // Allocate object
        int objectSize = calculateObjectSize(currentClass);
        result.append("    ;; Allocate object of size " + objectSize + "\n");
        result.append("    i32.const " + objectSize + "\n");
        result.append("    call $allocate\n");
        result.append("    local.set $obj\n");

        // Initialize object header
        result.append("    ;; Initialize VTable pointer\n");
        result.append("    local.get $obj\n");
        result.append("    i32.const " + vtableOffsets.get(currentClass.name) + "\n");
        result.append("    i32.store\n");

        result.append("    ;; Initialize type ID\n");
        result.append("    local.get $obj\n");
        result.append("    i32.const " + typeIds.get(currentClass.name) + "\n");
        result.append("    i32.store offset=4\n");

        // Initialize fields
        for (FieldDeclaration field : currentClass.fieldDeclarations) {
            result.append("    ;; Initialize field " + field.name + "\n");
            int fieldOffset = fieldOffsets.get(currentClass.name).get(field.name);
            if (field.init != null) {
                field.init.accept(this);
                result.append("    local.get $obj\n");
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

        // Call parent constructor if exists and no explicit super call
        if (currentClass.superClass != null && !hasExplicitSuperCall) {
            result.append("    ;; Call parent constructor implicitly\n");
            result.append("    local.get $obj\n");
            for (Param param : ctor.params) {
                result.append("    local.get $" + param.name + "\n");
            }
            result.append("    call $" + currentClass.superClass.name + ".constructor\n");
            result.append("    drop\n");
        }

        // Generate constructor body
        localCounter = 0;
        localIndices.clear();
        localIndices.put("this", localCounter++);
        for (Param param : ctor.params) {
            localIndices.put(param.name, localCounter++);
        }

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

        // Generate Real methods
        generateRealMethod("Plus", "f64.add");
        generateRealMethod("Minus", "f64.sub");
        generateRealMethod("Mult", "f64.mul");
        generateRealMethod("Div", "f64.div");
        generateRealMethod("Rem", "f64.rem");

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
        // Your existing generateRuntime implementation goes here
        // Make sure it includes all the functions we discussed:
        // - $allocate
        // - $create_integer, $create_real, $create_boolean
        // - $create_array, $create_list
        // - $Array.get, $Array.set, $Array.Length
        // - $List.append, $List.head, $List.tail, $List.size
        // - $dynamic_dispatch
    }

    private void generateEntryPoint(Program program) {
        result.append("  ;; Program entry point\n");
        result.append("  (func $main (export \"_start\")\n");
        result.append("    (local $temp i32)\n");

        // For now, create an instance of the first class and call its constructor
        if (!program.classes.isEmpty()) {
            ClassDeclaration firstClass = program.classes.get(0);
            result.append("    ;; Create instance of " + firstClass.name + "\n");
            if (!firstClass.constructorDeclarations.isEmpty()) {
                ConstructorDeclaration ctor = firstClass.constructorDeclarations.get(0);
                // Push default arguments
                for (int i = 0; i < ctor.params.size(); i++) {
                    result.append("    i32.const 0  ;; default argument " + i + "\n");
                }
                result.append("    call $" + firstClass.name + ".constructor\n");
                result.append("    drop\n");
            } else {
                // Default constructor
                result.append("    call $" + firstClass.name + ".constructor\n");
                result.append("    drop\n");
            }
        } else {
            // No user classes, create a simple integer
            result.append("    i32.const 42\n");
            result.append("    call $create_integer\n");
            result.append("    drop\n");
        }

        result.append("  )\n\n");
    }

    // Visitor implementations with proper method resolution
    @Override
    public Void visit(MethodCall node) {
        // Store target object
        node.target.accept(this);
        result.append("    local.set $temp\n");

        // Evaluate and push arguments
        for (Expression arg : node.args) {
            arg.accept(this);
        }

        // Resolve method call
        String methodName = extractMethodName(node);
        VariableType targetType = node.target.type;

        if (targetType != null && isStandardLibraryType(targetType.type)) {
            // Standard library method - direct call
            result.append("    local.get $temp\n");
            result.append("    call $" + targetType.type + "." + methodName + "\n");
        } else if (targetType != null) {
            // User-defined method - use dynamic dispatch
            int methodIndex = findMethodIndex(targetType.type, methodName, node.args.size());
            if (methodIndex != -1) {
                result.append("    local.get $temp\n");
                result.append("    i32.const " + methodIndex + "\n");
                result.append("    call $dynamic_dispatch\n");
                // The result is already on stack from dynamic_dispatch
            } else {
                // Method not found
                result.append("    call $method_not_found\n");
            }
        } else {
            // Unknown type
            result.append("    call $unknown_method\n");
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

    private boolean isStandardLibraryType(String typeName) {
        return typeName.equals("Integer") || typeName.equals("Real") ||
                typeName.equals("Boolean") || typeName.equals("Array") ||
                typeName.equals("List");
    }

    private int findMethodIndex(String typeName, String methodName, int argCount) {
        List<String> methods = methodTables.get(typeName);
        if (methods != null) {
            String fullMethodName = typeName + "." + methodName;
            return methods.indexOf(fullMethodName);
        }
        return -1;
    }

    // Other visitor methods remain the same as in previous version
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
        for (Expression arg : node.args) {
            arg.accept(this);
        }
        result.append("    call $" + node.className + ".constructor\n");
        return null;
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
        // If field not found, try as method (for chained calls)
        result.append("    ;; Member access - assuming method\n");
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
                result.append("    local.set $" + localIndex + "\n");
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
            result.append("    local.get $" + localIndex + "\n");
        } else {
            // Assume it's a field on 'this'
            result.append("    local.get $this\n");
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
            result.append("    i32.const 0  ;; default return\n");
        }
        result.append("    return\n");
        return null;
    }

    @Override
    public Void visit(VariableDeclaration node) {
        localIndices.put(node.name, localCounter);
        result.append("    (local $" + localCounter + " i32)\n");

        if (node.init != null) {
            node.init.accept(this);
            result.append("    local.set $" + localCounter + "\n");
        } else {
            result.append("    i32.const 0\n");
            result.append("    local.set $" + localCounter + "\n");
        }

        localCounter++;
        return null;
    }

    @Override
    public Void visit(ExpressionStatement node) {
        node.value.accept(this);
        if (!(node.value instanceof MethodCall) && !(node.value instanceof ConstructorCall)) {
            result.append("    drop\n");
        }
        return null;
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