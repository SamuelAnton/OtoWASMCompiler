// package Translator;

// import java.io.IOException;
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;

// import Semanticer.Components.Exceptions.ValidationException;
// import Semanticer.Components.Types.ProgramTypes;
// import Syntaxer.ast.ASTVisitor;
// import Syntaxer.ast.Program;
// import Syntaxer.ast.component.ExtensionType;
// import Syntaxer.ast.component.Param;
// import Syntaxer.ast.component.ReturnType;
// import Syntaxer.ast.component.Type;
// import Syntaxer.ast.declaration.ClassDeclaration;
// import Syntaxer.ast.declaration.ConstructorDeclaration;
// import Syntaxer.ast.declaration.FieldDeclaration;
// import Syntaxer.ast.declaration.MethodDeclaration;
// import Syntaxer.ast.expression.ConstructorCall;
// import Syntaxer.ast.expression.Expression;
// import Syntaxer.ast.expression.MemberAccess;
// import Syntaxer.ast.expression.MethodCall;
// import Syntaxer.ast.expression.SuperConstructorCall;
// import Syntaxer.ast.expression.ThisExpression;
// import Syntaxer.ast.literal.ArrayLiteral;
// import Syntaxer.ast.literal.BooleanLiteral;
// import Syntaxer.ast.literal.IntegerLiteral;
// import Syntaxer.ast.literal.ListLiteral;
// import Syntaxer.ast.literal.RealLiteral;
// import Syntaxer.ast.literal.StringLiteral;
// import Syntaxer.ast.statement.AssignmentStatement;
// import Syntaxer.ast.statement.ElseStatement;
// import Syntaxer.ast.statement.ExpressionStatement;
// import Syntaxer.ast.statement.IfStatement;
// import Syntaxer.ast.statement.ReturnStatement;
// import Syntaxer.ast.statement.Statement;
// import Syntaxer.ast.statement.ThenStatement;
// import Syntaxer.ast.statement.VariableDeclaration;
// import Syntaxer.ast.statement.VariableReference;
// import Syntaxer.ast.statement.WhileStatement;

// public class PreTranslator implements ASTVisitor<Void> {
// StringBuilder result = new StringBuilder();

// HashMap<String, Integer> typeIds = new HashMap<>();
// HashMap<String, Integer> vtableOffsets = new HashMap<>();
// HashMap<String, ArrayList<String>> methodTables = new HashMap<>();
// HashMap<String, HashMap<String, Integer>> fieldOffsets = new HashMap<>();

// int nextTypeID = 0;
// int nextVTableOffset = 100;

// ClassDeclaration curClass;
// MethodDeclaration curMethod;
// int localCounter = 0;
// HashMap<String, Integer> localIndices = new HashMap<>();
// int labelCounter = 0;

// public String generate(Program p) {
// // Initialize standard types
// standardTypesInit();

// // Generate WASM module header
// result.append("(module\n");
// result.append(" (memory 1)\n");
// result.append(" (global $heap_ptr (mut i32) (i32.const 0x1000))\n\n");
// result.append(" (export \"memory\" (memory 0))\n\n");

// // Calculate field offsets and VTables
// for (ClassDeclaration c : p.classes) {
// calculateClassLayout(c);
// }

// // Generate VTables
// generateVTables(p);

// // Generate standard library methods
// generateStandardLibraryMethods();

// // Generate runtime functions
// generateRuntime();

// // Generate functions for all methods
// for (ClassDeclaration c : p.classes) {
// curClass = c;
// for (MethodDeclaration m : c.methodDeclarations) {
// generateMethod(m);
// }
// for (ConstructorDeclaration cn : c.constructorDeclarations) {
// generateConstructor(cn);
// }
// }

// // Generate program entry point
// // generateEntryPoint(program);

// result.append(")\n");
// return result.toString();
// }

// private void standardTypesInit() {
// typeIds.put("Integer", nextTypeID++);
// typeIds.put("Real", nextTypeID++);
// typeIds.put("Boolean", nextTypeID++);
// typeIds.put("Array", nextTypeID++);
// typeIds.put("List", nextTypeID++);
// typeIds.put("AnyValue", nextTypeID++);
// typeIds.put("AnyRef", nextTypeID++);
// }

// private void calculateClassLayout(ClassDeclaration c) {
// if (fieldOffsets.containsKey(c.name)) {
// return;
// }

// HashMap<String, Integer> offsets = new HashMap<>();
// int curOffset = 8;

// if (c.superClass != null) {
// calculateClassLayout(c.superClass);
// HashMap<String, Integer> pOffsets = fieldOffsets.get(c.superClass.name);
// if (pOffsets != null) {
// offsets.putAll(pOffsets);
// curOffset =
// pOffsets.values().stream().mapToInt(Integer::intValue).max().orElse(8) + 4;
// }
// }

// for (FieldDeclaration f : c.fieldDeclarations) {
// offsets.put(f.name, curOffset);
// curOffset += 4;
// }

// fieldOffsets.put(c.name, offsets);
// typeIds.put(c.name, nextTypeID++);

// }

// private void generateVTables(Program p) {
// result.append(" ;; Virtual Method Tables\n");

// int vtableStart = 100;

