package Syntaxer.ast.literal;

import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.expression.Expression;

public final class RealLiteral extends Expression {
   public final double value;

   public RealLiteral(double v) {
       super(-1, -1);
       this.value = v;
   }

   @Override
   public <R> R accept(ASTVisitor<R> visitor) {
       return visitor.visit(this);
   }

}

