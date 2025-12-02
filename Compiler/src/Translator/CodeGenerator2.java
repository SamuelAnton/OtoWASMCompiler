package Translator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import Semanticer.Components.Types.ProgramTypes;
import Semanticer.Components.Types.VariableType;
import Syntaxer.ast.Program;
import Syntaxer.ast.component.ExtensionType;
import Syntaxer.ast.component.Param;
import Syntaxer.ast.component.ReturnType;
import Syntaxer.ast.component.Type;
import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.declaration.ClassDeclaration;
import Syntaxer.ast.declaration.ConstructorDeclaration;
import Syntaxer.ast.declaration.FieldDeclaration;
import Syntaxer.ast.declaration.MemberDeclaration;
import Syntaxer.ast.declaration.MethodDeclaration;
import Syntaxer.ast.expression.ConstructorCall;
import Syntaxer.ast.expression.Expression;
import Syntaxer.ast.expression.MemberAccess;
import Syntaxer.ast.expression.MethodCall;
import Syntaxer.ast.expression.SuperConstructorCall;
import Syntaxer.ast.expression.ThisExpression;
import Syntaxer.ast.literal.ArrayLiteral;
import Syntaxer.ast.literal.BooleanLiteral;
import Syntaxer.ast.literal.IntegerLiteral;
import Syntaxer.ast.literal.ListLiteral;
import Syntaxer.ast.literal.RealLiteral;
import Syntaxer.ast.literal.StringLiteral;
import Syntaxer.ast.statement.AssignmentStatement;
import Syntaxer.ast.statement.ElseStatement;
import Syntaxer.ast.statement.ExpressionStatement;
import Syntaxer.ast.statement.IfStatement;
import Syntaxer.ast.statement.ReturnStatement;
import Syntaxer.ast.statement.Statement;
import Syntaxer.ast.statement.ThenStatement;
import Syntaxer.ast.statement.VariableDeclaration;
import Syntaxer.ast.statement.VariableReference;
import Syntaxer.ast.statement.WhileStatement;

public class CodeGenerator2 implements ASTVisitor<String> {
    private final Program program;
    private final StringBuilder builder = new StringBuilder();

    private int nextTableIndex = 0;
    private final Map<String, Integer> functionToIndex = new HashMap<>();
    private final Map<Integer, String> indexToFunction = new HashMap<>();

    private final HashMap<String, Integer> typesID = new HashMap<>();
    private final HashMap<String, Integer> pointerToVTable = new HashMap<>();
    private final HashMap<String, ClassDeclaration> nameToClass = new HashMap<>();
    private int VTablePointer = 340;

    // Context tracking
    private ClassDeclaration currentClass;
    private MethodDeclaration currentMethod;
    private ConstructorDeclaration currentConstructor;
    private int labelCounter = 0;

    // For tracking local variables and parameters in current scope
    private Set<String> currentLocalVars = new HashSet<>();
    private List<String> currentParameters = new ArrayList<>();

    // Helper to generate unique labels
    private String newLabel(String prefix) {
        return prefix + "_" + (labelCounter++);
    }

    public CodeGenerator2(Program p) {
        program = p;
    }

    public StringBuilder translate() {
        // Pre construct things for user classes
        prepareClasses();

        // WASM type signatures for indirect calls
        builder.append(";; == Type signatures for indirect calls ==\n");
        builder.append("(type $method_sig_0 (func (param i32) (result i32)))\n");
        builder.append("(type $method_sig_1 (func (param i32 i32) (result i32)))\n");
        builder.append("(type $method_sig_2 (func (param i32 i32 i32) (result i32)))\n");
        builder.append("(type $method_sig_3 (func (param i32 i32 i32 i32) (result i32)))\n");

        // realization of constructors and methods
        translateClasses();

        // TODO find and export entry-point

        return builder;
    }

    private static String intToFormattedString(int value) {
        StringBuilder result = new StringBuilder();

        // Process each byte from least significant to most significant (little-endian)
        for (int i = 0; i < 4; i++) {
            // Extract the current byte (least significant byte first)
            int currentByte = (value >> (8 * i)) & 0xFF;

            // Format as "\xx" where xx is the hexadecimal representation
            result.append(String.format("\\%02x", currentByte));
        }

        return result.toString();
    }

    private void prepareClasses() {
        // Initialize types IDs
        typesID.put("Integer", 0);
        typesID.put("Real", 1);
        typesID.put("Boolean", 2);
        typesID.put("Array", 3);
        typesID.put("List", 4);

        // Assign indices to all user constructors and methods
        assignTableIndices();

        // Generate the user methods table initialization
        generateUserMethodsTableInitialization();

        // Generate Vtables
        builder.append(";; == Virtual method tables for user defined types types ==\n");
        for (ClassDeclaration c : program.classes) {
            generateVTable(c);
        }

        // Generate allocators for user's classes
        for (ClassDeclaration c : program.classes) {
            generateConstructorHelper(c);
        }

        // Generate field accessors for user's classes
        for (ClassDeclaration c : program.classes) {
            generateFieldsAccessors(c);
        }
    }

