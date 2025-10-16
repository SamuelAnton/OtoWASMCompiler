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
//#line 11 "src/Syntaxer/parser/Parser.y"
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
//#line 56 "Parser.java"




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
//## **user defined:ParserVal
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
public final static short COLON=275;
public final static short DOT=276;
public final static short COMMA=277;
public final static short LPAREN=278;
public final static short RPAREN=279;
public final static short LBRACKET=280;
public final static short RBRACKET=281;
public final static short SHORTBODY=282;
public final static short ASSIGN=283;
public final static short ARRAY=284;
public final static short LIST=285;
public final static short YYERRCODE=256;
final static short yylhs[] = {                           -1,
    0,    1,    1,    2,   26,   26,    3,    4,    4,    5,
    5,    5,    6,    6,    7,    8,    9,    9,   10,   10,
   11,   30,   30,   30,   12,   12,   27,   27,   27,   14,
   14,   15,   15,   15,   15,   15,   15,   13,   13,   16,
   22,   22,   17,   29,   28,   28,   18,   19,   19,   24,
   24,   24,   24,   24,   25,   25,   20,   20,   20,   20,
   21,   21,   21,   21,   21,   21,   21,   21,   21,   23,
   23,
};
final static short yylen[] = {                            2,
    1,    0,    2,    6,    0,    2,    1,    0,    2,    1,
    1,    1,    4,    4,    5,    5,    2,    3,    1,    3,
    3,    1,    4,    4,    0,    2,    0,    3,    2,    0,
    2,    1,    1,    1,    1,    1,    1,    4,    4,    3,
    1,    3,    5,    3,    0,    3,    5,    1,    2,    3,
    4,    7,   10,    7,    1,    3,    1,    1,    1,    3,
    1,    1,    3,    4,    7,    1,    1,    1,    1,    3,
    4,
};
final static short yydefred[] = {                         2,
    0,    0,    0,    3,    0,    0,    0,    6,    8,    0,
    0,    4,    0,    0,    0,    9,   10,   11,   12,    0,
    0,    0,    0,    0,    0,    0,   17,    0,   19,   30,
    0,    0,   69,   66,   67,   68,   61,    0,    0,    0,
    0,   57,    0,   58,   59,    0,    0,    0,   18,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,   22,
    0,    0,   21,   20,    0,   16,    0,    0,    0,   32,
   31,   33,   34,   35,   36,    0,    0,   26,   30,    0,
   15,   70,    0,    0,   63,    0,    0,    0,   42,   50,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,   71,    0,    0,   51,    0,    0,    0,    0,   30,
   30,    0,    0,   28,    0,    0,    0,   23,   24,    0,
    0,    0,    0,   30,    0,    0,    0,    0,   47,    0,
    0,   43,    0,   52,   54,    0,    0,    0,   53,
};
final static short yydgoto[] = {                          1,
    2,    4,   10,   11,   16,   17,   18,   19,   22,   28,
   29,   52,   70,   50,   71,   72,   73,   74,   75,   76,
   42,   43,   44,   45,   84,    7,   81,  125,  112,   63,
};
final static short yysindex[] = {                         0,
    0, -217, -241,    0, -215, -188, -183,    0,    0, -179,
 -228,    0, -163, -177, -155,    0,    0,    0,    0, -247,
 -254, -157, -177,  310,  310, -156,    0, -211,    0,    0,
 -145, -158,    0,    0,    0,    0,    0,  310, -149, -144,
 -134,    0, -160,    0,    0, -134, -253, -114,    0,  157,
  -98, -256, -237, -261,  -97,  -92,  310,  -79, -196,    0,
 -101,  -86,    0,    0,  -69,    0,  310,  310,  310,    0,
    0,    0,    0,    0,    0, -134, -225,    0,    0,  310,
    0,    0, -134, -130,    0,  -73,  -64, -134,    0,    0,
 -129,  -68,  -50, -226, -134, -219,    7,  310,  186, -134,
  310,    0,  -60,  -55,    0,  -51,  -45,  310,  310,    0,
    0,  -36, -134,    0, -134,  323,  310,    0,    0, -134,
 -134,  215,  294,    0,  -19, -203,    9,   10,    0,    0,
  294,    0,  -31,    0,    0,    0,  310,   14,    0,
};
final static short yyrindex[] = {                         0,
    0,  251,    0,    0,  -12,    0,    0,    0,    0,    0,
   -6,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
 -255, -162,    0,    0,    0,    0,    0,    0,    0,    0,
 -195,    0,  -46,    0,    0,  -91,    0,    0,    0,    0,
    0,  -62,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,   -8,    0,    0,    0,
    0,    0,    0,    0,    0,  -17,  244,    0,    0,    0,
    0,    0,   15,    0,    0,    0,    0, -133,    0,    0,
    0,    0,    0,    0,   12,    0,    0,    0,    0,  -33,
    0,    0,    0, -104,    0,    0,    0,    0,    0,    0,
    0,   -5,   41,    0,   33,    0,    0,    0,    0,   70,
   99,    0,    0,    0,    0, -186,    0,    0,    0,  128,
    0,    0,  -75,    0,    0,  265,    0,    0,    0,
};
final static short yygindex[] = {                         0,
    0,    0,    0,    0,    0,    0,    0,    0,  240,    0,
  218,    0,    0,  -74, -112,    0,    0,    0,    0,  -24,
    0,  -44,    0,    0,  -57,    0,    0,    0,    0,    0,
};
final static int YYTABLESIZE=608;
static short yytable[];
static { yytable();}
static void yytable(){
yytable = new short[]{                         41,
   46,   91,   26,   60,   99,   77,   25,   79,   25,   25,
  130,   25,   25,   54,   57,    5,   24,   85,  136,   32,
   33,   34,   35,   36,   27,   80,   25,   25,   83,   37,
   61,   62,   88,   13,   83,  122,  123,  108,   14,   15,
   38,   82,   95,   96,   97,    3,   39,   40,  109,  131,
   58,  110,   59,    6,   77,  100,   57,   98,  127,  128,
   32,   33,   34,   35,   36,   48,   14,   49,    8,   14,
   37,   14,   14,  113,   53,  133,  115,   77,   77,  138,
    9,   38,   90,  120,  121,   12,   77,   39,   40,   41,
   41,   83,   83,   20,   41,   41,   41,   41,   41,   41,
   21,   23,   41,   41,   41,   41,   30,   41,   41,   41,
   41,   41,   83,   41,   41,   58,   41,   59,   47,   53,
   41,   41,   41,   60,   60,   60,   60,   60,   60,   51,
   55,   60,   60,   60,   60,   56,   60,   60,   60,   60,
   60,   57,   26,   60,   60,   60,  101,  101,  102,  105,
   60,   60,   64,   64,   64,   64,   64,   64,   78,   86,
   64,   64,   64,   64,   87,   64,   64,   64,   64,   64,
   13,   64,   64,   13,   64,   13,   13,   89,   92,   64,
   64,   65,   65,   65,   65,   65,   65,   94,  106,   65,
   65,   65,   65,   93,   65,   65,   65,   65,   65,   27,
   65,   65,   27,   65,   27,   27,  107,  103,   65,   65,
   62,   62,   62,   62,   62,   62,  104,  116,   62,   62,
   62,   62,  117,   62,   62,   62,   62,   62,   29,  118,
   62,   29,   62,   29,   29,  119,  124,   62,   62,   37,
   37,   37,   37,   37,   37,  132,  137,   37,   37,   37,
    1,    5,   37,   48,   37,   37,   48,   48,    7,   45,
   37,   48,   31,   48,   48,   64,   37,   37,   49,   49,
   49,   49,   49,   49,    0,    0,   49,   49,   49,    0,
  111,   49,   57,   49,   49,  101,  101,  134,  135,   49,
  101,   55,  139,   55,    0,   49,   49,   40,   40,   40,
   40,   40,   40,    0,    0,   40,   40,   40,    0,   56,
   40,   56,   40,   40,    0,    0,    0,    0,   40,    0,
    0,    0,    0,    0,   40,   40,   39,   39,   39,   39,
   39,   39,    0,    0,   39,   39,   39,    0,    0,   39,
    0,   39,   39,    0,    0,    0,    0,   39,    0,    0,
    0,    0,    0,   39,   39,   38,   38,   38,   38,   38,
   38,    0,    0,   38,   38,   38,    0,    0,   38,    0,
   38,   38,    0,    0,    0,    0,   38,    0,    0,    0,
    0,    0,   38,   38,   31,   31,   31,   31,   31,   31,
    0,    0,   44,   31,   31,    0,    0,   31,    0,   31,
   44,    0,    0,    0,    0,   31,    0,    0,    0,    0,
    0,   31,   31,   32,   33,   34,   35,   36,   65,    0,
    0,   66,   67,   37,    0,    0,   68,    0,   69,    0,
    0,    0,    0,    0,   38,    0,    0,    0,    0,    0,
   39,   40,   32,   33,   34,   35,   36,   65,    0,    0,
  114,   67,   37,    0,    0,   68,    0,   69,    0,    0,
    0,    0,    0,   38,    0,    0,    0,    0,    0,   39,
   40,   32,   33,   34,   35,   36,   65,    0,    0,  129,
   67,   37,    0,    0,   68,    0,   69,    0,    0,    0,
    0,    0,   38,    0,    0,    0,    0,    0,   39,   40,
   62,   62,   62,   62,   62,   62,    0,    0,   62,   62,
   62,    0,    0,   62,    0,   62,   62,    0,    0,    0,
    0,   31,   31,   31,   31,   31,   31,   62,   62,   46,
   31,   31,    0,    0,   31,    0,   31,    0,    0,    0,
    0,    0,   31,    0,    0,    0,    0,    0,   31,   31,
   32,   33,   34,   35,   36,   65,    0,    0,    0,   67,
   37,    0,    0,   68,    0,   69,   32,   33,   34,   35,
   36,   38,    0,    0,    0,    0,   37,   39,   40,  126,
   33,   34,   35,   36,    0,    0,    0,   38,    0,   37,
    0,    0,    0,   39,   40,    0,    0,    0,    0,    0,
   38,    0,    0,    0,    0,    0,   39,   40,
};
}
static short yycheck[];
static { yycheck(); }
static void yycheck() {
yycheck = new short[] {                         24,
   25,   59,  257,  257,   79,   50,  262,  264,  264,  265,
  123,  267,  268,   38,  276,  257,  264,  279,  131,  257,
  258,  259,  260,  261,  279,  282,  282,  275,   53,  267,
  284,  285,   57,  262,   59,  110,  111,  264,  267,  268,
  278,  279,   67,   68,   69,  263,  284,  285,  275,  124,
  276,  271,  278,  269,   99,   80,  276,  283,  116,  117,
  257,  258,  259,  260,  261,  277,  262,  279,  257,  265,
  267,  267,  268,   98,  278,  279,  101,  122,  123,  137,
  264,  278,  279,  108,  109,  265,  131,  284,  285,  276,
  277,  116,  117,  257,  257,  258,  259,  260,  261,  262,
  278,  257,  265,  266,  267,  268,  264,  270,  271,  272,
  273,  274,  137,  276,  277,  276,  279,  278,  275,  278,
  283,  284,  285,  257,  258,  259,  260,  261,  262,  275,
  280,  265,  266,  267,  268,  280,  270,  271,  272,  273,
  274,  276,  257,  277,  278,  279,  277,  277,  279,  279,
  284,  285,  257,  258,  259,  260,  261,  262,  257,  257,
  265,  266,  267,  268,  257,  270,  271,  272,  273,  274,
  262,  276,  277,  265,  279,  267,  268,  257,  280,  284,
  285,  257,  258,  259,  260,  261,  262,  257,  257,  265,
  266,  267,  268,  280,  270,  271,  272,  273,  274,  262,
  276,  277,  265,  279,  267,  268,  257,  281,  284,  285,
  257,  258,  259,  260,  261,  262,  281,  278,  265,  266,
  267,  268,  278,  270,  271,  272,  273,  274,  262,  281,
  277,  265,  279,  267,  268,  281,  273,  284,  285,  257,
  258,  259,  260,  261,  262,  265,  278,  265,  266,  267,
    0,  264,  270,  262,  272,  273,  265,  266,  265,  265,
  278,  270,   23,  272,  273,   48,  284,  285,  257,  258,
  259,  260,  261,  262,   -1,   -1,  265,  266,  267,   -1,
  274,  270,  276,  272,  273,  277,  277,  279,  279,  278,
  277,  277,  279,  279,   -1,  284,  285,  257,  258,  259,
  260,  261,  262,   -1,   -1,  265,  266,  267,   -1,  277,
  270,  279,  272,  273,   -1,   -1,   -1,   -1,  278,   -1,
   -1,   -1,   -1,   -1,  284,  285,  257,  258,  259,  260,
  261,  262,   -1,   -1,  265,  266,  267,   -1,   -1,  270,
   -1,  272,  273,   -1,   -1,   -1,   -1,  278,   -1,   -1,
   -1,   -1,   -1,  284,  285,  257,  258,  259,  260,  261,
  262,   -1,   -1,  265,  266,  267,   -1,   -1,  270,   -1,
  272,  273,   -1,   -1,   -1,   -1,  278,   -1,   -1,   -1,
   -1,   -1,  284,  285,  257,  258,  259,  260,  261,  262,
   -1,   -1,  265,  266,  267,   -1,   -1,  270,   -1,  272,
  273,   -1,   -1,   -1,   -1,  278,   -1,   -1,   -1,   -1,
   -1,  284,  285,  257,  258,  259,  260,  261,  262,   -1,
   -1,  265,  266,  267,   -1,   -1,  270,   -1,  272,   -1,
   -1,   -1,   -1,   -1,  278,   -1,   -1,   -1,   -1,   -1,
  284,  285,  257,  258,  259,  260,  261,  262,   -1,   -1,
  265,  266,  267,   -1,   -1,  270,   -1,  272,   -1,   -1,
   -1,   -1,   -1,  278,   -1,   -1,   -1,   -1,   -1,  284,
  285,  257,  258,  259,  260,  261,  262,   -1,   -1,  265,
  266,  267,   -1,   -1,  270,   -1,  272,   -1,   -1,   -1,
   -1,   -1,  278,   -1,   -1,   -1,   -1,   -1,  284,  285,
  257,  258,  259,  260,  261,  262,   -1,   -1,  265,  266,
  267,   -1,   -1,  270,   -1,  272,  273,   -1,   -1,   -1,
   -1,  257,  258,  259,  260,  261,  262,  284,  285,  265,
  266,  267,   -1,   -1,  270,   -1,  272,   -1,   -1,   -1,
   -1,   -1,  278,   -1,   -1,   -1,   -1,   -1,  284,  285,
  257,  258,  259,  260,  261,  262,   -1,   -1,   -1,  266,
  267,   -1,   -1,  270,   -1,  272,  257,  258,  259,  260,
  261,  278,   -1,   -1,   -1,   -1,  267,  284,  285,  257,
  258,  259,  260,  261,   -1,   -1,   -1,  278,   -1,  267,
   -1,   -1,   -1,  284,  285,   -1,   -1,   -1,   -1,   -1,
  278,   -1,   -1,   -1,   -1,   -1,  284,  285,
};
}
final static short YYFINAL=1;
final static short YYMAXTOKEN=285;
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
"WHILE","LOOP","IF","ELSE","THEN","COLON","DOT","COMMA","LPAREN","RPAREN",
"LBRACKET","RBRACKET","SHORTBODY","ASSIGN","ARRAY","LIST",
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
"Statement : VarDeclaration",
"Statement : Assignment",
"Statement : IfStatement",
"Statement : WhileStatement",
"Statement : ReturnStatement",
"Statement : Expression",
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
"ArgumentList : Expression",
"ArgumentList : ArgumentList COMMA Expression",
"Expression : Primary",
"Expression : ConstructorInvocation",
"Expression : MethodCall",
"Expression : Expression DOT Expression",
"Primary : THIS",
"Primary : CompoundName",
"Primary : LPAREN Expression RPAREN",
"Primary : LIST LBRACKET IDENTIFIER RBRACKET",
"Primary : ARRAY LBRACKET IDENTIFIER RBRACKET LPAREN IDENTIFIER RPAREN",
"Primary : NUMBER",
"Primary : REAL_LITERAL",
"Primary : BOOLEAN_LITERAL",
"Primary : STRING_LITERAL",
"ConstructorInvocation : IDENTIFIER LPAREN RPAREN",
"ConstructorInvocation : IDENTIFIER LPAREN ArgumentList RPAREN",
};

