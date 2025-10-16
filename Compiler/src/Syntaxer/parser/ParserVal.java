package Syntaxer.parser;

import Syntaxer.ast.*;
import Syntaxer.ast.component.*;
import Syntaxer.ast.declaration.*;
import Syntaxer.ast.expression.*;
import Syntaxer.ast.statement.*;
import java.util.*;

public class ParserVal {
    // AST Nodes
    Program program;
    ClassDeclaration classDeclaration;
    MemberDeclaration memberDeclaration;
    FieldDeclaration fieldDeclaration;
    MethodDeclaration methodDeclaration;
    ConstructorDeclaration constructorDeclaration;
    Param param;
    Type type;
    ElseStatement elseClause;
    ThenStatement thenClause;
    Statement statement;
    Expression expression;
    ExtensionType extensionType;
    ReturnType returnType;
    VariableDeclaration variableDeclaration;
    ArrayList<ClassDeclaration> classDeclarationList;
    ArrayList<MemberDeclaration> memberDeclarationList;
    ArrayList<Param> paramList;
    ArrayList<Statement> statementList;
    ArrayList<Expression> expressionList;
    
    // Primitive types
    public String strVal;
    public int intVal;
    public double realVal;
    public boolean boolVal;

    // Default constructor
    public ParserVal() {}
    
    // Constructors for primitive types
    public ParserVal(String s) { strVal = s; }
    public ParserVal(int i) { intVal = i; }
    public ParserVal(double d) { realVal = d; }
    public ParserVal(boolean b) { boolVal = b; }
}