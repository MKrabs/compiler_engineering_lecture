package com.marc.expressions;

public class MultiplyExpr implements Expression{
    Expression expression1, expression2;

    public MultiplyExpr(Expression expression1, Expression expression2) {
        this.expression1 = expression1;
        this.expression2 = expression2;
    }

    public double eval() {
        return expression1.eval() * expression2.eval();
    }
}
