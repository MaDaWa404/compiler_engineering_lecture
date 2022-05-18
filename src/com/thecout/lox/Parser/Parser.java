package com.thecout.lox.Parser;


import com.thecout.lox.Parser.Expr.*;
import com.thecout.lox.Parser.Stmts.*;
import com.thecout.lox.Token;
import com.thecout.lox.TokenType;

import java.util.ArrayList;
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
            statements.add(declaration());
        }

        return statements;
    }

    private Expr expression() {
        return assignment();
    }

    private Stmt declaration() {
        try {
            if (match(FUN)) return function("function");
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
        ArrayList<Stmt> block = new ArrayList<>();
        Expr increment;
        Expr condition;

        consume(LEFT_PAREN, "Expect '(' at the beginning of for head.");
        if (match(VAR)) {
            block.add(varDeclaration());
        } else if (!match(SEMICOLON)) {
            block.add(expressionStatement());
        } else {
            consume(SEMICOLON, "Expect ';' in for statement if not starts with variable");
        }

        increment = expression();
        consume(SEMICOLON, "Expect ';' after increment expression.");

        condition = expression();
        consume(RIGHT_PAREN, "Expect ')' at the end of for head.");

        List<Stmt> body = new ArrayList<>();
        body.add(statement());
        body.add(0, new Expression(increment));
        Stmt stmt = new Block(body);
        While whileStmt = new While(condition, stmt);

        block.add(whileStmt);
        return new Block(block);
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
        consume(SEMICOLON, "Expect ';' after print statement.");
        return new Print(expr);
    }

    private Stmt returnStatement() {
        int old = current - 1;
        if (match(SEMICOLON))
            return new Return(null);
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after return statement.");
        return new Return(expr);
    }

    private Stmt varDeclaration() {
        Expr init = null;
        consume(IDENTIFIER, "Expect identifier after 'var'.");
        Token name = previous();
        if (match(EQUAL))
            init = expression();
        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Var(name, init);
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after while expression.");
        Stmt body = statement();
        return new While(condition, body);
    }

    private Stmt expressionStatement() {
        Expr expression = expression();
        consume(SEMICOLON, "Expect ';' after expression");
        return new Expression(expression);
    }

    private Function function(String kind) {
        Token name;
        List<Stmt> body;
        consume(TokenType.IDENTIFIER, "Expect identifier after 'fun'.");
        name = previous();
        consume(TokenType.LEFT_PAREN, "Expect '(' after function identifier.");
        List<Token> parameters = new ArrayList<>();
        while (check(TokenType.IDENTIFIER) || match(TokenType.COMMA)) {
            if (match(TokenType.IDENTIFIER)) {
                parameters.add(previous());
            }
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after function parameter list.");
        consume(TokenType.LEFT_BRACE, "Expect '{' at the beginning of a code block.");
        body = block();
        return new Function(name, parameters, body);
    }

    private List<Stmt> block() {
        List<Stmt> block = new ArrayList<>();
        while (!check(RIGHT_BRACE)) {
            block.add(declaration());
        }
        consume(RIGHT_BRACE, "Expect '}' at end of block");
        return block;
    }

    private Expr assignment() {
        Token name = previous();
        Expr value;
        value = or();
        return new Assign(name, value);

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
        Expr expr = equality();

        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(EQUAL_EQUAL) || match(BANG_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = addition();

        while (match(TokenType.GREATER) || match(TokenType.GREATER_EQUAL) || match(TokenType.LESS) || match(TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expr right = addition();
            expr = new Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr addition() {
        Expr expr = multiplication();

        while (match(TokenType.MINUS) || match(TokenType.PLUS)) {
            Token operator = previous();
            Expr right = multiplication();
            expr = new Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr multiplication() {
        Expr expr = unary();

        while (match(TokenType.SLASH) || match(TokenType.STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(TokenType.BANG) || match(TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Unary(operator, right);
        }
        return call();
    }

    private Expr call() {
        List<Expr> arguments = new ArrayList<>();
        Expr callee = primary();

        while (check(TokenType.RIGHT_PAREN) || check(TokenType.DOT)) {
            if (match(TokenType.RIGHT_PAREN))
                arguments.addAll(arguments());
            else {
                consume(TokenType.DOT, "Expect '.' after primary/argument list in call.");
                consume(TokenType.IDENTIFIER, "Expect identifier after '.' in call.");
            }
        }

        return new Call(callee, arguments);

    }
    private List<Expr> arguments() {
        ArrayList<Expr> arguments = new ArrayList<>();
        arguments.add(expression());
        while (!match(TokenType.RIGHT_PAREN)) {
            consume(TokenType.COMMA, "Expect ',' or '(' after expression in arguments");
            arguments.add(expression());
        }
        return arguments;
    }

    private Expr primary() {
        switch (peek().type) {
            case STRING:
                if (peek().lexeme.equals("super")) {
                    advance();
                    consume(TokenType.DOT, "Expect '.' after super keyword.");
                    consume(TokenType.IDENTIFIER, "Expect identifier after '.' with call to super");
                    return new Variable(advance());
                }
                return new Variable(advance());
            case LEFT_PAREN:
                advance();
                Grouping grouping = new Grouping(expression());
                consume(TokenType.RIGHT_PAREN, "Grouping has to end with ')'");
                return grouping;
            case TRUE:
            case FALSE:
            case NIL:
            case NUMBER:
            case IDENTIFIER:
                return new Variable(advance());
            default:
                return null;
        }
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


}
