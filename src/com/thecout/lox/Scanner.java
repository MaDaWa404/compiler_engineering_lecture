package com.thecout.lox;

import java.util.ArrayList;
import java.util.List;

import static com.thecout.lox.TokenType.EOF;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    public Scanner(String source) {
        this.source = source;
    }

    public List<Token> scanLine(String line, int lineNumber) {
        List<Token> tokenList = new ArrayList<>();

        String temp = "";
        Token token;
        Token newToken = null;
        char[] chars = line.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            temp += chars[i];
            token = newToken;
            newToken = getToken(temp, lineNumber);

            if (newToken == null && token != null) {
                tokenList.add(token);
                temp = "";
                if (chars[i] != ' ') i--;
            }

            //end of line:
            if (i == chars.length - 1 && newToken != null) {
                tokenList.add(newToken);
            }
        }
        return tokenList;
    }

    private Token getToken(String temp, int lineNumber) {

        Token token;

        if (temp.matches("\\d+([.](\\d+)?)?"))
            return new Token(TokenType.NUMBER, temp, Double.parseDouble(temp), lineNumber);

        if (temp.matches("//.*")) return new Token(TokenType.COMMENT, temp, temp.substring(2), lineNumber);

        if (temp.matches("\".*\""))
            return new Token(TokenType.STRING, temp, temp.substring(1, temp.length() - 1), lineNumber);

        token = getOtherToken(temp, lineNumber);
        if (token != null) return token;

        if (temp.matches("([a-z]|_)([a-z]|[A-Z]|_|\\d)*"))
            return new Token(TokenType.IDENTIFIER, temp, temp, lineNumber);
        else return null;
    }


    private Token getOtherToken(String temp, int lineNum) {
        return switch (temp) {
            case "and" -> new Token(TokenType.AND, temp, temp, lineNum);
            case "else" -> new Token(TokenType.ELSE, temp, temp, lineNum);
            case "false" -> new Token(TokenType.FALSE, temp, temp, lineNum);
            case "fun" -> new Token(TokenType.FUN, temp, temp, lineNum);
            case "for" -> new Token(TokenType.FOR, temp, temp, lineNum);
            case "if" -> new Token(TokenType.IF, temp, temp, lineNum);
            case "nil" -> new Token(TokenType.NIL, temp, temp, lineNum);
            case "or" -> new Token(TokenType.OR, temp, temp, lineNum);
            case "print" -> new Token(TokenType.PRINT, temp, temp, lineNum);
            case "return" -> new Token(TokenType.RETURN, temp, temp, lineNum);
            case "true" -> new Token(TokenType.TRUE, temp, temp, lineNum);
            case "var" -> new Token(TokenType.VAR, temp, temp, lineNum);
            case "while" -> new Token(TokenType.WHILE, temp, temp, lineNum);
            case "(" -> new Token(TokenType.LEFT_PAREN, temp, temp, lineNum);
            case ")" -> new Token(TokenType.RIGHT_PAREN, temp, temp, lineNum);
            case "{" -> new Token(TokenType.LEFT_BRACE, temp, temp, lineNum);
            case "}" -> new Token(TokenType.RIGHT_BRACE, temp, temp, lineNum);
            case "," -> new Token(TokenType.COMMA, temp, temp, lineNum);
            case "." -> new Token(TokenType.DOT, temp, temp, lineNum);
            case "-" -> new Token(TokenType.MINUS, temp, temp, lineNum);
            case "+" -> new Token(TokenType.PLUS, temp, temp, lineNum);
            case ";" -> new Token(TokenType.SEMICOLON, temp, temp, lineNum);
            case "/" -> new Token(TokenType.SLASH, temp, temp, lineNum);
            case "*" -> new Token(TokenType.STAR, temp, temp, lineNum);
            case "!=" -> new Token(TokenType.BANG_EQUAL, temp, temp, lineNum);
            case "!" -> new Token(TokenType.BANG, temp, temp, lineNum);
            case "==" -> new Token(TokenType.EQUAL_EQUAL, temp, temp, lineNum);
            case "=" -> new Token(TokenType.EQUAL, temp, temp, lineNum);
            case ">=" -> new Token(TokenType.GREATER_EQUAL, temp, temp, lineNum);
            case ">" -> new Token(TokenType.GREATER, temp, temp, lineNum);
            case "<=" -> new Token(TokenType.LESS_EQUAL, temp, temp, lineNum);
            case "<" -> new Token(TokenType.LESS, temp, temp, lineNum);
            default -> null;
        };
    }

    public List<Token> scan() {
        String[] lines = source.split("\n");
        for (int i = 0; i < lines.length; i++) {
            tokens.addAll(scanLine(lines[i], i));
        }
        tokens.add(new Token(EOF, "", "", lines.length));
        return tokens;
    }
}