    private void assignTableIndices() {
        // Process all classes
        for (ClassDeclaration c : program.classes) {
            nameToClass.put(c.name.toLowerCase(), c);
            c.fillFieldsList();
            c.fillMethodsList();
            // Assign indices to constructors
            if (c.constructorDeclarations.isEmpty()) {
                // Default constructor
                String funcName = c.name.toLowerCase() + ".constructor_default";
                assignIndex(funcName);
            } else {
                for (int i = 0; i < c.constructorDeclarations.size(); i++) {
                    c.constructorDeclarations.get(i).number = i + 1;
                    String funcName = generateConstructorFunctionName(c.constructorDeclarations.get(i));
                    assignIndex(funcName);
                }
            }

            // Assign indices to methods
            for (MethodDeclaration method : c.methodDeclarations) {
                String funcName = generateMethodFunctionName(method);
                assignIndex(funcName);
            }
        }
    }

    private void assignIndex(String funcName) {
        if (!functionToIndex.containsKey(funcName)) {
            functionToIndex.put(funcName, nextTableIndex);
            indexToFunction.put(nextTableIndex, funcName);
            nextTableIndex++;
        }
    }

    private String generateConstructorFunctionName(ConstructorDeclaration c) {
        if (c.params.isEmpty()) {
            return c.baseClass.name.toLowerCase() + ".constructor_default";
        }

        StringBuilder name = new StringBuilder(c.baseClass.name.toLowerCase() + ".constructor_");
        for (int i = 0; i < c.params.size(); i++) {
            name.append(c.params.get(i).t.name).append("_");
        }
        name.deleteCharAt(name.length() - 1);
        return name.toString();
    }

    private String generateConstructorName(String className, List<Expression> args) {
        if (args.isEmpty()) {
            return className.toLowerCase() + ".constructor_default";
        }

        StringBuilder name = new StringBuilder(className.toLowerCase() + ".constructor_");
        for (int i = 0; i < args.size(); i++) {
            name.append(args.get(i).type.type).append("_");
        }
        name.deleteCharAt(name.length() - 1);
        return name.toString();
    }

    private String generateMethodFunctionName(MethodDeclaration m) {
        String classNameLower = m.baseClass.name.toLowerCase();

        if (m.params.isEmpty()) {
            return classNameLower + "." + m.name;
        }

        StringBuilder name = new StringBuilder(classNameLower + "." + m.name + "_");
        for (int i = 0; i < m.params.size(); i++) {
            name.append(m.params.get(i).t.name).append("_");
        }
        name.deleteCharAt(name.length() - 1);
        return name.toString();
    }

    private void generateUserMethodsTableInitialization() {
        builder.append("\n;; == User method table initialization ==\n");
        builder.append("    (elem (table $user_methods) (i32.const 0)\n");

        for (int i = 0; i < nextTableIndex; i++) {
            builder.append("        $").append(indexToFunction.get(i)).append("\n");
        }

        builder.append("    )\n");
    }

    private void generateVTable(ClassDeclaration c) {
        typesID.put(c.name, typesID.size());

        // Store VTable address for this class
        pointerToVTable.put(c.name, VTablePointer);

        builder.append(";;; VTable for ").append(c.name).append("\n");

        // 1. Type ID (at offset 0)
        builder.append("  (data (i32.const ").append(VTablePointer).append(") ").append('"')
                .append(intToFormattedString(typesID.get(c.name))).append('"').append(")  ;; Type ID\n");
        VTablePointer += 4;

        // 2. Method count (including constructor)
        int methodCount = c.methodsList.size();
        builder.append("  (data (i32.const ").append(VTablePointer).append(") ").append('"')
                .append(intToFormattedString(methodCount)).append('"').append(")  ;; Method count\n");
        VTablePointer += 4;

        // 3. Function indices
        for (MemberDeclaration m : c.methodsList) {
            String funcName;
            String memberType;

            if (m instanceof MethodDeclaration) {
                MethodDeclaration method = (MethodDeclaration) m;
                funcName = generateMethodFunctionName(method);
                memberType = "Method " + method.name;
            } else if (m instanceof ConstructorDeclaration) {
                ConstructorDeclaration cons = (ConstructorDeclaration) m;
                funcName = generateConstructorFunctionName(cons);
                memberType = "Constructor";
            } else {
                continue;
            }

            int methodIndex = functionToIndex.get(funcName);
            builder.append("  (data (i32.const ").append(VTablePointer).append(") ").append('"')
                    .append(intToFormattedString(methodIndex)).append('"').append(")  ;; ").append(memberType)
                    .append(" index\n");
            VTablePointer += 4;
        }

        // 4. Global for VTable pointer
        builder.append("  (global $").append(c.name.toUpperCase()).append("_VTABLE i32 (i32.const ")
                .append(pointerToVTable.get(c.name)).append("))\n\n");
    }

    private void generateConstructorHelper(ClassDeclaration c) {
        int totalFields = c.fieldsList.size();

        builder.append(String.format("""
                ;;; Creates %s object
                (func $create_%s (result i32)
                    (local $obj i32)
                    (local $field_ptr i32)
                    (local $i i32)

                    ;;;; Allocate memory: header (4 bytes) + fields (%d fields * 4 bytes)
                    (local.set $obj (call $allocate (i32.const %d)))

                    ;;;; [0-4] VTable pointer
                    (i32.store (local.get $obj) (global.get $%s_VTABLE))
                """, c.name, c.name.toLowerCase(), totalFields, 4 + totalFields * 4,
                c.name.toUpperCase()));

        // Only add initialization loop if there are fields
        if (totalFields > 0) {
            builder.append(String.format("""

                        ;;;; Initialize fields to default values
                        (local.set $field_ptr (i32.add (local.get $obj) (i32.const 4)))
                        (local.set $i (i32.const 0))
                        (loop $init_loop
                            (i32.store (local.get $field_ptr) (i32.const 0))  ;; null pointer
                            (local.set $field_ptr (i32.add (local.get $field_ptr) (i32.const 4)))
                            (local.set $i (i32.add (local.get $i) (i32.const 1)))
                            (br_if $init_loop (i32.lt_u (local.get $i) (i32.const %d)))
                        )
                    """, totalFields));
        }

        builder.append("""

                    (local.get $obj)
                )
                """);
    }