//#line 281 "src/Syntaxer/parser/Parser.y"


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
//#line 514 "Parser.java"
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
//#line 109 "src/Syntaxer/parser/Parser.y"
{
        yyval.program = new Program(val_peek(0).classDeclarationList);
        parserResult = yyval.program;
      }
break;
case 2:
//#line 116 "src/Syntaxer/parser/Parser.y"
{yyval.classDeclarationList = new ArrayList<>();}
break;
case 3:
//#line 117 "src/Syntaxer/parser/Parser.y"
{val_peek(1).classDeclarationList.add(val_peek(0).classDeclaration); yyval.classDeclarationList = val_peek(1).classDeclarationList;}
break;
case 4:
//#line 121 "src/Syntaxer/parser/Parser.y"
{yyval.classDeclaration = new ClassDeclaration(val_peek(4).strVal, val_peek(3).extensionType, val_peek(1).memberDeclarationList);}
break;
case 5:
//#line 125 "src/Syntaxer/parser/Parser.y"
{yyval.extensionType = null;}
break;
case 6:
//#line 126 "src/Syntaxer/parser/Parser.y"
{yyval.extensionType = new ExtensionType(val_peek(0).strVal);}
break;
case 7:
//#line 130 "src/Syntaxer/parser/Parser.y"
{yyval.memberDeclarationList = val_peek(0).memberDeclarationList;}
break;
case 8:
//#line 134 "src/Syntaxer/parser/Parser.y"
{yyval.memberDeclarationList = new ArrayList<>();}
break;
case 9:
//#line 135 "src/Syntaxer/parser/Parser.y"
{val_peek(1).memberDeclarationList.add(val_peek(0).memberDeclaration); yyval.memberDeclarationList = val_peek(1).memberDeclarationList;}
break;
case 10:
//#line 139 "src/Syntaxer/parser/Parser.y"
{yyval.memberDeclaration = val_peek(0).fieldDeclaration;}
break;
case 11:
//#line 140 "src/Syntaxer/parser/Parser.y"
{yyval.memberDeclaration = val_peek(0).methodDeclaration;}
break;
case 12:
//#line 141 "src/Syntaxer/parser/Parser.y"
{yyval.memberDeclaration = val_peek(0).constructorDeclaration;}
break;
case 13:
//#line 145 "src/Syntaxer/parser/Parser.y"
{yyval.fieldDeclaration = new FieldDeclaration(val_peek(2).strVal, val_peek(0).expression);}
break;
case 14:
//#line 146 "src/Syntaxer/parser/Parser.y"
{yyval.fieldDeclaration = new FieldDeclaration(val_peek(2).strVal, val_peek(0).expression);}
break;
case 15:
//#line 150 "src/Syntaxer/parser/Parser.y"
{yyval.methodDeclaration = new MethodDeclaration(val_peek(3).strVal, val_peek(2).paramList, val_peek(1).returnType, val_peek(0).statementList);}
break;
case 16:
//#line 154 "src/Syntaxer/parser/Parser.y"
{yyval.constructorDeclaration = new ConstructorDeclaration(val_peek(3).paramList, val_peek(1).statementList);}
break;
case 17:
//#line 158 "src/Syntaxer/parser/Parser.y"
{yyval.paramList = new ArrayList<>();}
break;
case 18:
//#line 159 "src/Syntaxer/parser/Parser.y"
{yyval.paramList = val_peek(1).paramList;}
break;
case 19:
//#line 163 "src/Syntaxer/parser/Parser.y"
{yyval.paramList = new ArrayList<>(); yyval.paramList.add(val_peek(0).param);}
break;
case 20:
//#line 164 "src/Syntaxer/parser/Parser.y"
{yyval.paramList = val_peek(2).paramList; yyval.paramList.add(val_peek(0).param);}
break;
case 21:
//#line 168 "src/Syntaxer/parser/Parser.y"
{yyval.param = new Param(val_peek(2).strVal, val_peek(0).type);}
break;
case 22:
//#line 172 "src/Syntaxer/parser/Parser.y"
{yyval.type = new Type(val_peek(0).strVal);}
break;
case 23:
//#line 173 "src/Syntaxer/parser/Parser.y"
{yyval.type = new Type(new ArrayLiteral(val_peek(1).strVal, -1));}
break;
case 24:
//#line 174 "src/Syntaxer/parser/Parser.y"
{yyval.type = new Type(new ListLiteral(val_peek(1).strVal));}
break;
case 25:
//#line 178 "src/Syntaxer/parser/Parser.y"
{yyval.returnType = null;}
break;
case 26:
//#line 179 "src/Syntaxer/parser/Parser.y"
{yyval.returnType = new ReturnType(val_peek(0).strVal);}
break;
case 27:
//#line 183 "src/Syntaxer/parser/Parser.y"
{yyval.statementList = new ArrayList<>();}
break;
case 28:
//#line 184 "src/Syntaxer/parser/Parser.y"
{yyval.statementList = val_peek(1).statementList;}
break;
case 29:
//#line 185 "src/Syntaxer/parser/Parser.y"
{
        /* Convert single expression to return statement*/
        ArrayList<Statement> body = new ArrayList<>();
        body.add(new ReturnStatement(val_peek(0).expression));
        yyval.statementList = body;
      }
