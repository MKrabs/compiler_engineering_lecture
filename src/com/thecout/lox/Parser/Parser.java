package com.thecout.lox.Parser;


import com.thecout.lox.Parser.Expr.Assign;
import com.thecout.lox.Parser.Expr.Binary;
import com.thecout.lox.Parser.Expr.Call;
import com.thecout.lox.Parser.Expr.Expr;
import com.thecout.lox.Parser.Expr.Literal;
import com.thecout.lox.Parser.Expr.Logical;
import com.thecout.lox.Parser.Expr.Unary;
import com.thecout.lox.Parser.Expr.Variable;
import com.thecout.lox.Parser.Stmts.Block;
import com.thecout.lox.Parser.Stmts.Expression;
import com.thecout.lox.Parser.Stmts.Function;
import com.thecout.lox.Parser.Stmts.If;
import com.thecout.lox.Parser.Stmts.Print;
import com.thecout.lox.Parser.Stmts.Return;
import com.thecout.lox.Parser.Stmts.Stmt;
import com.thecout.lox.Parser.Stmts.Var;
import com.thecout.lox.Parser.Stmts.While;
import com.thecout.lox.Scanner;
import com.thecout.lox.Token;
import com.thecout.lox.TokenType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.thecout.lox.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            System.out.println(peek());
            statements.add(declaration());
        }

        return statements;
    }

    private Expr expression() {
        return assignment();
    }

    private Stmt declaration() {
        try {
            if (match(FUN)) return function();
            if (match(VAR)) return varDeclaration();

            return statement();
        } catch (ParseError error) {
            return null;
        }
    }

    private Stmt statement() {
        if (match(FOR)) return forStatement();
        if (match(IF)) return ifStatement();
        if (match(PRINT)) return printStatement();
        if (match(RETURN)) return returnStatement();
        if (match(WHILE)) return whileStatement();
        if (match(LEFT_BRACE)) return new Block(block());

        return expressionStatement();
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");
        Stmt varDeclaration = null;
        if (match(VAR))
            varDeclaration = varDeclaration();
        else if (match(SEMICOLON))
            consume(SEMICOLON, "Expected ';' after the variable declaration");
        else
            varDeclaration = expressionStatement();

        Expr condition = expression();

        consume(SEMICOLON, "Expected ';' after the condition");
        Expr expr = expression();
        consume(RIGHT_PAREN, "Expect ')' after for condition.");

        Stmt forBody = statement();

        forBody = new Block(Arrays.asList(forBody, new Expression(expr)));

        return new Block(Arrays.asList(varDeclaration, new While(condition, forBody))); //IDK
    }

    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition."); // [parens]

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new If(condition, thenBranch, elseBranch);
    }

    private Stmt printStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after print expression."); // [parens]

        return new Print(expr);
    }

    private Stmt returnStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after return expression."); // [parens]

        return new Return(expr); // TODO
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expected variable name as STRING");
        Expr expr = expression();

        return new Var(name, expr);
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition."); // [parens]

        Stmt block = statement();

        return new While(condition, block);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Missing ; after expression");
        return new Expression(expr);
    }


    // FUN               FUN                 fun
    // IDENTIFIER        IDENTIFIER          printSum
    // LEFT_PAREN        LEFT_PAREN          (
    // IDENTIFIER        IDENTIFIER          a
    // COMMA             COMMA               ,
    // IDENTIFIER        IDENTIFIER          b
    // RIGHT_PAREN       RIGHT_PAREN         )
    // LEFT_BRACE        LEFT_BRACE          {
    // PRINT             PRINT               print
    // IDENTIFIER        IDENTIFIER          a
    // PLUS              PLUS                +
    // IDENTIFIER        IDENTIFIER          b
    // SEMICOLON         SEMICOLON           ;
    // RIGHT_BRACE       RIGHT_BRACE         }
    // PRINT             PRINT               print
    // NUMBER            NUMBER              25.0
    // PLUS              PLUS                +
    // NUMBER            NUMBER              60.0
    // SEMICOLON         SEMICOLON           ;
    // EOF

    private Function function() {
        Token identifier = consume(IDENTIFIER, "Expected function identifier.");
        consume(LEFT_PAREN, "Expected '(' after function name.");

        List<Token> inputs = new ArrayList<>();
        if (!match(RIGHT_PAREN))
            inputs.add(consume(IDENTIFIER, "Expected function name."));

        while (!match(RIGHT_PAREN)) {
            consume(COMMA, "Expected ',' function name.");
            inputs.add(consume(IDENTIFIER, "Expected function name."));
        }

        List<Stmt> body = block();

        return new Function(identifier, inputs, body);
    }

    private List<Stmt> block() {
        List<Stmt> stmts = new ArrayList<>();

        consume(LEFT_BRACE, "Expected '{' at the beginning of a block");

        while (!match(RIGHT_BRACE)) {
            stmts.add(statement());
        }

        return stmts;
    }

    private Expr assignment() {
        Expr or = or();
        if (match(EQUAL)) {
            Expr assignment = assignment();

            if (or instanceof Variable) {
                assert assignment != null;
                Token name = ((Variable) assignment).name;
                return new Assign(name, assignment);
            }
        }
        return or;
    }

    private Expr or() {
        Expr expr = and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr equality = equality();

        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            equality = new Logical(equality, operator, right);
        }

        return equality;
    }

    private Expr equality() {
        Expr comparison = comparison();

        while (match(EQUAL_EQUAL, BANG_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            comparison = new Binary(comparison, operator, right);
        }

        return comparison;
    }

    private Expr comparison() {
        Expr addition = addition();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = addition();
            addition = new Binary(addition, operator, right);
        }

        return addition;
    }

    private Expr addition() {
        Expr multiplication = multiplication();

        while (match(PLUS, MINUS)) {
            Token operator = previous();
            Expr right = multiplication();
            multiplication = new Binary(multiplication, operator, right);
        }

        return multiplication;
    }

    private Expr multiplication() {
        Expr unary = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            unary = new Binary(unary, operator, right);
        }

        return unary;
    }

    private Expr unary() {
        if (match(BANG, MINUS))
            return unary();

        return call();
    }

    private Expr finishCall(Expr callee) {
        return null;
    }

    private Expr call() {
        Expr expr = primary();

        while (check(LEFT_PAREN) || check(DOT)) {
            if (match(LEFT_PAREN)) {
                List<Expr> exprList = arguments();
                consume(RIGHT_PAREN, "Expected ')' after list of args");
                expr = new Call(expr, exprList);
            } else if (match(DOT)) {
                List<Expr> exprList = new ArrayList<>();
                exprList.add(new Literal(consume(IDENTIFIER, "Expected identifier after dot call")));
                expr = new Call(expr, exprList);
            }
        }

        return expr;
    }

    private Expr primary() {
        if (match(TRUE, FALSE, NIL, NUMBER, STRING, IDENTIFIER))
            return new Literal(previous());
        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expected ')' after primary expression");
            return expr;
        }
        return null;
    }

    private List<Expr> arguments() {
        List<Expr> exprList = new ArrayList<>();
        exprList.add(expression());
        while (match(COMMA)) {
            exprList.add(expression());
        }
        return exprList;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private boolean check(TokenType tokenType) {
        if (isAtEnd()) return false;
        return peek().type == tokenType;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        ParserError.error(token, message);
        return new ParseError();
    }


    public static void main(String[] args) {

        final String program = """
                fun printSum(a,b) {
                print a+b;
                }
                print 25+60;
                """;

        Scanner scanner = new Scanner(program);
        List<Token> actual = scanner.scan();
        System.out.println(actual);
        Parser parser = new Parser(actual);
        List<Stmt> statements = parser.parse();
        statements.forEach(System.out::println);
    }
}
