package com.thecout.lox.Parser.Stmts;


import com.thecout.lox.Token;

import java.util.List;
import java.util.stream.Collectors;

public class Function extends Stmt {
    public Function(Token name, List<Token> parameters, List<Stmt> body) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }


    public final Token name;
    public final List<Token> parameters;
    public final List<Stmt> body;

    @Override
    public <R> R accept(StmtVisitor<R> stmtVisitor) {
        return stmtVisitor.visitFunctionStmt(this);
    }
}
