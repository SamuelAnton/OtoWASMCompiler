%{
import java.util.*;
import Syntaxer.ast.*;
import Syntaxer.ast.component.*;
import Syntaxer.ast.declaration.*;
import Syntaxer.ast.expression.*;
import Syntaxer.ast.literal.*;
import Syntaxer.ast.statement.*;
%}

%union {    
    // AST Nodes
    Program program;
    ClassDeclaration classDeclaration;
    MemberDeclaration memberDeclaration;
    FieldDeclaration fieldDeclaration;
    MethodDeclaration methodDeclaration;
    ConstructorDeclaration constructorDeclaration;
    Param param;
    Statement statement;
    Expression expression;
    ElseStatement elseClause;
    ThenStatement thenClause;
    ExtensionType extensionType;
    ReturnType returnType;
    Type type;
    VariableDeclaration variableDeclaration;
    ArrayList<ClassDeclaration> classDeclarationList;
    ArrayList<MemberDeclaration> memberDeclarationList;
    ArrayList<Param> paramList;
    ArrayList<Statement> statementList;
    ArrayList<Expression> expressionList;


    // Primitive types for tokens
    String strVal;
    int intVal;
    double realVal;
    boolean boolVal;
}

// Token type declarations
%token <strVal> IDENTIFIER STRING_LITERAL
%token <intVal> NUMBER
%token <realVal> REAL_LITERAL  
%token <boolVal> BOOLEAN_LITERAL

// keywords
%token VAR CLASS IS END RETURN THIS METHOD
%token EXTENDS WHILE LOOP IF ELSE THEN

// delimeters
%token COLON   // :
%token DOT      // .
%token COMMA    // ,
%token LPAREN   // (
%token RPAREN   // )
%token LBRACKET // [
%token RBRACKET // ]

// operator signs
%token SHORTBODY    // =>
%token ASSIGN       // :=

// Complex data types
%token ARRAY LIST

// Types for non-terminals
%type <program> CompilationUnit
%type <classDeclarationList> ClassDeclarations
%type <classDeclaration> ClassDeclaration
%type <memberDeclarationList> ClassBody
%type <memberDeclarationList> ClassMembers
%type <memberDeclaration> ClassMember
%type <fieldDeclaration> FieldDeclaration
%type <methodDeclaration> MethodDeclaration
%type <constructorDeclaration> ConstructorDeclaration
%type <paramList> Parameters
%type <paramList> ParameterList
%type <param> Parameter
%type <returnType> ReturnType
%type <variableDeclaration> VarDeclaration
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
%type <expression> MethodCall
%type <expressionList> ArgumentList
%type <extensionType;> Extension
%type <statementList> MethodBody
%type <elseClause> ElseStatement
%type <thenClause> ThenStatement
%type <type> Type


%start CompilationUnit


%%


CompilationUnit
    : ClassDeclarations {
        $$ = new Program($1);
        parserResult = $$;
      }
    ;

ClassDeclarations
    :                                    {$$ = new ArrayList<>();}
    | ClassDeclarations ClassDeclaration {$1.add($2); $$ = $1;}
    ;

ClassDeclaration
    : CLASS IDENTIFIER Extension IS ClassBody END {$$ = new ClassDeclaration($2, $3, $5);}
    ;

Extension
    :                    {$$ = null;}
    | EXTENDS IDENTIFIER {$$ = new ExtensionType($2);}
    ;

ClassBody
    : ClassMembers {$$ = $1;}
    ;

ClassMembers
    :                          {$$ = new ArrayList<>();}
    | ClassMembers ClassMember {$1.add($2); $$ = $1;}
    ;

ClassMember
    : FieldDeclaration       {$$ = $1;}
    | MethodDeclaration      {$$ = $1;}
    | ConstructorDeclaration {$$ = $1;}
    ;

FieldDeclaration
    : VAR IDENTIFIER COLON Expression  {$$ = new FieldDeclaration($2, $4);}
    | VAR IDENTIFIER IS Expression     {$$ = new FieldDeclaration($2, $4);}
    ;

MethodDeclaration
    : METHOD IDENTIFIER Parameters ReturnType MethodBody {$$ = new MethodDeclaration($2, $3, $4, $5);}
    ;

ConstructorDeclaration
    : THIS Parameters IS Statements END {$$ = new ConstructorDeclaration($2, $4);}
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
    : IDENTIFIER COLON Type {$$ = new Param($1, $3);}
    ;

Type
    :                IDENTIFIER          {$$ = new Type($1);}
    | ARRAY LBRACKET IDENTIFIER RBRACKET {$$ = new Type(new ArrayLiteral($3, -1));}
    | LIST  LBRACKET IDENTIFIER RBRACKET {$$ = new Type(new ListLiteral($3));}
    ;

ReturnType
    :                  {$$ = null;}
    | COLON IDENTIFIER {$$ = new ReturnType($2);}
    ;

MethodBody
    :                     {$$ = new ArrayList<>();}
    | IS Statements END   {$$ = $2;}
    | SHORTBODY Expression {
        // Convert single expression to return statement
        ArrayList<Statement> body = new ArrayList<>();
        body.add(new ReturnStatement($2));
        $$ = body;
      }
    ;