// String[] standardTypes = { "Integer", "Real", "Boolean", "Array", "List" };
// for (String type : standardTypes) {
// vtableOffsets.put(type, vtableStart);
// ArrayList<String> methods = getStandardTypeMethods(type);
// methodTables.put(type, methods);
// vtableStart += 4 + (methods.size() * 4);
// }

// for (ClassDeclaration cls : p.classes) {
// vtableOffsets.put(cls.name, vtableStart);
// ArrayList<String> methods = collectAllMethods(cls);
// methodTables.put(cls.name, methods);
// vtableStart += 4 + (methods.size() * 4);
// }

// for (String type : standardTypes) {
// ArrayList<String> methods = methodTables.get(type);
// int vtableOffset = vtableOffsets.get(type);

// // Write VTable size
// result.append(" (data (i32.const " + vtableOffset + ") \"\\" +
// String.format("%02x", methods.size()) + "\\00\\00\\00\") ;; vtable size for "
// + type + "\n");

// // Write method pointers (will be filled later)
// for (int i = 0; i < methods.size(); i++) {
// result.append(" (data (i32.const " + (vtableOffset + 4 + i * 4) + ")
// \"\\00\\00\\00\\00\") ;; method "
// + i + "\n");
// }
// }

// // Write VTable data for user classes
// for (ClassDeclaration cls : p.classes) {
// String className = cls.name;
// ArrayList<String> methods = methodTables.get(className);
// int vtableOffset = vtableOffsets.get(className);

// // Write VTable size
// result.append(" (data (i32.const " + vtableOffset + ") \"\\" +
// String.format("%02x", methods.size()) + "\\00\\00\\00\") ;; vtable size for "
// + className + "\n");

// // Write method pointers (will be filled later)
// for (int i = 0; i < methods.size(); i++) {
// result.append(" (data (i32.const " + (vtableOffset + 4 + i * 4) + ")
// \"\\00\\00\\00\\00\") ;; method "
// + i + "\n");
// }
// }
// result.append("\n");
// }

// private ArrayList<String> getStandardTypeMethods(String type) {
// ArrayList<String> methods = new ArrayList<>();
// switch (type) {
// case "Integer":
// methods.add("Integer.toReal");
// methods.add("Integer.toBoolean");
// methods.add("Integer.UnaryMinus");
// methods.add("Integer.Plus");
// methods.add("Integer.Minus");
// methods.add("Integer.Mult");
// methods.add("Integer.Div");
// methods.add("Integer.Rem");
// methods.add("Integer.Less");
// methods.add("Integer.LessEqual");
// methods.add("Integer.Greater");
// methods.add("Integer.GreaterEqual");
// methods.add("Integer.Equal");
// break;
// case "Real":
// methods.add("Real.toInteger");
// methods.add("Real.UnaryMinus");
// methods.add("Real.Plus");
// methods.add("Real.Minus");
// methods.add("Real.Mult");
// methods.add("Real.Div");
// methods.add("Real.Rem");
// methods.add("Real.Less");
// methods.add("Real.LessEqual");
// methods.add("Real.Greater");
// methods.add("Real.GreaterEqual");
// methods.add("Real.Equal");
// break;
// case "Boolean":
// methods.add("Boolean.toInteger");
// methods.add("Boolean.Or");
// methods.add("Boolean.And");
// methods.add("Boolean.Xor");
// methods.add("Boolean.Not");
// break;
// case "Array":
// methods.add("Array.toList");
// methods.add("Array.Length");
// methods.add("Array.get");
// methods.add("Array.set");
// break;
// case "List":
// methods.add("List.append");
// methods.add("List.head");
// methods.add("List.tail");
// break;
// }
// return methods;
// }

// private ArrayList<String> collectAllMethods(ClassDeclaration c) {
// ArrayList<String> methods = new ArrayList<>();

// if (c.superClass != null) {
// methods.addAll(collectAllMethods(c.superClass));
// }

// for (MethodDeclaration m : c.methodDeclarations) {
// methods.add(c.name + "." + m.name);
// }

// return methods;
// }

// private void generateStandardLibraryMethods() {
// // Generate Integer methods
// generateIntegerMethod("Plus", "i32.add");
// updateVTable("Integer", "Integer.Plus");
// generateIntegerMethod("Minus", "i32.sub");
// updateVTable("Integer", "Integer.Minus");
// generateIntegerMethod("Mult", "i32.mul");
// updateVTable("Integer", "Integer.Mult");
// generateIntegerMethod("Div", "i32.div_s");
// updateVTable("Integer", "Integer.Div");
// generateIntegerMethod("Rem", "i32.rem_s");
// updateVTable("Integer", "Integer.Rem");
// generateIntegerMethod("Less", "i32.lt_s");
// updateVTable("Integer", "Integer.Less");
// generateIntegerMethod("LessEqual", "i32.le_s");
// updateVTable("Integer", "Integer.LessEqual");
// generateIntegerMethod("Greater", "i32.gt_s");
// updateVTable("Integer", "Integer.Greater");
// generateIntegerMethod("GreaterEqual", "i32.ge_s");
// updateVTable("Integer", "Integer.GreaterEqual");
// generateIntegerMethod("Equal", "i32.eq");
// updateVTable("Integer", "Integer.Equal");

