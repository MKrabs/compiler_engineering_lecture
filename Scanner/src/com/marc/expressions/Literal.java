package com.marc.expressions;

import com.marc.declaration.varDeclaration;

public class Literal implements Expression {
    double literal;

    public Literal(double literal) {
        this.literal = literal;
    }

    public Literal(String literal) {
        this.literal = varDeclaration.getMemory(literal);
    }

    public double eval() {
        return literal;
    }
}
