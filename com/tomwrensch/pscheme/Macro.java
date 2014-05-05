package com.tomwrensch.pscheme;

/**
 * Represents a macro, e.g. a procedure-like object whose s-expression
 * is created at runtime by applying an expansion function to another
 * s-expression.
 */
public class Macro implements Procedure {
	Symbol name;
	Procedure macroFn;

	public Macro(Procedure macroFn) {
		this.macroFn = macroFn;
		name = Symbol.UNKNOWN;
	}

	public Symbol getName() { return name; }
	public void setName(Symbol name) { this.name = name; }

	public Object apply(Object args, Environment env) {
		return macroFn.apply(args, env);
	}

	public String toString() { return "<macro " + name + " " + macroFn + ">"; }
}
