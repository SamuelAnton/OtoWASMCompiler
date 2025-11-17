//### This file created by BYACC 1.8(/Java extension  1.15)
//### Java capabilities added 7 Jan 97, Bob Jamison
//### Updated : 27 Nov 97  -- Bob Jamison, Joe Nieten
//###           01 Jan 98  -- Bob Jamison -- fixed generic semantic constructor
//###           01 Jun 99  -- Bob Jamison -- added Runnable support
//###           06 Aug 00  -- Bob Jamison -- made state variables class-global
//###           03 Jan 01  -- Bob Jamison -- improved flags, tracing
//###           16 May 01  -- Bob Jamison -- added custom stack sizing
//###           04 Mar 02  -- Yuval Oren  -- improved java performance, added options
//###           14 Mar 02  -- Tomas Hurka -- -d support, static initializer workaround
//### Please send bug reports to tom@hukatronic.cz
//### static char yysccsid[] = "@(#)yaccpar	1.8 (Berkeley) 01/20/90";



package Syntaxer.parser;



//#line 2 "src/Syntaxer/parser/Parser.y"
import java.util.*;
import Lexer.*;
import Syntaxer.ast.*;
import Syntaxer.ast.component.*;
import Syntaxer.ast.declaration.*;
import Syntaxer.ast.expression.*;
import Syntaxer.ast.literal.*;
import Syntaxer.ast.statement.*;
//#line 12 "src/Syntaxer/parser/Parser.y"
// typedef union {    
//     /* AST Nodes*/
//     Program program;
//     ClassDeclaration classDeclaration;
//     MemberDeclaration memberDeclaration;
//     FieldDeclaration fieldDeclaration;
//     MethodDeclaration methodDeclaration;
//     ConstructorDeclaration constructorDeclaration;
//     Param param;
//     Statement statement;
//     Expression expression;
//     ElseStatement elseClause;
//     ThenStatement thenClause;
//     ExtensionType extensionType;
//     ReturnType returnType;
//     Type type;
//     VariableDeclaration variableDeclaration;
//     ArrayList<ClassDeclaration> classDeclarationList;
//     ArrayList<MemberDeclaration> memberDeclarationList;
//     ArrayList<Param> paramList;
//     ArrayList<Statement> statementList;
//     ArrayList<Expression> expressionList;


//     /* Primitive types for tokens*/
//     String strVal;
//     int intVal;
//     double realVal;
//     boolean boolVal;
// } YYSTYPE;
//#line 57 "Parser.java"