break;
case 30:
//#line 194 "src/Syntaxer/parser/Parser.y"
{yyval.statementList = new ArrayList<>();}
break;
case 31:
//#line 195 "src/Syntaxer/parser/Parser.y"
{val_peek(1).statementList.add(val_peek(0).statement); yyval.statementList = val_peek(1).statementList;}
break;
case 32:
//#line 199 "src/Syntaxer/parser/Parser.y"
{yyval.statement = val_peek(0).variableDeclaration;}
break;
case 33:
//#line 200 "src/Syntaxer/parser/Parser.y"
{yyval.statement = val_peek(0).statement;}
break;
case 34:
//#line 201 "src/Syntaxer/parser/Parser.y"
{yyval.statement = val_peek(0).statement;}
break;
case 35:
//#line 202 "src/Syntaxer/parser/Parser.y"
{yyval.statement = val_peek(0).statement;}
break;
case 36:
//#line 203 "src/Syntaxer/parser/Parser.y"
{yyval.statement = val_peek(0).statement;}
break;
case 37:
//#line 204 "src/Syntaxer/parser/Parser.y"
{yyval.statement = new ExpressionStatement(val_peek(0).expression);}
break;
case 38:
//#line 208 "src/Syntaxer/parser/Parser.y"
{yyval.variableDeclaration = new VariableDeclaration(val_peek(2).strVal, val_peek(0).expression);}
break;
case 39:
//#line 209 "src/Syntaxer/parser/Parser.y"
{yyval.variableDeclaration = new VariableDeclaration(val_peek(2).strVal, val_peek(0).expression);}
break;
case 40:
//#line 213 "src/Syntaxer/parser/Parser.y"
{yyval.statement = new AssignmentStatement(val_peek(2).expression, val_peek(0).expression);}
break;
case 41:
//#line 217 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new VariableReference(val_peek(0).strVal);}
break;
case 42:
//#line 218 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new MemberAccess(val_peek(2).expression, new VariableReference(val_peek(0).strVal));}
break;
case 43:
//#line 222 "src/Syntaxer/parser/Parser.y"
{yyval.statement = new IfStatement(val_peek(3).expression, val_peek(2).thenClause, val_peek(1).elseClause);}
break;
case 44:
//#line 226 "src/Syntaxer/parser/Parser.y"
{val_peek(1).statementList.add(val_peek(0).statement); yyval.thenClause = new ThenStatement(val_peek(1).statementList);}
break;
case 45:
//#line 229 "src/Syntaxer/parser/Parser.y"
{yyval.elseClause = null;}
break;
case 46:
//#line 230 "src/Syntaxer/parser/Parser.y"
{val_peek(1).statementList.add(val_peek(0).statement); yyval.elseClause = new ElseStatement(val_peek(1).statementList);}
break;
case 47:
//#line 234 "src/Syntaxer/parser/Parser.y"
{yyval.statement = new WhileStatement(val_peek(3).expression, val_peek(1).statementList);}
break;
case 48:
//#line 238 "src/Syntaxer/parser/Parser.y"
{yyval.statement = new ReturnStatement(null);}
break;
case 49:
//#line 239 "src/Syntaxer/parser/Parser.y"
{yyval.statement = new ReturnStatement(val_peek(0).expression);}
break;
case 50:
//#line 243 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new MethodCall(val_peek(2).expression, new ArrayList<>());}
break;
case 51:
//#line 244 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new MethodCall(val_peek(3).expression, val_peek(1).expressionList);}
break;
case 52:
//#line 245 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new MethodCall(new ArrayLiteral(val_peek(4).strVal, val_peek(1).expressionList.size()), val_peek(1).expressionList);}
break;
case 53:
//#line 246 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new MethodCall(new ArrayLiteral(val_peek(7).strVal, val_peek(4).strVal), val_peek(1).expressionList);}
break;
case 54:
//#line 247 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new MethodCall(new ListLiteral(val_peek(4).strVal), val_peek(1).expressionList);}
break;
case 55:
//#line 251 "src/Syntaxer/parser/Parser.y"
{yyval.expressionList = new ArrayList<>(); yyval.expressionList.add(val_peek(0).expression);}
break;
case 56:
//#line 252 "src/Syntaxer/parser/Parser.y"
{val_peek(2).expressionList.add(val_peek(0).expression); yyval.expressionList = val_peek(2).expressionList;}
break;
case 57:
//#line 256 "src/Syntaxer/parser/Parser.y"
{yyval.expression = val_peek(0).expression;}
break;
case 58:
//#line 257 "src/Syntaxer/parser/Parser.y"
{yyval.expression = val_peek(0).expression;}
break;
case 59:
//#line 258 "src/Syntaxer/parser/Parser.y"
{yyval.expression = val_peek(0).expression;}
break;
case 60:
//#line 259 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new MemberAccess(val_peek(2).expression, val_peek(0).expression);}
break;
case 61:
//#line 263 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new ThisExpression();}
break;
case 62:
//#line 264 "src/Syntaxer/parser/Parser.y"
{yyval.expression = val_peek(0).expression;}
break;
case 63:
//#line 265 "src/Syntaxer/parser/Parser.y"
{yyval.expression = val_peek(1).expression;}
break;
case 64:
//#line 266 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new ListLiteral(val_peek(1).strVal);}
break;
case 65:
//#line 267 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new ArrayLiteral(val_peek(4).strVal, val_peek(1).strVal);}
break;
case 66:
//#line 268 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new IntegerLiteral(val_peek(0).intVal);}
break;
case 67:
//#line 269 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new RealLiteral(val_peek(0).realVal);}
break;
case 68:
//#line 270 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new BooleanLiteral(val_peek(0).boolVal);}
break;
case 69:
//#line 271 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new StringLiteral(val_peek(0).strVal);}
break;
case 70:
//#line 275 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new ConstructorCall(val_peek(2).strVal, new ArrayList<>());}
break;
case 71:
//#line 276 "src/Syntaxer/parser/Parser.y"
{yyval.expression = new ConstructorCall(val_peek(3).strVal, val_peek(1).expressionList);}
break;
//#line 955 "Parser.java"
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
