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
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (isDigit(c)) {
				StringBuilder n = new StringBuilder();
				n.append(line.charAt(i));
				i++;
				while (isDigit(line.charAt(i)) || line.charAt(i) == '.') {
					n.append(line.charAt(i));
					i++;
					if (i>= line.length()) {
						break;
					}
				}
				i--;
				tokenList.add(new Token(TokenType.NUMBER, n.toString(), Double.parseDouble(n.toString()), lineNumber));
			} else if (isAlpha(c)) {
				StringBuilder l = new StringBuilder();
				l.append(line.charAt(i));
				i++;
				while (isAlpha(line.charAt(i)) || isDigit(line.charAt(i))) {
					l.append(line.charAt(i));
					i++;
				}
				i--;
				String literal = l.toString();
				switch (literal) {
					case "and":
						tokenList.add(new Token(TokenType.AND, literal, literal, lineNumber));
						break;
					case "else":
						tokenList.add(new Token(TokenType.ELSE, literal, literal, lineNumber));
						break;
					case "false":
						tokenList.add(new Token(TokenType.FALSE, literal, literal, lineNumber));
						break;
					case "fun":
						tokenList.add(new Token(TokenType.FUN, literal, literal, lineNumber));
						break;
					case "for":
						tokenList.add(new Token(TokenType.FOR, literal, literal, lineNumber));
						break;
					case "if":
						tokenList.add(new Token(TokenType.IF, literal, literal, lineNumber));
						break;
					case "nil":
						tokenList.add(new Token(TokenType.NIL, literal, literal, lineNumber));
						break;
					case "or":
						tokenList.add(new Token(TokenType.OR, literal, literal, lineNumber));
						break;
					case "print":
						tokenList.add(new Token(TokenType.PRINT, literal, literal, lineNumber));
						break;
					case "return":
						tokenList.add(new Token(TokenType.RETURN, literal, literal, lineNumber));
						break;
					case "true":
						tokenList.add(new Token(TokenType.TRUE, literal, literal, lineNumber));
						break;
					case "var":
						tokenList.add(new Token(TokenType.VAR, literal, literal, lineNumber));
						break;
					case "while":
						tokenList.add(new Token(TokenType.WHILE, literal, literal, lineNumber));
						break;
					default:
						tokenList.add(new Token(TokenType.IDENTIFIER, literal, literal, lineNumber));
						break;
				}

			} else {

				switch (c) {
					case ' ':
						break;
					case '(':
						tokenList.add(new Token(TokenType.LEFT_PAREN, "(", "(", lineNumber));
						break;
					case ')':
						tokenList.add(new Token(TokenType.RIGHT_PAREN, ")", ")", lineNumber));
						break;
					case '{':
						tokenList.add(new Token(TokenType.LEFT_BRACE, "{", "{", lineNumber));
						break;
					case '}':
						tokenList.add(new Token(TokenType.RIGHT_BRACE, "}", "}", lineNumber));
						break;
					case ',':
						tokenList.add(new Token(TokenType.COMMA, ",", ",", lineNumber));
						break;
					case '.':
						tokenList.add(new Token(TokenType.DOT, ".", ".", lineNumber));
						break;
					case '-':
						tokenList.add(new Token(TokenType.MINUS, "-", "-", lineNumber));
						break;
					case '+':
						tokenList.add(new Token(TokenType.PLUS, "+", "+", lineNumber));
						break;
					case ';':
						tokenList.add(new Token(TokenType.SEMICOLON, ";", ";", lineNumber));
						break;
					case '/':
						tokenList.add(new Token(TokenType.SLASH, "/", "/", lineNumber));
						break;
					case '*':
						tokenList.add(new Token(TokenType.STAR, "*", "*", lineNumber));
						break;
					case '!':
						if (line.charAt(i + 1) == '=') {
							tokenList.add(new Token(TokenType.BANG_EQUAL, "!=", "!=", lineNumber));
							i++;
							break;
						} else {
							tokenList.add(new Token(TokenType.BANG, "!", "!", lineNumber));
							break;
						}
					case '=':
						if (line.charAt(i + 1) == '=') {
							tokenList.add(new Token(TokenType.EQUAL_EQUAL, "==", "==", lineNumber));
							i++;
							break;
						} else {
							tokenList.add(new Token(TokenType.EQUAL, "=", "=", lineNumber));
							break;
						}
					case '>':
						if (line.charAt(i + 1) == '=') {
							tokenList.add(new Token(TokenType.GREATER_EQUAL, ">=", ">=", lineNumber));
							i++;
							break;
						} else {
							tokenList.add(new Token(TokenType.GREATER, ">", ">", lineNumber));
							break;
						}
					case '<':
						if (line.charAt(i + 1) == '=') {
							tokenList.add(new Token(TokenType.LESS_EQUAL, "<=", "<=", lineNumber));
							i++;
							break;
						} else {
							tokenList.add(new Token(TokenType.LESS, "<", "<", lineNumber));
							break;
						}
					case '"':
						StringBuilder w = new StringBuilder();
						w.append("\"");
						i++;
						while (line.charAt(i) != '"') {
							w.append(line.charAt(i));
							i++;
						}
						w.append(line.charAt(i));
						tokenList.add(new Token(TokenType.STRING, w.toString(), w.toString().substring(1, w.length() - 1), lineNumber));
						break;

				}
			}
		}

		return tokenList;
	}

	public List<Token> scan() {
		String[] lines = source.split("\n");
		for (int i = 0; i < lines.length; i++) {
			tokens.addAll(scanLine(lines[i], i));
		}
		tokens.add(new Token(EOF, "", "", lines.length));
		return tokens;
	}

	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	private boolean isAlpha(char c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_';
	}

}
