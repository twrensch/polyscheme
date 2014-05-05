package com.tomwrensch.pscheme;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * Built-in (primitive) procedures.
 *
 * Procedures are instances of this class with an integer member variable
 * identifying which procedure it represents. This makes for a large and
 * somewhat complex class with efficiently represented instances.
 *
 * To add a new built-in procedure:
 * <ul><li>Pick an unused primitive integer in the range allowed (see install)
 *     <li>Add the primitive name to the interned symbols as a static final variable
 *     <li>Add the primitive to the switch statement in getName()
 *     <li>Add the implementation to the switch statment in apply()
 * </ul>
 */
public class BuiltIn extends U implements Procedure {

	/** Install built-in procuedures into an environment. */
	public static Environment install(Environment env) {
		// Normal prims
		for (int id = 1; id <= 100; id++) {
			BuiltIn proc = new BuiltIn(id);
			if (proc.getName() != null)
				env.put(proc.getName(), proc);
		}

		// Extended prims for debugging
		for (int id = 1001; id < 1100; id++) {
			BuiltIn proc = new BuiltIn(id);
			if (proc.getName() != null)
				env.put(proc.getName(), proc);
		}
		return env;
	}

	static final Symbol CAR = Symbol.intern("car");
	static final Symbol CDR = Symbol.intern("cdr");
	static final Symbol CONS = Symbol.intern("cons");
	static final Symbol LIST = Symbol.intern("list");
	static final Symbol SETCAR = Symbol.intern("set-car!");
	static final Symbol SETCDR = Symbol.intern("set-cdr!");
	static final Symbol CADR = Symbol.intern("cadr");
	static final Symbol CDDR = Symbol.intern("cddr");
	static final Symbol REVERSE = Symbol.intern("reverse!");

	static final Symbol NOT = Symbol.intern("not");
	static final Symbol GT = Symbol.intern(">");
	static final Symbol LT = Symbol.intern("<");
	static final Symbol EQ = Symbol.intern("=");

	static final Symbol PLUS = Symbol.intern("+");
	static final Symbol MINUS = Symbol.intern("-");
	static final Symbol MULT = Symbol.intern("*");
	static final Symbol DIV = Symbol.intern("/");
	static final Symbol MOD = Symbol.intern("%");
	static final Symbol FLOOR = Symbol.intern("floor");
	static final Symbol POW = Symbol.intern("^");

	static final Symbol TRUE = Symbol.intern("true");
	static final Symbol FALSE = Symbol.intern("false");
	static final Symbol IDENTITY = Symbol.intern("identity");

	static final Symbol MAKEMACRO = Symbol.intern("makemacro");
	static final Symbol ACC = Symbol.intern("acc");
	static final Symbol TYPE = Symbol.intern("type");
	static final Symbol DO = Symbol.intern("do");
	static final Symbol APPLY = Symbol.intern("apply");

	static final Symbol MAKEARRAY = Symbol.intern("makearray");
	static final Symbol LEN = Symbol.intern("len");

	static final Symbol LOAD = Symbol.intern("load");

	int id;

	public BuiltIn(int id) { this.id = id; }

