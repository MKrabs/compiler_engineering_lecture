package com.thecout.lox.Traversal;

import com.thecout.lox.Parser.Expr.*;
import com.thecout.lox.Parser.Stmts.*;

import java.util.stream.Collectors;

public class AstPrinter implements ExprVisitor<String>, StmtVisitor<String> {
    public String print(Expr expr) {
        return expr.accept(this);
    }

    public String print(Stmt stmt) {
        return stmt.accept(this);
    }


    @Override
    public String visitAssignExpr(Assign expr) {
        return "(%s = %s)".formatted(expr.name.lexeme, expr.value.accept(this));
    }

    @Override
    public String visitBinaryExpr(Binary expr) {
        return "(%s %s %s)".formatted(expr.operator.lexeme, expr.left.accept(this), expr.right.accept(this));
    }

    @Override
    public String visitCallExpr(Call expr) {
        String args = expr.arguments.stream().map(expr -> ).collect(Collectors.joining(" "));
        return "(%s %s)".formatted(expr.callee.accept(this), args);
    }

    @Override
    public String visitGroupingExpr(Grouping expr) {
        return "(%s)".formatted(expr.expression.accept(this));
    }

    @Override
    public String visitLiteralExpr(Literal expr) {
        return String.valueOf(expr.value);
    }

    @Override
    public String visitLogicalExpr(Logical expr) {
        return "(%s %s %s)".formatted(expr.operator.lexeme, expr.left.accept(this), expr.right.accept(this));
    }

    @Override
    public String visitUnaryExpr(Unary expr) {
        return "%s%s".formatted(expr.operator.lexeme, expr.right.accept(this));
    }

    @Override
    public String visitVariableExpr(Variable expr) {
        return expr.name.lexeme;
    }

    @Override
    public String visitBlockStmt(Block stmt) {
        return "(%s)".formatted(stmt.statements.stream().map(Stmt::print).collect(Collectors.joining("\n")));
    }

    @Override
    public String visitExpressionStmt(Expression stmt) {
        return stmt.expression.accept(this);
    }

    @Override
    public String visitFunctionStmt(Function stmt) {
        String params = stmt.parameters.stream().map(t -> t.lexeme).collect(Collectors.joining(" "));
        String body = stmt.body.stream().map(Stmt::print).collect(Collectors.joining("\n"));
        return "(Function %s %s )".formatted(params, body);
    }

    @Override
    public String visitIfStmt(If stmt) {
        return "(if %s %s %s)".formatted(stmt.condition.accept(this), stmt.thenBranch.accept(this), stmt.elseBranch.accept(this));
    }

    @Override
    public String visitPrintStmt(Print stmt) {
        return "(print %s)".formatted(stmt.expression.accept(this));
    }

    @Override
    public String visitReturnStmt(Return stmt) {
        return "(return %s)".formatted(stmt.value.accept(this));
    }

    @Override
    public String visitVarStmt(Var stmt) {
        return "(= %s %s)".formatted(stmt.name.lexeme, stmt.initializer.accept(this));
    }

    @Override
    public String visitWhileStmt(While stmt) {
        return "(while %s %s)".formatted(stmt.condition.accept(this), stmt.body.accept(this));
    }
}
