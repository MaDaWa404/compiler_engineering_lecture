package com.thecout.lox.Parser;


import com.thecout.lox.Parser.Expr.*;
import com.thecout.lox.Parser.Stmts.*;
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
		Expr increment;
		Expr condition;
		consume(LEFT_PAREN, "Expect '(' after 'for'.");
		Stmt init = null;
		if (match(VAR)) {
			init = varDeclaration();
		} else if (!match(SEMICOLON)) {
			init = expressionStatement();
		}

			condition = expression();
			consume(SEMICOLON, "");

		increment = expression();
		consume(RIGHT_PAREN, "");
		Stmt stm = statement();
		Block body = new Block(Arrays.asList(stm, new Expression(increment)));
		return new Block(Arrays.asList(init, new While(condition, body)));
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
        if (match(SEMICOLON))
            return new Return(null);
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after return statement.");
        return new Return(expr);
    }

	private Stmt varDeclaration() {
		Token name = consume(IDENTIFIER, "");
		Expr expr = null;
		if (match(EQUAL)) {
			expr = expression();
		}
		consume(SEMICOLON, "");
		return new Var(name, expr);

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

	private Function function() {
		Token name = consume(IDENTIFIER, "Expext bla bla");
		consume(LEFT_PAREN, "bla");
		List<Token> params = new ArrayList<>();
		if (!check(RIGHT_PAREN)) {
			do {
				if (params.size() >= 8) {
					error(peek(), "");
				}
				params.add(consume(IDENTIFIER, ""));
			} while (match(COMMA));
		}
		consume(RIGHT_PAREN, "");
		consume(LEFT_BRACE, "");
		List<Stmt> body = block();
		return new Function(name, params, body);
	}

	private List<Stmt> block() {
		List<Stmt> block = new ArrayList<>();
		while (!match(RIGHT_BRACE)) {
			block.add(declaration());
		}
		return block;
	}

	private Expr assignment() {
		Expr expr = or();
		if (match(EQUAL)) {
			Token equals = previous();
			Expr value = assignment();

			if (expr instanceof Variable) {
				Token name = ((Variable) expr).name;
				return new Assign(name, value);
			}
			ParserError.error(equals, "");
		}
		return expr;
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
		Expr left = equality();
		if (match(AND)) {
			Token op = previous();
			Expr right = equality();
			return new Logical(left, op, right);
		}
		return left;
	}

	private Expr equality() {
		Expr left = comparison();
		if (match(EQUAL_EQUAL) || match(BANG_EQUAL)) {
			Token op = previous();
			Expr right = comparison();
			return new Binary(left, op, right);
		}
		return left;
	}

	private Expr comparison() {
		Expr left = addition();
		if (match(GREATER) || match(GREATER_EQUAL) || match(LESS_EQUAL) || match(LESS)) {
			Token op = previous();
			Expr right = addition();
			return new Binary(left, op, right);
		}
		return left;
	}

	private Expr addition() {
		Expr expr = multiplication();
		while (match(PLUS) || match(MINUS)) {
			Token op = previous();
			Expr right = multiplication();
			expr = new Binary(expr, op, right);
		}

        return expr;
    }

	private Expr multiplication() {
		Expr left = unary();
		while (match(SLASH) || match(STAR)) {
			Token op = previous();
			Expr right = unary();
			left = new Binary(left, op, right);
		}
		return left;
	}

	private Expr unary() {
		if (match(MINUS) || match(BANG)) {
			return new Unary(previous(), null);
		}
		Expr expr = call();
		return expr;
	}

	private Expr finishCall(Expr callee) {
		return null;
	}

	private Expr call() {
		Expr expr = primary();
		List<Expr> arguments = new ArrayList<>();
		if (match((LEFT_PAREN))) {
			arguments = arguments();
			consume(RIGHT_PAREN, "");
			return new Call(expr, arguments);
		}
		return expr;
	}

	private List<Expr> arguments() {
		List<Expr> exprs = new ArrayList<>();
		exprs.add(expression());
		while (match(COMMA)) {
			exprs.add(expression());
		}
		return exprs;
	}

	private Expr primary() {
		if (match(LEFT_PAREN)) {
			Expr expr = expression();
			consume(RIGHT_PAREN, "");
			return expr;
		} else if (match(IDENTIFIER)) {
			return new Variable(previous());
		}
		Token val = consume(tokens.get(current).type, "");
		return new Literal(val.literal);
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