// // Generate Integer.UnaryMinus
// result.append(" (func $Integer.UnaryMinus (param $this i32) (result i32)\n");
// result.append(" local.get $this\n");
// result.append(" i32.load offset=8\n");
// result.append(" i32.const 0\n");
// result.append(" i32.sub\n");
// result.append(" call $create_integer\n");
// result.append(" )\n\n");
// updateVTable("Integer", "Integer.UnaryMinus");

// // Generate Integer.toReal
// result.append(" (func $Integer.toReal (param $this i32) (result i32)\n");
// result.append(" local.get $this\n");
// result.append(" i32.load offset=8\n");
// result.append(" f64.convert_i32_s\n");
// result.append(" call $create_real\n");
// result.append(" )\n\n");
// updateVTable("Integer", "Integer.toReal");

// // Generate Real methods
// generateRealMethod("Plus", "f64.add");
// updateVTable("Real", "Real.Plus");
// generateRealMethod("Minus", "f64.sub");
// updateVTable("Real", "Real.Minus");
// generateRealMethod("Mult", "f64.mul");
// updateVTable("Real", "Real.Mult");
// generateRealMethod("Div", "f64.div");
// updateVTable("Real", "Real.Div");

// // Generate Boolean methods
// generateBooleanMethod("Or", "i32.or");
// updateVTable("Boolean", "Boolean.Or");
// generateBooleanMethod("And", "i32.and");
// updateVTable("Boolean", "Boolean.And");
// generateBooleanMethod("Xor", "i32.xor");
// updateVTable("Boolean", "Boolean.Xor");

// result.append(" (func $Boolean.Not (param $this i32) (result i32)\n");
// result.append(" local.get $this\n");
// result.append(" i32.load offset=8\n");
// result.append(" i32.eqz\n");
// result.append(" call $create_boolean\n");
// result.append(" )\n\n");
// updateVTable("Boolean", "Boolean.Not");

// // Generate Array methods
// result.append(" (func $Array.Length (param $this i32) (result i32)\n");
// result.append(" local.get $this\n");
// result.append(" i32.load offset=8\n");
// result.append(" call $create_integer\n");
// result.append(" )\n\n");
// updateVTable("Array", "Array.Length");
// }

// private void updateVTable(String className, String methodName) {
// List<String> methods = methodTables.get(className);
// if (methods != null) {
// int methodIndex = methods.indexOf(methodName);
// if (methodIndex != -1) {
// int vtableOffset = vtableOffsets.get(className);
// int methodPtrOffset = vtableOffset + 4 + (methodIndex * 4);
// // Calculate function pointer offset (functions start at 0x1000 in WASM)
// }
// }
// }

// private void generateIntegerMethod(String methodName, String wasmOp) {
// result.append(" (func $Integer." + methodName + " (param $this i32) (param
// $other i32) (result i32)\n");
// result.append(" local.get $this\n");
// result.append(" i32.load offset=8\n");
// result.append(" local.get $other\n");
// result.append(" i32.load offset=8\n");
// result.append(" " + wasmOp + "\n");
// result.append(" call $create_integer\n");
// result.append(" )\n\n");
// }

// private void generateRealMethod(String methodName, String wasmOp) {
// result.append(" (func $Real." + methodName + " (param $this i32) (param
// $other i32) (result i32)\n");
// result.append(" local.get $this\n");
// result.append(" f64.load offset=8\n");
// result.append(" local.get $other\n");
// result.append(" f64.load offset=8\n");
// result.append(" " + wasmOp + "\n");
// result.append(" call $create_real\n");
// result.append(" )\n\n");
// }

// private void generateBooleanMethod(String methodName, String wasmOp) {
// result.append(" (func $Boolean." + methodName + " (param $this i32) (param
// $other i32) (result i32)\n");
// result.append(" local.get $this\n");
// result.append(" i32.load offset=8\n");
// result.append(" local.get $other\n");
// result.append(" i32.load offset=8\n");
// result.append(" " + wasmOp + "\n");
// result.append(" call $create_boolean\n");
// result.append(" )\n\n");
// }

// private void generateRuntime() {
// result.append(" ;; Runtime functions\n");

// // Allocation function
// result.append(" (func $allocate (param $size i32) (result i32)\n");
// result.append(" (local $ptr i32)\n");
// result.append(" global.get $heap_ptr\n");
// result.append(" local.set $ptr\n");
// result.append(" global.get $heap_ptr\n");
// result.append(" local.get $size\n");
// result.append(" i32.add\n");
// result.append(" global.set $heap_ptr\n");
// result.append(" local.get $ptr\n");
// result.append(" )\n\n");

// // Create integer object
// result.append(" (func $create_integer (param $value i32) (result i32)\n");
// result.append(" (local $obj i32)\n");
// result.append(" i32.const 12 ;; size: header(8) + value(4)\n");
// result.append(" call $allocate\n");
// result.append(" local.set $obj\n");
// result.append(" local.get $obj\n");
// result.append(" i32.const " + vtableOffsets.get("Integer") + "\n");
// result.append(" i32.store\n");
// result.append(" local.get $obj\n");
// result.append(" i32.const " + typeIds.get("Integer") + "\n");
// result.append(" i32.store offset=4\n");
// result.append(" local.get $obj\n");
// result.append(" local.get $value\n");
// result.append(" i32.store offset=8\n");
// result.append(" local.get $obj\n");
// result.append(" )\n\n");

