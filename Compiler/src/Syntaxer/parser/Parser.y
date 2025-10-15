%{
package syntaxer;

import java.io.*;
import java.util.*;
import ast.*;
%}

%union {    
    // AST Nodes
    Program program;
    ClassDecl classDecl;
    MemberDecl memberDecl;
    FieldDecl fieldDecl;
    MethodDecl methodDecl;
    ConstructorDecl constructorDecl;
    Parameter parameter;
    Statement statement;
    Expression expression;
    List<ClassDecl> classDeclList;
    List<MemberDecl> memberDeclList;
    List<Parameter> parameterList;
    List<Statement> statementList;
    List<Expression> expressionList;
}

// identifiers & numbers
%token IDENTIFIER
%token NUMBER

// keywords
%token VAR CLASS IS END RETURN THIS METHOD
%token EXTENDS WHILE LOOP IF ELSE THEN

// delimeters
%token COLUMN   // :
%token DOT      // .
%token COMMA    // ,
%token LPAREN   // (
%token RPAREN   // )
%token LBRACKET // [
%token RBRACKET // ]

// operator signs
%token SHORTBODY    // =>
%token ASSIGN       // :=

// Types for non-terminals
%type <program> CompilationUnit
%type <classDeclList> ClassDeclarations
%type <classDecl> ClassDeclaration
%type <memberDeclList> ClassBody
%type <memberDeclList> ClassMembers
%type <memberDecl> ClassMember
%type <fieldDecl> FieldDeclaration
%type <methodDecl> MethodDeclaration
%type <constructorDecl> ConstructorDeclaration
%type <parameterList> Parameters
%type <parameterList> ParameterList
%type <parameter> Parameter
%type <type> ReturnType
%type <statementList> Statements
%type <statement> Statement
%type <statement> Assignment
%type <statement> IfStatement
%type <statement> WhileStatement
%type <statement> ReturnStatement
%type <expression> Expression
%type <expression> Primary
%type <expression> CompoundName
%type <expression> ConstructorInvocation
%type <expression> CallStatement
%type <expressionList> ArgumentList

%start CompilationUnit


%%


CompilationUnit
    : ClassDeclarations {
        $$ = new Program($1);
        // Set the result for the parser
        parserResult = $$;
      }
    ;

ClassDeclarations
    :                                    {$$ = new ArrayList<>();}
    | ClassDeclaration ClassDeclarations {
        $$ = new ArrayList<>();
        $$.add($1);
        $$.addAll($2);
      }
    ;

ClassDeclaration
    : CLASS IDENTIFIER Extension IS ClassBody END {$$ = new ClassDecl($2, $3, $5);}
    ;

Extension
    :                    {$$ = null;}
    | EXTENDS IDENTIFIER {$$ = $2;}
    ;

ClassBody
    : ClassMembers {$$ = $1;}
    ;

ClassMembers
    :
    | ClassMember              {$$ = new ArrayList<>();}
    | ClassMember ClassMembers {
        $$ = new ArrayList<>();
        $$.add($1);
        $$.addAll($2);
      }
    ;

ClassMember
    : FieldDeclaration       {$$ = $1;}
    | MethodDeclaration      {$$ = $1;}
    | ConstructorDeclaration {$$ = $1;}
    ;

FieldDeclaration
    : VAR IDENTIFIER COLUMN Expression {$$ = new FieldDecl($2, $4);}
    | VAR IDENTIFIER IS Expression     {$$ = new FieldDecl($2, $4);}
    ;

MethodDeclaration
    : METHOD IDENTIFIER Parameters ReturnType MethodBody {$$ = new MethodDecl($2, $3, $4, $5);}
    ;

ConstructorDeclaration
    : THIS Parameters MethodBody {$$ = new ConstructorDecl($2, $4);}
    ;

Parameters
    : LPAREN               RPAREN {$$ = new ArrayList<>();}
    | LPAREN ParameterList RPAREN {$$ = $2;}
    ;

ParameterList
    :                     Parameter {$$ = new ArrayList<>(); $$.add($1);}
    | ParameterList COMMA Parameter {$$ = $1; $$.add($3);}
    ;

Parameter
    : IDENTIFIER COLUMN IDENTIFIER {$$ = new Parameter($1, $3);}
    ;

ReturnType
    :                   {$$ = new Type("void");}
    | COLUMN IDENTIFIER {$$ = new Type($2);}
    ;

MethodBody
    :
    | IS Statements END   {$$ = $2;}
    | SHORTBODY Statement {
        // Convert single expression to return statement
        List<Statement> body = new ArrayList<>();
        body.add(new ReturnStatement($2));
        $$ = body;
      }
    ;

Statements
    :                      {$$ = new ArrayList<>();}
    | Statement Statements {
        $$ = new ArrayList<>();
        $$.add($1);
        $$.addAll($2);
      }
    ;

Statement
    : FieldDeclaration {$$ = $1;}
    | Assignment       {$$ = $1;}
    | IfStatement      {$$ = $1;}
    | whileStatement   {$$ = $1;}
    | ReturnStatement  {$$ = $1;}
    | callStatement    {$$ = new ExpressionStatement($1);}
    ;

Assignment
    : CompoundName ASSIGN Expression {$$ = new AssignmentStatement($1, $3);}
    ;

CompoundName
    :                  IDENTIFIER {$$ = new VariableReference($1);}
    | CompoundName DOT IDENTIFIER {$$ = new MemberAccess($1, new VariableReference($3));}
    ;

IfStatement
    : IF Expression THEN Statements ElseStatement END {$$ = new IfStatement($2, $4, $5);}
    ;

ElseStatement
    :                     {$$ = new ArrayList<>();}
    | ELSE Statements END {$$ = $2;}
    ;

whileStatement
    : WHILE Expression LOOP Statements END {$$ = new WhileStatement($2, $4);}
    ;

ReturnStatement
    : RETURN            {$$ = new ReturnStatement(null);}
    | RETURN Expression {$$ = new ReturnStatement($2);}
    ;

callStatement
    : CompoundName LPAREN              RPAREN {$$ = new MethodCall($1, new ArrayList<>());}
    | CompoundName LPAREN ArgumentList RPAREN {$$ = new MethodCall($1, $3);}
    ;

ArgumentList
    :                    Expression
    | ArgumentList COMMA Expression
    ;

Expression
    : Primary                   {$$ = $1;}
    | ConstructorInvocation     {$$ = $1;}
    | callStatement             {$$ = $1;}
    | Expression DOT Expression {$$ = new MemberAccess($1, $3);}

Primary
    : THIS                     {$$ = new ThisExpression();}
    | CompoundName             {$$ = $1;}
    | LPAREN Expression RPAREN {$$ = $2;}

ConstructorInvocation
    : IDENTIFIER LPAREN              RPAREN {$$ = new ConstructorCall($1, new ArrayList<>());}
    | IDENTIFIER LPAREN ArgumentList RPAREN {$$ = new ConstructorCall($1, $3);}


%%


// Parser instance variables
private Program parserResult;

// Error recovery function
public void yyerror(String msg) {
    System.out.println("Syntax error at line " + getLineNumber() + ": " + msg);
}

public int yylex() {
    // This will be implemented by your lexer
    return 0;
}

// Get the parsing result
public Program getParserResult() {
    return parserResult;
}

// Helper method to get current line number (you'll need to implement this based on your lexer)
private int getLineNumber() {
    return 0; // Implement based on your lexer
}