	@Override
	public Symbol getName() {
		switch (id) {
		case 1: return CAR;
		case 2: return CDR;
		case 3: return CONS;
		case 4: return LIST;
		case 5: return SETCAR;
		case 6: return SETCDR;
		case 7: return CADR;
		case 8: return CDDR;
		case 9: return REVERSE;

		case 10: return NOT;
		case 11: return GT;
		case 12: return LT;
		case 13: return EQ;

		case 14: return PLUS;
		case 15: return MINUS;
		case 16: return MULT;
		case 17: return DIV;
		case 18: return MOD;
		case 19: return FLOOR;
		case 20: return POW;

		case 21: return TRUE;
		case 22: return FALSE;
		case 23: return IDENTITY;

		case 24: return MAKEMACRO;
		case 25: return ACC;
		case 26: return TYPE;
		case 27: return DO;
		case 28: return APPLY;

		case 29: return MAKEARRAY;
		case 30: return LEN;

		case 31: return LOAD;

		// Built-in functions to aid in debugging.
		case 1001: return Symbol.intern("print");
		case 1002: return Symbol.intern("println");
		case 1003: return Symbol.intern("instReset");
		case 1004: return Symbol.intern("instPrint");
		case 1005: return Symbol.intern("instMap");

		default: return null;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object apply(Object args, Environment env) {
		Object first = car(args);
		Object second = second(args);
		switch (id) {
		case 1: return car(first);
		case 2: return cdr(first);
		case 3: return cons(first, second);
		case 4: return args;
		case 5: pair(first).setFirst(second); return first;
		case 6: pair(first).setRest(second); return second;
		case 7: return second(first);
		case 8: return cddr(first);
		case 9: return reverse(first);

		case 10: return args != Pair.EMPTY && isLogicalFalse(first);
		case 11: return gt(first, second);
		case 12: return !gt(first, second) && !eq(first, second);
		case 13: return eq(first, second);

		case 14: return plus(first, second);
		case 15: return fastminus(first, second);
		case 16: return mult(first, second);
		case 17: return div(first, second);
		case 18: return mod(first, second);
		case 19: return floor(first);
		case 20: return pow(first, second);

		case 21: return U.TRUE;
		case 22: return U.FALSE;
		case 23: return first;

		case 24: return macro(first);
		case 25: return cddr(args) == Pair.EMPTY
					? acc(first, second) : acc(first, second, third(args));
		case 26: return Type.typeOf(first);
		case 27: return doloop(first, cdr(args), env);
		case 28: return apply(first, second, env);

		case 29: return makeArray(first, second);
		case 30: return length(first);

		case 31: return load(first, env);

		// Debugging built-ins
		case 1001: System.out.print(stringify(first)); return first;
		case 1002: System.out.println(stringify(first)); return first;
		case 1003: Evaluator.instReset(); return U.NIL;
		case 1004:
			Evaluator.instPrint(first instanceof Map ? (Map) first : null);
			return U.NIL;
		case 1005: return Evaluator.instCounts();
		}
		return null;
	}

	@Override
	public void setName(Symbol name) {
		// Ignore, as built-in's can't change their name
	}

	public String toString() {
		return "<builtin " + id + " " + getName() + ">";
	}

	/** Destructive reverse (reverse! lst) */
	private Pair reverse(Object a) {
		Pair prev = Pair.EMPTY;
		Pair curr = pair(a);
		while (curr != Pair.EMPTY) {
			Pair next = pair(curr.getRest());
			curr.setRest(prev);
			prev = curr;
			curr = next;
		}
		return prev;
	}

	private boolean eq(Object a, Object b) {
		return a == null && b == null || a != null && a.equals(b);
	}

	private boolean gt(Object a, Object b) {
		if (a instanceof Long && b instanceof Long)
			return ((Long) a).longValue() > ((Long) b).longValue();
		else if (a instanceof Number && b instanceof Number)
			return ((Number) a).doubleValue() > ((Number) b).doubleValue();
		else if ((a instanceof String || a instanceof Symbol)
				&& (b instanceof String || b instanceof Symbol))
			return a.toString().compareTo(b.toString()) > 0;
		else {
			error("Can't '>' " + stringify(a) + " and " + stringify(b));
			return false;
		}
	}

	private Object plus(Object a, Object b) {
		if (a instanceof Number && b instanceof Number) {
			Number m = (Number) a; Number n = (Number) b;
			if (m instanceof Double || n instanceof Double
					|| m instanceof Float || n instanceof Float) 
				return m.doubleValue() + n.doubleValue();
			else
				return m.longValue() + n.longValue();
		} else if (a instanceof String || b instanceof String) {
			return stringify(a) + stringify(b);
		} else {
			error("Can't '+' " + stringify(a) + " and " + stringify(b));
			return null;
		}
	}

	@SuppressWarnings("unused")
	private Object minus(Object a, Object b) {
		if (a instanceof Number && b == Pair.EMPTY) {
			Number m = (Number) a;
			return - (m instanceof Long || m instanceof Integer
					? m.longValue() : m.doubleValue());
		} else if (a instanceof Number && b instanceof Number) {
			Number m = (Number) a; Number n = (Number) b;
			if (m instanceof Double || n instanceof Double
					|| m instanceof Float || n instanceof Float) 
				return m.doubleValue() - n.doubleValue();
			else
				return m.longValue() - n.longValue();
		} else {
			error("Can't '-' " + stringify(a) + " and " + stringify(b));
			return null;
		}
	}

	private Object fastminus(Object a, Object b) {
		if (a instanceof Double) {
			if (b instanceof Double) {
				return ((Double) a).doubleValue() - ((Double) b).doubleValue();
			} else if (b instanceof Long) {
				return ((Double) a).doubleValue() - ((Long) b).longValue();
			} else if (b == Pair.EMPTY) {
				return - ((Double) a).doubleValue();
			} else if (b instanceof Number) {
				return ((Double) a).doubleValue() - ((Double) b).doubleValue();
			}
		} else if (a instanceof Long) {
			if (b instanceof Number) {
				return ((Long) a).longValue() - ((Number) b).longValue();
			} else if (b == Pair.EMPTY) {
				return - ((Long) a).longValue();
			}
		}
		error("Can't '-' " + stringify(a) + " and " + stringify(b));
		return null;
	}

	private Object mult(Object a, Object b) {
		if (a instanceof Number && b instanceof Number) {
			Number m = (Number) a; Number n = (Number) b;
			if (m instanceof Double || n instanceof Double
					|| m instanceof Float || n instanceof Float) 
				return m.doubleValue() * n.doubleValue();
			else
				return m.longValue() * n.longValue();
		} else {
			error("Can't '*' " + stringify(a) + " and " + stringify(b));
			return null;
		}
	}

	private Object div(Object a, Object b) {
		if (a instanceof Number && b instanceof Number) {
			Number m = (Number) a; Number n = (Number) b;
			if (m instanceof Double || n instanceof Double
					|| m instanceof Float || n instanceof Float) 
				return m.doubleValue() / n.doubleValue();
			else
				return m.longValue() / n.longValue();
		} else {
			error("Can't '/' " + stringify(a) + " and " + stringify(b));
			return null;
		}
	}

	private Object mod(Object a, Object b) {
		if (a instanceof Number && b instanceof Number) {
			Number m = (Number) a; Number n = (Number) b;
			if (m instanceof Double || n instanceof Double
					|| m instanceof Float || n instanceof Float) 
				return m.doubleValue() % n.doubleValue();
			else
				return m.longValue() % n.longValue();
		} else {
			error("Can't '%' " + stringify(a) + " and " + stringify(b));
			return null;
		}
	}

	private Object floor(Object a) {
		if (a instanceof Number)
			return Math.floor(((Number) a).doubleValue());
		else {
			error("Can't 'floor' " + stringify(a));
			return null;
		}
	}

	private Object pow(Object a, Object b) {
		if (a instanceof Number && b instanceof Number) {
			Number m = (Number) a; Number n = (Number) b;
			return Math.pow(m.doubleValue(), n.doubleValue());
		} else {
			error("Can't '**' " + stringify(a) + " and " + stringify(b));
			return null;
		}
	}

	public Object macro(Object a) {
		if (a instanceof Procedure)
			return new Macro((Procedure) a);
		else
			return error("Macro argument must be a procedure");
	}

	/** General get accessor */
	public Object acc(Object a, Object b) {
		if (a instanceof Object[] && b instanceof Number) {
			try {
				return ((Object[]) a)[((Number) b).intValue()];
			}
			catch (ArrayIndexOutOfBoundsException e) {
				error("Index " + b + " out of bounds");
			}
		}
		else if (a instanceof Type && b instanceof Symbol)
			return ((Type) a).get((Symbol) b);
		else if (a instanceof Environment && b instanceof Symbol)
			return ((Environment) a).get((Symbol) b);
		else if (a instanceof Map<?,?>)
			return ((Map<?,?>) a).get(b);
		else if (a instanceof String && b instanceof Number)
			return String.valueOf(
					((String) a).charAt(((Number) b).intValue()));
		else
			error("Can't 'acc' " + stringify(a) + " with " + stringify(b));
		return null;
	}

	/** General put accessor */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object acc(Object a, Object b, Object c) {
		if (a instanceof Object[] && b instanceof Number)
			((Object[]) a)[((Number) b).intValue()] = c;
		else if (a instanceof Type && b instanceof Symbol)
			((Type) a).put((Symbol) b, c);
		else if (a instanceof Environment && b instanceof Symbol)
			((Environment) a).put((Symbol) b, c);
		else if (a instanceof Map<?,?>)
			((Map) a).put(b, c);
		else 
			syntax("Can't 'acc' " + stringify(a) + " with " + stringify(b));
		return c;
	}

	public Object doloop(Object fn, Object seqs, Environment env) {
		if (fn instanceof Procedure) {
			Object result = null;
			Procedure proc = (Procedure) fn; 
			Iterator<Object> iter = U.iter(car(seqs));
			while (iter.hasNext())
				result = proc.apply(U.list(iter.next()), env);
			return result;
		}
		return error("Can't 'do' over " + stringify(fn));
	}

	public Object apply(Object fn, Object args, Environment env) {
		if (fn instanceof Procedure)
			return ((Procedure) fn).apply(args, env);
		// Keywords act like a function that looks up a value
		else if (fn instanceof Symbol && ((Symbol) fn).isKeyword())
			return cdr(args) == Pair.EMPTY ? acc(fn, car(args))
					: acc(fn, car(args), second(args));
		else return error("Can't 'apply' over " + stringify(fn));
	}

	public Object makeArray(Object size, Object fillObj) {
		if (size instanceof Number) {
			Object[] array = new Object[((Number) size).intValue()];
			if (fillObj != null)
				Arrays.fill(array, fillObj);
			return array;
		} else {
			return error("Array size is not a number: " + stringify(size));
		}
	}

	public Object load(Object obj, Environment env) {
		Environment global = env.rootEnvironment();
		File file = obj instanceof File
				? (File) obj : new File(obj.toString());
		try {
			Reader reader = new FileReader(file);
			Object result = PScheme.staticLoad(reader, global);
			reader.close();
			return result;
		} catch (IOException e) {
			throw new PSchemeException("Unable to load file " + obj, e);
		}
	}
}