// // Create real object
// result.append(" (func $create_real (param $value f64) (result i32)\n");
// result.append(" (local $obj i32)\n");
// result.append(" i32.const 16 ;; size: header(8) + value(8)\n");
// result.append(" call $allocate\n");
// result.append(" local.set $obj\n");
// result.append(" local.get $obj\n");
// result.append(" i32.const " + vtableOffsets.get("Real") + "\n");
// result.append(" i32.store\n");
// result.append(" local.get $obj\n");
// result.append(" i32.const " + typeIds.get("Real") + "\n");
// result.append(" i32.store offset=4\n");
// result.append(" local.get $obj\n");
// result.append(" local.get $value\n");
// result.append(" f64.store offset=8\n");
// result.append(" local.get $obj\n");
// result.append(" )\n\n");

// // Create boolean object
// result.append(" (func $create_boolean (param $value i32) (result i32)\n");
// result.append(" (local $obj i32)\n");
// result.append(" i32.const 12 ;; size: header(8) + value(4)\n");
// result.append(" call $allocate\n");
// result.append(" local.set $obj\n");
// result.append(" local.get $obj\n");
// result.append(" i32.const " + vtableOffsets.get("Boolean") + "\n");
// result.append(" i32.store\n");
// result.append(" local.get $obj\n");
// result.append(" i32.const " + typeIds.get("Boolean") + "\n");
// result.append(" i32.store offset=4\n");
// result.append(" local.get $obj\n");
// result.append(" local.get $value\n");
// result.append(" i32.store offset=8\n");
// result.append(" local.get $obj\n");
// result.append(" )\n\n");

// // Create array object
// result.append(" (func $create_array (param $size i32) (result i32)\n");
// result.append(" (local $obj i32)\n");
// result.append(" (local $array_data i32)\n");
// result.append(" (local $i i32)\n");
// result.append(" ;; Calculate total size: header(8) + length(4) + data_ptr(4)
// + elements\n");
// result.append(" local.get $size\n");
// result.append(" i32.const 4\n");
// result.append(" i32.mul\n");
// result.append(" i32.const 16\n");
// result.append(" i32.add\n");
// result.append(" call $allocate\n");
// result.append(" local.set $obj\n");
// result.append(" \n");
// result.append(" ;; Initialize header\n");
// result.append(" local.get $obj\n");
// result.append(" i32.const " + vtableOffsets.get("Array") + "\n");
// result.append(" i32.store\n");
// result.append(" local.get $obj\n");
// result.append(" i32.const " + typeIds.get("Array") + "\n");
// result.append(" i32.store offset=4\n");
// result.append(" \n");
// result.append(" ;; Store array length\n");
// result.append(" local.get $obj\n");
// result.append(" local.get $size\n");
// result.append(" i32.store offset=8\n");
// result.append(" \n");
// result.append(" ;; Allocate and initialize array data\n");
// result.append(" local.get $size\n");
// result.append(" i32.const 4\n");
// result.append(" i32.mul\n");
// result.append(" call $allocate\n");
// result.append(" local.set $array_data\n");
// result.append(" \n");
// result.append(" ;; Store data pointer\n");
// result.append(" local.get $obj\n");
// result.append(" local.get $array_data\n");
// result.append(" i32.store offset=12\n");
// result.append(" \n");
// result.append(" ;; Initialize array elements to null (0)\n");
// result.append(" i32.const 0\n");
// result.append(" local.set $i\n");
// result.append(" loop $init_loop\n");
// result.append(" local.get $i\n");
// result.append(" local.get $size\n");
// result.append(" i32.lt_s\n");
// result.append(" if\n");
// result.append(" local.get $array_data\n");
// result.append(" local.get $i\n");
// result.append(" i32.const 4\n");
// result.append(" i32.mul\n");
// result.append(" i32.add\n");
// result.append(" i32.const 0\n");
// result.append(" i32.store\n");
// result.append(" local.get $i\n");
// result.append(" i32.const 1\n");
// result.append(" i32.add\n");
// result.append(" local.set $i\n");
// result.append(" br $init_loop\n");
// result.append(" end\n");
// result.append(" end\n");
// result.append(" \n");
// result.append(" local.get $obj\n");
// result.append(" )\n\n");

// // Create list object
// result.append(" (func $create_list (result i32)\n");
// result.append(" (local $obj i32)\n");
// result.append(" i32.const 20 ;; size: header(8) + capacity(4) + length(4) +
// data_ptr(4)\n");
// result.append(" call $allocate\n");
// result.append(" local.set $obj\n");
// result.append(" \n");
// result.append(" ;; Initialize header\n");
// result.append(" local.get $obj\n");
// result.append(" i32.const " + vtableOffsets.get("List") + "\n");
// result.append(" i32.store\n");
// result.append(" local.get $obj\n");
// result.append(" i32.const " + typeIds.get("List") + "\n");
// result.append(" i32.store offset=4\n");
// result.append(" \n");
// result.append(" ;; Initialize list fields\n");
// result.append(" local.get $obj\n");
// result.append(" i32.const 10 ;; initial capacity\n");
// result.append(" i32.store offset=8\n");
// result.append(" local.get $obj\n");
// result.append(" i32.const 0 ;; initial length\n");
// result.append(" i32.store offset=12\n");
// result.append(" \n");
// result.append(" ;; Allocate initial data array\n");
// result.append(" i32.const 40 ;; 10 elements * 4 bytes\n");
// result.append(" call $allocate\n");
// result.append(" local.get $obj\n");
// result.append(" i32.store offset=16\n");
// result.append(" \n");
// result.append(" local.get $obj\n");
// result.append(" )\n\n");