public class Parser
{
Lexer lexer;
boolean yydebug;        //do I want debug output?
int yynerrs;            //number of errors so far
int yyerrflag;          //was there an error?
int yychar;             //the current working character

//########## MESSAGES ##########
//###############################################################
// method: debug
//###############################################################
void debug(String msg)
{
  if (yydebug)
    System.out.println(msg);
}

//########## STATE STACK ##########
final static int YYSTACKSIZE = 1000;  //maximum stack size
int statestk[] = new int[YYSTACKSIZE]; //state stack
int stateptr;
int stateptrmax;                     //highest index of stackptr
int statemax;                        //state when highest index reached
//###############################################################
// methods: state stack push,pop,drop,peek
//###############################################################
final void state_push(int state)
{
  try {
		stateptr++;
		statestk[stateptr]=state;
	 }
	 catch (ArrayIndexOutOfBoundsException e) {
     int oldsize = statestk.length;
     int newsize = oldsize * 2;
     int[] newstack = new int[newsize];
     System.arraycopy(statestk,0,newstack,0,oldsize);
     statestk = newstack;
     statestk[stateptr]=state;
  }
}
final int state_pop()
{
  return statestk[stateptr--];
}
final void state_drop(int cnt)
{
  stateptr -= cnt; 
}
final int state_peek(int relative)
{
  return statestk[stateptr-relative];
}
//###############################################################
// method: init_stacks : allocate and prepare stacks
//###############################################################
final boolean init_stacks()
{
  stateptr = -1;
  val_init();
  return true;
}
//###############################################################
// method: dump_stacks : show n levels of the stacks
//###############################################################
void dump_stacks(int count)
{
int i;
  System.out.println("=index==state====value=     s:"+stateptr+"  v:"+valptr);
  for (i=0;i<count;i++)
    System.out.println(" "+i+"    "+statestk[i]+"      "+valstk[i]);
  System.out.println("======================");
}


//########## SEMANTIC VALUES ##########
//## **user defined:Object
String   yytext;//user variable to return contextual strings
ParserVal yyval; //used to return semantic vals from action routines
ParserVal yylval;//the 'lval' (result) I got from yylex()
ParserVal valstk[] = new ParserVal[YYSTACKSIZE];
int valptr;
//###############################################################
// methods: value stack push,pop,drop,peek.
//###############################################################
final void val_init()
{
  yyval=new ParserVal();
  yylval=new ParserVal();
  valptr=-1;
}
final void val_push(ParserVal val)
{
  try {
    valptr++;
    valstk[valptr]=val;
  }
  catch (ArrayIndexOutOfBoundsException e) {
    int oldsize = valstk.length;
    int newsize = oldsize*2;
    ParserVal[] newstack = new ParserVal[newsize];
    System.arraycopy(valstk,0,newstack,0,oldsize);
    valstk = newstack;
    valstk[valptr]=val;
  }
}
final ParserVal val_pop()
{
  return valstk[valptr--];
}
final void val_drop(int cnt)
{
  valptr -= cnt;
}
final ParserVal val_peek(int relative)
{
  return valstk[valptr-relative];
}
final ParserVal dup_yyval(ParserVal val)
{
  return val;
}
//#### end semantic value section ####
public final static short IDENTIFIER=257;
public final static short STRING_LITERAL=258;
public final static short NUMBER=259;
public final static short REAL_LITERAL=260;
public final static short BOOLEAN_LITERAL=261;
public final static short VAR=262;
public final static short CLASS=263;
public final static short IS=264;
public final static short END=265;
public final static short RETURN=266;
public final static short THIS=267;
public final static short METHOD=268;
public final static short EXTENDS=269;
public final static short WHILE=270;
public final static short LOOP=271;
public final static short IF=272;
public final static short ELSE=273;
public final static short THEN=274;
public final static short SUPER=275;
public final static short COLON=276;
public final static short DOT=277;
public final static short COMMA=278;
public final static short LPAREN=279;
public final static short RPAREN=280;
public final static short LBRACKET=281;
public final static short RBRACKET=282;
public final static short SEMICOLON=283;
public final static short SHORTBODY=284;
public final static short ASSIGN=285;
public final static short ARRAY=286;
public final static short LIST=287;
public final static short YYERRCODE=256;
final static short yylhs[] = {                           -1,
    0,    1,    1,    2,   27,   27,    3,    4,    4,    5,
    5,    5,    6,    6,    7,    8,    8,    9,    9,   10,
   10,   11,   31,   31,   31,   12,   12,   28,   28,   28,
   14,   14,   15,   15,   15,   15,   15,   15,   13,   13,
   16,   22,   22,   17,   30,   29,   29,   18,   19,   19,
   24,   24,   24,   24,   24,   25,   25,   26,   26,   20,
   20,   20,   20,   20,   21,   21,   21,   21,   21,   21,
   21,   21,   21,   21,   23,   23,
};
final static short yylen[] = {                            2,
    1,    0,    2,    6,    0,    2,    1,    0,    2,    1,
    1,    1,    4,    4,    5,    5,    4,    2,    3,    1,
    3,    3,    1,    4,    4,    0,    2,    0,    3,    2,
    0,    2,    2,    2,    1,    1,    2,    2,    4,    4,
    3,    1,    3,    5,    3,    0,    3,    5,    1,    2,
    3,    4,    7,   10,    7,    3,    4,    1,    3,    1,
    1,    1,    1,    3,    1,    1,    3,    4,    1,    7,
    1,    1,    1,    1,    3,    4,
};
final static short yydefred[] = {                         2,
    0,    0,    0,    3,    0,    0,    0,    6,    8,    0,
    0,    4,    0,    0,    0,    9,   10,   11,   12,    0,
    0,    0,    0,    0,    0,    0,   18,    0,   20,   31,
    0,    0,    0,   74,   71,   72,   73,   65,    0,    0,
    0,    0,    0,   60,    0,   62,   61,   63,    0,    0,
    0,   19,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,   23,    0,    0,   22,   21,    0,
   16,    0,    0,    0,    0,   32,    0,   35,   36,    0,
    0,    0,   27,   31,    0,   15,   75,    0,    0,   56,
    0,   67,    0,    0,    0,   43,   51,    0,    0,    0,
    0,    0,    0,    0,   33,   34,   37,   38,    0,    0,
    0,    0,   76,   57,    0,    0,   52,    0,    0,    0,
    0,   31,   31,    0,    0,   29,    0,    0,    0,   24,
   25,    0,    0,    0,    0,   31,    0,    0,    0,    0,
   48,    0,    0,   44,    0,   53,   55,    0,    0,    0,
   54,
};
final static short yydgoto[] = {                          1,
    2,    4,   10,   11,   16,   17,   18,   19,   22,   28,
   29,   56,   75,   53,   76,   77,   78,   79,   80,   88,
   44,   45,   46,   47,   48,   89,    7,   86,  137,  124,
   68,
};
final static short yysindex[] = {                         0,
    0, -257, -212,    0, -206, -200, -194,    0,    0, -178,
  -93,    0, -151, -170, -133,    0,    0,    0,    0, -259,
 -236, -237, -170,   42,   42, -158,    0, -203,    0,    0,
   42, -144, -132,    0,    0,    0,    0,    0,  -87,   42,
 -126, -103,  -65,    0, -146,    0,    0,    0,  -65, -253,
  -60,    0, -145,  -65,  -37, -233,  -30,   -6, -213,  -33,
  -32,   42,  -25,   18,    0,  -66,  -46,    0,    0,  -16,
    0,   42,   42,   42,  -41,    0,  -40,    0,    0,  -39,
 -231, -242,    0,    0,   42,    0,    0,  -65, -124,    0,
 -108,    0,  -36,  -35,  -65,    0,    0, -101,   -9,    1,
 -240,  -65, -217, -195,    0,    0,    0,    0,   42, -122,
  -65,   42,    0,    0,  -20,  -19,    0,  -18,  -17,   42,
   42,    0,    0,  -11,  -65,    0,  -65,   55,   42,    0,
    0,  -65,  -65,  -99,  -53,    0,   -2,  -78,  -85,  -80,
    0,    0,  -53,    0,  -13,    0,    0,    0,   42,  -62,
    0,
};
final static short yyrindex[] = {                         0,
    0,  267,    0,    0,    4,    0,    0,    0,    0,    0,
    5,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0, -184, -209,    0,    0,    0,    0,    0,    0,    0,
    0,   78, -173,    0,  108,    0,    0,    0, -164,    0,
    0,    0,    0, -160,    0, -139,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  -12,    0,    0,    0,    0,    0,    0,    0,    0,
    0,   -1,    0,    0,    0,    0,    0,  -57,    0,    0,
    0,    0,    0,    0,  122,    0,    0,    0,    0,    0,
    0,    3,    0,    0,    0,    0,    0,    0,    0,    0,
 -116,    0,    0,    0,    0,   86,    0,    0,    0,    0,
    0,    0,    0,    7,    6,    0,  -42,    0,    0,    0,
    0,    8,    9,    0,    0,    0,    0,  -38,    0,    0,
    0, -247,    0,    0,  100,    0,    0,  -76,    0,    0,
    0,
};
final static short yygindex[] = {                         0,
    0,    0,    0,    0,    0,    0,    0,    0,  260,    0,
  233,    0,    0,  -81, -113,    0,    0,    0,    0,  -24,
    0,  -44,    0,    0,    0,  -56,    0,    0,    0,    0,
    0,
};
final static int YYTABLESIZE=405;
static short yytable[];
static { yytable();}
static void yytable(){
yytable = new short[]{                         43,
   49,   91,  110,   65,   24,    3,   54,   98,   82,   32,
   32,   32,   32,   32,   32,   59,   25,   45,   32,   32,
   26,  142,   32,  120,   32,   45,   30,   32,   81,  148,
   84,   32,   66,   67,   63,  121,   64,   95,   32,   32,
  134,  135,  109,   27,    5,   62,   31,  102,  103,  104,
   85,  108,   42,  122,  143,   42,    8,   42,   42,   62,
  111,   42,    6,   62,   42,   82,   92,   42,   42,    9,
   42,  139,  140,   42,   51,   42,   52,   26,  123,   26,
   26,   62,   26,   26,  125,   81,   12,  127,   14,   82,
   82,   14,  150,   14,   14,  132,  133,   13,   82,   26,
   13,   17,   13,   13,   17,   20,   17,   17,   21,   81,
   81,   33,   34,   35,   36,   37,   70,   50,   81,   71,
   72,   38,   28,   23,   73,   28,   74,   28,   28,   39,
   63,   55,   64,   40,   33,   34,   35,   36,   37,   70,
   41,   42,  126,   72,   38,   30,   57,   73,   30,   74,
   30,   30,   39,  112,   60,  113,   40,   33,   34,   35,
   36,   37,   70,   41,   42,  141,   72,   38,   13,  112,
   73,  114,   74,   14,   15,   39,  112,   61,  117,   40,
   32,   32,   32,   32,   32,   32,   41,   42,   47,   32,
   32,   58,  112,   32,  146,   32,   26,  112,   32,  147,
   57,  145,   32,   33,   34,   35,   36,   37,   70,   32,
   32,   62,   72,   38,   99,  112,   73,  151,   74,   83,
   58,   39,   58,   93,   94,   40,   33,   34,   35,   36,
   37,   96,   41,   42,  100,   59,   38,   59,   42,   42,
  101,  105,  106,  107,   39,  115,  116,  118,   40,   87,
   33,   34,   35,   36,   37,   41,   42,  119,  128,  129,
   38,  136,  144,  130,  131,  149,    1,    5,   39,    7,
   49,   46,   40,   90,   33,   34,   35,   36,   37,   41,
   42,   66,   32,   69,   38,   50,    0,    0,   41,    0,
   40,   39,   39,    0,    0,    0,   40,   97,   33,   34,
   35,   36,   37,   41,   42,    0,    0,    0,   38,    0,
    0,  138,   34,   35,   36,   37,   39,    0,    0,    0,
   40,   38,    0,    0,    0,    0,    0,   41,   42,   39,
    0,    0,    0,   40,    0,    0,    0,    0,    0,   69,
   41,   42,   69,    0,   69,   69,    0,   68,   69,    0,
   68,   69,   68,   68,   69,   69,   68,   69,    0,   68,
   69,   70,   68,   68,   70,   68,   70,   70,   68,   66,
   70,    0,   66,   70,   66,   66,   70,   70,   66,   70,
    0,   66,   70,   64,    0,   66,   64,   66,   64,   64,
   66,    0,   64,    0,    0,   64,    0,    0,    0,   64,
    0,   64,    0,    0,   64,
};
}
static short yycheck[];
static { yycheck(); }
static void yycheck() {
yycheck = new short[] {                         24,
   25,   58,   84,  257,  264,  263,   31,   64,   53,  257,
  258,  259,  260,  261,  262,   40,  276,  265,  266,  267,
  257,  135,  270,  264,  272,  273,  264,  275,   53,  143,
  264,  279,  286,  287,  277,  276,  279,   62,  286,  287,
  122,  123,  285,  280,  257,  277,  284,   72,   73,   74,
  284,  283,  262,  271,  136,  265,  257,  267,  268,  277,
   85,  271,  269,  277,  274,  110,  280,  277,  278,  264,
  280,  128,  129,  283,  278,  285,  280,  262,  274,  264,
  265,  277,  267,  268,  109,  110,  265,  112,  262,  134,
  135,  265,  149,  267,  268,  120,  121,  262,  143,  284,
  265,  262,  267,  268,  265,  257,  267,  268,  279,  134,
  135,  257,  258,  259,  260,  261,  262,  276,  143,  265,
  266,  267,  262,  257,  270,  265,  272,  267,  268,  275,
  277,  276,  279,  279,  257,  258,  259,  260,  261,  262,
  286,  287,  265,  266,  267,  262,  279,  270,  265,  272,
  267,  268,  275,  278,  281,  280,  279,  257,  258,  259,
  260,  261,  262,  286,  287,  265,  266,  267,  262,  278,
  270,  280,  272,  267,  268,  275,  278,  281,  280,  279,
  257,  258,  259,  260,  261,  262,  286,  287,  265,  266,
  267,  279,  278,  270,  280,  272,  257,  278,  275,  280,
  279,  280,  279,  257,  258,  259,  260,  261,  262,  286,
  287,  277,  266,  267,  281,  278,  270,  280,  272,  257,
  278,  275,  280,  257,  257,  279,  257,  258,  259,  260,
  261,  257,  286,  287,  281,  278,  267,  280,  277,  278,
  257,  283,  283,  283,  275,  282,  282,  257,  279,  280,
  257,  258,  259,  260,  261,  286,  287,  257,  279,  279,
  267,  273,  265,  282,  282,  279,    0,  264,  275,  265,
  283,  265,  279,  280,  257,  258,  259,  260,  261,  286,
  287,  283,   23,   51,  267,  283,   -1,   -1,  283,   -1,
  283,  283,  275,   -1,   -1,   -1,  279,  280,  257,  258,
  259,  260,  261,  286,  287,   -1,   -1,   -1,  267,   -1,
   -1,  257,  258,  259,  260,  261,  275,   -1,   -1,   -1,
  279,  267,   -1,   -1,   -1,   -1,   -1,  286,  287,  275,
   -1,   -1,   -1,  279,   -1,   -1,   -1,   -1,   -1,  262,
  286,  287,  265,   -1,  267,  268,   -1,  262,  271,   -1,
  265,  274,  267,  268,  277,  278,  271,  280,   -1,  274,
  283,  262,  277,  278,  265,  280,  267,  268,  283,  262,
  271,   -1,  265,  274,  267,  268,  277,  278,  271,  280,
   -1,  274,  283,  262,   -1,  278,  265,  280,  267,  268,
  283,   -1,  271,   -1,   -1,  274,   -1,   -1,   -1,  278,
   -1,  280,   -1,   -1,  283,
};
}
final static short YYFINAL=1;
final static short YYMAXTOKEN=287;
final static String yyname[] = {
"end-of-file",null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,"IDENTIFIER","STRING_LITERAL","NUMBER","REAL_LITERAL",
"BOOLEAN_LITERAL","VAR","CLASS","IS","END","RETURN","THIS","METHOD","EXTENDS",
"WHILE","LOOP","IF","ELSE","THEN","SUPER","COLON","DOT","COMMA","LPAREN",
"RPAREN","LBRACKET","RBRACKET","SEMICOLON","SHORTBODY","ASSIGN","ARRAY","LIST",
};
final static String yyrule[] = {
"$accept : CompilationUnit",
"CompilationUnit : ClassDeclarations",
"ClassDeclarations :",
"ClassDeclarations : ClassDeclarations ClassDeclaration",
"ClassDeclaration : CLASS IDENTIFIER Extension IS ClassBody END",
"Extension :",
"Extension : EXTENDS IDENTIFIER",
"ClassBody : ClassMembers",
"ClassMembers :",
"ClassMembers : ClassMembers ClassMember",
"ClassMember : FieldDeclaration",
"ClassMember : MethodDeclaration",
"ClassMember : ConstructorDeclaration",
"FieldDeclaration : VAR IDENTIFIER COLON Expression",
"FieldDeclaration : VAR IDENTIFIER IS Expression",
"MethodDeclaration : METHOD IDENTIFIER Parameters ReturnType MethodBody",
"ConstructorDeclaration : THIS Parameters IS Statements END",
"ConstructorDeclaration : THIS Parameters SHORTBODY Expression",
"Parameters : LPAREN RPAREN",
"Parameters : LPAREN ParameterList RPAREN",
"ParameterList : Parameter",
"ParameterList : ParameterList COMMA Parameter",
"Parameter : IDENTIFIER COLON Type",
"Type : IDENTIFIER",
"Type : ARRAY LBRACKET IDENTIFIER RBRACKET",
"Type : LIST LBRACKET IDENTIFIER RBRACKET",
"ReturnType :",
"ReturnType : COLON IDENTIFIER",
"MethodBody :",
"MethodBody : IS Statements END",
"MethodBody : SHORTBODY Expression",
"Statements :",
"Statements : Statements Statement",
"Statement : VarDeclaration SEMICOLON",
"Statement : Assignment SEMICOLON",
"Statement : IfStatement",
"Statement : WhileStatement",
"Statement : ReturnStatement SEMICOLON",
"Statement : Expression SEMICOLON",
"VarDeclaration : VAR IDENTIFIER COLON Expression",
"VarDeclaration : VAR IDENTIFIER IS Expression",
"Assignment : CompoundName ASSIGN Expression",
"CompoundName : IDENTIFIER",
"CompoundName : CompoundName DOT IDENTIFIER",
"IfStatement : IF Expression ThenStatement ElseStatement END",
"ThenStatement : THEN Statements Statement",
"ElseStatement :",
"ElseStatement : ELSE Statements Statement",
"WhileStatement : WHILE Expression LOOP Statements END",
"ReturnStatement : RETURN",
"ReturnStatement : RETURN Expression",
"MethodCall : CompoundName LPAREN RPAREN",
"MethodCall : CompoundName LPAREN ArgumentList RPAREN",
"MethodCall : ARRAY LBRACKET IDENTIFIER RBRACKET LPAREN ArgumentList RPAREN",
"MethodCall : ARRAY LBRACKET IDENTIFIER RBRACKET LPAREN IDENTIFIER RPAREN LPAREN ArgumentList RPAREN",
"MethodCall : LIST LBRACKET IDENTIFIER RBRACKET LPAREN ArgumentList RPAREN",
"SuperConstructorCall : SUPER LPAREN RPAREN",
"SuperConstructorCall : SUPER LPAREN ArgumentList RPAREN",
"ArgumentList : Expression",
"ArgumentList : ArgumentList COMMA Expression",
"Expression : Primary",
"Expression : MethodCall",
"Expression : ConstructorInvocation",
"Expression : SuperConstructorCall",
"Expression : Expression DOT Expression",
"Primary : THIS",
"Primary : CompoundName",
"Primary : LPAREN Expression RPAREN",
"Primary : LIST LBRACKET IDENTIFIER RBRACKET",
"Primary : LIST",
"Primary : ARRAY LBRACKET IDENTIFIER RBRACKET LPAREN IDENTIFIER RPAREN",
"Primary : NUMBER",
"Primary : REAL_LITERAL",
"Primary : BOOLEAN_LITERAL",
"Primary : STRING_LITERAL",
"ConstructorInvocation : IDENTIFIER LPAREN RPAREN",
"ConstructorInvocation : IDENTIFIER LPAREN ArgumentList RPAREN",
};

//#line 297 "src/Syntaxer/parser/Parser.y"


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
//#line 490 "Parser.java"
//###############################################################
// method: yylexdebug : check lexer state
//###############################################################
void yylexdebug(int state,int ch)
{
String s=null;
  if (ch < 0) ch=0;
  if (ch <= YYMAXTOKEN) //check index bounds
     s = yyname[ch];    //now get it
  if (s==null)
    s = "illegal-symbol";
  debug("state "+state+", reading "+ch+" ("+s+")");
}





//The following are now global, to aid in error reporting
int yyn;       //next next thing to do
int yym;       //
int yystate;   //current parsing state from state table
String yys;    //current token string


//###############################################################
// method: yyparse : parse input and execute indicated items
//###############################################################
public int yyparse()
{
boolean doaction;
  init_stacks();
  yynerrs = 0;
  yyerrflag = 0;
  yychar = -1;          //impossible char forces a read
  yystate=0;            //initial state
  state_push(yystate);  //save it
  val_push(yylval);     //save empty value
  while (true) //until parsing is done, either correctly, or w/error
    {
    doaction=true;
    if (yydebug) debug("loop"); 
    //#### NEXT ACTION (from reduction table)
    for (yyn=yydefred[yystate];yyn==0;yyn=yydefred[yystate])
      {
      if (yydebug) debug("yyn:"+yyn+"  state:"+yystate+"  yychar:"+yychar);
      if (yychar < 0)      //we want a char?
        {
        yychar = yylex();  //get next token
        if (yydebug) debug(" next yychar:"+yychar);
        //#### ERROR CHECK ####
        if (yychar < 0)    //it it didn't work/error
          {
          yychar = 0;      //change it to default string (no -1!)
          if (yydebug)
            yylexdebug(yystate,yychar);
          }
        }//yychar<0
      yyn = yysindex[yystate];  //get amount to shift by (shift index)
      if ((yyn != 0) && (yyn += yychar) >= 0 &&
          yyn <= YYTABLESIZE && yycheck[yyn] == yychar)
        {
        if (yydebug)
          debug("state "+yystate+", shifting to state "+yytable[yyn]);
        //#### NEXT STATE ####
        yystate = yytable[yyn];//we are in a new state
        state_push(yystate);   //save it
        val_push(yylval);      //push our lval as the input for next rule
        yychar = -1;           //since we have 'eaten' a token, say we need another
        if (yyerrflag > 0)     //have we recovered an error?
           --yyerrflag;        //give ourselves credit
        doaction=false;        //but don't process yet
        break;   //quit the yyn=0 loop
        }

    yyn = yyrindex[yystate];  //reduce
    if ((yyn !=0 ) && (yyn += yychar) >= 0 &&
            yyn <= YYTABLESIZE && yycheck[yyn] == yychar)
      {   //we reduced!
      if (yydebug) debug("reduce");
      yyn = yytable[yyn];
      doaction=true; //get ready to execute
      break;         //drop down to actions
      }
    else //ERROR RECOVERY
      {
      if (yyerrflag==0)
        {
        yyerror("syntax error");
        yynerrs++;
        }
      if (yyerrflag < 3) //low error count?
        {
        yyerrflag = 3;
        while (true)   //do until break
          {
          if (stateptr<0)   //check for under & overflow here
            {
            yyerror("stack underflow. aborting...");  //note lower case 's'
            return 1;
            }
          yyn = yysindex[state_peek(0)];
          if ((yyn != 0) && (yyn += YYERRCODE) >= 0 &&
                    yyn <= YYTABLESIZE && yycheck[yyn] == YYERRCODE)
            {
            if (yydebug)
              debug("state "+state_peek(0)+", error recovery shifting to state "+yytable[yyn]+" ");
            yystate = yytable[yyn];
            state_push(yystate);
            val_push(yylval);
            doaction=false;
            break;
            }
          else
            {
            if (yydebug)
              debug("error recovery discarding state "+state_peek(0)+" ");
            if (stateptr<0)   //check for under & overflow here
              {
              yyerror("Stack underflow. aborting...");  //capital 'S'
              return 1;
              }
            state_pop();
            val_pop();
            }
          }
        }
      else            //discard this token
        {
        if (yychar == 0)
          return 1; //yyabort
        if (yydebug)
          {
          yys = null;
          if (yychar <= YYMAXTOKEN) yys = yyname[yychar];
          if (yys == null) yys = "illegal-symbol";
          debug("state "+yystate+", error recovery discards token "+yychar+" ("+yys+")");
          }
        yychar = -1;  //read another
        }
      }//end error recovery
    }//yyn=0 loop
    if (!doaction)   //any reason not to proceed?
      continue;      //skip action
    yym = yylen[yyn];          //get count of terminals on rhs
    if (yydebug)
      debug("state "+yystate+", reducing "+yym+" by rule "+yyn+" ("+yyrule[yyn]+")");
    if (yym>0)                 //if count of rhs not 'nil'
      yyval = val_peek(yym-1); //get current semantic value
    yyval = dup_yyval(yyval); //duplicate yyval if ParserVal is used as semantic value
    switch(yyn)
      {
//########## USER-SUPPLIED ACTIONS ##########
case 1:
//#line 112 "src/Syntaxer/parser/Parser.y"
{
        yyval.program = new Program(val_peek(0).classDeclarationList);
        parserResult = yyval.program;
      }
break;
case 2:
//#line 119 "src/Syntaxer/parser/Parser.y"
{yyval.classDeclarationList = new ArrayList<>();}
break;
case 3:
//#line 120 "src/Syntaxer/parser/Parser.y"
{val_peek(1).classDeclarationList.add(val_peek(0).classDeclaration); yyval.classDeclarationList = val_peek(1).classDeclarationList;}
break;
case 4:
//#line 124 "src/Syntaxer/parser/Parser.y"
{yyval.classDeclaration = new ClassDeclaration(val_peek(4).strVal, val_peek(3).extensionType, val_peek(1).memberDeclarationList);}
break;
case 5:
//#line 128 "src/Syntaxer/parser/Parser.y"
{yyval.extensionType = null;}
break;
case 6:
//#line 129 "src/Syntaxer/parser/Parser.y"
{yyval.extensionType = new ExtensionType(val_peek(0).strVal);}
break;
case 7:
//#line 133 "src/Syntaxer/parser/Parser.y"
{yyval.memberDeclarationList = val_peek(0).memberDeclarationList;}
break;
case 8:
//#line 137 "src/Syntaxer/parser/Parser.y"
{yyval.memberDeclarationList = new ArrayList<>();}
break;
case 9:
//#line 138 "src/Syntaxer/parser/Parser.y"
{val_peek(1).memberDeclarationList.add(val_peek(0).memberDeclaration); yyval.memberDeclarationList = val_peek(1).memberDeclarationList;}
break;
case 10:
//#line 142 "src/Syntaxer/parser/Parser.y"
{yyval.memberDeclaration = val_peek(0).fieldDeclaration;}
break;
case 11:
//#line 143 "src/Syntaxer/parser/Parser.y"
{yyval.memberDeclaration = val_peek(0).methodDeclaration;}
break;
case 12:
//#line 144 "src/Syntaxer/parser/Parser.y"
{yyval.memberDeclaration = val_peek(0).constructorDeclaration;}
break;
case 13:
//#line 148 "src/Syntaxer/parser/Parser.y"
{yyval.fieldDeclaration = new FieldDeclaration(val_peek(2).strVal, val_peek(0).expression);}
break;
case 14:
//#line 149 "src/Syntaxer/parser/Parser.y"
{yyval.fieldDeclaration = new FieldDeclaration(val_peek(2).strVal, val_peek(0).expression);}
break;
case 15:
//#line 153 "src/Syntaxer/parser/Parser.y"
{yyval.methodDeclaration = new MethodDeclaration(val_peek(3).strVal, val_peek(2).paramList, val_peek(1).returnType, val_peek(0).statementList);}
break;
case 16:
//#line 157 "src/Syntaxer/parser/Parser.y"
{yyval.constructorDeclaration = new ConstructorDeclaration(val_peek(3).paramList, val_peek(1).statementList);}
break;
case 17:
//#line 158 "src/Syntaxer/parser/Parser.y"
{
        /* Convert single expression to return statement*/
        ArrayList<Statement> body = new ArrayList<>();
        body.add(new ReturnStatement(val_peek(0).expression));
        yyval.constructorDeclaration = new ConstructorDeclaration(val_peek(2).paramList, body);
      }
break;
case 18:
//#line 167 "src/Syntaxer/parser/Parser.y"
{yyval.paramList = new ArrayList<>();}
break;
case 19:
//#line 168 "src/Syntaxer/parser/Parser.y"
{yyval.paramList = val_peek(1).paramList;}
break;
case 20:
//#line 172 "src/Syntaxer/parser/Parser.y"
{yyval.paramList = new ArrayList<>(); yyval.paramList.add(val_peek(0).param);}
break;
case 21:
//#line 173 "src/Syntaxer/parser/Parser.y"
{yyval.paramList = val_peek(2).paramList; yyval.paramList.add(val_peek(0).param);}
break;
case 22:
//#line 177 "src/Syntaxer/parser/Parser.y"
{yyval.param = new Param(val_peek(2).strVal, val_peek(0).type);}
break;
case 23:
//#line 181 "src/Syntaxer/parser/Parser.y"
{yyval.type = new Type(val_peek(0).strVal);}
break;
case 24:
//#line 182 "src/Syntaxer/parser/Parser.y"
{yyval.type = new Type(new ArrayLiteral(val_peek(1).strVal, -1));}
break;
case 25:
//#line 183 "src/Syntaxer/parser/Parser.y"
{yyval.type = new Type(new ListLiteral(val_peek(1).strVal));}
break;
case 26:
//#line 187 "src/Syntaxer/parser/Parser.y"
{yyval.returnType = null;}
break;
case 27:
//#line 188 "src/Syntaxer/parser/Parser.y"
{yyval.returnType = new ReturnType(val_peek(0).strVal);}
break;
case 28:
//#line 192 "src/Syntaxer/parser/Parser.y"
{yyval.statementList = new ArrayList<>();}
break;
case 29:
//#line 193 "src/Syntaxer/parser/Parser.y"
{yyval.statementList = val_peek(1).statementList;}
break;
case 30:
//#line 194 "src/Syntaxer/parser/Parser.y"
{
        /* Convert single expression to return statement*/
        ArrayList<Statement> body = new ArrayList<>();
        body.add(new ReturnStatement(val_peek(0).expression));
        yyval.statementList = body;
      }
break;
case 31:
//#line 203 "src/Syntaxer/parser/Parser.y"
{yyval.statementList = new ArrayList<>();}
break;
case 32:
//#line 204 "src/Syntaxer/parser/Parser.y"
{val_peek(1).statementList.add(val_peek(0).statement); yyval.statementList = val_peek(1).statementList;}
break;
case 33:
//#line 208 "src/Syntaxer/parser/Parser.y"
{yyval.statement = val_peek(1).variableDeclaration;}
break;
case 34:
//#line 209 "src/Syntaxer/parser/Parser.y"
{yyval.statement = val_peek(1).statement;}
break;
case 35:
//#line 210 "src/Syntaxer/parser/Parser.y"
{yyval.statement = val_peek(0).statement;}
break;
case 36:
//#line 211 "src/Syntaxer/parser/Parser.y"
{yyval.statement = val_peek(0).statement;}
break;
case 37:
//#line 212 "src/Syntaxer/parser/Parser.y"
{yyval.statement = val_peek(1).statement;}
break;
case 38:
//#line 213 "src/Syntaxer/parser/Parser.y"
{yyval.statement = new ExpressionStatement(val_peek(1).expression);}
break;
case 39:
//#line 217 "src/Syntaxer/parser/Parser.y"
{yyval.variableDeclaration = new VariableDeclaration(val_peek(2).strVal, val_peek(0).expression);}
break;
case 40:
//#line 218 "src/Syntaxer/parser/Parser.y"
{yyval.variableDeclaration = new VariableDeclaration(val_peek(2).strVal, val_peek(0).expression);}
break;
case 41:
//#line 222 "src/Syntaxer/parser/Parser.y"
{yyval.statement = new AssignmentStatement(val_peek(2).expression, val_peek(0).expression);}
break;
case 42:
//#line 226 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new VariableReference(val_peek(0).strVal);}
break;
case 43:
//#line 227 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new MemberAccess(val_peek(2).expression, new VariableReference(val_peek(0).strVal));}
break;
case 44:
//#line 231 "src/Syntaxer/parser/Parser.y"
{yyval.statement = new IfStatement(val_peek(3).expression, val_peek(2).thenClause, val_peek(1).elseClause);}
break;
case 45:
//#line 235 "src/Syntaxer/parser/Parser.y"
{val_peek(1).statementList.add(val_peek(0).statement); yyval.thenClause = new ThenStatement(val_peek(1).statementList);}
break;
case 46:
//#line 238 "src/Syntaxer/parser/Parser.y"
{yyval.elseClause = null;}
break;
case 47:
//#line 239 "src/Syntaxer/parser/Parser.y"
{val_peek(1).statementList.add(val_peek(0).statement); yyval.elseClause = new ElseStatement(val_peek(1).statementList);}
break;
case 48:
//#line 243 "src/Syntaxer/parser/Parser.y"
{yyval.statement = new WhileStatement(val_peek(3).expression, val_peek(1).statementList);}
break;
case 49:
//#line 247 "src/Syntaxer/parser/Parser.y"
{yyval.statement = new ReturnStatement(null);}
break;
case 50:
//#line 248 "src/Syntaxer/parser/Parser.y"
{yyval.statement = new ReturnStatement(val_peek(0).expression);}
break;
case 51:
//#line 252 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new MethodCall(val_peek(2).expression, new ArrayList<>());}
break;
case 52:
//#line 253 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new MethodCall(val_peek(3).expression, val_peek(1).expressionList);}
break;
case 53:
//#line 254 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new MethodCall(new ArrayLiteral(val_peek(4).strVal, val_peek(1).expressionList.size()), val_peek(1).expressionList);}
break;
case 54:
//#line 255 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new MethodCall(new ArrayLiteral(val_peek(7).strVal, val_peek(4).strVal), val_peek(1).expressionList);}
break;
case 55:
//#line 256 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new MethodCall(new ListLiteral(val_peek(4).strVal), val_peek(1).expressionList);}
break;
case 56:
//#line 260 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new SuperConstructorCall(new ArrayList<>());}
break;
case 57:
//#line 261 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new SuperConstructorCall(val_peek(1).expressionList);}
break;
case 58:
//#line 265 "src/Syntaxer/parser/Parser.y"
{yyval.expressionList = new ArrayList<>(); yyval.expressionList.add(val_peek(0).expression);}
break;
case 59:
//#line 266 "src/Syntaxer/parser/Parser.y"
{val_peek(2).expressionList.add(val_peek(0).expression); yyval.expressionList = val_peek(2).expressionList;}
break;
case 60:
//#line 270 "src/Syntaxer/parser/Parser.y"
{yyval.expression = val_peek(0).expression;}
break;
case 61:
//#line 271 "src/Syntaxer/parser/Parser.y"
{yyval.expression = val_peek(0).expression;}
break;
case 62:
//#line 272 "src/Syntaxer/parser/Parser.y"
{yyval.expression = val_peek(0).expression;}
break;
case 63:
//#line 273 "src/Syntaxer/parser/Parser.y"
{yyval.expression = val_peek(0).expression;}
break;
case 64:
//#line 274 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new MemberAccess(val_peek(2).expression, val_peek(0).expression);}
break;
case 65:
//#line 278 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new ThisExpression();}
break;
case 66:
//#line 279 "src/Syntaxer/parser/Parser.y"
{yyval.expression = val_peek(0).expression;}
break;
case 67:
//#line 280 "src/Syntaxer/parser/Parser.y"
{yyval.expression = val_peek(1).expression;}
break;
case 68:
//#line 281 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new ListLiteral(val_peek(1).strVal);}
break;
case 69:
//#line 282 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new ListLiteral("void");}
break;
case 70:
//#line 283 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new ArrayLiteral(val_peek(4).strVal, val_peek(1).strVal);}
break;
case 71:
//#line 284 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new IntegerLiteral(val_peek(0).intVal);}
break;
case 72:
//#line 285 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new RealLiteral(val_peek(0).realVal);}
break;
case 73:
//#line 286 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new BooleanLiteral(val_peek(0).boolVal);}
break;
case 74:
//#line 287 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new StringLiteral(val_peek(0).strVal);}
break;
case 75:
//#line 291 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new ConstructorCall(val_peek(2).strVal, new ArrayList<>());}
break;
case 76:
//#line 292 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new ConstructorCall(val_peek(3).strVal, val_peek(1).expressionList);}
break;
//#line 956 "Parser.java"
//########## END OF USER-SUPPLIED ACTIONS ##########
    }//switch
    //#### Now let's reduce... ####
    if (yydebug) debug("reduce");
    state_drop(yym);             //we just reduced yylen states
    yystate = state_peek(0);     //get new state
    val_drop(yym);               //corresponding value drop
    yym = yylhs[yyn];            //select next TERMINAL(on lhs)
    if (yystate == 0 && yym == 0)//done? 'rest' state and at first TERMINAL
      {
      if (yydebug) debug("After reduction, shifting from state 0 to state "+YYFINAL+"");
      yystate = YYFINAL;         //explicitly say we're done
      state_push(YYFINAL);       //and save it
      val_push(yyval);           //also save the semantic value of parsing
      if (yychar < 0)            //we want another character?
        {
        yychar = yylex();        //get next character
        if (yychar<0) yychar=0;  //clean, if necessary
        if (yydebug)
          yylexdebug(yystate,yychar);
        }
      if (yychar == 0)          //Good exit (if lex returns 0 ;-)
         break;                 //quit the loop--all DONE
      }//if yystate
    else                        //else not done yet
      {                         //get next state and push, for next yydefred[]
      yyn = yygindex[yym];      //find out where to go
      if ((yyn != 0) && (yyn += yystate) >= 0 &&
            yyn <= YYTABLESIZE && yycheck[yyn] == yystate)
        yystate = yytable[yyn]; //get new state
      else
        yystate = yydgoto[yym]; //else go to new defred
      if (yydebug) debug("after reduction, shifting from state "+state_peek(0)+" to state "+yystate+"");
      state_push(yystate);     //going again, so push state & val...
      val_push(yyval);         //for next action
      }
    }//main loop
  return 0;//yyaccept!!
}
//## end of method parse() ######################################



//## run() --- for Thread #######################################
//## The -Jnorun option was used ##
//## end of method run() ########################################



//## Constructors ###############################################
/**
 * Default constructor.  Turn off with -Jnoconstruct .

 */
public Parser(Lexer l)
{
  lexer = l;
  //nothing to do
}


/**
 * Create a parser, setting the debug to true or false.
 * @param debugMe true for debugging, false for no debug.
 */
public Parser(boolean debugMe)
{
  yydebug=debugMe;
}
//###############################################################



}
//################### END OF CLASS ##############################
