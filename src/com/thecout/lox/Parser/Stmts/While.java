package com.thecout.lox.Parser.Stmts;

import com.thecout.lox.Parser.Expr.Expr;

public class While extends Stmt {
    public While(Expr condition, Stmt body) {
        this.condition = condition;
        this.body = body;
    }


    public final Expr condition;
    public final Stmt body;

    @Override
    public <R> R accept(StmtVisitor<R> stmtVisitor) {
        return stmtVisitor.visitWhileStmt(this);
    }
}