    private void generateFieldsAccessors(ClassDeclaration c) {
        for (int i = 0; i < c.fieldsList.size(); i++) {
            builder.append(String.format("""
                    ;;; Get field %s.%s
                    (func $%s_get_%s (param $obj i32) (result i32)
                        (i32.load offset=%d (local.get $obj))
                    )

                    ;;; Set field %s.%s
                    (func $%s_set_%s (param $obj i32) (param $value i32) (result i32)
                        (i32.store offset=%d (local.get $obj) (local.get $value))
                        (local.get $obj)
                    )
                    """, c.name, c.fieldsList.get(i).name, c.name.toLowerCase(), c.fieldsList.get(i).name, 4 + i * 4,
                    c.name, c.fieldsList.get(i).name, c.name.toLowerCase(), c.fieldsList.get(i).name, 4 + i * 4));
        }
    }

    private void translateClasses() {
        // Generate constructors
        for (ClassDeclaration c : program.classes) {
            currentClass = c;

            for (int i = 0; i < c.constructorDeclarations.size(); i++) {
                currentConstructor = c.constructorDeclarations.get(i);
                generateConstructor(c.constructorDeclarations.get(i));
                currentConstructor = null;
            }

            if (c.constructorDeclarations.isEmpty()) {
                generateDefaultConstructor(c);
            }

            for (int i = 0; i < c.methodDeclarations.size(); i++) {
                currentMethod = c.methodDeclarations.get(i);
                generateMethod(c.methodDeclarations.get(i), i);
                currentMethod = null;
            }
        }
        currentClass = null;
    }

    private void generateDefaultConstructor(ClassDeclaration c) {
        // Constructor function
        builder.append(String.format("""
                ;;; Default constructor for %s
                (func $%s.constructor_default (param $this i32) (result i32)
                %s ;;; Call super constructor if needed
                %s ;;; Initialize fields
                (local.get $this)
                )

                """, c.name, c.name.toLowerCase(),
                generateSuperConstructorCall(c),
                generateFieldInitialization(c)));
    }

    private String generateSuperConstructorCall(ClassDeclaration c) {
        // If no superclass, return empty string
        if (c.superClass == null) {
            return "";
        }

        // Call superclass's default constructor
        String superClassName = c.superClass.name.toLowerCase();
        return String.format(" ;;; Call superclass constructor\n (call $%s.constructor_default (local.get $this))\n",
                superClassName);
    }

    private String generateFieldInitialization(ClassDeclaration c) {
        StringBuilder b = new StringBuilder();

        // Calculate start of current class fields in memory
        int fieldOffset = 4 + (c.fieldsList.size() - c.fieldDeclarations.size()) * 4;

        b.append(" ;;; Initialize fields\n");

        for (FieldDeclaration f : c.fieldDeclarations) {
            String initValue = f.init.accept(this);

            if (initValue.isEmpty()) {
                initValue = "(i32.const 0)";
            }

            b.append(" (i32.store offset=").append(fieldOffset).append(" (local.get $this) ").append(initValue)
                    .append(")\n");
            fieldOffset += 4;
        }

        return b.toString();
    }

    private void generateConstructor(ConstructorDeclaration c) {
        String constructorName = generateConstructorFunctionName(c);

        // Generate constructor function (this will create $funcName)
        // Function signature
        builder.append(";;; Constructor for ").append(c.baseClass.name);
        if (!c.params.isEmpty()) {
            builder.append(" with ").append(c.params.size()).append("parameter(s)");
        }
        builder.append("\n");

        builder.append("  (func $").append(constructorName).append(" (param $this i32)");

        // Add parameters
        for (int i = 0; i < c.params.size(); i++) {
            builder.append(" (param $").append(c.params.get(i).name).append(" i32)");
        }
        builder.append(" (result i32)\n");

        currentParameters.clear();
        for (Param param : c.params) {
            currentParameters.add(param.name);
        }

        // Set locals from body
        HashSet<String> locals = getLocals(c.body);

        for (String s : locals) {
            builder.append("    (local $").append(s).append(" i32)\n");
        }

        builder.append("    (local $temp_obj i32)\n");
        builder.append("    (local $temp_target i32)\n");
        for (int i = 0; i < 5; i++) { // Reserve some temps
            builder.append("    (local $temp_").append(i).append(" i32)\n");
        }

        // Generate field initializations
        builder.append(generateFieldInitialization(c.baseClass));

        // Generate constructor body
        builder.append(generateFunctionBody(c.body));

        // Return this
        builder.append(" (local.get $this)\n");
        builder.append(")\n\n");
        currentParameters.clear();
        resetTempVars();
    }

