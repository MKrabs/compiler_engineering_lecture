package com.thecout.lox;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.thecout.lox.TokenType.DOT;
import static com.thecout.lox.TokenType.EOF;
import static com.thecout.lox.TokenType.IDENTIFIER;
import static com.thecout.lox.TokenType.NUMBER;
import static com.thecout.lox.TokenType.STRING;

public class Scanner {


    private final String source;
    private final List<Token> tokens = new ArrayList<>();


    public Scanner(String source) {
        this.source = source;
    }


    public List<Token> scanLine(String line, int lineNumber) {
        List<Token> returnList = new ArrayList<>();

        if (line.equals("") || line.equals(" ")) {
            return new ArrayList<>();
        }

        Pattern p1 = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"");
        Matcher m1 = p1.matcher(line);

        if (m1.find()) {

            String[] temp = {
                    line.substring(0,m1.start()),
                    line.substring(m1.start()+1, m1.end()-1).replaceAll("\\\\\"", "\""),
                    line.substring(m1.end()),
            };

            returnList.addAll(Objects.requireNonNull(scanLine(temp[0], lineNumber)));
            returnList.add(new Token(STRING, temp[1], temp[1], lineNumber));
            returnList.addAll(Objects.requireNonNull(scanLine(temp[2], lineNumber)));

            return returnList;
        } else {
            String[] special = {" ", "(", ")", "{", "}", ",", "+", "-", ";", "/", "*", "!=", "!", "<=", ">=", "==", "=", ">", "<", "&&", "||"};

            for (String str : special) {
                if (line.contains(str)) {
                    returnList.addAll(Objects.requireNonNull(scanLine(line.substring(0, line.indexOf(str)), lineNumber)));
                    switch (str) {
                        case "(" ->
                                returnList.add(new Token(TokenType.LEFT_PAREN, str, str, lineNumber));
                        case ")" ->
                                returnList.add(new Token(TokenType.RIGHT_PAREN, str, str, lineNumber));
                        case "{" ->
                                returnList.add(new Token(TokenType.LEFT_BRACE, str, str, lineNumber));
                        case "}" ->
                                returnList.add(new Token(TokenType.RIGHT_BRACE, str, str, lineNumber));
                        case "," ->
                                returnList.add(new Token(TokenType.COMMA, str, str, lineNumber));
                        case "+" ->
                                returnList.add(new Token(TokenType.PLUS, str, str, lineNumber));
                        case "-" ->
                                returnList.add(new Token(TokenType.MINUS, str, str, lineNumber));
                        case ";" ->
                                returnList.add(new Token(TokenType.SEMICOLON, str, str, lineNumber));
                        case "/" ->
                                returnList.add(new Token(TokenType.SLASH, str, str, lineNumber));
                        case "*" ->
                                returnList.add(new Token(TokenType.STAR, str, str, lineNumber));
                        case "!" ->
                                returnList.add(new Token(TokenType.BANG, str, str, lineNumber));
                        case "=" ->
                                returnList.add(new Token(TokenType.EQUAL, str, str, lineNumber));
                        case ">" ->
                                returnList.add(new Token(TokenType.GREATER, str, str, lineNumber));
                        case "<" ->
                                returnList.add(new Token(TokenType.LESS, str, str, lineNumber));
                        case "<=" ->
                                returnList.add(new Token(TokenType.LESS_EQUAL, str, str, lineNumber));
                        case "&&" ->
                                returnList.add(new Token(TokenType.AND, str, str, lineNumber));
                        case "||" ->
                                returnList.add(new Token(TokenType.OR, str, str, lineNumber));
                        case ">=" ->
                                returnList.add(new Token(TokenType.GREATER_EQUAL, str, str, lineNumber));
                        case "==" ->
                                returnList.add(new Token(TokenType.EQUAL_EQUAL, str, str, lineNumber));
                        case "!=" ->
                                returnList.add(new Token(TokenType.BANG_EQUAL, str, str, lineNumber));
                        default -> {}
                    }
                    returnList.addAll(Objects.requireNonNull(scanLine(line.substring(line.indexOf(str) + str.length()), lineNumber)));
                    return returnList;
                }
            }
            String[] keywords = {"else", "false", "fun", "for", "if", "print", "return", "true", "var", "while"};

            for (String keyword : keywords) {
                if(line.equals(keyword)) {
                    switch (keyword) {
                        case "else" ->
                                returnList.add(new Token(TokenType.ELSE, keyword, keyword, lineNumber));
                        case "false" ->
                                returnList.add(new Token(TokenType.FALSE, keyword, keyword, lineNumber));
                        case "fun" ->
                                returnList.add(new Token(TokenType.FUN, keyword, keyword, lineNumber));
                        case "for" ->
                                returnList.add(new Token(TokenType.FOR, keyword, keyword, lineNumber));
                        case "if" ->
                                returnList.add(new Token(TokenType.IF, keyword, keyword, lineNumber));
                        case "print" ->
                                returnList.add(new Token(TokenType.PRINT, keyword, keyword, lineNumber));
                        case "return" ->
                                returnList.add(new Token(TokenType.RETURN, keyword, keyword, lineNumber));
                        case "true" ->
                                returnList.add(new Token(TokenType.TRUE, keyword, keyword, lineNumber));
                        case "var" ->
                                returnList.add(new Token(TokenType.VAR, keyword, keyword, lineNumber));
                        case "while" ->
                                returnList.add(new Token(TokenType.WHILE, keyword, keyword, lineNumber));
                        default -> {
                        }
                    }
                    return returnList;
                }
            }

            try {
                returnList.add(new Token(NUMBER, line, Double.parseDouble(line), lineNumber));
                return returnList;
            } catch (Exception e) {
                if(line.contains(".")){
                    String[] temp = line.split("\\.");

                    returnList.addAll(Objects.requireNonNull(scanLine(temp[0], lineNumber)));
                    returnList.add(new Token(DOT, ".", ".", lineNumber));
                    returnList.addAll(Objects.requireNonNull(scanLine(temp[1], lineNumber)));

                    return returnList;
                }

                returnList.add(new Token(IDENTIFIER, line, line, lineNumber));
            }

            return returnList;
        }
    }

    public List<Token> scan() {
        String[] lines = source.split("\n");
        for (int i = 0; i < lines.length; i++) {
            tokens.addAll(scanLine(lines[i], i));
        }
        tokens.add(new Token(EOF, "", "", lines.length));
        return tokens;
    }

    public static void main(String[] args) {
        String programm = "fun printaSum(\"Hello \\\"\\\"\\\" World\" + variable + \"b\" + empty  / \"\", c) {";

        Scanner scanner = new Scanner(programm);
        List<Token> actual = scanner.scanLine(programm, 0);

        System.out.printf("%-15s%-15s%-20s%3s%n", "Type", "Lexem", "Literal", "Line");
        for (Token s : actual) {
            System.out.printf("%-15s%-15s%-20s%3s%n", s.type, s.lexeme, s.literal, s.line);
        }

    }
}
