package com.tomwrensch.pscheme;

/**
 * A procedure implemented in PScheme.
 */
public class SchemeProcedure implements Procedure {

	public Object params;
	public Pair body;
	Environment env; // Nullable
	Symbol name;  // Nullable

	public SchemeProcedure(Object params, Pair body, Environment env) {
		this.params = params;  // symbol or pair
		this.body = body;
		this.env = env;
		this.name = Symbol.UNKNOWN;
	}

	public void setName(Symbol name) { this.name = name; }
	public Symbol getName() { return name==null ? Symbol.UNKNOWN : name; }

	public Object apply(Object args, Environment callingEnv) {
		Environment execEnv = Environment.env(env);
		execEnv.bindArgs(params, args);
		return Evaluator.evalBody(body, execEnv);
	}

	public String toString() { return "<schemeproc " + name + ">"; }
}
