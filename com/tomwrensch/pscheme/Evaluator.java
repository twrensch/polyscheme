package com.tomwrensch.pscheme;

import java.util.HashMap;
import java.util.Map;

import com.tomwrensch.pscheme.java.Java;

/**
 * S-expression evaluator.
 *
 * TODO: Improve documentation of the evaluator functions.
 */
public class Evaluator extends U {

	// Static variables used to gather statistics
	public static long instJavaSymbol = 0;
	public static long instKeyword = 0;
	public static long instSymbol = 0;
	public static long instOtherAtom = 0; // Non-symbol atoms
	public static long instSpecialIf = 0;
	public static long instSpecialFn = 0;
	public static long instSpecialBegin = 0;
	public static long instSpecialSet = 0;
	public static long instSpecialAnd = 0;
	public static long instSpecialOr = 0;
	public static long instSpecialQuote = 0;
	public static long instKeywordCall = 0;
	public static long instMacroCall = 0;
	public static long instSchemeCall = 0;
	public static long instOtherCall = 0;
	public static long instGrow = 0;

	/** Return a table of statistics for debugging and performance tuning */
	public static Map<String, Long> instCounts() {
		HashMap<String, Long> map = new HashMap<String, Long>();
		map.put("javaSymbol", instJavaSymbol);
		map.put("keyword", instKeyword);
		map.put("symbol", instSymbol);
		map.put("otherAtom", instOtherAtom);
		map.put("if", instSpecialIf);
		map.put("fn", instSpecialFn);
		map.put("begin", instSpecialBegin);
		map.put("set", instSpecialSet);
		map.put("and", instSpecialAnd);
		map.put("or", instSpecialOr);
		map.put("quote", instSpecialQuote);
		map.put("keywordCall", instKeywordCall);
		map.put("macroCall", instMacroCall);
		map.put("schemeCall", instSchemeCall);
		map.put("otherCall", instOtherCall);
		map.put("envGrow", instGrow);
		return map;
	}

	/** Reset the statistics counters. */
	public static void instReset() {
		instJavaSymbol = 0;
		instKeyword = 0;
		instSymbol = 0;
		instOtherAtom = 0;
		instSpecialIf = 0;
		instSpecialFn = 0;
		instSpecialBegin = 0;
		instSpecialSet = 0;
		instSpecialAnd = 0;
		instSpecialOr = 0;
		instSpecialQuote = 0;
		instKeywordCall = 0;
		instMacroCall = 0;
		instSchemeCall = 0;
		instOtherCall = 0;
		instGrow = 0;
	}

	/** Print a readable version of a statistics table. */
	public static void instPrint(Map<String, Long> map) {
		if (map == null) map = instCounts();
		for (String key : map.keySet())
			System.out.println(key + ": " + map.get(key));
	}