// // Array get method implementation
// result.append(" (func $Array.get (param $this i32) (param $index i32) (result
// i32)\n");
// result.append(" (local $data_ptr i32)\n");
// result.append(" (local $length i32)\n");
// result.append(" ;; Bounds check\n");
// result.append(" local.get $this\n");
// result.append(" i32.load offset=8\n");
// result.append(" local.set $length\n");
// result.append(" local.get $index\n");
// result.append(" i32.const 0\n");
// result.append(" i32.lt_s\n");
// result.append(" local.get $index\n");
// result.append(" local.get $length\n");
// result.append(" i32.ge_s\n");
// result.append(" i32.or\n");
// result.append(" if\n");
// result.append(" i32.const 0 ;; return null on out of bounds\n");
// result.append(" return\n");
// result.append(" end\n");
// result.append(" \n");
// result.append(" ;; Load element\n");
// result.append(" local.get $this\n");
// result.append(" i32.load offset=12\n");
// result.append(" local.set $data_ptr\n");
// result.append(" local.get $data_ptr\n");
// result.append(" local.get $index\n");
// result.append(" i32.const 4\n");
// result.append(" i32.mul\n");
// result.append(" i32.add\n");
// result.append(" i32.load\n");
// result.append(" )\n\n");

// // Array set method implementation
// result.append(" (func $Array.set (param $this i32) (param $index i32) (param
// $value i32)\n");
// result.append(" (local $data_ptr i32)\n");
// result.append(" (local $length i32)\n");
// result.append(" ;; Bounds check\n");
// result.append(" local.get $this\n");
// result.append(" i32.load offset=8\n");
// result.append(" local.set $length\n");
// result.append(" local.get $index\n");
// result.append(" i32.const 0\n");
// result.append(" i32.lt_s\n");
// result.append(" local.get $index\n");
// result.append(" local.get $length\n");
// result.append(" i32.ge_s\n");
// result.append(" i32.or\n");
// result.append(" if\n");
// result.append(" return ;; ignore out of bounds\n");
// result.append(" end\n");
// result.append(" \n");
// result.append(" ;; Store element\n");
// result.append(" local.get $this\n");
// result.append(" i32.load offset=12\n");
// result.append(" local.set $data_ptr\n");
// result.append(" local.get $data_ptr\n");
// result.append(" local.get $index\n");
// result.append(" i32.const 4\n");
// result.append(" i32.mul\n");
// result.append(" i32.add\n");
// result.append(" local.get $value\n");
// result.append(" i32.store\n");
// result.append(" )\n\n");

// // Array length method
// result.append(" (func $Array.Length (param $this i32) (result i32)\n");
// result.append(" local.get $this\n");
// result.append(" i32.load offset=8\n");
// result.append(" call $create_integer\n");
// result.append(" )\n\n");

