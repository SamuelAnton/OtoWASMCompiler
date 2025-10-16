package Syntaxer.ast.literal;

import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.expression.Expression;

public final class ArrayLiteral extends Expression {
   public final String type;
   public final int size;

   public ArrayLiteral(String t, int i) {
       super(-1, -1);
       this.type = t;
       size = i;
   }

   public ArrayLiteral(String t, String s) {
    super(-1, -1);
    this.type = t;
    int i = 0;
    try {
        i = Integer.parseInt(s);
    } catch (Exception e) {
        i = -1;
    }
    size = i;
   }

   @Override
   public <R> R accept(ASTVisitor<R> visitor) {
       return visitor.visit(this);
   }

}
