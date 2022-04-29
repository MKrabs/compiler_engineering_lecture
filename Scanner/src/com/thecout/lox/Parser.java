package com.thecout.lox;

import com.marc.declaration.Declaration;
import com.marc.declaration.funDeclaration;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int currentIndex;

    // fun   echoThis (     a     ,     b     )     {
    // token token    token token token token token token

    // print    (        "Hello World!"    )        ;
    // token    token    token             token    token

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Declaration> parse(List<Token> tokens, int lineNumber) {
        List<Declaration> expressionList = new ArrayList<>();

        while (!atEnd()) {
            expressionList.add(nextDeclaration());
        }

        return expressionList;
    }

    private Declaration nextDeclaration() {
        switch (peek().type) {
            case FUN -> {
                return new funDeclaration();
            }
            case VAR -> {
                return new findVariable();
            }
            default -> findStatement();
        }
        return null;
    }

    private void findStatement() {
    }

    private void findVariable() {
        
    }

    private void findFunction() {
    }

    private boolean atEnd() {
        return peek().type == TokenType.EOF;
    }

    public Token peek(){
        return tokens.get(currentIndex);
    }

    public void pop(){
        tokens.remove(currentIndex);
    }
}