// // List append method
// result.append(" (func $List.append (param $this i32) (param $value i32)
// (result i32)\n");
// result.append(" (local $length i32)\n");
// result.append(" (local $capacity i32)\n");
// result.append(" (local $data_ptr i32)\n");
// result.append(" (local $new_data_ptr i32)\n");
// result.append(" (local $i i32)\n");
// result.append(" \n");
// result.append(" ;; Load current state\n");
// result.append(" local.get $this\n");
// result.append(" i32.load offset=12\n");
// result.append(" local.set $length\n");
// result.append(" local.get $this\n");
// result.append(" i32.load offset=8\n");
// result.append(" local.set $capacity\n");
// result.append(" local.get $this\n");
// result.append(" i32.load offset=16\n");
// result.append(" local.set $data_ptr\n");
// result.append(" \n");
// result.append(" ;; Check if we need to resize\n");
// result.append(" local.get $length\n");
// result.append(" local.get $capacity\n");
// result.append(" i32.ge_s\n");
// result.append(" if\n");
// result.append(" ;; Double the capacity\n");
// result.append(" local.get $capacity\n");
// result.append(" i32.const 2\n");
// result.append(" i32.mul\n");
// result.append(" local.set $capacity\n");
// result.append(" \n");
// result.append(" ;; Allocate new data array\n");
// result.append(" local.get $capacity\n");
// result.append(" i32.const 4\n");
// result.append(" i32.mul\n");
// result.append(" call $allocate\n");
// result.append(" local.set $new_data_ptr\n");
// result.append(" \n");
// result.append(" ;; Copy old data\n");
// result.append(" i32.const 0\n");
// result.append(" local.set $i\n");
// result.append(" loop $copy_loop\n");
// result.append(" local.get $i\n");
// result.append(" local.get $length\n");
// result.append(" i32.lt_s\n");
// result.append(" if\n");
// result.append(" local.get $new_data_ptr\n");
// result.append(" local.get $i\n");
// result.append(" i32.const 4\n");
// result.append(" i32.mul\n");
// result.append(" i32.add\n");
// result.append(" local.get $data_ptr\n");
// result.append(" local.get $i\n");
// result.append(" i32.const 4\n");
// result.append(" i32.mul\n");
// result.append(" i32.add\n");
// result.append(" i32.load\n");
// result.append(" i32.store\n");
// result.append(" local.get $i\n");
// result.append(" i32.const 1\n");
// result.append(" i32.add\n");
// result.append(" local.set $i\n");
// result.append(" br $copy_loop\n");
// result.append(" end\n");
// result.append(" end\n");
// result.append(" \n");
// result.append(" ;; Update list state\n");
// result.append(" local.get $this\n");
// result.append(" local.get $capacity\n");
// result.append(" i32.store offset=8\n");
// result.append(" local.get $this\n");
// result.append(" local.get $new_data_ptr\n");
// result.append(" i32.store offset=16\n");
// result.append(" local.set $data_ptr\n");
// result.append(" end\n");
// result.append(" \n");
// result.append(" ;; Append the value\n");
// result.append(" local.get $data_ptr\n");
// result.append(" local.get $length\n");
// result.append(" i32.const 4\n");
// result.append(" i32.mul\n");
// result.append(" i32.add\n");
// result.append(" local.get $value\n");
// result.append(" i32.store\n");
// result.append(" \n");
// result.append(" ;; Update length\n");
// result.append(" local.get $this\n");
// result.append(" local.get $length\n");
// result.append(" i32.const 1\n");
// result.append(" i32.add\n");
// result.append(" i32.store offset=12\n");
// result.append(" \n");
// result.append(" ;; Return the list for chaining\n");
// result.append(" local.get $this\n");
// result.append(" )\n\n");

// // List head method
// result.append(" (func $List.head (param $this i32) (result i32)\n");
// result.append(" (local $length i32)\n");
// result.append(" local.get $this\n");
// result.append(" i32.load offset=12\n");
// result.append(" local.set $length\n");
// result.append(" local.get $length\n");
// result.append(" i32.const 0\n");
// result.append(" i32.gt_s\n");
// result.append(" if\n");
// result.append(" local.get $this\n");
// result.append(" i32.load offset=16\n");
// result.append(" i32.load\n");
// result.append(" else\n");
// result.append(" i32.const 0 ;; return null for empty list\n");
// result.append(" end\n");
// result.append(" )\n\n");

// // List tail method
// result.append(" (func $List.tail (param $this i32) (result i32)\n");
// result.append(" (local $new_list i32)\n");
// result.append(" (local $length i32)\n");
// result.append(" (local $i i32)\n");
// result.append(" \n");
// result.append(" local.get $this\n");
// result.append(" i32.load offset=12\n");
// result.append(" local.set $length\n");
// result.append(" \n");
// result.append(" local.get $length\n");
// result.append(" i32.const 1\n");
// result.append(" i32.le_s\n");
// result.append(" if\n");
// result.append(" call $create_list ;; return empty list\n");
// result.append(" return\n");
// result.append(" end\n");
// result.append(" \n");
// result.append(" ;; Create new list\n");
// result.append(" call $create_list\n");
// result.append(" local.set $new_list\n");
// result.append(" \n");
// result.append(" ;; Copy all but first element\n");
// result.append(" i32.const 1\n");
// result.append(" local.set $i\n");
// result.append(" loop $copy_loop\n");
// result.append(" local.get $i\n");
// result.append(" local.get $length\n");
// result.append(" i32.lt_s\n");
// result.append(" if\n");
// result.append(" local.get $new_list\n");
// result.append(" local.get $this\n");
// result.append(" i32.load offset=16\n");
// result.append(" local.get $i\n");
// result.append(" i32.const 4\n");
// result.append(" i32.mul\n");
// result.append(" i32.add\n");
// result.append(" i32.load\n");
// result.append(" call $List.append\n");
// result.append(" drop\n");
// result.append(" local.get $i\n");
// result.append(" i32.const 1\n");
// result.append(" i32.add\n");
// result.append(" local.set $i\n");
// result.append(" br $copy_loop\n");
// result.append(" end\n");
// result.append(" end\n");
// result.append(" \n");
// result.append(" local.get $new_list\n");
// result.append(" )\n\n");

// // Dynamic dispatch function
// result.append(" (func $dynamic_dispatch (param $obj i32) (param $method_index
// i32) (result i32)\n");
// result.append(" (local $vtable i32)\n");
// result.append(" (local $method_ptr i32)\n");
// result.append(" local.get $obj\n");
// result.append(" i32.load\n");
// result.append(" local.set $vtable\n");
// result.append(" local.get $vtable\n");
// result.append(" local.get $method_index\n");
// result.append(" i32.const 4\n");
// result.append(" i32.mul\n");
// result.append(" i32.add\n");
// result.append(" i32.const 4\n");
// result.append(" i32.add\n"); // Skip size field
// result.append(" i32.load\n");
// result.append(" local.set $method_ptr\n");
// result.append(" local.get $method_ptr\n");
// result.append(" )\n\n");

