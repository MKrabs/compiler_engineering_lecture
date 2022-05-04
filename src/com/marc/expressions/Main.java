package com.marc.expressions;

import com.marc.declaration.varDeclaration;
import com.marc.statements.PrintLine;

public class Main {

    public static void main(String[] args) throws Exception {

        new varDeclaration("bla", new Literal(13.5)).execute();

        new varDeclaration("pi", new Literal(3.14159)).execute();

        new varDeclaration("exp", new PlusExpr(
                new Literal(1), new Literal(1.5)
        )).execute();

        new PrintLine("Hello World! " + (new Literal("pi").eval())).execute();

        new varDeclaration("result", new PlusExpr(
                new MinusExpr(new Literal("bla"), new Literal("pi")),
                new DivisionExpr(new Literal(7.5), new Literal("exp"))
        )).execute();
        
        new PrintLine("result = " + new Literal("result").eval()).execute();
    }
}