    private HashSet<String> getLocals(List<Statement> body) {
        HashSet<String> locals = new HashSet<>();
        for (Statement s : body) {
            if (s instanceof VariableDeclaration) {
                locals.add(((VariableDeclaration) s).name);
            } else if (s instanceof WhileStatement) {
                locals.addAll(getLocals(((WhileStatement) s).body));
            } else if (s instanceof IfStatement) {
                IfStatement i = (IfStatement) s;
                locals.addAll(getLocals(i.thenBody.body));
                if (i.elseBody != null) {
                    locals.addAll(getLocals(i.elseBody.body));
                }
            }
        }
        return locals;
    }

    private void generateMethod(MethodDeclaration method, int index) {
        String methodName = generateMethodFunctionName(method);

        // Method signature
        builder.append(";;; Method ").append(method.baseClass.name)
                .append(".").append(method.name).append("\n");

        // Check if method has return type
        boolean hasReturnValue = !method.returnType.name.equals("null");

        // Function declaration
        builder.append("  (func $").append(methodName);

        // Type
        builder.append(" (type ").append(getMethodSignatureType(method.params.size(), true)).append(")\n");

        // Add parameters
        builder.append(" (param $this i32)");
        for (int i = 0; i < method.params.size(); i++) {
            builder.append(" (param $").append(method.params.get(i).name).append(" i32)");
        }

        // Return type
        builder.append(" (result i32)\n");

        // Set locals from method body
        HashSet<String> locals = getLocals(method.body);
        for (String localName : locals) {
            builder.append("    (local $").append(localName).append(" i32)\n");
        }

        builder.append("    (local $temp_obj i32)\n");
        builder.append("    (local $temp_target i32)\n");
        for (int i = 0; i < 5; i++) { // Reserve some temps
            builder.append("    (local $temp_").append(i).append(" i32)\n");
        }

        // Set up parameter context
        currentParameters.clear();
        for (Param param : method.params) {
            currentParameters.add(param.name);
        }
        currentLocalVars.clear();

        // Method body generation
        builder.append(generateFunctionBody(method.body));

        if (!hasReturnValue) {
            builder.append("  (local.get $this)");
        }
        builder.append("  )\n\n");

        // Clear context
        currentParameters.clear();
        resetTempVars();
    }