Statements
    :                      {$$ = new ArrayList<>();}
    | Statements Statement {$1.add($2); $$ = $1;}
    ;

Statement
    : VarDeclaration   {$$ = $1;}
    | Assignment       {$$ = $1;}
    | IfStatement      {$$ = $1;}
    | WhileStatement   {$$ = $1;}
    | ReturnStatement  {$$ = $1;}
    | Expression       {$$ = new ExpressionStatement($1);}
    ;

VarDeclaration
    : VAR IDENTIFIER COLON Expression {$$ = new VariableDeclaration($2, $4);}
    | VAR IDENTIFIER IS Expression     {$$ = new VariableDeclaration($2, $4);}
    ;

Assignment
    : CompoundName ASSIGN Expression {$$ = new AssignmentStatement($1, $3);}
    ;

CompoundName
    :                  IDENTIFIER {$$ = new VariableReference($1);}
    | CompoundName DOT IDENTIFIER {$$ = new MemberAccess($1, new VariableReference($3));}
    ;

IfStatement
    : IF Expression ThenStatement ElseStatement END {$$ = new IfStatement($2, $3, $4);}
    ;

ThenStatement
    : THEN Statements Statement {$2.add($3); $$ = new ThenStatement($2);}

ElseStatement
    :                           {$$ = null;}
    | ELSE Statements Statement {$2.add($3); $$ = new ElseStatement($2);}
    ;

WhileStatement
    : WHILE Expression LOOP Statements END {$$ = new WhileStatement($2, $4);}
    ;

ReturnStatement
    : RETURN            {$$ = new ReturnStatement(null);}
    | RETURN Expression {$$ = new ReturnStatement($2);}
    ;

MethodCall
    : CompoundName LPAREN              RPAREN                       {$$ = new MethodCall($1, new ArrayList<>());}
    | CompoundName LPAREN ArgumentList RPAREN                       {$$ = new MethodCall($1, $3);}
    | ARRAY LBRACKET IDENTIFIER RBRACKET LPAREN ArgumentList RPAREN {$$ = new MethodCall(new ArrayLiteral($3, $6.size()), $6);}
    | ARRAY LBRACKET IDENTIFIER RBRACKET LPAREN IDENTIFIER RPAREN LPAREN ArgumentList RPAREN {$$ = new MethodCall(new ArrayLiteral($3, $6), $9);}
    | LIST  LBRACKET IDENTIFIER RBRACKET LPAREN ArgumentList RPAREN {$$ = new MethodCall(new ListLiteral($3), $6);}
    ;

ArgumentList
    :                    Expression {$$ = new ArrayList<>(); $$.add($1);}
    | ArgumentList COMMA Expression {$1.add($3); $$ = $1;}
    ;

Expression
    : Primary                   {$$ = $1;}
    | ConstructorInvocation     {$$ = $1;}
    | MethodCall                {$$ = $1;}
    | Expression DOT Expression {$$ = new MemberAccess($1, $3);}
    ;

Primary
    : THIS                                                    {$$ = new ThisExpression();}
    | CompoundName                                            {$$ = $1;}
    | LPAREN Expression RPAREN                                {$$ = $2;}
    | LIST  LBRACKET IDENTIFIER RBRACKET                      {$$ = new ListLiteral($3);}
    | LIST                                                    {$$ = new ListLiteral("void")}
    | ARRAY LBRACKET IDENTIFIER RBRACKET LPAREN IDENTIFIER RPAREN {$$ = new ArrayLiteral($3, $6);}
    | NUMBER                                                  {$$ = new IntegerLiteral($1);}
    | REAL_LITERAL                                            {$$ = new RealLiteral($1);}
    | BOOLEAN_LITERAL                                         {$$ = new BooleanLiteral($1);}
    | STRING_LITERAL                                          {$$ = new StringLiteral($1);}
    ; 

ConstructorInvocation
    : IDENTIFIER LPAREN              RPAREN {$$ = new ConstructorCall($1, new ArrayList<>());}
    | IDENTIFIER LPAREN ArgumentList RPAREN {$$ = new ConstructorCall($1, $3);}
    ;


%%


// Parser instance variables
private Program parserResult;

// Error recovery function
public void yyerror(String msg) {
    System.out.println("Syntax error at line " + getLineNumber() + ": " + msg);
}

public int yylex() {
    // This will be implemented by your lexer
    NamedToken token = lexer.nextToken();
    switch (token.getToken()) {
      case tkIdentifier:
      case tkStringLiteral:
          yylval = new ParserVal(token.getValue());
          break;
      case tkNumber:
          try {
              yylval = new ParserVal(Integer.parseInt(token.getValue()));
          } catch (NumberFormatException e) {
              yylval = new ParserVal(Double.parseDouble(token.getValue()));
          }
          break;
      case tkRealLiteral:
          yylval = new ParserVal(Double.parseDouble(token.getValue()));
          break;
      case tkBooleanLiteral:
          yylval = new ParserVal(Boolean.parseBoolean(token.getValue()));
          break;
      default:
        break;
    }
    return token.getType();
}

// Get the parsing result
public Program getParserResult() {
    return parserResult;
}

// Helper method to get current line number (you'll need to implement this based on your lexer)
private int getLineNumber() {
    return lexer.getLineNumber();
}