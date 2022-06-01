package com.thecout.lox.Traversal;


import com.thecout.lox.Parser.Expr.*;
import com.thecout.lox.Parser.Stmts.*;
import com.thecout.lox.Token;
import com.thecout.lox.TokenType;
import com.thecout.lox.Traversal.InterpreterUtils.Environment;
import com.thecout.lox.Traversal.InterpreterUtils.LoxCallable;
import com.thecout.lox.Traversal.InterpreterUtils.LoxFunction;
import com.thecout.lox.Traversal.InterpreterUtils.LoxReturn;
import com.thecout.lox.Traversal.InterpreterUtils.RuntimeError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Interpreter implements ExprVisitor<Object>,
        StmtVisitor<Void> {

    public final Environment globals = new Environment();
    private Environment environment = globals;




    public Interpreter() {
        globals.define("clock", new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter,
                               List<Object> arguments) {
                return (double) System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }

    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            error.printStackTrace();
        }
    }

    public void executeBlock(List<Stmt> statements,
                             Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (LoxReturn loxReturn) {
            throw loxReturn;
        } finally {
            this.environment = previous;
        }
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    public void execute(Stmt stmt) {
        stmt.accept(this);
    }


    @Override
    public Object visitAssignExpr(Assign expr) {
        Object result = this.evaluate(expr.value);
        environment.assign(expr.name, result);
        return result;
    }

    @Override
    public Object visitBinaryExpr(Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        if (left instanceof Literal)
            left = ((Literal) left).value;
        if (right instanceof Literal)
            right = ((Literal) right).value;

        switch (expr.operator.type) {
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
            case MINUS:
                if (left instanceof Double && right instanceof Double)
                    return (double) left + (double) right;

                if (left instanceof String && right instanceof Double) {
                    String leftString = (String) left;
                    if (leftString.length() < (int) right)
                        throw new RuntimeError(expr.operator, "Cannot shorten string beyond itself");

                    return leftString.substring(leftString.length() - (int) right);
                }
            case PLUS:
                if (left instanceof Double && right instanceof Double)
                    return (double) left + (double) right;

                if (left instanceof String || right instanceof String)
                    return "%s%s".formatted(left, right);


                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                if ((double) right == 0)
                    throw new RuntimeError(expr.operator, "Cannot divide by zero !");

                return (double) left / (double) right;
            case STAR:
                if (left instanceof Double && right instanceof Double)
                    return (double) left * (double) right;

                if (left instanceof String && right instanceof Double)
                    return ((String) left).repeat((int) right);

                throw new RuntimeError(expr.operator, "Operands must be two numbers or a strings and a number.");
            default:
                throw new RuntimeError(expr.operator, "Operation requires legal arguments.");
        }
    }

    @Override
    public Object visitCallExpr(Call expr) {

        if(this.environment.get(((Variable) expr.callee).name) instanceof LoxFunction) {
            LoxFunction fun = (LoxFunction) this.environment.get(((Variable) expr.callee).name);
            return fun.call(this, expr.arguments.stream().map(Object.class::cast).collect(Collectors.toList()));
        }
        if(this.environment.get(((Variable) expr.callee).name) instanceof Interpreter) {
            Interpreter intp = (Interpreter) this.environment.get(((Variable) expr.callee).name);
            return ((LoxCallable)intp.globals.get(((Variable) expr.callee).name)).call(intp, null);
        }
        return null;

        /*
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(null, "Can only call functions and classes.");
        }

        LoxCallable function = (LoxCallable)callee;
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(null, "Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
        }

        return function.call(this, arguments);
         */
    }

    @Override
    public Object visitGroupingExpr(Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR)
            return (left != null) ? left : evaluate(expr.right);

        return (left == null) ? null : evaluate(expr.right);

    }

    @Override
    public Object visitUnaryExpr(Unary expr) {
        Object right = this.evaluate(expr.right);
        return switch (expr.operator.type) {
            case BANG -> !(boolean) right;
            case MINUS -> -(double) right;
            default -> null;
        };
    }

    @Override
    public Object visitVariableExpr(Variable expr) {
        return environment.get(expr.name);
    }

    @Override
    public Void visitBlockStmt(Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Function stmt) {
        this.environment.define(stmt.name.lexeme, new LoxFunction(stmt, environment));
        return null;
    }

    @Override
    public Void visitIfStmt(If stmt) {
        if ((boolean) evaluate(stmt.condition))
            execute(stmt.thenBranch);
        else if (stmt.elseBranch != null)
            execute(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitPrintStmt(Print stmt) {
        System.out.println(evaluate(stmt.expression));
        return null;
    }

    @Override
    public Void visitReturnStmt(Return stmt) {
        throw new LoxReturn(evaluate(stmt.value));
    }

    @Override
    public Void visitVarStmt(Var stmt) {
        environment.define(stmt.name.lexeme, evaluate(stmt.initializer));
        return null;
    }

    @Override
    public Void visitWhileStmt(While stmt) {
        while ((boolean) evaluate(stmt.condition)) {
            execute(stmt.body);
        }
        return null;
    }


    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null ^ b == null) return false;

        return a.equals(b);
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }
}