	/**
     * Evaluate an object within an environement.
     *
     * This is the heart of the system and is responsible for interpreting
     * s-expressions and turning them into actions. This version supports
     * tail-recursion.
     */
	public static Object eval(Object obj, Environment env,
			boolean useType, Object within) { while (true) {
		// Lookup symbols (unless it's a keyword)
		if (obj instanceof Symbol) {
			Symbol sym = (Symbol) obj;
			if (sym.isJavaSymbol()) { instJavaSymbol++;
				return Java.evalJavaSymbol(sym, env, useType, within);
			} else if (sym.isKeyword()) { instKeyword++;
				return sym;
			} else if (useType) { instSymbol++;
				return env.get(sym, within);
			} else {  instSymbol++;
				return env.get(sym);
			}
		}

		// If it's not a pair, return it
		if (!(obj instanceof Pair)) { instOtherAtom++;
			return obj;
		} else if (obj == Pair.EMPTY) { instOtherAtom++;
			return obj;
		}

		Pair form = (Pair) obj;
		Object first = car(form);
		// System.out.println("PASS " + pass + ": " + U.stringify(obj));

		// Special forms
		if (isSymbol(first, "set!")) { instSpecialSet++;
			Object value = eval(third(form), env, false, null);
			env.put(symbol(second(form)), value);
			if (value instanceof Procedure)
				ensureName(value, second(form));
			return value;
		} else if (isSymbol(first, "fn")) { instSpecialFn++;
			return new SchemeProcedure(
					second(form), pair(cddr(form)), env);
		} else if (isSymbol(first, "if")) { instSpecialIf++;
			useType = false; within = null;
			obj = isLogicalTrue(eval(second(form), env, false, null))
					? third(form) : forth(form);
			continue;
		} else if (isSymbol(first, "begin")) { instSpecialBegin++;
			Object body = form.getRest();
			if (body == NIL) return null;
			Object next;
			do {
				next = car(body);
				body = cdr(body);
				if (body == NIL || !(body instanceof Pair)) break;
				eval(next, env, false, null);
			} while (true);
			useType = false; within = null;
			obj = next;
			continue;
		} else if (isSymbol(first, "and")) { instSpecialAnd++;
			Object body = cdr(form);
			if (body == Pair.EMPTY) return FALSE;
			Object next;
			while (true) {
				next = car(body);
				body = cdr(body);
				if (body == Pair.EMPTY) break;  // last one is outside loop
				if (isLogicalFalse(eval(next, env, false, null)))
					return FALSE;
			}
			useType = false; within = null;
			obj = next;
			continue;  // Tail call
		} else if (isSymbol(first, "or")) { instSpecialOr++;
			Object body = cdr(form);
			if (body == Pair.EMPTY) return TRUE;
			Object next;
			while (true) {
				next = car(body);
				body = cdr(body);
				if (body == Pair.EMPTY) break;  // last one is outside loop
				Object result = eval(next, env, false, null);
				if (isLogicalTrue(result))
					return result;
			}
			useType = false; within = null;
			obj = next;
			continue;  // Tail call
		} else if (isSymbol(first, "quote")) { instSpecialQuote++;
			return second(form);

		// (:keyword map)  (:keyword map value)
		} else if (first instanceof Symbol && ((Symbol) first).isKeyword()
				|| first instanceof Number) { instKeywordCall++;
			useType = false; within = null;
			obj = (cddr(form) == NIL)
					? list(Symbol.ACC, second(form), first(form))
					: list(Symbol.ACC, second(form), first(form));
			continue;

		// Call or macro with no arguments
		} else if (cdr(form) == Pair.EMPTY) {
			Object proc = eval(first, env, false, null);
			if (proc instanceof Macro) { instMacroCall++;
				useType = false; within = null;
				// HACK: next line is very limiting on structure of macros
				obj = ((Macro) proc).macroFn.apply(Pair.EMPTY, env);
				continue;
			} else if (proc instanceof SchemeProcedure) { instSchemeCall++;
				SchemeProcedure sproc = (SchemeProcedure) proc;
				Environment execEnv = Environment.env(sproc.env);
				Object body = sproc.body;
				if (body == Pair.EMPTY) return null;
				Object next;
				while (true) {
					next = car(body);
					body = cdr(body);
					if (body == Pair.EMPTY) break; // last one is outside loop
					eval(next, env, false, null);
				}
				// Last one, do tail call
				env = execEnv;
				useType = false; within = null;
				obj = next;
				continue;
			} else if (proc instanceof Procedure) { instOtherCall++;
				return ((Procedure) proc).apply(Pair.EMPTY,
						env.rootEnvironment());
			} else {
				unableToApply(proc, first);
			}

		// Hard case: symbol lookup for proc/macro calls
		} else {
			Object proc = eval(first, env, true, second(form));
			if (proc instanceof Macro) { instMacroCall++;
				obj = ((Macro) proc).apply(cdr(form), env);
				useType = false; within = null;
				continue; // 'tail call' macro result
			}
			Object args = evalArgs(cdr(form), env);
			proc = eval(first, env, true, first(args));
			if (proc instanceof Macro) {
				unableToApply(proc, first);
			} else if (proc instanceof SchemeProcedure) { instSchemeCall++;
				SchemeProcedure sproc = (SchemeProcedure) proc;
				Environment execEnv = Environment.env(sproc.env);
				execEnv.bindArgs(sproc.params, args);
				Object body = sproc.body;
				if (body == Pair.EMPTY) return null;
				Object next;
				while (true) {
					next = car(body);
					body = cdr(body);
					if (body == Pair.EMPTY) break; // last one is outside loop
					eval(next, execEnv, false, null);
				}
				// Last one, do tail call
				env = execEnv;
				useType = false; within = null;
				obj = next;
				continue;
			} else if (proc instanceof Procedure) { instOtherCall++;
				return ((Procedure) proc).apply(args,
						env.rootEnvironment());
			} else
				return unableToApply(proc, first);
		}
	}}

	private static Object unableToApply(Object proc, Object original) {
		return syntax("Unable to apply " + (proc==null ? original : proc));
	}

	public static Object apply(Procedure proc, Object args, Environment env) {
		return proc instanceof Macro
				? proc.apply(args, env)
				: proc.apply(evalArgs(args, env), env);
	}

	public static Object evalArgs(Object args, Environment env) {
		Pair head = NIL, tail = NIL;
		if (!(args instanceof Pair))
			return eval(args, env, false, null);
		if (args == NIL)
			return NIL;
		head = tail = cons(eval(car(args), env, false, null), NIL);
		args = cdr(args);
		while (args instanceof Pair && args != NIL) {
			Pair pair = cons(eval(car(args), env, false, null), NIL);
			tail.setRest(pair);
			tail = pair;
			args = cdr(args);
		}
		if (args != NIL) tail.setRest(eval(args, env, false, null));
		return head;
	}

	public static Object evalBody(Object body, Environment env) {
		Object result = NIL;
		while (body != null && body != NIL) {
			result = eval(car(body), env, false, null);
			body = cdr(body);
		}
		return result;
	}

	static Object doAnd(Object body, Environment env) {
		Object result = TRUE;
		while (isLogicalTrue(result) && body != NIL) {
			result = eval(car(body), env, false, null);
			body = cdr(body);
		}
		return result;
	}

	static Object doOr(Object body, Environment env) {
		Object result = FALSE;
		while (isLogicalFalse(result) && body != NIL) {
			result = eval(car(body), env, false, null);
			body = cdr(body);
		}
		return result;
	}
}