// // Unknown method stub (for unimplemented methods)
// result.append(" (func $unknown_method (result i32)\n");
// result.append(" i32.const 0 ;; return null\n");
// result.append(" )\n\n");
// }

// private void generateMethod(MethodDeclaration m) {
// curMethod = m;
// localCounter = 0;
// localIndices.clear();

// String funcName = curClass.name + "." + m.name;
// result.append(" (func $" + funcName);

// // Parameters - first parameter is always 'this'
// result.append(" (param $this i32)");
// for (Param param : m.params) {
// result.append(" (param $" + param.name + " i32)");
// }

// // Return type
// if (!m.returnType.name.equals("null")) {
// result.append(" (result i32)");
// }

// result.append("\n");

// // Local variables
// result.append(" (local $temp i32)\n");

// // Method body
// result.append(" ;; Method body for " + funcName + "\n");

// // Setup locals for parameters
// localIndices.put("this", localCounter++);
// for (Param param : m.params) {
// localIndices.put(param.name, localCounter++);
// }

// // Generate body statements
// for (Statement stmt : m.body) {
// stmt.accept(this);
// }

// // Default return if no explicit return
// if (m.returnType.name.equals("null") &&
// (m.body.isEmpty() || !(m.body.get(m.body.size() - 1) instanceof
// ReturnStatement))) {
// result.append(" return\n");
// }

// result.append(" )\n\n");

// // Update VTable with method pointer
// updateVTable(curClass.name, funcName);
// }

// private void generateConstructor(ConstructorDeclaration c) {
// String funcName = curClass.name + ".constructor";
// result.append(" (func $" + funcName);

// // Parameters
// for (Param param : c.params) {
// result.append(" (param $" + param.name + " i32)");
// }
// result.append(" (result i32)\n");

// result.append(" (local $obj i32)\n");
// result.append(" (local $temp i32)\n");

// // Allocate object
// int objectSize = calculateObjectSize(curClass);
// result.append(" ;; Allocate object of size " + objectSize + "\n");
// result.append(" i32.const " + objectSize + "\n");
// result.append(" call $allocate\n");
// result.append(" local.set $obj\n");

// // Initialize object header
// result.append(" ;; Initialize VTable pointer\n");
// result.append(" local.get $obj\n");
// result.append(" i32.const " + vtableOffsets.get(curClass.name) + "\n");
// result.append(" i32.store\n");

// result.append(" ;; Initialize type ID\n");
// result.append(" local.get $obj\n");
// result.append(" i32.const " + typeIds.get(curClass.name) + "\n");
// result.append(" i32.store offset=4\n");

// // Initialize fields
// for (FieldDeclaration field : curClass.fieldDeclarations) {
// result.append(" ;; Initialize field " + field.name + "\n");
// int fieldOffset = fieldOffsets.get(curClass.name).get(field.name);
// if (field.init != null) {
// field.init.accept(this);
// result.append(" local.get $obj\n");
// result.append(" i32.store offset=" + fieldOffset + "\n");
// } else {
// result.append(" local.get $obj\n");
// result.append(" i32.const 0\n");
// result.append(" i32.store offset=" + fieldOffset + "\n");
// }
// }

// // Call parent constructor if exists and has super call
// if (curClass.superClass != null) {
// boolean hasSuperCall = c.body.stream()
// .anyMatch(stmt -> stmt instanceof ExpressionStatement &&
// ((ExpressionStatement) stmt).value instanceof SuperConstructorCall);

// if (!hasSuperCall) {
// result.append(" ;; Call parent constructor\n");
// result.append(" local.get $obj\n");
// for (Param param : c.params) {
// result.append(" local.get $" + param.name + "\n");
// }
// result.append(" call $" + curClass.superClass.name + ".constructor\n");
// result.append(" drop\n");
// }
// }

// // Generate constructor body
// localCounter = 0;
// localIndices.clear();
// localIndices.put("this", localCounter++);
// for (Param param : c.params) {
// localIndices.put(param.name, localCounter++);
// }

// for (Statement stmt : c.body) {
// stmt.accept(this);
// }

// // Return the object
// result.append(" local.get $obj\n");
// result.append(" )\n\n");
// }

// private int calculateObjectSize(ClassDeclaration cls) {
// Map<String, Integer> offsets = fieldOffsets.get(cls.name);
// int maxOffset =
// offsets.values().stream().mapToInt(Integer::intValue).max().orElse(8);
// return maxOffset + 4; // Add padding
// }

// @Override
// public Void visit(Program n) {
// return null;
// }

// @Override
// public Void visit(ClassDeclaration n) {
// return null;
// }

// @Override
// public Void visit(FieldDeclaration n) {
// return null;
// }

// @Override
// public Void visit(MethodDeclaration n) {
// return null;
// }

// @Override
// public Void visit(ConstructorDeclaration n) {
// return null;
// }

// @Override
// public Void visit(Param n) {
// // TODO Auto-generated method stub
// throw new UnsupportedOperationException("Unimplemented method 'visit'");
// }

// @Override
// public Void visit(AssignmentStatement n) {
// // TODO Auto-generated method stub
// throw new UnsupportedOperationException("Unimplemented method 'visit'");
// }

