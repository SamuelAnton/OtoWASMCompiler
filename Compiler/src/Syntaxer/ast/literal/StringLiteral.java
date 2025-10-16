package Syntaxer.ast.literal;

import Syntaxer.ast.ASTVisitor;
import Syntaxer.ast.expression.Expression;

public final class StringLiteral extends Expression {
   public final String value;

   public StringLiteral(String v) {
       super(-1, -1);
       this.value = v;
   }

   @Override
   public <R> R accept(ASTVisitor<R> visitor) {
       return visitor.visit(this);
   }

}