    private String generateFunctionBody(List<Statement> body) {
        if (body == null || body.isEmpty()) {
            return "    ;;; Empty body\n";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("    ;;; Function body start\n");

        // Generate body statements
        for (Statement stmt : body) {
            String stmtCode = stmt.accept(this);
            sb.append("    ").append(stmtCode).append("\n");
        }

        sb.append("    ;;; Function body end\n");
        return sb.toString();
    }

    private String generateMethodWasmName(VariableType targetType, String methodName, List<Expression> args) {
        String className = getClassNameFromType(targetType);

        // For built-in types, we need exact matching with WASM base
        if (args.isEmpty()) {
            return className + "." + methodName;
        }

        StringBuilder name = new StringBuilder(className + "." + methodName + "_");
        for (Expression arg : args) {
            String typeName = getTypeName(arg.type);
            name.append(typeName).append("_");
        }
        name.deleteCharAt(name.length() - 1);
        return name.toString();
    }

    private boolean isStaticBuiltinField(String typeName, String memberName) {
        if (!isBuiltinTypeName(typeName)) {
            return false;
        }

        if (typeName.equals("Integer")) {
            return memberName.equals("Min") || memberName.equals("Max");
        } else if (typeName.equals("Real")) {
            return memberName.equals("Min") || memberName.equals("Max") || memberName.equals("Epsilon");
        }

        return false;
    }

    private String getMethodSignatureType(int paramCount, boolean hasReturn) {
        if (hasReturn) {
            switch (paramCount) {
                case 0:
                    return "$method_sig_0";
                case 1:
                    return "$method_sig_1";
                case 2:
                    return "$method_sig_2";
                case 3:
                    return "$method_sig_3";
                default:
                    throw new RuntimeException("Too many parameters: " + paramCount);
            }
        } else {
            switch (paramCount) {
                case 0:
                    return "$void_method_sig_0";
                case 1:
                    return "$void_method_sig_1";
                case 2:
                    return "$void_method_sig_2";
                default:
                    throw new RuntimeException("Too many parameters: " + paramCount);
            }
        }
    }

    // Helper: Get class name from VariableType
    private String getClassNameFromType(VariableType type) {
        if (type == null) {
            throw new RuntimeException("Null type encountered");
        }

        return type.type;
    }

    // Helper: Check if type name is built-in
    private boolean isBuiltinTypeName(String typeName) {
        typeName = typeName.toLowerCase();
        return typeName.equals("integer") || typeName.equals("real") ||
                typeName.equals("boolean") || typeName.equals("array") ||
                typeName.equals("list") || typeName.equals("class") ||
                typeName.equals("anyvalue") || typeName.equals("anyref") || typeName.equals("void");
    }

    // Helper: Check if type is built-in
    private boolean isBuiltinType(VariableType type) {
        return isBuiltinTypeName(getClassNameFromType(type));
    }

    // Helper: Get simple type name for method signature generation
    private String getTypeName(VariableType type) {
        if (type == null)
            return "null";
        String name = getClassNameFromType(type);

        // Map to canonical names
        if (name.equals("AnyUsersClass"))
            return "Class";
        return name;
    }

    // Helper: Find class declaration from type
    private ClassDeclaration findClassFromType(VariableType type) {
        String className = getClassNameFromType(type);

        // Check if it's a built-in type
        if (isBuiltinTypeName(className)) {
            return null; // Built-in types don't have ClassDeclaration
        }

        // Find user class
        for (ClassDeclaration c : program.classes) {
            if (c.name.equals(className)) {
                return c;
            }
        }

        throw new RuntimeException("Class not found: " + className);
    }

    // Helper: Calculate field offset
    private int calculateFieldOffset(VariableType type, String fieldName) {
        ClassDeclaration classDecl = findClassFromType(type);
        if (classDecl == null) {
            throw new RuntimeException("Cannot calculate field offset for built-in type: " + type.type);
        }

        // Find field in fieldsList
        for (int i = 0; i < classDecl.fieldsList.size(); i++) {
            FieldDeclaration field = classDecl.fieldsList.get(i);
            if (field.name.equals(fieldName)) {
                // Offset calculation: 4 (header) + i * 4
                return 4 + i * 4;
            }
        }

        throw new RuntimeException("Field not found: " + fieldName + " in class " + classDecl.name);
    }

    private boolean isTypeNameReference(Expression expr) {
        if (expr instanceof VariableReference) {
            String name = ((VariableReference) expr).name;
            return isBuiltinTypeName(name) ||
                    program.classes.stream().anyMatch(c -> c.name.equals(name));
        }
        return false;
    }

    // Helper: Check if member is a field access
    private boolean isFieldAccess(VariableType type, String memberName) {
        ClassDeclaration classDecl = findClassFromType(type);
        if (classDecl == null) {
            // Built-in type - check if it has this field
            return type.fields.containsKey(memberName);
        }

        // User class - check fieldsList
        for (FieldDeclaration field : classDecl.fieldsList) {
            if (field.name.equals(memberName)) {
                return true;
            }
        }
        return false;
    }

    private String getMethodNameFromMemberAccess(Expression target) {
        if (target instanceof MemberAccess) {
            MemberAccess ma = (MemberAccess) target;
            if (ma.member instanceof VariableReference) {
                return ((VariableReference) ma.member).name;
            }
        }
        // Handle direct method calls like "c.get()" where c is VariableReference
        else if (target instanceof VariableReference) {
            // This actually shouldn't happen in your AST structure
            // Method calls should be MethodCall nodes with targets that are MemberAccess
            // But let's handle it just in case
            return ((VariableReference) target).name;
        } else if (target instanceof MethodCall) {
            // If we have nested method calls like a.b().c()
            // This is complex - for now throw error
            throw new RuntimeException("Nested method calls not yet supported");
        }
        throw new RuntimeException("Cannot extract method name from expression: " +
                target.getClass().getSimpleName());
    }

    // Helper: Generate block code
    private String generateBlockCode(List<Statement> statements) {
        StringBuilder sb = new StringBuilder();
        Set<String> previousLocals = new HashSet<>(currentLocalVars);

        for (Statement stmt : statements) {
            sb.append(stmt.accept(this)).append("\n");
        }

        // Restore local variables (for nested scopes)
        currentLocalVars.retainAll(previousLocals);

        return sb.toString();
    }

    // Helper: Get parameter types from arguments
    private List<VariableType> getParamTypesFromArgs(List<Expression> args) {
        List<VariableType> types = new ArrayList<>();
        for (Expression arg : args) {
            types.add(arg.type);
        }
        return types;
    }

    // Helper: Match method signature
    private boolean matchesSignature(MemberDeclaration member, String methodName,
            List<VariableType> paramTypes) {
        if (member instanceof MethodDeclaration) {
            MethodDeclaration method = (MethodDeclaration) member;

            // Check name
            if (!method.name.equals(methodName)) {
                return false;
            }

            // Check parameter count
            if (method.params.size() != paramTypes.size()) {
                return false;
            }

            // Check parameter types
            for (int i = 0; i < paramTypes.size(); i++) {
                String paramType = method.params.get(i).t.name;
                String argType = getTypeName(paramTypes.get(i));

                if (ProgramTypes.canCast(method.params.get(i).type, paramTypes.get(i))) {
                    continue;
                }

                // Allow AnyValue/AnyRef compatibility
                if (!paramType.equals(argType) &&
                        !(paramType.equals("AnyValue") && isValueType(argType)) &&
                        !(paramType.equals("AnyRef") && isReferenceType(argType))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    // Helper: Check if type is a value type
    private boolean isValueType(String typeName) {
        return typeName.equals("Integer") || typeName.equals("Real") ||
                typeName.equals("Boolean");
    }

    private String getMethodCallTargetCode(Expression target) {
        if (target instanceof MemberAccess) {
            MemberAccess ma = (MemberAccess) target;
            // For built-in types, we need the object itself, not field access
            return ma.target.accept(this);
        } else {
            return target.accept(this);
        }
    }

    // Helper: Check if type is a reference type
    private boolean isReferenceType(String typeName) {
        return typeName.equals("Array") || typeName.equals("List") ||
                program.classes.stream().anyMatch(c -> c.name.equals(typeName));
    }

    // Helper: Calculate method VTable index
    private int calculateMethodIndex(Expression target, String methodName,
            List<Expression> args) {
        VariableType targetType = target.type;
        if (target instanceof MemberAccess) {
            targetType = ((MemberAccess) target).target.type;
        }
        ClassDeclaration targetClass = findClassFromType(targetType);

        if (targetClass == null) {
            throw new RuntimeException("Cannot calculate method index for built-in type");
        }

        List<VariableType> paramTypes = getParamTypesFromArgs(args);

        // Search through methodsList
        for (int i = 0; i < targetClass.methodsList.size(); i++) {
            MemberDeclaration member = targetClass.methodsList.get(i);
            if (matchesSignature(member, methodName, paramTypes)) {
                return i; // VTable index
            }
        }

        throw new RuntimeException("Method not found: " + methodName +
                " in class " + targetClass.name);
    }

    private List<String> tempVars = new ArrayList<>();

    private void resetTempVars() {
        tempVars.clear();
    }

    @Override
    public String visit(BooleanLiteral n) {
        int value = n.value ? 1 : 0;
        return "(call $create_boolean (i32.const " + value + "))";
    }

    @Override
    public String visit(IntegerLiteral n) {
        // Return raw i32 for integer constructors
        return "(i32.const " + n.value + ")";
    }

    @Override
    public String visit(RealLiteral n) {
        // Format double properly
        String valueStr = String.format(Locale.US, "%.16e", n.value);
        return "(f64.const " + valueStr + ")";
    }

    @Override
    public String visit(ThisExpression n) {
        return "(local.get $this)";
    }

    // Then update visit(VariableReference) to handle static fields:
    @Override
    public String visit(VariableReference n) {
        String varName = n.name;

        // Check if it's a parameter (in current scope)
        if (currentParameters.contains(varName)) {
            return "(local.get $" + varName + ")";
        }

        // Check if it's a local variable
        if (currentLocalVars.contains(varName)) {
            return "(local.get $" + varName + ")";
        }

        // Check if it's a field of the current class
        if (currentClass != null) {
            String className = currentClass.name.toLowerCase();
            return "(call $" + className + "_get_" + varName + " (local.get $this))";
        }

        // Check if it's a static field of built-in type
        if (n.type != null && isStaticBuiltinField(n.type.type, varName)) {
            String className = getClassNameFromType(n.type);
            return "(call $" + className + "." + varName + ")";
        }

        throw new RuntimeException("Unknown variable: " + varName);
    }

    @Override
    public String visit(MemberAccess n) {
        // Check if this is a static field access (e.g., Integer.Min)
        if (isTypeNameReference(n.target) && n.member instanceof VariableReference) {
            String typeName = ((VariableReference) n.target).name;
            String memberName = ((VariableReference) n.member).name;

            if (isStaticBuiltinField(typeName, memberName)) {
                return "(call $" + typeName + "." + memberName + ")";
            }
        }

        // Original code for instance field/method access
        String targetCode = n.target.accept(this);

        if (n.member instanceof VariableReference) {
            String memberName = ((VariableReference) n.member).name;

            if (isFieldAccess(n.target.type, memberName)) {
                if (isBuiltinType(n.target.type)) {
                    // Built-in type instance field (if any exist)
                    String className = getClassNameFromType(n.target.type);
                    throw new RuntimeException(
                            "Built-in type instance field not implemented: " + className + "." + memberName);
                } else {
                    int fieldOffset = calculateFieldOffset(n.target.type, memberName);
                    return "(i32.load offset=" + fieldOffset + " " + targetCode + ")";
                }
            } else {
                return targetCode; // Method reference
            }
        }

        throw new RuntimeException("Unsupported member: " + n.member.getClass().getSimpleName());
    }

    @Override
    public String visit(VariableDeclaration n) {
        StringBuilder sb = new StringBuilder();

        // Add to local variables
        currentLocalVars.add(n.name);

        // Generate initialization code if present
        if (n.init != null) {
            String initCode = n.init.accept(this);
            sb.append("(local.set $").append(n.name).append(" ").append(initCode).append(")\n");
        }
        return sb.toString();
    }

    @Override
    public String visit(ReturnStatement n) {
        if (currentConstructor != null) {
            // Constructor - return this
            return "(local.get $this)";
        } else if (currentMethod != null) {
            if (currentMethod.returnType.name.equals("null")) {
                // Void method - just end execution
                return "(return)";
            } else if (n.value != null) {
                // Return with value
                String valueCode = n.value.accept(this);
                return "(return " + valueCode + ")";
            } else {
                throw new RuntimeException("Method must return a value");
            }
        }
        throw new RuntimeException("Return outside method/constructor");
    }

    @Override
    public String visit(ConstructorCall n) {
        StringBuilder sb = new StringBuilder();

        // Evaluate all arguments
        List<String> argCodes = new ArrayList<>();
        for (Expression arg : n.args) {
            argCodes.add(arg.accept(this));
        }

        String className = n.className.toLowerCase();

        // Handle built-in types
        if (className.equals("integer")) {
            if (n.args.size() != 1) {
                throw new RuntimeException("Integer constructor expects 1 argument");
            }
            return "(call $create_integer " + argCodes.get(0) + ")";
        } else if (className.equals("real")) {
            if (n.args.size() != 1) {
                throw new RuntimeException("Real constructor expects 1 argument");
            }
            return "(call $create_real " + argCodes.get(0) + ")";
        } else if (className.equals("boolean")) {
            if (n.args.size() != 1) {
                throw new RuntimeException("Boolean constructor expects 1 argument");
            }
            return "(call $create_boolean " + argCodes.get(0) + ")";
        } else if (className.equals("array")) {
            if (n.args.size() != 1) {
                throw new RuntimeException("Array constructor expects 1 argument (length)");
            }
            return "(call $create_array " + argCodes.get(0) + ")";
        } else if (className.equals("list")) {
            // List has multiple constructors
            if (n.args.size() == 0) {
                return "(call $List.constructor0 (call $create_list))";
            } else if (n.args.size() == 1) {
                // List with single element
                return "(call $List.constructor1 (call $create_list) " + argCodes.get(0) + ")";
            } else if (n.args.size() == 2) {
                // List with element and count
                return "(call $List.constructor2 (call $create_list) " +
                        argCodes.get(0) + " " + argCodes.get(1) + ")";
            } else {
                throw new RuntimeException("List constructor with " + n.args.size() +
                        " arguments not supported");
            }
        } else {
            // User-defined class constructor
            // Generate constructor name
            String constructorName = generateConstructorName(n.className, n.args);

            // Create object first - FIX THIS LINE
            sb.append("(local.set $temp_obj (call $create_").append(className).append("))\n");

            // Call constructor
            sb.append("(call $").append(constructorName).append(" (local.get $temp_obj)");
            for (String argCode : argCodes) {
                sb.append(" ").append(argCode);
            }
            sb.append(")\n");

            // Return object
            sb.append("(local.get $temp_obj)");

            sb.append("  (drop)\n");
            return sb.toString();
        }
    }

    @Override
    public String visit(AssignmentStatement n) {
        StringBuilder sb = new StringBuilder();

        // Evaluate the value first
        String valueCode = n.value.accept(this);

        if (n.target instanceof VariableReference) {
            String varName = ((VariableReference) n.target).name;

            // Check if it's a local variable or parameter
            if (currentLocalVars.contains(varName) || currentParameters.contains(varName)) {
                sb.append("(local.set $").append(varName).append(" ").append(valueCode).append(")");
            } else if (currentClass != null && isFieldAccess(currentClass.type, varName)) {
                // Field assignment
                String className = currentClass.name.toLowerCase();
                sb.append("(call $").append(className).append("_set_").append(varName)
                        .append(" (local.get $this) ").append(valueCode).append(")");
                sb.append("(drop)");
            } else {
                throw new RuntimeException("Cannot assign to variable: " + varName);
            }
        } else if (n.target instanceof MemberAccess) {
            // Field assignment through member access
            MemberAccess ma = (MemberAccess) n.target;
            String targetCode = ma.target.accept(this);

            if (ma.member instanceof VariableReference) {
                String fieldName = ((VariableReference) ma.member).name;

                if (!isFieldAccess(ma.target.type, fieldName)) {
                    throw new RuntimeException("Cannot assign to non-field: " + fieldName);
                }

                // For built-in types, we can't assign to fields (they're immutable)
                if (isBuiltinType(ma.target.type)) {
                    throw new RuntimeException("Cannot assign to field of built-in type: " +
                            ma.target.type.type);
                }

                // Calculate field offset
                int fieldOffset = calculateFieldOffset(ma.target.type, fieldName);
                sb.append("(i32.store offset=").append(fieldOffset)
                        .append(" ").append(targetCode).append(" ").append(valueCode).append(")");
            } else {
                throw new RuntimeException("Unsupported assignment target");
            }
        } else {
            throw new RuntimeException("Unsupported assignment target type: " +
                    n.target.getClass().getSimpleName());
        }
        return sb.toString();
    }

    @Override
    public String visit(MethodCall n) {
        StringBuilder sb = new StringBuilder();

        // 1. Get target object code
        String targetCode;
        if (n.target instanceof MemberAccess) {
            // For method calls like a.b.c()
            targetCode = getMethodCallTargetCode(n.target);
        } else {
            // For direct method calls
            targetCode = n.target.accept(this);
        }

        // 2. Evaluate all arguments
        List<String> argCodes = new ArrayList<>();
        for (Expression arg : n.args) {
            argCodes.add(arg.accept(this));
        }

        // 3. Get method name from target
        String methodName = getMethodNameFromMemberAccess(n.target);

        // 4. Check target type
        VariableType targetType = n.target.type;
        if (n.target instanceof MemberAccess) {
            targetType = ((MemberAccess) n.target).target.type;
        }

        if (isBuiltinType(targetType)) {
            // Built-in type method call
            String wasmMethodName = generateMethodWasmName(targetType, methodName, n.args);

            // Direct call for built-in types
            sb.append("(call $").append(wasmMethodName).append(" ")
                    .append(targetCode);
            for (String argCode : argCodes) {
                sb.append(" ").append(argCode);
            }
            sb.append(")");

            return sb.toString();
        } else {
            // User type - virtual dispatch

            // 4.1 Calculate method index
            int methodIndex = calculateMethodIndex(n.target, methodName, n.args);

            // 4.2 Get VTable pointer from object (offset 0)
            sb.append("(local.set $temp_target ").append(targetCode).append(")\n");

            // 4.3 Determine the correct signature type

            // Or we need to find the actual method declaration
            String signatureType = getMethodSignatureType(n.args.size(), true);

            // 4.4 Call via indirect dispatch with CORRECT signature
            sb.append("(call_indirect $user_methods (type ").append(signatureType).append(") ")
                    .append("(local.get $temp_target)");

            // Add arguments
            for (String argCode : argCodes) {
                sb.append(" ").append(argCode);
            }

            // Add function index (from VTable lookup)
            sb.append(" (call $get_method (local.get $temp_target) (i32.const ")
                    .append(methodIndex).append(")))");
            return sb.toString();
        }
    }

    @Override
    public String visit(SuperConstructorCall n) {
        if (currentClass == null || currentClass.superClass == null) {
            throw new RuntimeException("Super constructor call outside class or no superclass");
        }

        StringBuilder sb = new StringBuilder();
        String superClassName = currentClass.superClass.name.toLowerCase();

        // Evaluate arguments
        List<String> argCodes = new ArrayList<>();
        for (Expression arg : n.args) {
            argCodes.add(arg.accept(this));
        }

        // Generate constructor name
        String constructorName = superClassName + ".constructor";
        if (!n.args.isEmpty()) {
            constructorName += "_";
            for (Expression arg : n.args) {
                String typeName = getTypeName(arg.type);
                constructorName += typeName.toLowerCase() + "_";
            }
            constructorName = constructorName.substring(0, constructorName.length() - 1);
        } else {
            constructorName += "_default";
        }

        // Call super constructor
        sb.append("(call $").append(constructorName).append(" (local.get $this)");
        for (String argCode : argCodes) {
            sb.append(" ").append(argCode);
        }
        sb.append(")");

        return sb.toString();
    }

    @Override
    public String visit(IfStatement n) {
        StringBuilder sb = new StringBuilder();

        // Evaluate condition
        String conditionCode = n.cond.accept(this);

        // Extract boolean value from Boolean object
        sb.append("(if (i32.eqz (call $get_boolean_value ").append(conditionCode).append("))\n");
        sb.append("    (then ");
        String thenCode = n.thenBody.accept(this);
        sb.append(thenCode).append("    )\n");

        // Has else - branch to else block
        if (n.elseBody != null) {
            sb.append("    (else  ");
            String elseCode = n.elseBody.accept(this);
            sb.append(elseCode).append(")\n");
        }
        sb.append(")\n");

        return sb.toString();
    }

    @Override
    public String visit(WhileStatement n) {
        StringBuilder sb = new StringBuilder();

        String startLabel = newLabel("while_start");
        String endLabel = newLabel("while_end");

        sb.append("(block $").append(endLabel).append("\n");
        sb.append("(loop $").append(startLabel).append("\n");

        // Evaluate condition
        String conditionCode = n.cond.accept(this);

        // Check condition and break if false
        sb.append("(br_if $").append(endLabel)
                .append(" (i32.eqz (call $get_boolean_value ").append(conditionCode).append(")))\n");

        // Generate body
        sb.append(generateBlockCode(n.body));

        // Continue loop
        sb.append("(br $").append(startLabel).append(")\n");

        sb.append(")\n"); // end loop
        sb.append(")\n"); // end block

        return sb.toString();
    }

    @Override
    public String visit(ThenStatement n) {
        return generateBlockCode(n.body);
    }

    @Override
    public String visit(ElseStatement n) {
        return generateBlockCode(n.body);
    }

    @Override
    public String visit(Program n) {
        // Program visitor is handled in translate() method
        throw new UnsupportedOperationException("Program should be processed via translate()");
    }

    @Override
    public String visit(ClassDeclaration n) {
        // Class declaration is handled in prepareClasses() and translateClasses()
        throw new UnsupportedOperationException("ClassDeclaration should be processed in class-level generation");
    }

    @Override
    public String visit(FieldDeclaration n) {
        // Field declarations are handled in prepareClasses()
        throw new UnsupportedOperationException("FieldDeclaration should be processed in class-level generation");
    }

    @Override
    public String visit(MethodDeclaration n) {
        // Method declarations are handled in translateClasses()
        throw new UnsupportedOperationException("MethodDeclaration should be processed in method generation");
    }

    @Override
    public String visit(ConstructorDeclaration n) {
        // Constructor declarations are handled in translateClasses()
        throw new UnsupportedOperationException("ConstructorDeclaration should be processed in constructor generation");
    }

    @Override
    public String visit(Param n) {
        // Parameters are handled in function signatures
        throw new UnsupportedOperationException("Param should be processed in function signature generation");
    }

    @Override
    public String visit(StringLiteral stringLiteral) {
        // Not supported in current language
        throw new UnsupportedOperationException("String literals not supported");
    }

    @Override
    public String visit(ExtensionType extensionType) {
        // Handled in class inheritance
        throw new UnsupportedOperationException("ExtensionType should be processed in class generation");
    }

    @Override
    public String visit(ReturnType returnType) {
        // Handled in method signatures
        throw new UnsupportedOperationException("ReturnType should be processed in method generation");
    }

    @Override
    public String visit(ArrayLiteral arrayLiteral) {
        // Array creation with size
        if (arrayLiteral.size >= 0) {
            return "(call $create_array (i32.const " + arrayLiteral.size + "))";
        }
        throw new RuntimeException("Invalid array size");
    }

    @Override
    public String visit(ListLiteral listiteral) {
        // Empty list creation
        return "(call $List.constructor0 (call $create_list))";
    }

    @Override
    public String visit(Type type) {
        // Type nodes are used for declarations, not code generation
        throw new UnsupportedOperationException("Type should be processed in semantic analysis");
    }

    @Override
    public String visit(ExpressionStatement expressionStatement) {
        // Evaluate expression and drop result
        String exprCode = expressionStatement.value.accept(this);
        return exprCode + "\n(drop)";
    }
}