// @Override
// public Void visit(IfStatement n) {
// // TODO Auto-generated method stub
// throw new UnsupportedOperationException("Unimplemented method 'visit'");
// }

// @Override
// public Void visit(WhileStatement n) {
// // TODO Auto-generated method stub
// throw new UnsupportedOperationException("Unimplemented method 'visit'");
// }

// @Override
// public Void visit(ReturnStatement n) {
// // TODO Auto-generated method stub
// throw new UnsupportedOperationException("Unimplemented method 'visit'");
// }

// @Override
// public Void visit(VariableDeclaration n) {
// // TODO Auto-generated method stub
// throw new UnsupportedOperationException("Unimplemented method 'visit'");
// }

// @Override
// public Void visit(VariableReference n) {
// // TODO Auto-generated method stub
// throw new UnsupportedOperationException("Unimplemented method 'visit'");
// }

// @Override
// public Void visit(MemberAccess n) {
// // TODO Auto-generated method stub
// throw new UnsupportedOperationException("Unimplemented method 'visit'");
// }

// @Override
// public Void visit(MethodCall n) {
// // Evaluate target object
// n.target.accept(this);

// // Evaluate arguments
// for (Expression arg : n.args) {
// arg.accept(this);
// }

// // For now, use direct call (we'll implement dynamic dispatch later)
// if (n.target instanceof VariableReference) {
// String targetName = ((VariableReference) n.target).name;
// // This is a simplified approach - real implementation needs method
// resolution
// result.append(" call $unknown_method\n");
// }
// return null;
// }

// @Override
// public Void visit(ConstructorCall n) {
// switch (n.className) {
// case "Integer":
// if (n.args.size() > 1) {
// throw new ValidationException("Too many arguments for constructor call of
// class Integer!");
// }
// if (n.args.size() == 0) {
// return "i32.const 0";
// }
// if (n.args.get(0) instanceof IntegerLiteral) {
// return "i32.const " + ((IntegerLiteral) n.args.get(0)).value;
// }
// if (n.args.get(0).type == ProgramTypes.Integer) {
// return n.args.get(0).accept(this);
// }
// if (n.args.get(0).type == ProgramTypes.Real) {
// Void s = n.args.get(0).accept(this);
// return
// }
// case "Real":
// if (n.args.get(0) instanceof RealLiteral) {
// return "f32.const " + ((RealLiteral) n.args.get(0)).value;
// }
// else {
// return n.args.get(0).accept(this);
// }
// case "Boolean"
// default:
// break;
// }

// return null;
// }

// @Override
// public Void visit(ThisExpression n) {
// // TODO Auto-generated method stub
// throw new UnsupportedOperationException("Unimplemented method 'visit'");
// }

// @Override
// public Void visit(ExpressionStatement expressionStatement) {
// // TODO Auto-generated method stub
// throw new UnsupportedOperationException("Unimplemented method 'visit'");
// }

// @Override
// public Void visit(BooleanLiteral n) {
// result.append(" i32.const " + (n.value ? "1" : "0") + "\n");
// result.append(" call $create_boolean\n");
// return null;
// }

// @Override
// public Void visit(IntegerLiteral n) {
// result.append(" i32.const " + n.value + "\n");
// result.append(" call $create_integer\n");
// return null;
// }

// @Override
// public Void visit(RealLiteral n) {
// long bits = Double.doubleToLongBits(n.value);
// result.append(" i64.const " + bits + "\n");
// result.append(" f64.reinterpret_i64\n");

// return null;
// }

// @Override
// public Void visit(StringLiteral stringLiteral) {
// // TODO Auto-generated method stub
// throw new UnsupportedOperationException("Unimplemented method 'visit'");
// }

// @Override
// public Void visit(ExtensionType extensionType) {
// // TODO Auto-generated method stub
// throw new UnsupportedOperationException("Unimplemented method 'visit'");
// }

// @Override
// public Void visit(ReturnType returnType) {
// // TODO Auto-generated method stub
// throw new UnsupportedOperationException("Unimplemented method 'visit'");
// }

// @Override
// public Void visit(ArrayLiteral arrayLiteral) {
// // TODO Auto-generated method stub
// throw new UnsupportedOperationException("Unimplemented method 'visit'");
// }

// @Override
// public Void visit(ListLiteral listiteral) {
// // TODO Auto-generated method stub
// throw new UnsupportedOperationException("Unimplemented method 'visit'");
// }

// @Override
// public Void visit(Type type) {
// // TODO Auto-generated method stub
// throw new UnsupportedOperationException("Unimplemented method 'visit'");
// }

// @Override
// public Void visit(ElseStatement elseStatement) {
// // TODO Auto-generated method stub
// throw new UnsupportedOperationException("Unimplemented method 'visit'");
// }

// @Override
// public Void visit(ThenStatement thenStatement) {
// // TODO Auto-generated method stub
// throw new UnsupportedOperationException("Unimplemented method 'visit'");
// }

// @Override
// public Void visit(SuperConstructorCall superConstructorCall) {
// // TODO Auto-generated method stub
// throw new UnsupportedOperationException("Unimplemented method 'visit'");
// }

// }
