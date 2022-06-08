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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
				if (statement != null) {
					execute(statement);
				}
			}
		} catch (LoxReturn lr) {
			throw lr;
		}
		finally {
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
		Object r = evaluate(expr.value);
		this.environment.assign(expr.name, r);
		return r;
	}

	@Override
	public Object visitBinaryExpr(Binary expr) {
		Object left = this.evaluate(expr.left);
		Object right = this.evaluate(expr.right);
		if (left instanceof Literal) {
			left = ((Literal) left).value;
		}
		if (right instanceof Literal) {
			right = ((Literal) right).value;
		}
		return switch (expr.operator.type) {
			case EQUAL_EQUAL -> (boolean) left == (boolean) right;
			case BANG_EQUAL -> (boolean) left != (boolean) right;
			case GREATER -> (double) left > (double) right;
			case GREATER_EQUAL -> (double) left >= (double) right;
			case LESS -> (double) left < (double) right;
			case LESS_EQUAL -> (double) left <= (double) right;
			case PLUS -> (double) left + (double) right;
			case MINUS -> (double) left - (double) right;
			case STAR -> (double) left * (double) right;
			case SLASH -> (double) left / (double) right;
			default -> null;
		};
	}

	@Override
	public Object visitCallExpr(Call expr) {
		Object f = evaluate(expr.callee);
		LoxFunction function = (LoxFunction) f;
		List<Object> arffewergebmtrsklg = new ArrayList<>();
		arffewergebmtrsklg.addAll(expr.arguments);
		return function.call(this, arffewergebmtrsklg);
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
		Object right = this.evaluate(expr.right);
		Object left = this.evaluate(expr.left);
		return switch (expr.operator.type) {
			case OR -> (boolean) right || (boolean) left;
			case AND -> (boolean) right && (boolean) left;
			default -> null;
		};
	}

	@Override
	public Object visitUnaryExpr(Unary expr) {
		Object right = this.evaluate(expr.right);
		return switch (expr.operator.type) {
			case MINUS -> -(double) right;
			case BANG -> !(boolean) right;
			default -> null;
		};
	}

	@Override
	public Object visitVariableExpr(Variable expr) {
		return  environment.get(expr.name);
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
		LoxFunction whatever = new LoxFunction(stmt, environment);
		environment.define(stmt.name.lexeme, whatever);
		return null;
	}

	@Override
	public Void visitIfStmt(If stmt) {
		if ((boolean) evaluate(stmt.condition)) {
			execute(stmt.thenBranch);
		} else if (stmt.elseBranch != null) {
			execute(stmt.elseBranch);
		}
		return null;
	}

	@Override
	public Void visitPrintStmt(Print stmt) {
		Object fu = evaluate(stmt.expression);
		System.out.println(fu);
		return null;
	}

	@Override
	public Void visitReturnStmt(Return stmt) {
		Object val = null;
		if (stmt.value != null) {
			val = evaluate(stmt.value);
		}
		throw new LoxReturn(val);
	}

	@Override
	public Void visitVarStmt(Var stmt) {
		this.environment.define(stmt.name.lexeme, evaluate(stmt.initializer));
		return null;
	}

	@Override
	public Void visitWhileStmt(While stmt) {
		Object cond = evaluate(stmt.condition);
		while ((boolean) cond) {
			execute(stmt.body);
			cond = evaluate(stmt.condition);
		}
		return null;
	}

}